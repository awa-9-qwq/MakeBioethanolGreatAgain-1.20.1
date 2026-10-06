package com.mbga.event;

import com.mbga.MBGA;
import com.mbga.item.MBGAItems;
import com.mbga.item.equipment.EmberGear;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.advancement.Advancement;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 需要「代码触发」的进度 / 成就。
 *
 * <p>这些进度的 criteria 使用 {@code minecraft:impossible}，名字统一为 {@code code}，
 * 由本类在满足条件时调用 {@link #grant} 授予。其余进度完全由数据包触发（物品 / 状态效果）。
 */
public final class MBGAAdvancementHandler {
    /** 达到该速度（m/s）即授予「目不能追，耳未可及」。 */
    private static final double SPEED_TARGET_PER_SECOND = 340.0D;
    /** 「欲火焚身」需要的累计火焰伤害。 */
    private static final float BURNING_DESIRE_DAMAGE = 20.0F;
    /** 效率附魔等级要求。 */
    private static final int EFFICIENCY_LEVEL = 6;

    /** 玩家当前累计的火焰伤害（只统计穿着全套余烬盔甲时受到的伤害）。 */
    private static final Map<UUID, Float> FIRE_DAMAGE_TAKEN = new HashMap<>();

    private MBGAAdvancementHandler() {
    }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(MBGAAdvancementHandler::onServerTick);
        PlayerBlockBreakEvents.AFTER.register(MBGAAdvancementHandler::onBlockBroken);
    }

    /** 授予某个「代码触发」进度的 {@code code} 条件。 */
    public static void grant(ServerPlayerEntity player, String advancementId) {
        if (player == null || player.getServer() == null) {
            return;
        }
        Advancement advancement = player.getServer().getAdvancementLoader()
                .get(new Identifier(MBGA.MOD_ID, advancementId));
        if (advancement != null) {
            player.getAdvancementTracker().grantCriterion(advancement, "code");
        }
    }

    private static void onServerTick(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            // 目不能追，耳未可及：速度达到 340 m/s。
            if (player.getVelocity().length() * 20.0D >= SPEED_TARGET_PER_SECOND) {
                grant(player, "speed_340");
            }
        }
    }

    /** 超越瞬间的极限：持有超级燃烧时用附有效率Ⅵ的下界合金 / 余烬镐挖黑曜石。 */
    private static void onBlockBroken(World world, PlayerEntity player, BlockPos pos, BlockState state,
                                      BlockEntity blockEntity) {
        if (world.isClient || !(player instanceof ServerPlayerEntity serverPlayer)) {
            return;
        }
        if (!state.isOf(Blocks.OBSIDIAN) || !serverPlayer.hasStatusEffect(MBGA.SUPER_BURN)) {
            return;
        }

        ItemStack tool = serverPlayer.getMainHandStack();
        boolean isPickaxe = tool.isOf(Items.NETHERITE_PICKAXE)
                || (MBGAItems.EMBER_PICKAXE != null && tool.isOf(MBGAItems.EMBER_PICKAXE));
        if (!isPickaxe) {
            return;
        }
        if (EnchantmentHelper.getLevel(Enchantments.EFFICIENCY, tool) >= EFFICIENCY_LEVEL) {
            grant(serverPlayer, "efficiency6_obsidian");
        }
    }

    /** 隐藏进度「Firefox」：让一只狐狸获得爆燃状态（由施加爆燃的实体调用）。 */
    public static void onDeflagrationApplied(net.minecraft.entity.Entity target, net.minecraft.entity.Entity owner) {
        if (target instanceof net.minecraft.entity.passive.FoxEntity && owner instanceof ServerPlayerEntity player) {
            grant(player, "firefox");
        }
    }

    /**
     * 欲火焚身：穿着全套（4 件）余烬盔甲时连续累计受到 20 点火焰伤害。
     * 由 {@link MBGAEventHandlers#onDamage} 在火焰伤害结算前调用。
     */
    public static void onFireDamage(PlayerEntity player, float amount) {
        if (!(player instanceof ServerPlayerEntity serverPlayer)) {
            return;
        }
        if (!isWearingFullEmberSet(player)) {
            FIRE_DAMAGE_TAKEN.remove(player.getUuid());
            return;
        }

        float total = FIRE_DAMAGE_TAKEN.getOrDefault(player.getUuid(), 0.0F) + amount;
        FIRE_DAMAGE_TAKEN.put(player.getUuid(), total);
        if (total >= BURNING_DESIRE_DAMAGE) {
            grant(serverPlayer, "burning_desire");
        }
    }

    private static boolean isWearingFullEmberSet(PlayerEntity player) {
        for (EquipmentSlot slot : new EquipmentSlot[]{
                EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            if (!EmberGear.isEmber(player.getEquippedStack(slot))) {
                return false;
            }
        }
        return true;
    }
}
