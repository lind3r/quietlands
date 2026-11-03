package com.lind3r.quietlands.world;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;

/** Saved in the Quiet Lands dimension's own data folder: where the shared arrival hub is. */
public class QuietLandsData extends WorldSavedData {

    private static final String NAME = "quietlands";

    private ChunkCoordinates hub;

    public QuietLandsData(String name) {
        super(name);
    }

    public static QuietLandsData get(World world) {
        QuietLandsData data = (QuietLandsData) world.perWorldStorage.loadData(QuietLandsData.class, NAME);
        if (data == null) {
            data = new QuietLandsData(NAME);
            world.perWorldStorage.setData(NAME, data);
        }
        return data;
    }

    public ChunkCoordinates getHub() {
        return hub;
    }

    public void setHub(ChunkCoordinates hub) {
        this.hub = hub;
        markDirty();
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        if (tag.hasKey("hubY")) {
            hub = new ChunkCoordinates(tag.getInteger("hubX"), tag.getInteger("hubY"), tag.getInteger("hubZ"));
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        if (hub != null) {
            tag.setInteger("hubX", hub.posX);
            tag.setInteger("hubY", hub.posY);
            tag.setInteger("hubZ", hub.posZ);
        }
    }
}
