package com.mbga.mixin;

import com.mbga.block.MBGABlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.SugarCaneBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 富饶泥土可以直接种植甘蔗而不需要水源（原版要求下方是泥土/沙子且相邻有水或霜冰）。
 */
@Mixin(SugarCaneBlock.class)
public abstract class SugarCaneBlockMixin {

    @Inject(method = "canPlaceAt", at = @At("HEAD"), cancellable = true)
    private void mbga$allowWithoutWater(BlockState state, WorldView world, BlockPos pos,
                                        CallbackInfoReturnable<Boolean> cir) {
        if (world.getBlockState(pos.down()).isOf(MBGABlocks.FERTILE_DIRT)) {
            cir.setReturnValue(true);
        }
    }
}
