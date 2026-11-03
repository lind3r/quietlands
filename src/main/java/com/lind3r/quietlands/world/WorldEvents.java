package com.lind3r.quietlands.world;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityLiving;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.MapGenBase;
import net.minecraft.world.gen.structure.MapGenMineshaft;
import net.minecraft.world.gen.structure.MapGenStronghold;
import net.minecraft.world.gen.structure.MapGenVillage;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;
import net.minecraftforge.event.terraingen.InitMapGenEvent;
import net.minecraftforge.event.terraingen.OreGenEvent;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;

import com.lind3r.quietlands.Config;
import com.lind3r.quietlands.QuietLands;

import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

public class WorldEvents {

    private final Set<Class<?>> loggedMobs = new HashSet<>();

    // ---- mobs ----

    /** Natural spawning off for this world only (the doMobSpawning gamerule is shared by all dimensions). */
    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase == TickEvent.Phase.START && QuietLands.isQuietLands(event.world)) {
            event.world.setAllowedSpawnTypes(false, false);
        }
    }

    /** Spawners and spawn eggs are allowed; just report what shows up so other mods' spawning can be found. */
    @SubscribeEvent
    public void onJoin(EntityJoinWorldEvent event) {
        if (!Config.logMobs || event.world.isRemote || !(event.entity instanceof EntityLiving)) return;
        if (!QuietLands.isQuietLands(event.world)) return;
        if (loggedMobs.add(event.entity.getClass())) {
            QuietLands.LOG.info(
                "Mob in Quiet Lands: {} at {}, {}, {} (logged once per type)",
                event.entity.getClass()
                    .getName(),
                (int) event.entity.posX,
                (int) event.entity.posY,
                (int) event.entity.posZ);
        }
    }

    // ---- world generation ----

    @SubscribeEvent
    public void onPopulate(PopulateChunkEvent.Populate event) {
        if (!QuietLands.isQuietLands(event.world)) return;
        switch (event.type) {
            case ANIMALS:
                event.setResult(Event.Result.DENY);
                break;
            case DUNGEON:
            case LAKE:
            case LAVA:
                if (Config.noStructures) event.setResult(Event.Result.DENY);
                break;
            default:
                break;
        }
    }

    @SubscribeEvent
    public void onDecorate(DecorateBiomeEvent.Decorate event) {
        // vanilla decorator's springs (RWG2's own springs are removed in Rwg2Bridge)
        if (Config.noSprings && event.type == DecorateBiomeEvent.Decorate.EventType.LAKE
            && QuietLands.isQuietLands(event.world)) {
            event.setResult(Event.Result.DENY);
        }
    }

    @SubscribeEvent
    public void onOre(OreGenEvent.GenerateMinable event) {
        if (!Config.noVanillaOres || !QuietLands.isQuietLands(event.world)) return;
        switch (event.type) {
            case COAL:
            case IRON:
            case GOLD:
            case REDSTONE:
            case DIAMOND:
            case LAPIS:
            case QUARTZ:
                event.setResult(Event.Result.DENY);
                break;
            default:
                break;
        }
    }

    /** Fires while RWG2 builds its generator; only replace the generators of our own dimension. */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onInitMapGen(InitMapGenEvent event) {
        if (!Config.noStructures || !Rwg2Bridge.BUILDING.get()) return;
        switch (event.type) {
            case CAVE:
                event.newGen = new NoCaves();
                break;
            case MINESHAFT:
                event.newGen = new NoMineshaft();
                break;
            case VILLAGE:
                event.newGen = new NoVillage();
                break;
            case STRONGHOLD:
                event.newGen = new NoStronghold();
                break;
            default:
                break;
        }
    }

    private static final class NoCaves extends MapGenBase {

        @Override
        public void func_151539_a(IChunkProvider provider, World world, int x, int z, Block[] blocks) {}
    }

    private static final class NoMineshaft extends MapGenMineshaft {

        @Override
        protected boolean canSpawnStructureAtCoords(int x, int z) {
            return false;
        }
    }

    private static final class NoVillage extends MapGenVillage {

        @Override
        protected boolean canSpawnStructureAtCoords(int x, int z) {
            return false;
        }
    }

    private static final class NoStronghold extends MapGenStronghold {

        @Override
        protected boolean canSpawnStructureAtCoords(int x, int z) {
            return false;
        }

        @Override
        public ChunkPosition func_151545_a(World world, int x, int y, int z) {
            return null;
        }
    }
}
