package com.mbga.item.equipment;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** 余烬剑：伤害比下界合金剑高 2 点（提示伤害 9）。 */
public class EmberSwordItem extends SwordItem implements EmberGear {
    public EmberSwordItem(Settings settings) {
        super(EmberMaterials.TOOL, 9, -2.4F, settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        EmberGear.appendTooltips(tooltip, world, context);
    }
}
