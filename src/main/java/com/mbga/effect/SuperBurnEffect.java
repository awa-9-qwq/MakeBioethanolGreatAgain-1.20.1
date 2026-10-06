package com.mbga.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

/**
 * 超级燃烧（正面效果）：持续期间给予玩家隐藏的「速度Ⅱ」「急迫Ⅱ」「夜视」。
 */
public class SuperBurnEffect extends StatusEffect {
    private static final int REFRESH_TICKS = 2;

    public SuperBurnEffect() {
        // 蓝紫色粒子
        super(StatusEffectCategory.BENEFICIAL, 0x8A2BE2);
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return true;
    }

    @Override
    public void applyUpdateEffect(LivingEntity entity, int amplifier) {
        if (entity.getWorld().isClient) {
            return;
        }
        entity.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, REFRESH_TICKS, 1, true, false, false));
        entity.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, REFRESH_TICKS, 1, true, false, false));
        entity.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, REFRESH_TICKS, 0, true, false, false));
    }
}
