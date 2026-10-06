package com.mbga.integration.farmersdelight;

import com.mbga.MBGA;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

/**
 * MBGA × 农夫乐事（Farmer's Delight，refabricated）联动注册表。
 *
 * <p>本类只负责注册「余烬砧板」方块、其方块物品与方块实体类型，
 * 并对外暴露余烬刀的字段 / 工厂方法。刀的具体注册由父级（{@code MBGAItems}）完成，
 * 例如：
 * <pre>{@code
 * EMBER_KNIFE = register("ember_knife", new EmberKnifeItem(new Item.Settings().maxCount(1).fireproof()));
 * }</pre>
 * 因此在 {@code MBGA.onInitialize()} 里调用顺序无论先后都不会重复注册：
 * {@link #register()} 若发现 {@code mbga:ember_knife} 已被注册，只会把它记录到
 * {@link #EMBER_KNIFE}；若还没注册，可用 {@link #registerEmberKnife()} 让本类代劳。
 */
public final class MBGAFarmersDelight {
    /** 余烬刀的物品 id：{@code mbga:ember_knife}。 */
    public static final Identifier EMBER_KNIFE_ID = new Identifier(MBGA.MOD_ID, "ember_knife");
    /** 余烬砧板的方块 / 方块物品 / 方块实体 id：{@code mbga:ember_cutting_board}。 */
    public static final Identifier EMBER_CUTTING_BOARD_ID = new Identifier(MBGA.MOD_ID, "ember_cutting_board");

    /** 余烬砧板方块（创造模式物品栏用 {@link #EMBER_CUTTING_BOARD_ITEM} 或方块本身）。 */
    public static Block EMBER_CUTTING_BOARD;
    /** 余烬砧板的方块物品。 */
    public static BlockItem EMBER_CUTTING_BOARD_ITEM;
    /** 余烬砧板的方块实体类型（{@code mbga:ember_cutting_board}）。 */
    public static BlockEntityType<EmberCuttingBoardBlockEntity> EMBER_CUTTING_BOARD_ENTITY;
    /**
     * 余烬刀。由父级（{@code MBGAItems}）注册后赋值；{@link #register()} 也会自动接管
     * 已经注册好的 {@code mbga:ember_knife}。取用请用 {@link #emberKnife()} 以兼容两种注册顺序。
     */
    @Nullable
    public static Item EMBER_KNIFE;

    private static boolean registered;

    private MBGAFarmersDelight() {
    }

    /** 注册余烬砧板方块 + 方块物品 + 方块实体类型（可安全重复调用）。 */
    public static void register() {
        if (registered) {
            return;
        }
        registered = true;

        EMBER_CUTTING_BOARD = Registry.register(Registries.BLOCK, EMBER_CUTTING_BOARD_ID,
                new EmberCuttingBoardBlock(AbstractBlock.Settings.create()
                        .mapColor(MapColor.ORANGE)
                        .strength(2.0F)
                        .sounds(BlockSoundGroup.WOOD)));

        EMBER_CUTTING_BOARD_ITEM = Registry.register(Registries.ITEM, EMBER_CUTTING_BOARD_ID,
                new BlockItem(EMBER_CUTTING_BOARD, new Item.Settings().fireproof()));

        EMBER_CUTTING_BOARD_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, EMBER_CUTTING_BOARD_ID,
                FabricBlockEntityTypeBuilder.create(EmberCuttingBoardBlockEntity::new, EMBER_CUTTING_BOARD).build());

        // 让漏斗等自动化也能访问砧板（与农夫乐事原版砧板一致）。
        ItemStorage.SIDED.registerForBlockEntity(
                (board, direction) -> board.getStorage(direction), EMBER_CUTTING_BOARD_ENTITY);

        // 若父级已经先注册了 mbga:ember_knife，这里直接接管它的实例。
        if (EMBER_KNIFE == null && Registries.ITEM.containsId(EMBER_KNIFE_ID)) {
            EMBER_KNIFE = Registries.ITEM.get(EMBER_KNIFE_ID);
        }
    }

    // ------------------------------------------------------------------
    // 余烬刀
    // ------------------------------------------------------------------

    /** 创建余烬刀实例（父级自行注册时使用；数值见 {@link EmberKnifeItem}）。 */
    public static Item createEmberKnife() {
        return new EmberKnifeItem(new Item.Settings().maxCount(1).fireproof());
    }

    /**
     * 注册余烬刀（仅当 {@code mbga:ember_knife} 尚未注册）。
     * 父级若已在 {@code MBGAItems} 里注册，请勿调用本方法。
     *
     * @return 已注册的余烬刀物品
     */
    public static Item registerEmberKnife() {
        if (EMBER_KNIFE == null) {
            EMBER_KNIFE = Registries.ITEM.containsId(EMBER_KNIFE_ID)
                    ? Registries.ITEM.get(EMBER_KNIFE_ID)
                    : Registry.register(Registries.ITEM, EMBER_KNIFE_ID, createEmberKnife());
        }
        return EMBER_KNIFE;
    }

    /**
     * 读取余烬刀：优先返回 {@link #EMBER_KNIFE}，否则从注册表按 id 解析。
     * 两种注册顺序（父级先注册 / 本类代注册）下都能拿到实例。
     */
    @Nullable
    public static Item emberKnife() {
        if (EMBER_KNIFE == null && Registries.ITEM.containsId(EMBER_KNIFE_ID)) {
            EMBER_KNIFE = Registries.ITEM.get(EMBER_KNIFE_ID);
        }
        return EMBER_KNIFE;
    }
}
