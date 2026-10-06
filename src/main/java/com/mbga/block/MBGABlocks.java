package com.mbga.block;

import com.mbga.MBGA;
import com.mbga.block.entity.FertileDirtBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.TransparentBlock;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

/**
 * MBGA 的方块注册表。
 *
 * <p>本类只负责「注册」与「数据驱动的行为」（燃料、挖掘等级标签），
 * 具体机制由 {@link SingularityBlock}、{@link FertileDirtBlock} 等方块类实现。
 */
public final class MBGABlocks {
    /** 固态生物乙醇：可烧制 8192 个物品（原版每个物品 200 tick）。 */
    public static final int SOLID_BIOETHANOL_BURN_TICKS = 8192 * 200;
    /** 固态石油：可烧制 6400 个物品。 */
    public static final int SOLID_OIL_BURN_TICKS = 6400 * 200;

    /** 自定义挖掘等级标签：需要「下界合金」级工具才会掉落。 */
    public static final TagKey<Block> NEEDS_NETHERITE_TOOL =
            TagKey.of(RegistryKeys.BLOCK, new Identifier(MBGA.MOD_ID, "needs_netherite_tool"));

    // ---- 方块 ----
    /** 空间超压器（占位：暂无机制）。 */
    public static Block SPACE_SUPERPRESSOR;
    /** 固态生物乙醇：燃料。 */
    public static Block SOLID_BIOETHANOL;
    /** 紫冰：摩擦力 0（比蓝冰更滑）。 */
    public static Block PURPLE_ICE;
    /** 强化深板岩：需要钻石级工具才能采掘，下界合金级才会掉落。 */
    public static Block REINFORCED_DEEPSLATE;
    /** 奇点块：无碰撞体积，接触的实体会持续受到虚空伤害与负面效果。 */
    public static Block SINGULARITY_BLOCK;
    /** 发光圆石：15 级光源。 */
    public static Block GLOWING_COBBLESTONE;
    /** 有机泥土（占位：暂无机制）。 */
    public static Block ORGANIC_DIRT;
    /** 富饶泥土：可直接种植作物（含甘蔗 / 海泡菜 / 仙人掌）、无视亮度生长、定时催熟上方作物；不可耕地。 */
    public static Block FERTILE_DIRT;
    /** 固态石油：燃料。 */
    public static Block SOLID_OIL;
    /** 余烬金属块：3×3 余烬金属合成，也是余烬升级模板配方里的材料。 */
    public static Block EMBER_METAL_BLOCK;

    // ---- 方块实体 ----
    public static BlockEntityType<FertileDirtBlockEntity> FERTILE_DIRT_BLOCK_ENTITY;

    private MBGABlocks() {
    }

