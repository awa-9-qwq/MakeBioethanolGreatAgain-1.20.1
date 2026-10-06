package com.mbga.event;

import com.mbga.MBGA;
import com.mbga.duck.BlazeBurnerAccessor;
import com.mbga.item.MBGAItems;
import com.mbga.item.charm.CharmItem;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * MBGA 的事件处理：
 * <ul>
 *     <li>火焰伤害转换「超级燃烧」为「爆燃」；</li>
 *     <li>右击烈焰人燃烧室转移超级燃烧；</li>
 *     <li>护符：伤害免疫（红宝石 / 黄玉 / 翡翠）与被动效果（每 tick）；</li>
 *     <li>护符生效位置：主手、副手、物品栏右下角。</li>
 * </ul>
 */
public final class MBGAEventHandlers {
    /** 单次转移消耗的玩家「超级燃烧」时长：1 分钟（1200 tick）。 */
    private static final int TRANSFER_COST_TICKS = 60 * 20;

    private MBGAEventHandlers() {
    }

    public static void register() {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register(MBGAEventHandlers::onDamage);
        UseBlockCallback.EVENT.register(MBGAEventHandlers::onUseBlock);
        ServerTickEvents.END_SERVER_TICK.register(MBGAEventHandlers::onServerTick);
        EmberGearHandler.register();
        MBGAAdvancementHandler.register();
    }

    private static boolean onDamage(LivingEntity entity, DamageSource source, float amount) {
        // 护符免疫优先：被护符挡下的伤害不会造成任何效果（也不会触发超级燃烧 -> 爆燃）。
        if (entity instanceof PlayerEntity player && tryCharmImmunity(player, source)) {
            return false;
        }

        // 持有「超级燃烧」时受到火焰伤害，将「超级燃烧」转换为「爆燃」。
        if (isFireDamage(source)) {
            if (entity instanceof PlayerEntity player) {
                // 隐藏成就「欲火焚身」：穿着全套余烬盔甲累计受到 20 点火焰伤害。
                MBGAAdvancementHandler.onFireDamage(player, amount);
            }
            if (entity.hasStatusEffect(MBGA.SUPER_BURN)) {
                int remaining = entity.getStatusEffect(MBGA.SUPER_BURN).getDuration();
                entity.removeStatusEffect(MBGA.SUPER_BURN);
                entity.addStatusEffect(new StatusEffectInstance(MBGA.DEFLAGRATION, remaining, 0));
            }
        }
        return true; // 始终允许该伤害
    }

    /** 依次询问玩家身上「生效」的护符是否免疫本次伤害。 */
    private static boolean tryCharmImmunity(PlayerEntity player, DamageSource source) {
        for (CharmItem charm : MBGAItems.ALL_CHARMS) {
            if (charm.isActive(player) && charm.blocksDamage(player, source)) {
                charm.onDamageBlocked(player, source);
                return true;
            }
        }
        return false;
    }

    /** 每 tick 为持有护符的玩家施加被动效果。 */
    private static void onServerTick(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            for (CharmItem charm : MBGAItems.ALL_CHARMS) {
                if (charm.isActive(player)) {
                    charm.tickPassive(player);
                }
            }
        }
    }

    /** 判断是否为火焰类伤害（红宝石护符免疫，烈焰人相关的转换也使用这一判定）。 */
    public static boolean isFireDamage(DamageSource source) {
        return source.isOf(DamageTypes.IN_FIRE)
                || source.isOf(DamageTypes.ON_FIRE)
                || source.isOf(DamageTypes.LAVA)
                || source.isOf(DamageTypes.HOT_FLOOR)
                || source.isOf(DamageTypes.FIREBALL)
                || source.isOf(DamageTypes.UNATTRIBUTED_FIREBALL);
    }

    /**
     * 持有至少 1 分钟「超级燃烧」的玩家，空手右击烈焰人燃烧室：
     * 未沸腾时用 Create 的公开 API 喂一块烈焰蛋糕使其进入沸腾状态；
     * 已沸腾时通过 Mixin 注入的方法增加 1 分钟燃烧时长；玩家消耗 1 分钟超级燃烧。
     */
    private static ActionResult onUseBlock(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
        if (world.isClient) {
            return ActionResult.PASS;
        }

        // 只有空手才能触发转移。
        if (!player.getStackInHand(hand).isEmpty()) {
            return ActionResult.PASS;
        }

        StatusEffectInstance burn = player.getStatusEffect(MBGA.SUPER_BURN);
        if (burn == null || burn.getDuration() < TRANSFER_COST_TICKS) {
            return ActionResult.PASS;
        }

        BlockPos pos = hitResult.getBlockPos();
        BlockState state = world.getBlockState(pos);

        // 烈焰蛋糕是 Create 的「超级燃料」，会让烈焰人燃烧室进入沸腾（SEETHING）状态。
        Item blazeCake = Registries.ITEM.get(new Identifier("create", "blaze_cake"));
        if (blazeCake == Items.AIR) {
            return ActionResult.PASS;
        }

        boolean inserted = false;
        try (Transaction tx = Transaction.openOuter()) {
            TypedActionResult<ItemStack> result = BlazeBurnerBlock.tryInsert(
                    state, world, pos, new ItemStack(blazeCake), true, false, tx);
            if (result.getResult() == ActionResult.SUCCESS) {
                tx.commit();
                inserted = true;
            }
        }

        // 已处于沸腾状态且燃料充足时，改用 Mixin 注入的方法增加 1 分钟。
        if (!inserted) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof BlazeBurnerAccessor accessor) {
                accessor.mbga$addBurnTime(TRANSFER_COST_TICKS);
            } else {
                // 不是（可用的）烈焰人燃烧室。
                return ActionResult.PASS;
            }
        }

        // 转移成功：玩家消耗 1 分钟超级燃烧。
        int newDuration = burn.getDuration() - TRANSFER_COST_TICKS;
        player.removeStatusEffect(MBGA.SUPER_BURN);
        player.addStatusEffect(new StatusEffectInstance(MBGA.SUPER_BURN, newDuration, 0));

        // 隐藏进度「给你一半」。
        if (player instanceof ServerPlayerEntity serverPlayer) {
            MBGAAdvancementHandler.grant(serverPlayer, "blaze_burner_transfer");
        }

        return ActionResult.SUCCESS;
    }
}
