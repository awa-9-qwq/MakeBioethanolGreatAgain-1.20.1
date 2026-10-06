package com.mbga.block.windmill;

import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;

/**
 * 风力发电机的方块实体：恒定转速 + 恒定应力容量的 Create 动能来源。
 *
 * <p>转速与容量都从所属方块读取（{@link WindmillBlock#getRpm()} /
 * {@link WindmillBlock#getStressCapacityPerRpm()}），方块实体本身不保存任何 NBT。
 *
 * <p>刻意不覆写 {@code tick()}：{@code GeneratingKineticBlockEntity.tick()} 已经负责
 * {@code reActivateSource} 的重新激活逻辑。
 */
public class WindmillBlockEntity extends GeneratingKineticBlockEntity {

    /** 供 {@code FabricBlockEntityTypeBuilder} 使用的工厂构造器。 */
    public WindmillBlockEntity(BlockPos pos, BlockState state) {
        this(MBGAWindmills.WINDMILL_ENTITY, pos, state);
    }

    public WindmillBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public float getGeneratedSpeed() {
        BlockState state = getCachedState();
        if (!(state.getBlock() instanceof WindmillBlock windmill)) {
            return 0; // 不是风力发电机 -> 不是来源
        }
        int rpm = windmill.getRpm();
        if (state.get(WindmillBlock.REVERSED)) {
            rpm = -rpm; // 右键只翻转这里的符号
        }
        // 与创造电机一致：按输出面（FACING）做方向约定归一化。
        return convertToDirection(rpm, state.get(WindmillBlock.FACING));
    }

    @Override
    public float calculateAddedStressCapacity() {
        BlockState state = getCachedState();
        float capacity = state.getBlock() instanceof WindmillBlock windmill
                ? windmill.getStressCapacityPerRpm()
                : 0.0F;
        this.lastCapacityProvided = capacity;
        return capacity;
    }

    @Override
    public void initialize() {
        super.initialize();
        // 与 CreativeMotorBlockEntity#initialize 相同的模式。
        if (!hasSource() || getGeneratedSpeed() > getTheoreticalSpeed()) {
            updateGeneratedRotation();
        }
    }
}
