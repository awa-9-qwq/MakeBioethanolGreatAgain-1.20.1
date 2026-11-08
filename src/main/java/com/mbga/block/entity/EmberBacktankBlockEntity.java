package com.mbga.block.entity;

import com.mbga.block.MBGABlocks;
import com.simibubi.create.content.equipment.armor.BacktankBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;

/**
 * 余烬背罐的方块实体。
 *
 * <p>本身不加重写：充气、比较器、NBT 全部沿用 Create 的 {@link BacktankBlockEntity}，唯一的区别是
 * 「容量上限」——由 {@code com.mbga.mixin.BacktankBlockEntityMixin} 把该类里对
 * {@code BacktankUtil.maxAir(int)} 的调用重定向到
 * {@link com.mbga.item.equipment.EmberBacktankItem#maxAir(int)}，仅当方块实体是本类实例时生效。
 *
 * <p>保留独立子类的意义：① 给 mixin 一个可判别的类型；② 之后想加「余烬背罐」专属逻辑（例如自动补气）
 * 时不用再改注册。
 */
public class EmberBacktankBlockEntity extends BacktankBlockEntity {

    public EmberBacktankBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    /** 供 {@code FabricBlockEntityTypeBuilder} 使用（类型由 {@link MBGABlocks#EMBER_BACKTANK_ENTITY} 提供）。 */
    public EmberBacktankBlockEntity(BlockPos pos, BlockState state) {
        this(MBGABlocks.EMBER_BACKTANK_ENTITY, pos, state);
    }
}
