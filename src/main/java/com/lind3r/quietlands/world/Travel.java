package com.lind3r.quietlands.world;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.S1DPacketEntityEffect;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.Teleporter;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;

import com.lind3r.quietlands.Config;
import com.lind3r.quietlands.QuietLands;

/**
 * Portal logic. Entering: remember where you came from, go to where you last left Quiet Lands (first time: the shared
 * hub). Leaving: remember where you are, go back to where you entered from (fallback: overworld spawn).
 */
public final class Travel {

    private static final String TAG = "quietlands";

    private Travel() {}

    public static void use(EntityPlayerMP player) {
        if (player.ridingEntity != null || player.riddenByEntity != null) {
            player.addChatMessage(new ChatComponentText("Dismount first."));
            return;
        }
        MinecraftServer server = MinecraftServer.getServer();
        NBTTagCompound data = playerData(player);

        if (player.dimension == Config.dimensionId) {
            data.setTag("last", point(player.dimension, player.posX, player.posY, player.posZ, player.rotationYaw));
            if (data.hasKey("return")) {
                NBTTagCompound r = data.getCompoundTag("return");
                int dim = r.getInteger("dim");
                if (DimensionManager.isDimensionRegistered(dim) && server.worldServerForDimension(dim) != null) {
                    teleport(player, r);
                    return;
                }
            }
            WorldServer overworld = server.worldServerForDimension(0);
            ChunkCoordinates s = overworld.getSpawnPoint();
            int y = groundY(overworld, s.posX, s.posZ, false) + 1;
            teleport(player, point(0, s.posX + 0.5, Math.max(y, 1), s.posZ + 0.5, player.rotationYaw));
        } else {
            data.setTag("return", point(player.dimension, player.posX, player.posY, player.posZ, player.rotationYaw));
            WorldServer quiet = server.worldServerForDimension(Config.dimensionId);
            if (quiet == null) {
                player.addChatMessage(new ChatComponentText("Quiet Lands is not available (see the server log)."));
                return;
            }
            if (data.hasKey("last")) {
                NBTTagCompound last = data.getCompoundTag("last");
                if (!canStand(quiet, last.getDouble("x"), last.getDouble("y"), last.getDouble("z"))) {
                    int y = groundY(quiet, floor(last.getDouble("x")), floor(last.getDouble("z")), false);
                    if (y > 0) last.setDouble("y", y + 1);
                    else last = null;
                }
                if (last != null) {
                    teleport(player, last);
                    return;
                }
            }
            ChunkCoordinates hub = ensureHub(quiet);
            teleport(player, point(Config.dimensionId, hub.posX + 0.5, hub.posY, hub.posZ + 0.5, -90f));
        }
    }

    private static NBTTagCompound playerData(EntityPlayer player) {
        NBTTagCompound persisted = player.getEntityData()
            .getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
        player.getEntityData()
            .setTag(EntityPlayer.PERSISTED_NBT_TAG, persisted);
        NBTTagCompound mine = persisted.getCompoundTag(TAG);
        persisted.setTag(TAG, mine);
        return mine;
    }

    private static NBTTagCompound point(int dim, double x, double y, double z, float yaw) {
        NBTTagCompound t = new NBTTagCompound();
        t.setInteger("dim", dim);
        t.setDouble("x", x);
        t.setDouble("y", y);
        t.setDouble("z", z);
        t.setFloat("yaw", yaw);
        return t;
    }

    private static void teleport(EntityPlayerMP player, NBTTagCompound p) {
        int dim = p.getInteger("dim");
        double x = p.getDouble("x"), y = p.getDouble("y"), z = p.getDouble("z");
        float yaw = p.getFloat("yaw");
        if (player.dimension == dim) {
            player.playerNetServerHandler.setPlayerLocation(x, y, z, yaw, player.rotationPitch);
            return;
        }
        WorldServer target = MinecraftServer.getServer()
            .worldServerForDimension(dim);
        MinecraftServer.getServer()
            .getConfigurationManager()
            .transferPlayerToDimension(player, dim, new FixedTeleporter(target, x, y, z, yaw));
        // 1.7.10 forgets to resend these after a dimension change
        player.addExperienceLevel(0);
        for (Object o : player.getActivePotionEffects()) {
            player.playerNetServerHandler.sendPacket(new S1DPacketEntityEffect(player.getEntityId(), (PotionEffect) o));
        }
    }

