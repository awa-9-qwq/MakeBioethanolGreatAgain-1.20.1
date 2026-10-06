package com.mbga.item.charm;

import com.mbga.MBGA;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;

/**
 * 生物乙醇护符：持续消除「醉酒」状态；持有「超级燃烧」时额外获得
 * 「生命恢复Ⅰ」「抗性提升Ⅰ」「力量Ⅰ」「跳跃提升Ⅰ」。
 */
public class BioethanolCharmItem extends CharmItem {
    public BioethanolCharmItem(Settings settings) {
        super(settings);
    }

    @Override
    public void tickPassive(PlayerEntity player) {
        // 消除醉酒状态。
        player.removeStatusEffect(MBGA.DRUNK);

        if (!player.hasStatusEffect(MBGA.SUPER_BURN)) {
            return;
        }
        applyEffect(player, StatusEffects.REGENERATION, PASSIVE_DURATION_TICKS, 0);
        applyEffect(player, StatusEffects.RESISTANCE, PASSIVE_DURATION_TICKS, 0);
        applyEffect(player, StatusEffects.STRENGTH, PASSIVE_DURATION_TICKS, 0);
        applyEffect(player, StatusEffects.JUMP_BOOST, PASSIVE_DURATION_TICKS, 0);
    }
}
