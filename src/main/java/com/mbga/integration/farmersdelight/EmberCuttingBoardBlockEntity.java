package com.mbga.integration.farmersdelight;

import com.mbga.item.equipment.EmberGear;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.SmeltingRecipe;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import vectorwing.farmersdelight.common.block.entity.CuttingBoardBlockEntity;
import vectorwing.farmersdelight.common.utility.ItemUtils;

import java.util.Optional;

/**
 * 余烬砧板的方块实体。
 *
 * <p>继承农夫乐事的 {@link CuttingBoardBlockEntity}，因此「读取 / 放置 / 取回 / 刀切割」的
 * 全部 API 与行为原样继承（{@link #getStoredItem()}、{@link #addItem(ItemStack)}、
 * {@link #removeItem()}、{@link #isEmpty()}、{@link #isItemCarvingBoard()}、
 * {@link #processStoredItemUsingTool(ItemStack, PlayerEntity)} 等）。
 *
 * <p>本类额外实现两个机制（均为服务端逻辑）：
 * <ol>
 *     <li><b>自动修复余烬装备</b>：每 20 tick（1 秒）检查砧板上的物品，
 *         若是 {@link EmberGear} 且可损坏、耐久有损耗，则恢复 5 点耐久。</li>
 *     <li><b>自动熔炼生食</b>：用原版 {@code minecraft:smelting} 配方匹配砧板上的物品，
 *         放置满 200 tick 后把该物品替换为熔炼产物（一次 1 个；
 *         正常情况下砧板每个槽位上限为 1，多余数量会掉落为物品实体）。</li>
 * </ol>
 *
 * <p><b>为什么重写 {@link #getType()}：</b>农夫乐事的构造函数把自己的
 * {@code ModBlockEntityTypes.CUTTING_BOARD} 写进了 {@code BlockEntity} 的 type 字段，
 * 而方块实体 ticker 的类型是 {@code blockEntity.getType()}（见
 * {@code WorldChunk.updateTicker}）——若不重写，{@code checkType} 永远不匹配，
 * ticker 不会执行，且 {@code BlockEntityUpdateS2CPacket}（同步包）会用农夫乐事的类型 id，
 * 客户端 {@code World#getBlockEntity(pos, type)} 也会取不到该方块实体。
 */
public class EmberCuttingBoardBlockEntity extends CuttingBoardBlockEntity {
    /** 修复检查间隔：20 tick = 1 秒。 */
    private static final int REPAIR_INTERVAL_TICKS = 20;
    /** 每次修复的耐久点数。 */
    private static final int REPAIR_AMOUNT = 5;
    /** 自动熔炼一个物品所需的 tick（与原版熔炉一致）。 */
    private static final int COOKING_TIME_TICKS = 200;

    private int repairTimer;
    private int cookingTime;
    /** 当前正在熔炼的物品快照（用于检测物品被换掉 / 拿走时重置进度）。 */
    private ItemStack cookingTarget = ItemStack.EMPTY;
    /** 只读单槽 Inventory 视图，供原版熔炼配方匹配使用。 */
    private final Inventory cookInventory = new CookInventory();

