package com.mbga.block;

import com.mbga.block.entity.FertileDirtBlockEntity;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * 富饶泥土：可以像耕地一样直接种植作物（由 CropBlockMixin / StemBlockMixin 放行），
 * 并且每 10 tick 有 50% 概率催熟一次上方作物。
 *
 * <p>「催熟」逻辑需要精确的 10 tick 周期，因此使用方块实体而不是随机刻（{@link net.minecraft.block.Block#randomTick}）。
 */
public class FertileDirtBlock extends BlockWithEntity implements BlockEntityProvider {
    public FertileDirtBlock(Settings settings) {
        super(settings);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new FertileDirtBlockEntity(pos, state);
    }

    /**
     * {@link BlockWithEntity} 默认把方块渲染类型设成 {@code INVISIBLE}（它的设计前提是「外观由方块实体渲染器负责」），
     * 富饶泥土的外观只是一张普通贴图，必须显式改回 {@code MODEL}，
     * 否则放置到世界里什么都看不见（表现为「贴图缺失」）。
     */
    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        // 只在服务端执行生长逻辑。
        return world.isClient ? null : checkType(type, MBGABlocks.FERTILE_DIRT_BLOCK_ENTITY, FertileDirtBlockEntity::tick);
    }
}
