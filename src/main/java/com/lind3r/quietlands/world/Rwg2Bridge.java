package com.lind3r.quietlands.world;

import java.lang.reflect.Field;
import java.util.Random;

import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.WorldChunkManager;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.feature.WorldGenLiquids;

import com.lind3r.quietlands.Config;
import com.lind3r.quietlands.QuietLands;

/**
 * Everything that touches RWG2 internals lives here, via reflection, so the mod needs no compile-time dependency on
 * RWG2 and fails with a log message (instead of a crash) if a future RWG2 version renames something.
 */
public final class Rwg2Bridge {

    private static final String WORLD_TYPE_CLASS = "rwg2.world.WorldTypeRealistic";
    private static final String SAVED_DATA_CLASS = "rwg2.world.RwgWorldSavedData";
    private static final String GENERATOR_CLASS = "rwg2.world.ChunkGeneratorRealistic";

    /** True while we are constructing our own RWG2 generator; WorldEvents swaps the structure generators then. */
    static final ThreadLocal<Boolean> BUILDING = ThreadLocal.withInitial(() -> false);

    private static Class<?> worldTypeClass;
    private static Field savedDataInstance;
    private static Class<?> savedDataClass;
    private static Field springWater;
    private static Field springLava;

    private Rwg2Bridge() {}

    public static boolean init() {
        try {
            worldTypeClass = Class.forName(WORLD_TYPE_CLASS);
            savedDataClass = Class.forName(SAVED_DATA_CLASS);
            savedDataInstance = savedDataClass.getDeclaredField("INSTANCE");
            savedDataInstance.setAccessible(true);
        } catch (ReflectiveOperationException | RuntimeException e) {
            QuietLands.LOG.error("Unsupported RWG2 version (core classes not found)", e);
            return false;
        }
        try {
            Class<?> gen = Class.forName(GENERATOR_CLASS);
            springWater = gen.getDeclaredField("gen_liquid_water");
            springLava = gen.getDeclaredField("gen_liquid_lava");
            springWater.setAccessible(true);
            springLava.setAccessible(true);
        } catch (ReflectiveOperationException | RuntimeException e) {
            QuietLands.LOG.warn("Can't find RWG2's spring generators; springs will generate in Quiet Lands", e);
            springWater = springLava = null;
        }
        return true;
    }

    public static WorldType worldType() {
        WorldType t = WorldType.parseWorldType(Config.continents ? "RWG2_CONTINENTS" : "RWG2");
        if (t == null || !worldTypeClass.isInstance(t)) {
            throw new IllegalStateException("RWG2 world type not registered");
        }
        return t;
    }

    public static WorldChunkManager createChunkManager(World world) {
        prepareSavedData(world);
        return worldType().getChunkManager(world);
    }

    public static IChunkProvider createGenerator(World world) {
        prepareSavedData(world);
        IChunkProvider gen;
        BUILDING.set(true);
        try {
            gen = worldType().getChunkGenerator(world, "");
        } finally {
            BUILDING.set(false);
        }
        if (Config.noSprings) removeSprings(gen);
        return gen;
    }

    /**
     * Old RWG and RWG2 both store their saved data under the key "rwg_data" in the overworld's MapStorage. In a world
     * made with old RWG, RWG2 would get old RWG's object back and crash with a ClassCastException. If the overworld
     * isn't RWG2, give RWG2 its own (unsaved) instance up front so it never reads that file. Its default noise setting
     * is the same one a new RWG2 world uses.
     */
    private static void prepareSavedData(World world) {
        WorldType overworldType = world.getWorldInfo()
            .getTerrainType();
        if (worldTypeClass.isInstance(overworldType)) return; // real RWG2 world: leave RWG2's own data alone
        try {
            if (savedDataInstance.get(null) == null) {
                savedDataInstance.set(
                    null,
                    savedDataClass.getConstructor()
                        .newInstance());
            }
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Could not prepare RWG2 saved data", e);
        }
    }

    private static void removeSprings(IChunkProvider gen) {
        if (springWater == null) return;
        try {
            springWater.set(gen, new NoSpring());
            springLava.set(gen, new NoSpring());
        } catch (ReflectiveOperationException | RuntimeException e) {
            QuietLands.LOG.warn("Could not turn off RWG2 springs; they will generate in Quiet Lands", e);
            springWater = springLava = null;
        }
    }

    private static final class NoSpring extends WorldGenLiquids {

        NoSpring() {
            super(Blocks.water);
        }

        @Override
        public boolean generate(World world, Random rand, int x, int y, int z) {
            return false;
        }
    }
}
