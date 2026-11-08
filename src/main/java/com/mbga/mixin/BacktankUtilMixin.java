package com.mbga.mixin;

import com.mbga.item.equipment.EmberBacktankItem;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.equipment.armor.BacktankUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 让余烬背罐拥有「下界合金背罐两倍」的储气量。
 *
 * <p>Create 的空气容量是 {@link BacktankUtil} 里的<b>全局静态</b>计算
 * （{@code maxAir(ItemStack) → maxAir(enchantLevel) → 配置值 + 附魔等级 × 配置值}），
 * 铜背罐与下界合金背罐没有区别，因此没法靠「给物品加个字段」来扩容。这里改为接管
 * 两个与「单个背罐」相关的方法：
 *
 * <ul>
 *     <li>{@code getAir(ItemStack)}：读取空气量，上限换成
 *         {@link EmberBacktankItem#maxAir(ItemStack)}；</li>
 *     <li>{@code consumeAir(LivingEntity, ItemStack, float)}：写回空气量。Create 原本会把写回的值
 *         夹到「基础上限」，若不替换，多出来的那一半空气在第一次消耗时就会被削掉。
 *         低气量 / 耗尽的提示与 Create 保持一致。</li>
 * </ul>
 *
 * <p>方块形态（{@code BacktankBlockEntity}）的容量由 {@link BacktankBlockEntityMixin} 处理。
 *
 * <p><b>为什么是 {@code remap = false} 且只写方法名：</b>目标是 Create 自己的类（不参与 yarn 映射），
 * 这样注解里的字符串在开发环境（yarn）与生产环境（intermediary）都一致，不依赖 refmap。
 */
@Mixin(value = BacktankUtil.class, remap = false)
public class BacktankUtilMixin {

    /** 余烬背罐：按 2 倍上限返回空气量。 */
    @Inject(method = "getAir", at = @At("HEAD"), cancellable = true)
    private static void mbga$getEmberAir(ItemStack backtank, CallbackInfoReturnable<Float> cir) {
        if (backtank.getItem() instanceof EmberBacktankItem) {
            NbtCompound tag = backtank.getOrCreateNbt();
            cir.setReturnValue(Math.min(tag.getFloat("Air"), (float) EmberBacktankItem.maxAir(backtank)));
        }
    }

    /** 余烬背罐：按 2 倍上限扣空气（Create 原版实现 + 两档提示）。 */
    @Inject(method = "consumeAir", at = @At("HEAD"), cancellable = true)
    private static void mbga$consumeEmberAir(LivingEntity entity, ItemStack backtank, float amount,
                                             CallbackInfo ci) {
        if (!(backtank.getItem() instanceof EmberBacktankItem)) {
            return;
        }
        NbtCompound tag = backtank.getOrCreateNbt();
        int maxAir = EmberBacktankItem.maxAir(backtank);
        float air = Math.min(tag.getFloat("Air"), (float) maxAir);
        float newAir = Math.max(air - amount, 0.0F);
        tag.putFloat("Air", Math.min(newAir, (float) maxAir));
        backtank.setNbt(tag);
        if (entity instanceof ServerPlayerEntity player) {
            mbga$sendWarning(player, air, newAir, maxAir / 10.0F);
            mbga$sendWarning(player, air, newAir, 1.0F);
        }
        ci.cancel();
    }

    /** 与 Create 的 {@code BacktankUtil#sendWarning} 一致：跨越「10% / 1 点」阈值时提示一次。 */
    @Unique
    private static void mbga$sendWarning(ServerPlayerEntity player, float air, float newAir, float threshold) {
        if (newAir > threshold || air <= threshold) {
            return;
        }
        boolean depleted = threshold == 1.0F;
        Text message = Text.translatable(depleted ? "create.backtank.depleted" : "create.backtank.low");
        AllSoundEvents.DENY.play(player.getWorld(), null, player.getBlockPos(), 1.0F, 1.25F);
        AllSoundEvents.STEAM.play(player.getWorld(), null, player.getBlockPos(), 0.5F, 0.5F);
        player.networkHandler.sendPacket(new TitleFadeS2CPacket(10, 40, 10));
        player.networkHandler.sendPacket(new TitleS2CPacket(Text.literal("⚠ ")
                .formatted(depleted ? Formatting.RED : Formatting.GOLD)
                .append(message.copy().formatted(Formatting.GRAY))));
        player.networkHandler.sendPacket(new SubtitleS2CPacket(Text.empty()));
    }
}
