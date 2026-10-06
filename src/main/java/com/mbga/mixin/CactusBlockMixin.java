package com.mbga.mixin;

import com.mbga.block.MBGABlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.CactusBlock;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 富饶泥土可以让仙人掌正常生长。
 *
 * <p>原版 {@code CactusBlock#canPlaceAt} 只允许下方是仙人掌或 {@code minecraft:sand} 标签；
 * 这里把那一处 {@code isIn(BlockTags.SAND)} 改成「沙子 或 富饶泥土」，
 * 其余检查（水平方向不能有固体方块、上方不能是液体）全部保持原样。
 * 仙人掌的生长逻辑（{@code randomTick}）本身不检查下方方块，因此无需额外改动。
 */
@Mixin(CactusBlock.class)
public abstract class CactusBlockMixin {

    @Redirect(
            method = "canPlaceAt",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/BlockState;isIn(Lnet/minecraft/registry/tag/TagKey;)Z"))
    private boolean mbga$allowFertileDirtBelow(BlockState floor, TagKey<Block> tag) {
        return floor.isIn(tag) || floor.isOf(MBGABlocks.FERTILE_DIRT);
    }
}
