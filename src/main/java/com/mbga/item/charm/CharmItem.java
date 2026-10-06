package com.mbga.item.charm;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;

/**
 * 护符基类。
 *
 * <p>生效位置：<b>主手、副手、物品栏右下角</b>（即快捷栏第 9 格，物品栏界面最右下角那一格）。
 * 生效时每 tick 可以施加被动效果（{@link #tickPassive}），并且可以完全免疫特定伤害
 * （{@link #blocksDamage}）。
 */
public abstract class CharmItem extends Item {
    /** 「物品栏右下角」对应的快捷栏槽位（0-8 中的最后一格）。 */
    public static final int ACTIVE_INVENTORY_SLOT = 8;

    /** 被动效果的持续时长：2 秒。 */
    protected static final int PASSIVE_DURATION_TICKS = 40;
    /** 剩余时长低于「持续时长 - 该值」时才重新施加，避免每 tick 刷屏。 */
    private static final int REFRESH_MARGIN_TICKS = 20;

    protected CharmItem(Settings settings) {
        super(settings);
    }

    /** 护符是否对玩家生效。 */
    public boolean isActive(PlayerEntity player) {
        return player.getMainHandStack().isOf(this)
                || player.getOffHandStack().isOf(this)
                || player.getInventory().getStack(ACTIVE_INVENTORY_SLOT).isOf(this);
    }

    /** 每 tick 对生效玩家施加的被动效果。 */
    public void tickPassive(PlayerEntity player) {
    }

    /** 是否完全免疫本次伤害。 */
    public boolean blocksDamage(PlayerEntity player, DamageSource source) {
        return false;
    }

    /** 成功免疫一次伤害之后的反馈（例如回血）。 */
    public void onDamageBlocked(PlayerEntity player, DamageSource source) {
    }

    /** 施加 / 刷新一个状态效果（无粒子、保留图标）。 */
    protected static void applyEffect(LivingEntity entity, StatusEffect effect, int durationTicks, int amplifier) {
        StatusEffectInstance current = entity.getStatusEffect(effect);
        boolean needsRefresh = current == null
                || current.getAmplifier() < amplifier
                || current.getDuration() <= durationTicks - REFRESH_MARGIN_TICKS;
        if (needsRefresh) {
            entity.addStatusEffect(new StatusEffectInstance(effect, durationTicks, amplifier, false, false, true));
        }
    }
}
