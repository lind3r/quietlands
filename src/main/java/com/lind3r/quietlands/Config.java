package com.lind3r.quietlands;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

public class Config {

    public static int dimensionId = 1337;
    public static long seedSalt = 0x5155494554L; // "QUIET"
    public static boolean continents = true;
    public static boolean noWeather = true;
    public static boolean noVanillaOres = true;
    public static boolean noStructures = true;
    public static boolean noSprings = true;
    public static boolean logMobs = true;
    public static boolean enableRecipe = true;
    public static String[] excludedBiomes = { "Tainted Land" };

    public static void load(File file) {
        Configuration c = new Configuration(file);
        String g = Configuration.CATEGORY_GENERAL;

        dimensionId = c.getInt(
            "dimensionId",
            g,
            dimensionId,
            Integer.MIN_VALUE,
            Integer.MAX_VALUE,
            "Dimension ID of Quiet Lands. Must be the same on server and clients. Don't change it once the world exists.");
        seedSalt = Long.parseLong(
            c.getString(
                "seedSalt",
                g,
                Long.toString(seedSalt),
                "Mixed into the world seed so Quiet Lands doesn't look like an RWG2 overworld with the same seed. "
                    + "Changing it after chunks were generated causes seams."));
        continents = c.getBoolean(
            "continents",
            g,
            continents,
            "true = RWG2 'continents' preset (RWG2_CONTINENTS), false = plain RWG2. Don't change after generating chunks.");

        noWeather = c.getBoolean("noWeather", g, noWeather, "No rain, snow, thunder, snow layers or ice forming.");
        noVanillaOres = c.getBoolean("noVanillaOres", g, noVanillaOres, "Skip vanilla ores (coal, iron, ... emerald).");
        noStructures = c.getBoolean(
            "noStructures",
            g,
            noStructures,
            "No caves, mineshafts, villages, strongholds, dungeons or underground water/lava lakes.");
        noSprings = c.getBoolean(
            "noSprings",
            g,
            noSprings,
            "No single water/lava source blocks in cliffs and caves (they keep flowing and cause block updates).");
        logMobs = c.getBoolean(
            "logMobs",
            g,
            logMobs,
            "Log the first appearance of each mob type in Quiet Lands (spawners and spawn eggs are allowed; "
                + "this shows if some other mod spawns things).");
        enableRecipe = c.getBoolean("enableRecipe", g, enableRecipe, "Add a crafting recipe for the portal.");
        excludedBiomes = c.getStringList(
            "excludedBiomes",
            g,
            excludedBiomes,
            "Biomes RWG2 must not place in Quiet Lands (biome names as shown in F3, case-insensitive). Default: "
                + "Thaumcraft's Tainted Land, whose taint spreads and whose Flux Goo spawns Thaumic Slimes. "
                + "Changing it after chunks were generated can cause seams at the border to new chunks.");

        if (c.hasChanged()) c.save();
    }
}
