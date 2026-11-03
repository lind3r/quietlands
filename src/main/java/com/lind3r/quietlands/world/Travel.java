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
            int y = overworld.getTopSolidOrLiquidBlock(s.posX, s.posZ);
            teleport(player, point(0, s.posX + 0.5, y, s.posZ + 0.5, player.rotationYaw));
        } else {
            data.setTag("return", point(player.dimension, player.posX, player.posY, player.posZ, player.rotationYaw));
            WorldServer quiet = server.worldServerForDimension(Config.dimensionId);
            if (quiet == null) {
                player.addChatMessage(new ChatComponentText("Quiet Lands is not available (see the server log)."));
                return;
            }
            if (data.hasKey("last")) {
                teleport(player, data.getCompoundTag("last"));
            } else {
                ChunkCoordinates hub = ensureHub(quiet);
                teleport(player, point(Config.dimensionId, hub.posX + 0.5, hub.posY, hub.posZ + 0.5, -90f));
            }
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

    /** First visit ever: find dry land near 0,0, put down a small platform with a return portal. */
    static ChunkCoordinates ensureHub(WorldServer world) {
        QuietLandsData data = QuietLandsData.get(world);
        if (data.getHub() != null) return data.getHub();

        int bx = 0, bz = 0, by = -1;
        // spiral outwards in 64-block steps, up to ~1 km
        int x = 0, z = 0, dx = 0, dz = -1;
        for (int i = 0; i < 289; i++) {
            int px = x * 64, pz = z * 64;
            int y = world.getTopSolidOrLiquidBlock(px, pz);
            Material below = world.getBlock(px, y - 1, pz)
                .getMaterial();
            if (by < 0) {
                bx = px;
                bz = pz;
                by = Math.max(y, 64);
            }
            if (!below.isLiquid() && below != Material.ice && y > 63 && y < 200) {
                bx = px;
                bz = pz;
                by = y;
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

        Block floor = Blocks.stonebrick;
        for (int ix = -2; ix <= 2; ix++) {
            for (int iz = -2; iz <= 2; iz++) {
                world.setBlock(bx + ix, by - 1, bz + iz, floor);
                for (int iy = 0; iy < 4; iy++) world.setBlockToAir(bx + ix, by + iy, bz + iz);
            }
        }
        world.setBlock(bx + 2, by, bz, QuietLands.portal);

        ChunkCoordinates hub = new ChunkCoordinates(bx, by, bz);
        data.setHub(hub);
        QuietLands.LOG.info("Quiet Lands hub created at {}, {}, {}", bx, by, bz);
        return hub;
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
