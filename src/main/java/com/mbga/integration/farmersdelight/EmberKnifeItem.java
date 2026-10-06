package com.mbga.integration.farmersdelight;

import com.mbga.item.equipment.EmberGear;
import com.mbga.item.equipment.EmberMaterials;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import vectorwing.farmersdelight.common.item.KnifeItem;

import java.util.List;

/**
 * 余烬刀：农夫乐事（Farmer's Delight，refabricated）的刀 + MBGA 的余烬装备标记。
 *
 * <p>继承 {@link KnifeItem}（唯一构造函数为
 * {@code KnifeItem(ToolMaterial, float attackDamage, float attackSpeed, Item.Settings)}），
 * 因此自动获得农夫乐事的全部刀机制：砧板切割（{@code farmersdelight:cutting} 配方）、
 * 蛋糕取片、可附魔等。工具材料直接复用 {@link EmberMaterials#TOOL}
 * （耐久 3031 / 挖掘速度 9.0 / 攻击力 0.0 / 挖掘等级 4 / 附魔度 15 / 用余烬金属修复）。
 *
 * <p>实现 {@link EmberGear} 后，{@code EmberGearHandler} 的「弑主」「重铸」耐久机制
 * 与橙色提示文案同样生效（与余烬剑 / 斧 / 镐等一致）。
 *
 * <p>注册示例（父级 MBGAItems 内）：
 * {@code EMBER_KNIFE = register("ember_knife", new EmberKnifeItem(new Item.Settings().maxCount(1).fireproof()));}
 */
public class EmberKnifeItem extends KnifeItem implements EmberGear {
    /** 攻击伤害加成：与农夫乐事自家五把刀一致。 */
    private static final float ATTACK_DAMAGE = 0.5F;
    /** 攻击速度加成：与农夫乐事自家五把刀一致。 */
    private static final float ATTACK_SPEED = -2.0F;

    public EmberKnifeItem(Settings settings) {
        super(EmberMaterials.TOOL, ATTACK_DAMAGE, ATTACK_SPEED, settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        EmberGear.appendTooltips(tooltip, world, context);
    }
}