    public EmberCuttingBoardBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state);
    }

    /**
     * 返回本方块实体真正注册的类型（{@code mbga:ember_cutting_board}）。
     *
     * <p>注册完成前（理论上不会发生）回退到父类类型，避免返回 null。
     */
    @Override
    public BlockEntityType<?> getType() {
        BlockEntityType<EmberCuttingBoardBlockEntity> type = MBGAFarmersDelight.EMBER_CUTTING_BOARD_ENTITY;
        return type != null ? type : super.getType();
    }

    public static void tick(World world, BlockPos pos, BlockState state, EmberCuttingBoardBlockEntity board) {
        if (world.isClient) {
            return;
        }
        if (++board.repairTimer >= REPAIR_INTERVAL_TICKS) {
            board.repairTimer = 0;
            board.repairStoredGear();
        }
        board.tickSmelting(world);
        board.spawnCookingParticles(world);
    }

    // ------------------------------------------------------------------
    // 1) 自动修复余烬装备
    // ------------------------------------------------------------------

    /** 每 20 tick 调用一次：修复砧板上余烬装备的耐久。 */
    private void repairStoredGear() {
        ItemStack stored = getStoredItem();
        if (stored.isEmpty() || !(stored.getItem() instanceof EmberGear)) {
            return;
        }
        if (!stored.isDamageable() || stored.getDamage() <= 0) {
            return;
        }
        int repaired = Math.max(0, stored.getDamage() - REPAIR_AMOUNT);
        if (repaired == stored.getDamage()) {
            return;
        }
        // porting_lib 的 ItemStackHandlerSlot#getStack 返回的是槽位内真实的 ItemStack 引用，
        // 因此原地修改 + inventoryChanged()（markDirty + 广播）即可同步。
        stored.setDamage(repaired);
        sync();
    }

    // ------------------------------------------------------------------
    // 2) 自动熔炼生食
    // ------------------------------------------------------------------

    private void tickSmelting(World world) {
        ItemStack stored = getStoredItem();
        if (stored.isEmpty() || isItemCarvingBoard()) {
            resetCooking();
            return;
        }

        Optional<SmeltingRecipe> match = world.getRecipeManager()
                .getFirstMatch(RecipeType.SMELTING, cookInventory, world);
        if (match.isEmpty()) {
            resetCooking();
            return;
        }

        // 物品被换掉时重新计时。
        if (!ItemStack.areEqual(stored, cookingTarget)) {
            cookingTarget = stored.copy();
            cookingTime = 0;
        }
        if (++cookingTime < COOKING_TIME_TICKS) {
            return;
        }

        finishCooking(world, match.get());
    }

    /** 把砧板上正在熔炼的物品替换为产物（一次 1 个）。 */
    private void finishCooking(World world, SmeltingRecipe recipe) {
        ItemStack result = recipe.getOutput(world.getRegistryManager()).copy();
        resetCooking();
        if (result.isEmpty()) {
            return;
        }

        ItemStack removed = removeItem();
        if (removed.isEmpty()) {
            return;
        }

        // 砧板槽位上限为 1，正常不会出现 count > 1；若出现，把多余部分掉落出来。
        if (removed.getCount() > 1) {
            ItemStack remainder = removed.copy();
            remainder.setCount(removed.getCount() - 1);
            ItemUtils.spawnItemEntity(world, remainder,
                    pos.getX() + 0.5D, pos.getY() + 1.05D, pos.getZ() + 0.5D,
                    0.0D, 0.0D, 0.0D);
        }

        result.setCount(1);
        addItem(result);
    }

    private void resetCooking() {
        cookingTime = 0;
        if (!cookingTarget.isEmpty()) {
            cookingTarget = ItemStack.EMPTY;
        }
    }

    /** 用「取回 + 放回」的方式写入槽位（只使用农夫乐事的公开 API，避开 porting_lib 类型）。 */
    private void setStoredStack(ItemStack stack) {
        removeItem();
        if (!stack.isEmpty()) {
            addItem(stack);
        }
    }

    /** 标记脏数据并广播给客户端（父类 {@code SyncedBlockEntity#inventoryChanged}）。 */
    private void sync() {
        inventoryChanged();
    }

    /**
     * 单槽只读 {@link Inventory} 视图：让原版熔炼配方能匹配砧板上的物品。
     * 写入会转发回砧板本身。
     */
    private final class CookInventory implements Inventory {
        @Override
        public int size() {
            return 1;
        }

        @Override
        public boolean isEmpty() {
            return getStoredItem().isEmpty();
        }

        @Override
        public ItemStack getStack(int slot) {
            return slot == 0 ? getStoredItem() : ItemStack.EMPTY;
        }

        @Override
        public ItemStack removeStack(int slot, int amount) {
            return slot == 0 ? removeItem() : ItemStack.EMPTY;
        }

        @Override
        public ItemStack removeStack(int slot) {
            return removeStack(slot, 1);
        }

        @Override
        public void setStack(int slot, ItemStack stack) {
            if (slot == 0) {
                setStoredStack(stack);
            }
        }

        @Override
        public void markDirty() {
            sync();
        }

        @Override
        public boolean canPlayerUse(PlayerEntity player) {
            return true;
        }

        @Override
        public void clear() {
        }
    }
    /** 烹饪期间每隔几 tick 冒出橙色（flame）与黄色（electric_spark）粒子。 */
    private void spawnCookingParticles(net.minecraft.world.World world) {
        if (this.cookingTime <= 0 || this.cookingTime % 3 != 0) {
            return;
        }
        if (!(world instanceof net.minecraft.server.world.ServerWorld serverWorld)) {
            return;
        }
        double x = this.getPos().getX() + 0.5D;
        double y = this.getPos().getY() + 0.2D;
        double z = this.getPos().getZ() + 0.5D;
        serverWorld.spawnParticles(net.minecraft.particle.ParticleTypes.FLAME, x, y, z, 6, 0.22D, 0.08D, 0.22D, 0.01D);
        serverWorld.spawnParticles(net.minecraft.particle.ParticleTypes.ELECTRIC_SPARK, x, y, z, 3, 0.22D, 0.08D, 0.22D, 0.01D);
    }
}
