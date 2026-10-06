package com.mbga.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

/**
 * 醉酒：持续期间给予玩家隐藏的「反胃Ⅱ」「缓慢Ⅰ」。
 */
public class DrunkEffect extends StatusEffect {
    private static final int REFRESH_TICKS = 2;

    public DrunkEffect() {
        super(StatusEffectCategory.HARMFUL, 0x8E44AD);
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
        entity.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, REFRESH_TICKS, 1, true, false, false));
        entity.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, REFRESH_TICKS, 0, true, false, false));
    }
}
