package com.mbga.item.equipment;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** 余烬斧：伤害比下界合金斧高 2 点（提示伤害 11）。 */
public class EmberAxeItem extends AxeItem implements EmberGear {
    public EmberAxeItem(Settings settings) {
        super(EmberMaterials.TOOL, 11.0F, -3.0F, settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        EmberGear.appendTooltips(tooltip, world, context);
    }
}
