package com.mbga.item.equipment;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PickaxeItem;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** 余烬镐：伤害比下界合金镐高 2 点（提示伤害 7）。 */
public class EmberPickaxeItem extends PickaxeItem implements EmberGear {
    public EmberPickaxeItem(Settings settings) {
        super(EmberMaterials.TOOL, 7, -2.8F, settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        EmberGear.appendTooltips(tooltip, world, context);
    }
}
