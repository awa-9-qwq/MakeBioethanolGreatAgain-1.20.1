package com.mbga.event;

import com.mbga.item.equipment.EmberGear;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 「余烬」装备 / 工具的两条耐久机制：
 *
 * <ul>
 *     <li><b>弑主</b>：装备者每损失 5 点生命值，装备恢复 1 点耐久（不足的损失会累积）；</li>
 *     <li><b>重铸</b>：装备者在火或熔岩中时，每 tick 恢复 1 点耐久。</li>
 * </ul>
 *
 * 只对<b>装备中</b>的余烬物品生效（主手 / 副手 / 四个盔甲槽）。
 */
public final class EmberGearHandler {
    /** 弑主：每损失多少点生命值恢复 1 点耐久。 */
    private static final int HEALTH_LOSS_PER_DURABILITY = 5;

    private static final Map<UUID, State> STATES = new HashMap<>();

    private EmberGearHandler() {
    }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(EmberGearHandler::onServerTick);
    }

    private static void onServerTick(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            State state = STATES.computeIfAbsent(player.getUuid(), uuid -> new State());

            float health = player.getHealth();
            if (state.initialized) {
                // 弑主：累计损失的生命值，每满 5 点恢复 1 点耐久。
                state.pendingHealthLoss += Math.max(0.0F, state.lastHealth - health);
                int points = (int) (state.pendingHealthLoss / HEALTH_LOSS_PER_DURABILITY);
                if (points > 0) {
                    state.pendingHealthLoss -= points * HEALTH_LOSS_PER_DURABILITY;
                    if (repairEquipped(player, points)) {
                        sync(player);
                    }
                }
            } else {
                state.initialized = true;
            }
            state.lastHealth = health;

            // 重铸：在火或熔岩中每 tick 恢复 1 点耐久。
            if (player.isOnFire() || player.isInLava()) {
                if (repairEquipped(player, 1)) {
                    sync(player);
                }
            }
        }
    }

    /** @return 是否有物品的耐久被恢复。 */
    private static boolean repairEquipped(PlayerEntity player, int amount) {
        boolean repaired = false;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = player.getEquippedStack(slot);
            if (!EmberGear.isEmber(stack) || !stack.isDamageable()) {
                continue;
            }
            int damage = stack.getDamage();
            if (damage > 0) {
                stack.setDamage(Math.max(0, damage - amount));
                repaired = true;
            }
        }
        return repaired;
    }

    private static void sync(ServerPlayerEntity player) {
        player.currentScreenHandler.sendContentUpdates();
    }

    private static final class State {
        private float lastHealth;
        private float pendingHealthLoss;
        private boolean initialized;
    }
}
