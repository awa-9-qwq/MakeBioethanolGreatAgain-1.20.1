package com.mbga.block.windmill;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
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

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 六种风力发电机的注册表。
 *
 * <p>只注册方块、方块物品、共用的方块实体类型，以及序列组装配方需要的过渡物品
 * {@code mbga:incomplete_windmill}。物品栏归组由 {@code MBGA}/{@code MBGAItems} 负责。
 */
public final class MBGAWindmills {

    /** 与 {@code MBGA.MOD_ID} 相同（这里重复定义是为了让本包可以独立编译检查）。 */
    public static final String MOD_ID = "mbga";

    /** 过渡物品的注册 id。 */
    public static final String INCOMPLETE_WINDMILL_PATH = "incomplete_windmill";

    /** 六个等级：注册 id、转速（RPM）、每 RPM 应力容量（SU/RPM）、总应力（SU，= 容量 × 转速）。 */
    public enum Tier {
        SMALL("small_windmill", 16, 512.0F, 8192L),
        LARGE("large_windmill", 256, 4096.0F, 1048576L),
        GROUP("windmill_group", 256, 524288.0F, 134217728L),
        MEDIUM_GROUP("medium_windmill_group", 256, 67108864.0F, 17179869184L),
        LARGE_GROUP("large_windmill_group", 256, 8589934592.0F, 2199023255552L),
        GIANT_GROUP("giant_windmill_group", 256, 1099511627776.0F, 281474976710656L);

        private final String path;
        private final int rpm;
        private final float stressCapacityPerRpm;
        private final long totalStress;

        Tier(String path, int rpm, float stressCapacityPerRpm, long totalStress) {
            this.path = path;
            this.rpm = rpm;
            this.stressCapacityPerRpm = stressCapacityPerRpm;
            this.totalStress = totalStress;
        }

        /** 注册 id 的路径部分（{@code mbga:<path>}）。 */
        public String getPath() {
            return path;
        }

        public Identifier getId() {
            return new Identifier(MOD_ID, path);
        }

        public int getRpm() {
            return rpm;
        }

        /** SU / RPM。 */
        public float getStressCapacityPerRpm() {
            return stressCapacityPerRpm;
        }

        /** 该等级在额定转速下的总应力（SU），仅用于文档 / 显示。 */
        public long getTotalStress() {
            return totalStress;
        }
    }

    /** 注册 id 路径 -> 方块（保持枚举顺序）。 */
    public static final Map<String, WindmillBlock> BLOCKS = new LinkedHashMap<>();
    /** 注册 id 路径 -> 方块物品。 */
    public static final Map<String, Item> ITEMS = new LinkedHashMap<>();

    /** 序列组装过渡物品 {@code mbga:incomplete_windmill}。 */
    public static Item INCOMPLETE_WINDMILL;

    /** 六个方块共用的方块实体类型。 */
    public static BlockEntityType<WindmillBlockEntity> WINDMILL_ENTITY;

    private MBGAWindmills() {
    }

    public static void register() {
        // 1) 六个方块
        for (Tier tier : Tier.values()) {
            WindmillBlock block = new WindmillBlock(
                    AbstractBlock.Settings.create()
                            .mapColor(MapColor.WHITE)
                            .strength(2.0F)
                            .sounds(BlockSoundGroup.WOOD),
                    tier.getRpm(),
                    tier.getStressCapacityPerRpm());
            Registry.register(Registries.BLOCK, tier.getId(), block);
            BLOCKS.put(tier.getPath(), block);
        }

        // 2) 共用的方块实体类型（六个方块都有效；ticker 由 IBE 默认实现提供）
        Block[] validBlocks = BLOCKS.values().toArray(new Block[0]);
        WINDMILL_ENTITY = Registry.register(
                Registries.BLOCK_ENTITY_TYPE,
                new Identifier(MOD_ID, "windmill"),
                FabricBlockEntityTypeBuilder.create(WindmillBlockEntity::new, validBlocks).build());

        // 3) 方块物品
        for (Map.Entry<String, WindmillBlock> entry : BLOCKS.entrySet()) {
            Item item = new BlockItem(entry.getValue(), new Item.Settings());
            Registry.register(Registries.ITEM, new Identifier(MOD_ID, entry.getKey()), item);
            ITEMS.put(entry.getKey(), item);
        }

        // 4) 序列组装过渡物品
        INCOMPLETE_WINDMILL = Registry.register(
                Registries.ITEM,
                new Identifier(MOD_ID, INCOMPLETE_WINDMILL_PATH),
                new Item(new Item.Settings()));
    }

    /** 按注册 id 路径取方块（例如 {@code "small_windmill"}），未注册时返回 {@code null}。 */
    public static WindmillBlock getBlock(String path) {
        return BLOCKS.get(path);
    }

    /** 按等级取方块，未注册时返回 {@code null}。 */
    public static WindmillBlock getBlock(Tier tier) {
        return BLOCKS.get(tier.getPath());
    }

    /** 供物品栏遍历使用的只读视图，顺序与 {@link Tier} 一致。 */
    public static Collection<WindmillBlock> allBlocks() {
        return Collections.unmodifiableCollection(BLOCKS.values());
    }

    /** 供物品栏遍历使用的只读视图，顺序与 {@link Tier} 一致。 */
    public static Collection<Item> allItems() {
        return Collections.unmodifiableCollection(ITEMS.values());
    }
}
