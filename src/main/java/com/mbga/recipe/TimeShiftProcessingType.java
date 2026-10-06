package com.mbga.recipe;

import com.mbga.MBGA;
import com.mbga.block.MBGABlocks;
import com.simibubi.create.content.kinetics.fan.processing.FanProcessingType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.recipe.RecipeType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * 「时移」：鼓风机指向<b>奇点块</b>时，气流中的物品会被时移处理（类似批量熔炼），
 * 进入气流的玩家每 20 tick 会受到 2 点虚空伤害。
 *
 * <p>通过 Create 的 {@link com.simibubi.create.content.kinetics.fan.processing.FanProcessingTypeRegistry}
 * 注册，因此传送带、置物台、掉落的物品都会像其他鼓风机处理方式一样生效。
 */
public class TimeShiftProcessingType implements FanProcessingType {
    /**
     * 判定优先级。Create 自带的处理方式是
     * 烟熏(200) / 高炉(100) / haunting(300) / 洗涤(400)，
     * 而 {@code FanProcessingTypeRegistry} 不允许两个处理方式拥有相同优先级（会抛异常），
     * 因此这里取 250 这个未被占用的值。
     */
    private static final int PRIORITY = 250;
    /** 气流颜色：与奇点一致的蓝紫色。 */
    private static final int AIR_FLOW_COLOR = 0x8A2BE2;
    /** 玩家伤害间隔：20 tick。 */
    private static final int ENTITY_DAMAGE_INTERVAL_TICKS = 20;
    /** 每次对玩家造成的虚空伤害：2 点。 */
    private static final float ENTITY_VOID_DAMAGE = 2.0F;

    /** 复用的单格容器，用于查询配方。 */
    private static final SimpleInventory WRAPPER = new SimpleInventory(1);

    /** 上一次对玩家造成伤害的游戏刻（只会在服务端线程读写）。 */
    private final Map<Entity, Long> lastEntityDamageTick = new WeakHashMap<>();

    @Override
    public boolean isValidAt(World world, BlockPos pos) {
        return world.getBlockState(pos).isOf(MBGABlocks.SINGULARITY_BLOCK);
    }

    @Override
    public int getPriority() {
        return PRIORITY;
    }

    @Override
    public boolean canProcess(ItemStack stack, World world) {
        return findRecipe(stack, world) != null;
    }

    /**
     * 批量处理：按<b>输入堆叠数量</b>逐个结算配方，并把结果合并成尽量大的堆叠
     * （修复了「一整叠原料只产出一份」的问题）。
     */
    @Override
    public List<ItemStack> process(ItemStack stack, World world) {
        TimeShiftRecipe recipe = findRecipe(stack, world);
        if (recipe == null) {
            return List.of();
        }

        int count = Math.max(1, stack.getCount());
        List<ItemStack> produced = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            produced.addAll(recipe.rollResults(world.random));
        }
        return mergeStacks(produced);
    }

    @Override
    public void spawnProcessingParticles(World world, Vec3d pos) {
        if (world instanceof ServerWorld serverWorld) {
            serverWorld.spawnParticles(ParticleTypes.PORTAL,
                    pos.x, pos.y + 0.25D, pos.z, 2, 0.15D, 0.15D, 0.15D, 0.02D);
        }
    }

    @Override
    public void morphAirFlow(AirFlowParticleAccess access, Random random) {
        access.setColor(AIR_FLOW_COLOR);
        access.setAlpha(0.6F);
        if (random.nextInt(4) == 0) {
            access.spawnExtraParticle(ParticleTypes.PORTAL, 0.1F);
        }
    }

    /** 玩家进入时移范围：每 20 tick 受到 2 点虚空伤害。 */
    @Override
    public void affectEntity(Entity entity, World world) {
        if (world.isClient || !(entity instanceof PlayerEntity player) || !player.isAlive()) {
            return;
        }

        long now = world.getTime();
        Long last = this.lastEntityDamageTick.get(player);
        if (last != null && now - last < ENTITY_DAMAGE_INTERVAL_TICKS) {
            return;
        }
        this.lastEntityDamageTick.put(player, now);
        player.damage(player.getDamageSources().create(MBGA.SINGULARITY), ENTITY_VOID_DAMAGE);

        // 隐藏成就「迷失」：迷失在时间的长河（吃下奇点 / 被时移）。
        if (player instanceof net.minecraft.server.network.ServerPlayerEntity serverPlayer) {
            com.mbga.event.MBGAAdvancementHandler.grant(serverPlayer, "lost_in_time");
        }
    }

    /** 把相同物品的结果堆叠合并（上限为物品自身的最大堆叠数）。 */
    private static List<ItemStack> mergeStacks(List<ItemStack> stacks) {
        List<ItemStack> merged = new ArrayList<>();
        for (ItemStack stack : stacks) {
            if (stack.isEmpty()) {
                continue;
            }
            boolean absorbed = false;
            for (ItemStack existing : merged) {
                if (!ItemStack.canCombine(existing, stack)) {
                    continue;
                }
                int space = existing.getMaxCount() - existing.getCount();
                if (space <= 0) {
                    continue;
                }
                int moved = Math.min(space, stack.getCount());
                existing.increment(moved);
                stack.decrement(moved);
                if (stack.isEmpty()) {
                    absorbed = true;
                    break;
                }
            }
            if (!absorbed) {
                merged.add(stack);
            }
        }
        return merged;
    }

    @SuppressWarnings("unchecked")
    private static TimeShiftRecipe findRecipe(ItemStack stack, World world) {
        if (stack.isEmpty()) {
            return null;
        }
        WRAPPER.setStack(0, stack);
        RecipeType<TimeShiftRecipe> type = MBGARecipeTypes.TIME_SHIFT.getType();
        return world.getRecipeManager().getFirstMatch(type, WRAPPER, world).orElse(null);
    }
}
