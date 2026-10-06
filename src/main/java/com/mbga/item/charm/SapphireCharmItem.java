package com.mbga.item.charm;

import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;

/**
 * 蓝宝石护符：在水中时持续获得「潮涌能量」。
 */
public class SapphireCharmItem extends CharmItem {
    public SapphireCharmItem(Settings settings) {
        super(settings);
    }

    @Override
    public void tickPassive(PlayerEntity player) {
        if (player.isTouchingWater()) {
            applyEffect(player, StatusEffects.CONDUIT_POWER, PASSIVE_DURATION_TICKS, 0);
        }
    }
}
