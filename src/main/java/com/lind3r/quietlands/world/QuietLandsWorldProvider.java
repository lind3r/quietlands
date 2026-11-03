package com.lind3r.quietlands.world;

import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;

import com.lind3r.quietlands.Config;

public class QuietLandsWorldProvider extends WorldProvider {

    @Override
    protected void registerWorldChunkManager() {
        this.terrainType = Rwg2Bridge.worldType();
        this.worldChunkMgr = Rwg2Bridge.createChunkManager(worldObj);
    }

    @Override
    public IChunkProvider createChunkGenerator() {
        return new QuietChunkProvider(Rwg2Bridge.createGenerator(worldObj));
    }

    @Override
    public String getDimensionName() {
        return "Quiet Lands";
    }

    @Override
    public long getSeed() {
        return super.getSeed() ^ Config.seedSalt;
    }

    @Override
    public String getWelcomeMessage() {
        return "Entering the Quiet Lands";
    }

    @Override
    public String getDepartMessage() {
        return "Leaving the Quiet Lands";
    }

    /** Beds work (no explosion); dying here respawns you here (at your bed in this dimension, or the arrival hub). */
    @Override
    public boolean canRespawnHere() {
        return true;
    }

    @Override
    public ChunkCoordinates getSpawnPoint() {
        if (!worldObj.isRemote) {
            ChunkCoordinates hub = QuietLandsData.get(worldObj)
                .getHub();
            if (hub != null) return new ChunkCoordinates(hub);
        }
        return super.getSpawnPoint();
    }

    @Override
    public ChunkCoordinates getRandomizedSpawnPoint() {
        return getSpawnPoint();
    }

    // ---- weather ----

    @Override
    public void updateWeather() {
        if (!Config.noWeather) {
            super.updateWeather();
            return;
        }
        worldObj.prevRainingStrength = worldObj.rainingStrength = 0f;
        worldObj.prevThunderingStrength = worldObj.thunderingStrength = 0f;
    }

    @Override
    public boolean canDoRainSnowIce(Chunk chunk) {
        return !Config.noWeather && super.canDoRainSnowIce(chunk);
    }

    @Override
    public boolean canDoLightning(Chunk chunk) {
        return !Config.noWeather && super.canDoLightning(chunk);
    }
}
