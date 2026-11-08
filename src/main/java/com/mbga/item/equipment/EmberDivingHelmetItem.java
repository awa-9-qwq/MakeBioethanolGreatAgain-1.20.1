package com.mbga.item.equipment;

import com.mbga.MBGA;
import com.simibubi.create.content.equipment.armor.DivingHelmetItem;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 余烬潜水头盔（参照 Create 的 {@code DivingHelmetItem}）。
 *
 * <p>直接继承 Create 的潜水头盔，因此自动获得它的全部行为：
 * <ul>
 *     <li>{@code DivingHelmetItem#breatheUnderwater} 在 Create 的 {@code CommonEvents} 里被调用，
 *         条件是「戴着的头盔是 {@code DivingHelmetItem}」——本类满足，于是水下 / 熔岩下会消耗背罐空气
 *         并补充氧气；</li>
 *     <li>固定 1 级「水下呼吸」附魔（{@code CustomEnchantmentsItem} 行为）。</li>
 * </ul>
 *
 * <p>数值使用 {@link EmberMaterials#ARMOR}，与其它余烬盔甲完全一致（头盔：护甲 5、耐久 37×11+1000、
 * 韧性 5、击退抗性 0.1、附魔 15、下界合金装备音效、余烬金属修复），并带「弑主 / 重铸」两条余烬机制
 * （{@code com.mbga.event.EmberGearHandler} 按 {@link EmberGear} 标记识别）。
 *
 * <p>护甲层贴图走 Create 的 {@code BaseArmorItem} 约定：{@code mbga:textures/models/armor/mbga_ember_layer_N.png}，
 * 与余烬盔甲共用同一套贴图。
 */
public class EmberDivingHelmetItem extends DivingHelmetItem implements EmberGear {

    public EmberDivingHelmetItem(ArmorMaterial material, Settings settings) {
        super(material, settings, armorTexture());
    }

    /** 余烬盔甲层贴图的位置（Create 的 {@code BaseArmorItem} 只取命名空间与路径名）。 */
    public static Identifier armorTexture() {
        return new Identifier(MBGA.MOD_ID, "mbga_ember");
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        EmberGear.appendTooltips(tooltip, world, context);
    }
}
