package com.mbga.client.render;

import com.mbga.block.entity.EmberBacktankBlockEntity;
import com.mbga.client.MBGAPartialModels;
import com.simibubi.create.content.equipment.armor.BacktankBlock;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.foundation.render.CachedBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;

/**
 * 余烬背罐方块实体的渲染器，参照 Create 的 {@code BacktankRenderer}。
 *
 * <p>方块本体（罐体）由区块渲染器用 blockstate 模型绘制；这里只额外画两个会转的部分：
 * 背面的传动轴与侧面齿轮。Create 把轴交给 Flywheel 的 {@code BacktankInstance} 实例化渲染，
 * 而本模组的方块实体类型没有注册实例工厂（Flywheel 对未知类型直接跳过），所以这里
 * 把「轴」也放在 BER 里用 {@link #renderRotatingBuffer} 旋转绘制，避免放置后看不见轴。
 *
 * <p>齿轮的位移 / 旋转公式与 Create 一致（这样挂到玩家背上时，
 * {@code BacktankArmorLayer} 渲染的罐体与这里的齿轮位置能对上）。
 */
public class EmberBacktankRenderer extends KineticBlockEntityRenderer<EmberBacktankBlockEntity> {

    public EmberBacktankRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    protected SuperByteBuffer getRotatedModel(EmberBacktankBlockEntity blockEntity, BlockState state) {
        return CachedBufferer.partial(MBGAPartialModels.EMBER_BACKTANK_SHAFT, state);
    }

    @Override
    protected void renderSafe(EmberBacktankBlockEntity blockEntity, float partialTicks, MatrixStack ms,
                              VertexConsumerProvider buffer, int light, int overlay) {
        BlockState state = blockEntity.getCachedState();

        // 传动轴：随转速绕 Z 轴旋转（与 Create 的 BacktankInstance 相同的变换）。
        renderRotatingBuffer(blockEntity,
                CachedBufferer.partial(MBGAPartialModels.EMBER_BACKTANK_SHAFT, state),
                ms, buffer.getBuffer(RenderLayer.getSolid()), light);

        // 齿轮：Create 的 BacktankRenderer#renderSafe 同款变换。
        SuperByteBuffer cogs = CachedBufferer.partial(MBGAPartialModels.EMBER_BACKTANK_COGS, state);
        ((SuperByteBuffer) ((SuperByteBuffer) ((SuperByteBuffer) cogs.centre()
                .rotateY(180.0F + AngleHelper.horizontalAngle(
                        state.get(BacktankBlock.HORIZONTAL_FACING))))
                .unCentre())
                .translate(0.0, 0.40625, 0.6875)
                .rotate(Direction.EAST,
                        AngleHelper.rad(blockEntity.getSpeed() / 4.0F
                                * AnimationTickHolder.getRenderTime(blockEntity.getWorld()) % 360.0F)))
                .translate(0.0, -0.40625, -0.6875)
                .light(light)
                .renderInto(ms, buffer.getBuffer(RenderLayer.getSolid()));
    }
}
