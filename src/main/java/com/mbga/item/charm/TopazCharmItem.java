package com.mbga.item.charm;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;

/**
 * 黄玉护符：免疫雷电伤害，并持续获得「力量Ⅰ」。
 */
public class TopazCharmItem extends CharmItem {
    public TopazCharmItem(Settings settings) {
        super(settings);
    }

    @Override
    public boolean blocksDamage(PlayerEntity player, DamageSource source) {
        return source.isOf(DamageTypes.LIGHTNING_BOLT);
    }

    @Override
    public void tickPassive(PlayerEntity player) {
        applyEffect(player, StatusEffects.STRENGTH, PASSIVE_DURATION_TICKS, 0);
    }
}
