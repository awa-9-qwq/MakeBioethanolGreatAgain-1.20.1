package com.mbga.integration.oreexcavation;

import com.mbga.MBGA;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 探矿杖注册表：{@code mbga:netherite_prospector}（下界合金探矿杖）与
 * {@code mbga:ember_prospector}（余烬探矿杖）。
 *
 * <p>两个物品都直接注册进 {@link Registries#ITEM}。由 {@code MBGA#onInitialize} 调用一次
 * {@link #register()}，之后即可用 {@link #NETHERITE_PROSPECTOR} / {@link #EMBER_PROSPECTOR}
 * 把它们加进创造模式物品栏。
 */
public final class MBGAProspectors {

    /** 下界合金探矿杖：报告最近矿脉的距离（区块）。 */
    public static NetheriteProspectorItem NETHERITE_PROSPECTOR;
    /** 余烬探矿杖：打印附近矿脉的区块坐标，无冷却。 */
    public static EmberProspectorItem EMBER_PROSPECTOR;

    private MBGAProspectors() {
    }

    /** 注册两个探矿杖物品（可安全重复调用）。 */
    public static void register() {
        if (NETHERITE_PROSPECTOR != null && EMBER_PROSPECTOR != null) {
            return;
        }
        NetheriteProspectorItem netherite = new NetheriteProspectorItem(new Item.Settings().maxCount(1));
        NETHERITE_PROSPECTOR = Registry.register(Registries.ITEM, id("netherite_prospector"), netherite);
        EmberProspectorItem ember = new EmberProspectorItem(new Item.Settings().maxCount(1).fireproof());
        EMBER_PROSPECTOR = Registry.register(Registries.ITEM, id("ember_prospector"), ember);
    }

    /** 已注册的探矿杖（未注册时为空列表）。 */
    public static List<Item> all() {
        List<Item> items = new ArrayList<>(2);
        if (NETHERITE_PROSPECTOR != null) {
            items.add(NETHERITE_PROSPECTOR);
        }
        if (EMBER_PROSPECTOR != null) {
            items.add(EMBER_PROSPECTOR);
        }
        return Collections.unmodifiableList(items);
    }

    private static Identifier id(String path) {
        return new Identifier(MBGA.MOD_ID, path);
    }
}