    public static void register() {
        SPACE_SUPERPRESSOR = registerBlock("space_superpressor", new Block(
                AbstractBlock.Settings.create()
                        .mapColor(MapColor.IRON_GRAY)
                        .requiresTool()
                        .strength(5.0F, 1200.0F)
                        .sounds(BlockSoundGroup.METAL)));

        SOLID_BIOETHANOL = registerBlock("solid_bioethanol", new Block(
                AbstractBlock.Settings.create()
                        .mapColor(MapColor.GREEN)
                        .strength(1.5F)
                        .sounds(BlockSoundGroup.SLIME)));

        // 类似蓝冰：透明方块 + 极高光滑度。这里取 1 / 0.91 ≈ 1.0989，
        // 使原版移动公式里的速度乘数（slipperiness * 0.91）正好等于 1.0，也就是「完全不减速」。
        PURPLE_ICE = registerBlock("purple_ice", new TransparentBlock(
                AbstractBlock.Settings.create()
                        .mapColor(MapColor.PALE_PURPLE)
                        .strength(2.8F)
                        .slipperiness(1.0F / 0.91F)
                        .sounds(BlockSoundGroup.GLASS)));

        REINFORCED_DEEPSLATE = registerBlock("reinforced_deepslate", new Block(
                AbstractBlock.Settings.create()
                        .mapColor(MapColor.DEEPSLATE_GRAY)
                        .requiresTool()
                        .strength(50.0F, 1200.0F)
                        .sounds(BlockSoundGroup.DEEPSLATE)));

        SINGULARITY_BLOCK = registerBlock("singularity_block",
                new SingularityBlock(
                        AbstractBlock.Settings.create()
                                .mapColor(MapColor.BLACK)
                                .requiresTool()
                                .strength(50.0F, 1200.0F)
                                .noCollision()
                                .sounds(BlockSoundGroup.SCULK)),
                // 掉落物免疫火焰（爆炸 / 虚空由 ItemEntityMixin 处理）。
                new Item.Settings().fireproof());

        GLOWING_COBBLESTONE = registerBlock("glowing_cobblestone", new Block(
                AbstractBlock.Settings.create()
                        .mapColor(MapColor.STONE_GRAY)
                        .requiresTool()
                        .strength(2.0F, 6.0F)
                        .luminance(state -> 15)
                        .sounds(BlockSoundGroup.STONE)));

        ORGANIC_DIRT = registerBlock("organic_dirt", new Block(
                AbstractBlock.Settings.create()
                        .mapColor(MapColor.DIRT_BROWN)
                        .strength(0.5F)
                        .sounds(BlockSoundGroup.GRAVEL)));

        FERTILE_DIRT = registerBlock("fertile_dirt", new FertileDirtBlock(
                AbstractBlock.Settings.create()
                        .mapColor(MapColor.DIRT_BROWN)
                        .strength(0.5F)
                        .sounds(BlockSoundGroup.GRAVEL)));

        SOLID_OIL = registerBlock("solid_oil", new Block(
                AbstractBlock.Settings.create()
                        .mapColor(MapColor.BLACK)
                        .strength(2.0F)
                        .sounds(BlockSoundGroup.STONE)));

        EMBER_METAL_BLOCK = registerBlock("ember_metal_block", new Block(
                AbstractBlock.Settings.create()
                        .mapColor(MapColor.ORANGE)
                        .requiresTool()
                        .strength(50.0F, 1200.0F)
                        .sounds(BlockSoundGroup.NETHERITE)),
                new Item.Settings().fireproof());

        // ---- 方块实体 ----
        FERTILE_DIRT_BLOCK_ENTITY = Registry.register(
                Registries.BLOCK_ENTITY_TYPE,
                new Identifier(MBGA.MOD_ID, "fertile_dirt"),
                FabricBlockEntityTypeBuilder.create(FertileDirtBlockEntity::new, FERTILE_DIRT).build());

        // ---- 燃料 ----
        FuelRegistry.INSTANCE.add(SOLID_BIOETHANOL.asItem(), SOLID_BIOETHANOL_BURN_TICKS);
        FuelRegistry.INSTANCE.add(SOLID_OIL.asItem(), SOLID_OIL_BURN_TICKS);

        // 富饶泥土不再能被锄头耕成耕地（按需求删除该机制）。
    }

    /**
     * 该物品是否是奇点块的掉落物（掉落物免疫火焰 / 爆炸 / 虚空伤害）。
     */
    public static boolean isSingularityDrop(ItemStack stack) {
        return SINGULARITY_BLOCK != null && !stack.isEmpty() && stack.isOf(SINGULARITY_BLOCK.asItem());
    }

    private static Block registerBlock(String name, Block block) {
        return registerBlock(name, block, new Item.Settings());
    }

    private static Block registerBlock(String name, Block block, Item.Settings itemSettings) {
        Identifier id = new Identifier(MBGA.MOD_ID, name);
        Registry.register(Registries.BLOCK, id, block);
        Registry.register(Registries.ITEM, id, new BlockItem(block, itemSettings));
        return block;
    }
}
