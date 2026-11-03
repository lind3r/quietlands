package com.lind3r.quietlands;

import net.minecraft.block.Block;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lind3r.quietlands.block.BlockQuietPortal;
import com.lind3r.quietlands.world.QuietLandsWorldProvider;
import com.lind3r.quietlands.world.Rwg2Bridge;
import com.lind3r.quietlands.world.WorldEvents;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;

@Mod(
    modid = QuietLands.MODID,
    version = Tags.VERSION,
    name = "Quiet Lands",
    acceptedMinecraftVersions = "[1.7.10]",
    dependencies = "required-after:rwg2")
public class QuietLands {

    public static final String MODID = "quietlands";
    public static final Logger LOG = LogManager.getLogger(MODID);

    public static Block portal;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        Config.load(event.getSuggestedConfigurationFile());

        portal = new BlockQuietPortal();
        GameRegistry.registerBlock(portal, "portal");

        if (!Rwg2Bridge.init()) {
            LOG.error("RWG2 could not be hooked; the Quiet Lands dimension is NOT registered. See the errors above.");
            return;
        }
        int id = Config.dimensionId;
        if (DimensionManager.isDimensionRegistered(id)) {
            LOG.error("Dimension ID {} is already taken by another mod; change dimensionId in quietlands.cfg.", id);
            return;
        }
        // keepLoaded = false: the dimension unloads when nobody is in it and its spawn area is not kept loaded.
        DimensionManager.registerProviderType(id, QuietLandsWorldProvider.class, false);
        DimensionManager.registerDimension(id, id);
        LOG.info("Quiet Lands registered as dimension {}", id);

        WorldEvents events = new WorldEvents();
        MinecraftForge.EVENT_BUS.register(events);
        MinecraftForge.TERRAIN_GEN_BUS.register(events);
        MinecraftForge.ORE_GEN_BUS.register(events);
        FMLCommonHandler.instance()
            .bus()
            .register(events);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        Recipes.register();
    }

    public static boolean isQuietLands(net.minecraft.world.World world) {
        return world != null && world.provider != null && world.provider.dimensionId == Config.dimensionId;
    }
}
