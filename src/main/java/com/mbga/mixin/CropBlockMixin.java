package com.mbga.mixin;

import com.mbga.block.MBGABlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 让所有 {@link CropBlock} 作物（小麦、胡萝卜、马铃薯、甜菜等）可以：
 * <ul>
 *     <li>直接种在富饶泥土上（原版只允许耕地）；</li>
 *     <li>无视亮度要求种植；</li>
 *     <li>无视亮度要求生长（随机刻的亮度门槛在下方被替换）。</li>
 * </ul>
 */
@Mixin(CropBlock.class)
public abstract class CropBlockMixin {

    @Inject(method = "canPlantOnTop", at = @At("HEAD"), cancellable = true)
    private void mbga$allowFertileDirtBelow(BlockState floor, BlockView world, BlockPos pos,
                                            CallbackInfoReturnable<Boolean> cir) {
        if (floor.isOf(MBGABlocks.FERTILE_DIRT)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "canPlaceAt", at = @At("HEAD"), cancellable = true)
    private void mbga$ignoreLightWhenPlanting(BlockState state, WorldView world, BlockPos pos,
                                              CallbackInfoReturnable<Boolean> cir) {
        if (world.getBlockState(pos.down()).isOf(MBGABlocks.FERTILE_DIRT)) {
            cir.setReturnValue(true);
        }
    }

    /** 原版：{@code if (world.getBaseLightLevel(pos, 0) >= 9)}；在富饶泥土上视为满足亮度。 */
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
