package com.mbga.item.equipment;

import net.minecraft.item.SmithingTemplateItem;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;

/**
 * 余烬升级锻造模板：与 {@link SmithingTemplateItem} 完全一致，但物品本身免疫火焰与熔岩
 * （{@code SmithingTemplateItem} 的构造函数固定使用默认 {@code Item.Settings}，无法通过 settings 设置 fireproof，
 * 因此这里直接覆盖 {@link #isFireproof()}）。
 */
public class EmberUpgradeTemplateItem extends SmithingTemplateItem {
    public EmberUpgradeTemplateItem(Text appliesToText, Text ingredientsText, Text titleText,
                                    Text baseSlotDescriptionText, Text additionsSlotDescriptionText,
                                    List<Identifier> emptyBaseSlotTextures, List<Identifier> emptyAdditionsSlotTextures) {
        super(appliesToText, ingredientsText, titleText, baseSlotDescriptionText, additionsSlotDescriptionText,
                emptyBaseSlotTextures, emptyAdditionsSlotTextures);
    }

    @Override
    public boolean isFireproof() {
        return true;
    }
}
