package com.mbga.item.equipment;

import com.simibubi.create.content.equipment.armor.DivingBootsItem;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 余烬潜水靴（参照 Create 的 {@code DivingBootsItem}）。
 *
 * <p>直接继承 Create 的潜水靴，因此自动获得它的全部行为：Create 的 {@code LavaSwimmingMixin} 会按
 * 「靴子是 {@code DivingBootsItem}」判断，让穿戴者在水中 / 熔岩中加速下潜、上岸后提高移动速度；
 * {@code CommonEvents} 也会调用 {@code DivingBootsItem#accellerateDescentUnderwater}。
 *
 * <p>数值与其它余烬靴子完全一致（护甲 5、耐久 37×13+1000、韧性 5、击退抗性 0.1、附魔 15、
 * 下界合金装备音效、余烬金属修复），并带「弑主 / 重铸」两条余烬机制。
 */
public class EmberDivingBootsItem extends DivingBootsItem implements EmberGear {

    public EmberDivingBootsItem(ArmorMaterial material, Settings settings) {
        super(material, settings, EmberDivingHelmetItem.armorTexture());
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        EmberGear.appendTooltips(tooltip, world, context);
    }
}
