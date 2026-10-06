package com.mbga.integration.farmersdelight;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import vectorwing.farmersdelight.common.block.CuttingBoardBlock;

/**
 * 余烬砧板：直接继承农夫乐事的 {@link CuttingBoardBlock}，
 * 因此放置 / 取回物品、刀切割、蹲下放刀雕刻、比较器输出、含水等行为全部原样继承
 * （农夫乐事的 {@code CuttingBoardBlock.onUse} 通过 {@code instanceof CuttingBoardBlockEntity} 识别方块实体，
 * 我们的 {@link EmberCuttingBoardBlockEntity} 是其子类，所以无需复制任何交互代码）。
 *
 * <p>与农夫乐事原版砧板的区别只有两点：
 * <ol>
 *     <li>方块实体改为 {@link EmberCuttingBoardBlockEntity}（自动修复余烬装备 + 自动熔炼）；</li>
 *     <li>注册自己的 {@code BlockEntityTicker}（农夫乐事原版砧板没有 ticker）。</li>
 * </ol>
 */
public class EmberCuttingBoardBlock extends CuttingBoardBlock {

    public EmberCuttingBoardBlock(Settings settings) {
        super(settings);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new EmberCuttingBoardBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        // 只做服务端逻辑（修复 / 熔炼都在 tick 内部再次判断 isClient）。
        return world.isClient ? null
                : checkType(type, MBGAFarmersDelight.EMBER_CUTTING_BOARD_ENTITY, EmberCuttingBoardBlockEntity::tick);
    }
}
