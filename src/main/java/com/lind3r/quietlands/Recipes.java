package com.lind3r.quietlands;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;

import cpw.mods.fml.common.registry.GameRegistry;

public final class Recipes {

    private Recipes() {}

    /**
     * HV tier, needs Titanium (the Personal Dimension quest is gated the same way):
     *
     * <pre>
     * P E P     P = Titanium Plate
     * E F E     F = Titanium Frame Box
     * P E P     E = Eye of Ender
     * </pre>
     */
    public static void register() {
        if (!Config.enableRecipe) return;
        for (String ore : new String[] { "plateTitanium", "frameGtTitanium" }) {
            if (OreDictionary.getOres(ore)
                .isEmpty()) {
                QuietLands.LOG.warn("Ore dictionary has no '{}'; the portal recipe can't be crafted.", ore);
            }
        }
        GameRegistry.addRecipe(
            new ShapedOreRecipe(
                new ItemStack(QuietLands.portal),
                "PEP",
                "EFE",
                "PEP",
                'P',
                "plateTitanium",
                'F',
                "frameGtTitanium",
                'E',
                Items.ender_eye));
    }
}
