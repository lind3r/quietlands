package com.lind3r.quietlands.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;

import com.lind3r.quietlands.world.Travel;

/** Placeable portal. Right-click to travel; sneak-right-click does nothing (so you can place blocks against it). */
public class BlockQuietPortal extends Block {

    public BlockQuietPortal() {
        super(Material.rock);
        setBlockName("quietlands.portal");
        setBlockTextureName("quietlands:portal");
        setCreativeTab(CreativeTabs.tabTransport);
        setHardness(5f);
        setResistance(2000f);
        setLightLevel(0.6f);
        setHarvestLevel("pickaxe", 1);
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hx,
        float hy, float hz) {
        if (player.isSneaking()) return false;
        if (!world.isRemote && player instanceof EntityPlayerMP) {
            Travel.use((EntityPlayerMP) player);
        }
        return true;
    }
}
