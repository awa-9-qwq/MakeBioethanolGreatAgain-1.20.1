package com.mbga.mixin;

import com.mbga.block.MBGABlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SeaPickleBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 富饶泥土可以让海泡菜正常生长。
 *
 * <p>海泡菜本身没有随机刻，唯一的生长途径是骨粉（{@code Fertilizable#grow}）；
 * 而原版 {@code grow} 要求下方方块属于 {@code minecraft:coral_blocks}，否则什么都不做。
 * 富饶泥土的方块实体每 10 tick 会催熟上方作物，因此这里补上「下方是富饶泥土」的分支：
 * 直接让海泡菜的数量 +1（最多 4 个），其余情况完全交给原版逻辑。
 */
@Mixin(SeaPickleBlock.class)
public abstract class SeaPickleBlockMixin {
    private static final int MAX_PICKLES = 4;

    @Inject(method = "grow", at = @At("HEAD"), cancellable = true)
    private void mbga$growOnFertileDirt(ServerWorld world, Random random, BlockPos pos, BlockState state,
                                        CallbackInfo ci) {
        if (!world.getBlockState(pos.down()).isOf(MBGABlocks.FERTILE_DIRT)) {
            return;
        }

        int pickles = state.get(SeaPickleBlock.PICKLES);
        if (pickles < MAX_PICKLES) {
            world.setBlockState(pos, state.with(SeaPickleBlock.PICKLES, pickles + 1), Block.NOTIFY_LISTENERS);
        }
        ci.cancel();
    }
}
