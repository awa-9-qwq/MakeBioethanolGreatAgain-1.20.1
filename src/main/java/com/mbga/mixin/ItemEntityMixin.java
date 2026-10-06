package com.mbga.mixin;

import com.mbga.block.MBGABlocks;
import com.mbga.item.MBGAItems;
import com.mbga.item.SingularityItem;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.DamageTypeTags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 让「奇点」与「奇点块」的掉落物：免疫虚空、免疫爆炸。
 * （火焰免疫由 {@link SingularityItem#isFireproof()} 与奇点块物品的 {@code fireproof()} 提供。）
 *
 * <p>「奇点」本身还额外不会自然消失（奇点块的掉落物没有这个特性）。
 */
@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void mbga$tick(CallbackInfo ci) {
        ItemEntity self = (ItemEntity) (Object) this;
        ItemStack stack = self.getStack();

        // 「重铸」：余烬系列装备的掉落物在火 / 熔岩中每 tick 恢复 1 点耐久。
        if (stack.isDamageable() && stack.getDamage() > 0 && (self.isOnFire() || self.isInLava())
                && MBGAItems.isEmberSeries(stack)) {
            stack.setDamage(Math.max(0, stack.getDamage() - 1));
        }

        boolean singularityItem = stack.getItem() instanceof SingularityItem;
        if (!singularityItem && !MBGABlocks.isSingularityDrop(stack)) {
            return;
        }
        if (singularityItem) {
            self.setNeverDespawn();
        }
        // 免疫虚空：掉到虚空阈值以下时拉回世界底部，避免被 tickInVoid 丢弃。
        double threshold = self.getWorld().getBottomY() - 64.0;
        if (self.getY() < threshold) {
            self.setPosition(self.getX(), self.getWorld().getBottomY() + 1.0, self.getZ());
            self.setVelocity(self.getVelocity().multiply(1.0, 0.0, 1.0));
        }
    }

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void mbga$damage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        ItemEntity self = (ItemEntity) (Object) this;
        ItemStack stack = self.getStack();
        boolean protectedDrop = stack.getItem() instanceof SingularityItem || MBGABlocks.isSingularityDrop(stack);
        if (protectedDrop && source.isIn(DamageTypeTags.IS_EXPLOSION)) {
            cir.setReturnValue(false);
        }
        // 余烬系列物品的掉落物免疫火焰 / 熔岩伤害。
        if (MBGAItems.isEmberSeries(stack) && source.isIn(DamageTypeTags.IS_FIRE)) {
            cir.setReturnValue(false);
        }
    }
}
