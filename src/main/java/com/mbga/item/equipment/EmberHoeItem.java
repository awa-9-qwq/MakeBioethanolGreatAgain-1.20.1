package com.mbga.item.equipment;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.HoeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** 余烬锄：伤害比下界合金锄高 2 点（提示伤害 2）。 */
public class EmberHoeItem extends HoeItem implements EmberGear {
    public EmberHoeItem(Settings settings) {
        super(EmberMaterials.TOOL, -2, 0.0F, settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        EmberGear.appendTooltips(tooltip, world, context);
    }
}
