package com.mbga.item.equipment;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShovelItem;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** 余烬锹：伤害比下界合金锹高 2 点（提示伤害 7.5）。 */
public class EmberShovelItem extends ShovelItem implements EmberGear {
    public EmberShovelItem(Settings settings) {
        super(EmberMaterials.TOOL, 7.5F, -3.0F, settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        EmberGear.appendTooltips(tooltip, world, context);
    }
}
