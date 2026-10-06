package com.mbga.block.windmill;

import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

/**
 * 风力发电机：全方块、恒定转速、恒定应力输出的 Create 动能来源。
 *
 * <p><b>输出面 = {@code FACING} 面</b>：传动杆从 {@code FACING} 那一面接出，旋转轴就是
 * {@code FACING} 的轴。模型侧约定「<b>贴图的底面</b>（{@code windmill_bottom}，接口那张）永远朝着
 * {@code FACING}」，所以 blockstate 里把模型绕成「底面朝向 FACING」而不是原版 {@code end_rod} 的
 * 「上表面朝向 FACING」——这样接上传动杆的那一面看到的永远是接口贴图。
 *
 * <p>朝向与创造电机一致：
 * <ul>
 *   <li>放置时优先与相邻动能方块对接（{@link #getPlacementState(ItemPlacementContext)}）；</li>
 *   <li><b>扳手</b>右键可以像其他机械动力装置一样旋转它——{@code DirectionalKineticBlock} 带
 *       {@code FACING}，Create 的 {@code IWrenchable#onWrenched} 默认实现会按「玩家点击的那个面」
 *       决定绕哪根轴转 90°，并自动把发电者重新激活；潜行 + 扳手则是拆下（掉落物品）；</li>
 *   <li>空手（或非扳手物品）右键只在正转 / 反转之间切换（{@link #REVERSED}），不改速度也不改朝向。</li>
 * </ul>
 *
 * <p>动能契约：{@link IBE#getTicker} 的默认实现会在方块实体是 {@code SmartBlockEntity} 子类时返回
 * {@code SmartBlockEntityTicker}，因此本方块不需要自己写 ticker；转速方向由
 * {@link WindmillBlockEntity#getGeneratedSpeed()} 提供，应力容量由
 * {@link WindmillBlockEntity#calculateAddedStressCapacity()} 提供。
 */
public class WindmillBlock extends DirectionalKineticBlock implements IBE<WindmillBlockEntity> {

    /** 反转标记：只影响 {@code getGeneratedSpeed()} 的符号，不影响朝向与外观。 */
    public static final BooleanProperty REVERSED = BooleanProperty.of("reversed");

    private final int rpm;
    private final float stressCapacityPerRpm;

    public WindmillBlock(AbstractBlock.Settings settings, int rpm, float stressCapacityPerRpm) {
        super(settings);
        this.rpm = rpm;
        this.stressCapacityPerRpm = stressCapacityPerRpm;
    }

    /** 恒定转速（RPM），始终为正；符号由 {@link #REVERSED} 与 {@code FACING} 决定。 */
    public int getRpm() {
        return rpm;
    }

    /** 恒定应力容量（SU / RPM）。网络总容量 = 该值 × |实际转速|。 */
    public float getStressCapacityPerRpm() {
        return stressCapacityPerRpm;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(REVERSED);
        super.appendProperties(builder); // 加上 FACING
    }

    // ---- IRotate ----

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.get(FACING).getAxis();
    }

    @Override
    public boolean hasShaftTowards(WorldView world, BlockPos pos, BlockState state, Direction side) {
        // 轴在 FACING 那一面（输出面 / 背面）。
        return side == state.get(FACING);
    }

    @Override
    public boolean hideStressImpact() {
        // 是发电者，不消耗应力。
        return true;
    }

    // ---- 放置：优先与相邻动能方块对接（与创造电机一致） ----

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        Direction preferredFacing = getPreferredFacing(context);
        if ((context.getPlayer() != null && context.getPlayer().isSneaking()) || preferredFacing == null) {
            return super.getPlacementState(context);
        }
        return getDefaultState().with(FACING, preferredFacing);
    }

    // ---- 右键：只切换旋转方向，绝不改速度、绝不 cycle(FACING) ----
    // 注意：手持 Create 扳手时走的是 IWrenchable#onWrenched（旋转朝向），不会走到这里。

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player,
                              Hand hand, BlockHitResult hit) {
        if (world.isClient) {
            // 客户端只回 SUCCESS 做预测，真正的状态改动在服务端。
            return ActionResult.SUCCESS;
        }
        world.setBlockState(pos, state.cycle(REVERSED), Block.NOTIFY_ALL);
        withBlockEntityDo(world, pos, WindmillBlockEntity::updateGeneratedRotation);
        return ActionResult.SUCCESS;
    }

    // ---- IBE ----

    @Override
    public Class<WindmillBlockEntity> getBlockEntityClass() {
        return WindmillBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends WindmillBlockEntity> getBlockEntityType() {
        return MBGAWindmills.WINDMILL_ENTITY;
    }
}
