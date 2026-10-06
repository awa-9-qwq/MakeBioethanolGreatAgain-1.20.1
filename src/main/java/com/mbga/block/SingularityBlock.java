package com.mbga.block;

import com.mbga.MBGA;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * 奇点块：没有碰撞体积，但任何接触它的实体（掉落物除外）每 5 tick 会受到 5 点虚空伤害；
 * 生物同时获得「缓慢Ⅱ」与「失明」。
 *
 * <p>由于没有碰撞体积，判定使用的是 {@link Block#onEntityCollision}：
 * 原版在 {@code Entity#checkBlockCollision} 中按「实体碰撞箱覆盖到的方块坐标」回调，
 * 与方块自身是否可碰撞无关。
 */
@SuppressWarnings("deprecation")
public class SingularityBlock extends Block {
    /** 伤害间隔：5 tick。 */
    private static final int DAMAGE_INTERVAL_TICKS = 5;
    /** 每次伤害：5 点虚空伤害。 */
    private static final float VOID_DAMAGE = 5.0F;
    /** 负面效果持续时长：60 tick（远离奇点块后会很快消失）。 */
    private static final int EFFECT_DURATION_TICKS = 60;
    private static final int SLOWNESS_AMPLIFIER = 1;
    private static final int BLINDNESS_AMPLIFIER = 0;

    /** 上一次造成伤害的游戏刻；只会在服务端线程读写。 */
    private final Map<Entity, Long> lastDamageTick = new WeakHashMap<>();

    public SingularityBlock(Settings settings) {
        super(settings);
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        // 掉落物（物品实体）不受影响；其余所有实体——包括船、矿车等非生物实体——都会被伤害。
        if (world.isClient || entity instanceof ItemEntity) {
            return;
        }
        if (entity instanceof LivingEntity living && !living.isAlive()) {
            return;
        }

        long now = world.getTime();
        Long last = this.lastDamageTick.get(entity);
        if (last != null && now - last < DAMAGE_INTERVAL_TICKS) {
            return;
        }
        this.lastDamageTick.put(entity, now);

        // 虚空伤害：复用 mbga:singularity 伤害类型（已加入 bypasses_armor / bypasses_resistance 等标签）。
        entity.damage(entity.getDamageSources().create(MBGA.SINGULARITY), VOID_DAMAGE);

        // 负面效果只能施加在生物身上。
        if (entity instanceof LivingEntity living) {
            living.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.SLOWNESS, EFFECT_DURATION_TICKS, SLOWNESS_AMPLIFIER, false, false, true));
            living.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.BLINDNESS, EFFECT_DURATION_TICKS, BLINDNESS_AMPLIFIER, false, false, true));
        }
    }
}
