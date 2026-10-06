package com.mbga.mixin;

import com.mbga.block.MBGABlocks;
import net.minecraft.block.BlockState;
import net.minecraft.item.MiningToolItem;
import net.minecraft.item.ToolItem;
import net.minecraft.item.ToolMaterials;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 原版 1.20.1 只有「石头 / 铁 / 钻石」三档挖掘等级标签，没有「下界合金」等级。
 * 这里为 {@link MBGABlocks#NEEDS_NETHERITE_TOOL} 标签补上第四档判定：
 * 只有挖掘等级 ≥ 下界合金（4）的工具才「适合」该方块，也就是才会掉落。
 */
@Mixin(MiningToolItem.class)
public abstract class MiningToolItemMixin {

    @Inject(method = "isSuitableFor", at = @At("RETURN"), cancellable = true)
    private void mbga$requireNetheriteLevel(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ() || !state.isIn(MBGABlocks.NEEDS_NETHERITE_TOOL)) {
            return;
        }
        if (((ToolItem) (Object) this).getMaterial().getMiningLevel() < ToolMaterials.NETHERITE.getMiningLevel()) {
            cir.setReturnValue(false);
        }
    }
}
