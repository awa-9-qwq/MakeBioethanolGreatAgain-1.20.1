package com.mbga.item.equipment;

import com.simibubi.create.AllEnchantments;
import com.simibubi.create.content.equipment.armor.BacktankItem;
import com.simibubi.create.content.equipment.armor.BacktankUtil;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

/**
 * 余烬背罐（参照 Create 的 {@code BacktankItem} / {@code NetheriteBacktank}）。
 *
 * <p>作为 {@code BacktankItem} 的子类，它自动获得 Create 背罐的全部行为：
 * <ul>
 *     <li>穿戴在胸甲槽（{@code BacktankItem.SLOT}），由 {@code BacktankArmorLayer} 渲染背上的罐体；</li>
 *     <li>可放置成方块（{@code mbga:ember_backtank}，见 {@code EmberBacktankBlock}），
 *         接上传动杆后会按转速充气；</li>
 *     <li>被潜水头盔（{@code DivingHelmetItem}）、伸缩握把等消耗空气；</li>
 *     <li>接受 Create 的「容量」附魔（{@link BacktankItem} 实现 {@code ICapacityEnchantable}）；</li>
 *     <li>因为物品是 fireproof，与 fireproof 的潜水头盔 / 靴子组成全套后，Create 的
 *         {@code NetheriteDivingHandler} 会给予熔岩免疫（与下界合金潜水套装一致）。</li>
 * </ul>
 *
 * <p><b>容量</b>：余烬背罐的储气上限是同附魔等级下界合金背罐的 {@link #CAPACITY_MULTIPLIER} 倍。
 * Create 的容量是一个全局静态值（{@code BacktankUtil.maxAir(ItemStack)}），因此这里不能只靠覆盖物品方法，
 * 还需要 {@code com.mbga.mixin.BacktankUtilMixin} 把「读取空气 / 消耗空气」两处换成
 * {@link #maxAir(ItemStack)}；方块形态的容量则由 {@code BacktankBlockEntityMixin} 处理。
 *
 * <p>数值与其它余烬装备一致：护甲 10（胸甲）、耐久 37×16+1000、韧性 5、击退抗性 0.1、附魔 15、
 * 下界合金装备音效、余烬金属修复，并带「弑主 / 重铸」两条余烬机制。
 */
public class EmberBacktankItem extends BacktankItem implements EmberGear {

    /** 储气上限 = 下界合金背罐 × 2。 */
    public static final int CAPACITY_MULTIPLIER = 2;

    public EmberBacktankItem(ArmorMaterial material, Settings settings,
                             Supplier<BacktankBlockItem> placeable) {
        super(material, settings, EmberDivingHelmetItem.armorTexture(), placeable);
    }

    /**
     * 该背罐的储气上限：Create 会按「容量」附魔等级算出下界合金背罐的上限，这里再乘以 2。
     *
     * <p>由 {@code BacktankUtilMixin} 在 {@code BacktankUtil#getAir} / {@code #consumeAir} 中调用。
     */
    public static int maxAir(ItemStack stack) {
        int capacityLevel = EnchantmentHelper.getLevel(AllEnchantments.CAPACITY.get(), stack);
        return maxAir(capacityLevel);
    }

    /** 方块形态使用的上限（附魔等级 → 储气上限，已翻倍）。 */
    public static int maxAir(int capacityEnchantLevel) {
        return CAPACITY_MULTIPLIER * BacktankUtil.maxAir(capacityEnchantLevel);
    }

    /** 与 Create 的背罐一致：耐久条显示的是「空气量」，但以 2 倍容量为满。 */
    @Override
    public int getItemBarStep(ItemStack stack) {
        float remaining = getRemainingAir(stack);
        return Math.round(13.0F * MathHelper.clamp(remaining / (float) maxAir(stack), 0.0F, 1.0F));
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        EmberGear.appendTooltips(tooltip, world, context);
        tooltip.add(Text.translatable("item.mbga.ember_backtank.tooltip.capacity")
                .formatted(Formatting.AQUA));
    }
}
