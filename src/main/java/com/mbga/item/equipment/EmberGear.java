package com.mbga.item.equipment;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 「余烬」装备 / 工具的标记接口：橙色提示文案 + 两条特殊耐久机制。
 *
 * <ul>
 *     <li><b>弑主</b>：装备者每损失 1 点生命值，恢复 1 点耐久；</li>
 *     <li><b>重铸</b>：在火或熔岩中每 tick 恢复 1 点耐久。</li>
 * </ul>
 *
 * 具体结算在 {@code com.mbga.event.EmberGearHandler}。
 */
public interface EmberGear {

    /** 橙色提示文案（在所有余烬装备 / 工具上显示）。 */
    static void appendTooltips(List<Text> tooltip, @Nullable World world, TooltipContext context) {
        tooltip.add(Text.translatable("item.mbga.ember.tooltip.soul_bound").formatted(Formatting.GOLD));
        tooltip.add(Text.translatable("item.mbga.ember.tooltip.reforge").formatted(Formatting.GOLD));
    }

    static boolean isEmber(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof EmberGear;
    }
}
