package com.mbga.item.equipment;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** 余烬盔甲：基础数值与下界合金盔甲一致（护甲 3/8/6/3、韧性 3、击退抗性 0.1）。 */
public class EmberArmorItem extends ArmorItem implements EmberGear {
    public EmberArmorItem(Type type, Settings settings) {
        super(EmberMaterials.ARMOR, type, settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        EmberGear.appendTooltips(tooltip, world, context);
    }
}
