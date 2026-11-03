package com.lind3r.quietlands.world;

import java.util.Collections;
import java.util.List;

import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;

/** Wraps RWG2's generator; only change: no creature lists, so nothing that asks the world what may spawn gets mobs. */
public class QuietChunkProvider implements IChunkProvider {

    private final IChunkProvider delegate;

    public QuietChunkProvider(IChunkProvider delegate) {
        this.delegate = delegate;
    }

    @Override
    public List getPossibleCreatures(EnumCreatureType type, int x, int y, int z) {
        return Collections.emptyList();
    }

    @Override
    public ChunkPosition func_147416_a(World world, String structure, int x, int y, int z) {
        return null; // no structures here (ender eyes don't fly)
    }

    @Override
    public boolean chunkExists(int x, int z) {
        return delegate.chunkExists(x, z);
    }

    @Override
    public Chunk provideChunk(int x, int z) {
        return delegate.provideChunk(x, z);
    }

    @Override
    public Chunk loadChunk(int x, int z) {
        return delegate.loadChunk(x, z);
    }

    @Override
    public void populate(IChunkProvider provider, int x, int z) {
        delegate.populate(provider, x, z);
    }

    @Override
    public boolean saveChunks(boolean all, IProgressUpdate progress) {
        return delegate.saveChunks(all, progress);
    }

    @Override
    public boolean unloadQueuedChunks() {
        return delegate.unloadQueuedChunks();
    }

    @Override
    public boolean canSave() {
        return delegate.canSave();
    }

    @Override
    public String makeString() {
        return "QuietLands/" + delegate.makeString();
    }

    @Override
    public int getLoadedChunkCount() {
        return delegate.getLoadedChunkCount();
    }

    @Override
    public void recreateStructures(int x, int z) {
        delegate.recreateStructures(x, z);
    }

    @Override
    public void saveExtraData() {
        delegate.saveExtraData();
    }
}