    /**
     * First visit ever: find natural dry ground near 0,0 and put down a small platform with a return portal. Chunks are
     * loaded (generated) explicitly; an unloaded chunk would look empty.
     */
    static ChunkCoordinates ensureHub(WorldServer world) {
        QuietLandsData data = QuietLandsData.get(world);
        if (data.getHub() != null) return data.getHub();

        int bx = 0, bz = 0, floorY = -1;
        // spiral outwards in 64-block steps (up to 11x11 probes, ~350 blocks out)
        int x = 0, z = 0, dx = 0, dz = -1;
        for (int i = 0; i < 121; i++) {
            int px = x * 64, pz = z * 64;
            int gy = groundY(world, px, pz, true);
            if (floorY < 0 && gy > 0) { // first usable column, even if it's water: fallback
                bx = px;
                bz = pz;
                floorY = gy;
            }
            if (gy >= 63 && gy < 200
                && isNaturalGround(
                    world.getBlock(px, gy, pz)
                        .getMaterial())) {
                bx = px;
                bz = pz;
                floorY = gy;
                break;
            }
            if (x == z || (x < 0 && x == -z) || (x > 0 && x == 1 - z)) {
                int t = dx;
                dx = -dz;
                dz = t;
            }
            x += dx;
            z += dz;
        }
        if (floorY < 0) floorY = 64;

        Block floor = Blocks.stonebrick;
        for (int ix = -2; ix <= 2; ix++) {
            for (int iz = -2; iz <= 2; iz++) {
                world.setBlock(bx + ix, floorY, bz + iz, floor);
                for (int iy = 1; iy <= 4; iy++) world.setBlockToAir(bx + ix, floorY + iy, bz + iz);
            }
        }
        world.setBlock(bx + 2, floorY + 1, bz, QuietLands.portal);

        ChunkCoordinates hub = new ChunkCoordinates(bx, floorY + 1, bz);
        data.setHub(hub);
        QuietLands.LOG.info("Quiet Lands hub created at {}, {}, {}", bx, floorY + 1, bz);
        return hub;
    }

    private static boolean isNaturalGround(Material m) {
        return m == Material.grass || m == Material.ground
            || m == Material.sand
            || m == Material.rock
            || m == Material.clay
            || m == Material.craftedSnow;
    }

    /**
     * Highest block you could stand on (solid or liquid) at x,z, or -1. Loads/generates the chunk first. With
     * {@code groundOnly}, tree parts and plants are skipped so the result is the ground under them.
     */
    static int groundY(WorldServer world, int x, int z, boolean groundOnly) {
        world.theChunkProviderServer.loadChunk(x >> 4, z >> 4);
        for (int y = world.getActualHeight() - 1; y > 0; y--) {
            Material m = world.getBlock(x, y, z)
                .getMaterial();
            // passable or decoration: look further down (snow = thin snow layer)
            if (m == Material.air || m == Material.leaves
                || m == Material.plants
                || m == Material.vine
                || m == Material.snow) continue;
            if (groundOnly && (m == Material.wood || m == Material.cactus || m == Material.gourd)) continue;
            if (m.blocksMovement() || m.isLiquid()) return y;
        }
        return -1;
    }

    /** Solid block below, two free blocks for the player. */
    static boolean canStand(WorldServer world, double x, double y, double z) {
        int ix = floor(x), iy = floor(y), iz = floor(z);
        if (iy < 1 || iy > world.getActualHeight() - 2) return false;
        world.theChunkProviderServer.loadChunk(ix >> 4, iz >> 4);
        return world.getBlock(ix, iy - 1, iz)
            .getMaterial()
            .blocksMovement()
            && !world.getBlock(ix, iy, iz)
                .getMaterial()
                .blocksMovement()
            && !world.getBlock(ix, iy + 1, iz)
                .getMaterial()
                .blocksMovement();
    }

    private static int floor(double d) {
        return (int) Math.floor(d);
    }

    private static final class FixedTeleporter extends Teleporter {

        private final double x, y, z;
        private final float yaw;

        FixedTeleporter(WorldServer world, double x, double y, double z, float yaw) {
            super(world);
            this.x = x;
            this.y = y;
            this.z = z;
            this.yaw = yaw;
        }

        @Override
        public void placeInPortal(Entity entity, double px, double py, double pz, float rot) {
            entity.setLocationAndAngles(x, y, z, yaw, entity.rotationPitch);
            entity.motionX = entity.motionY = entity.motionZ = 0;
        }

        @Override
        public boolean placeInExistingPortal(Entity entity, double px, double py, double pz, float rot) {
            placeInPortal(entity, px, py, pz, rot);
            return true;
        }

        @Override
        public boolean makePortal(Entity entity) {
            return true;
        }

        @Override
        public void removeStalePortalLocations(long time) {}
    }
}
