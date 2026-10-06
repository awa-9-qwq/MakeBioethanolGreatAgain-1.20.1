package com.mbga.item.charm;

import com.mbga.event.MBGAEventHandlers;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;

/**
 * 红宝石护符：免疫火焰伤害；每次成功免疫时有 10% 概率恢复 1 点生命值。
 */
public class RubyCharmItem extends CharmItem {
    private static final float HEAL_CHANCE = 0.10F;
    private static final float HEAL_AMOUNT = 1.0F;

    public RubyCharmItem(Settings settings) {
        super(settings);
    }

    @Override
    public boolean blocksDamage(PlayerEntity player, DamageSource source) {
        return MBGAEventHandlers.isFireDamage(source);
    }

    @Override
    public void onDamageBlocked(PlayerEntity player, DamageSource source) {
        if (player.getWorld().random.nextFloat() < HEAL_CHANCE) {
            player.heal(HEAL_AMOUNT);
        }
    }
}
