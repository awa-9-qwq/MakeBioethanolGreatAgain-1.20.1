package com.mbga.item.charm;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.tag.DamageTypeTags;

/**
 * 翡翠护符：持续获得「村庄英雄」；受伤时有 25% 概率完全免疫，并获得 10 秒「生命恢复Ⅰ」。
 */
public class JadeCharmItem extends CharmItem {
    private static final float DODGE_CHANCE = 0.25F;
    private static final int REGENERATION_DURATION_TICKS = 10 * 20;

    public JadeCharmItem(Settings settings) {
        super(settings);
    }

    @Override
    public void tickPassive(PlayerEntity player) {
        applyEffect(player, StatusEffects.HERO_OF_THE_VILLAGE, PASSIVE_DURATION_TICKS, 0);
    }

    @Override
    public boolean blocksDamage(PlayerEntity player, DamageSource source) {
        // 与「无敌」类伤害（/kill 的 generic_kill、虚空伤害等）保持一致：这类伤害不可闪避。
        if (source.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return false;
        }
        return player.getWorld().random.nextFloat() < DODGE_CHANCE;
    }

    @Override
    public void onDamageBlocked(PlayerEntity player, DamageSource source) {
        applyEffect(player, StatusEffects.REGENERATION, REGENERATION_DURATION_TICKS, 0);
    }
}
