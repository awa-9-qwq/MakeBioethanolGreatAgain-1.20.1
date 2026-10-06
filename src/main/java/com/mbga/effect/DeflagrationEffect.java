package com.mbga.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * 爆燃：持有「超级燃烧」时受到火焰伤害后由「超级燃烧」转换而来。
 * 持续期间给予玩家隐藏的「缓慢Ⅰ」，且一旦玩家被点燃就无法熄灭。
 */
public class DeflagrationEffect extends StatusEffect {
    private static final int REFRESH_TICKS = 2;
    private static final int MIN_FIRE_TICKS = 40;

    /** 记录已被点燃的实体，使其在「爆燃」期间无法熄灭。 */
    private final Set<LivingEntity> ignited = Collections.newSetFromMap(new WeakHashMap<>());

    public DeflagrationEffect() {
        super(StatusEffectCategory.HARMFUL, 0xFF4500);
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

        entity.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, REFRESH_TICKS, 0, true, false, false));

        // 一旦被点燃就记录下来，并把火焰时间维持在不低于 MIN_FIRE_TICKS，使火焰无法自然熄灭。
        if (entity.getFireTicks() > 0) {
            ignited.add(entity);
        }
        if (ignited.contains(entity)) {
            entity.setFireTicks(Math.max(entity.getFireTicks(), MIN_FIRE_TICKS));
        }
    }

    @Override
    public void onRemoved(LivingEntity entity, AttributeContainer attributes, int amplifier) {
        super.onRemoved(entity, attributes, amplifier);
        ignited.remove(entity);
    }
}
