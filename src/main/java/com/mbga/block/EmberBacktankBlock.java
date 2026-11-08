package com.mbga.block;

import com.simibubi.create.content.equipment.armor.BacktankBlock;
import com.simibubi.create.content.equipment.armor.BacktankBlockEntity;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.entity.BlockEntityType;

/**
 * 余烬背罐方块（放下后的形态），参照 Create 的 {@code BacktankBlock}。
 *
 * <p>除了方块实体类型以外，其余行为全部沿用 Create：
 * <ul>
 *     <li>水平朝向 + 可含水，背面接传动杆（<code>hasShaftTowards(NORTH)</code>）；</li>
 *     <li>有转速时按转速逐步充气，比较器输出气量；</li>
 *     <li>空手右键可直接穿到胸甲槽；放置 / 破坏时通过 NBT 保留空气与附魔。</li>
 * </ul>
 *
 * <p>容量翻倍的实现见 {@code com.mbga.mixin.BacktankBlockEntityMixin}（把 {@code BacktankUtil.maxAir(int)}
 * 的返回值替换为 {@link com.mbga.item.equipment.EmberBacktankItem#maxAir(int)}）。
 */
public class EmberBacktankBlock extends BacktankBlock {

    public EmberBacktankBlock(AbstractBlock.Settings settings) {
        super(settings);
    }

    @Override
    public BlockEntityType<? extends BacktankBlockEntity> getBlockEntityType() {
        return MBGABlocks.EMBER_BACKTANK_ENTITY;
    }
}
