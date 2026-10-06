package com.mbga.mixin;

import com.mbga.block.MBGABlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.StemBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 与 {@link CropBlockMixin} 相同，但作用于南瓜 / 西瓜的 {@link StemBlock}。
 */
@Mixin(StemBlock.class)
public abstract class StemBlockMixin {

    @Inject(method = "canPlantOnTop", at = @At("HEAD"), cancellable = true)
    private void mbga$allowFertileDirtBelow(BlockState floor, BlockView world, BlockPos pos,
                                            CallbackInfoReturnable<Boolean> cir) {
        if (floor.isOf(MBGABlocks.FERTILE_DIRT)) {
            cir.setReturnValue(true);
        }
    }

    @Redirect(
            method = "randomTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/world/ServerWorld;getBaseLightLevel(Lnet/minecraft/util/math/BlockPos;I)I"))
    private int mbga$growInDarkness(ServerWorld world, BlockPos pos, int minLight) {
        int light = world.getBaseLightLevel(pos, minLight);
        return world.getBlockState(pos.down()).isOf(MBGABlocks.FERTILE_DIRT) ? Math.max(light, 9) : light;
    }
}
