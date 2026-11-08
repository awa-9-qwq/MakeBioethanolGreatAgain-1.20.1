package com.mbga.mixin;

import com.mbga.block.entity.EmberBacktankBlockEntity;
import com.mbga.item.equipment.EmberBacktankItem;
import com.simibubi.create.content.equipment.armor.BacktankBlockEntity;
import com.simibubi.create.content.equipment.armor.BacktankUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 让「方块形态」的余烬背罐也按 2 倍容量充气。
 *
 * <p>Create 的 {@link BacktankBlockEntity} 里共有三处用到容量上限，全部调用
 * {@code BacktankUtil.maxAir(capacityEnchantLevel)}：
 * <ul>
 *     <li>{@code tick()}：按转速往 {@code airLevel} 里充气；</li>
 *     <li>{@code getComparatorOutput()}：把气量换算成红石信号；</li>
 *     <li>{@code read(...)}：客户端读到「刚充满」时播放音效。</li>
 * </ul>
 *
 * <p>这里把这三处调用重定向到 {@link EmberBacktankItem#maxAir(int)}，但只在方块实体是
 * {@link EmberBacktankBlockEntity} 时生效，其余背罐（铜 / 下界合金）保持原样。
 *
 * <p>目标方法名在 Create 的类里都是唯一的，因此只写方法名、不写描述符；配合 {@code remap = false}，
 * 开发环境与生产环境都能命中。
 */
@Mixin(value = BacktankBlockEntity.class, remap = false)
public abstract class BacktankBlockEntityMixin {

    @Redirect(
            method = "tick",
            at = @At(value = "INVOKE",
                    target = "Lcom/simibubi/create/content/equipment/armor/BacktankUtil;maxAir(I)I"))
    private int mbga$tickMaxAir(int capacityEnchantLevel) {
        return mbga$maxAir(this, capacityEnchantLevel);
    }

    @Redirect(
            method = "getComparatorOutput",
            at = @At(value = "INVOKE",
                    target = "Lcom/simibubi/create/content/equipment/armor/BacktankUtil;maxAir(I)I"))
    private int mbga$comparatorMaxAir(int capacityEnchantLevel) {
        return mbga$maxAir(this, capacityEnchantLevel);
    }

    @Redirect(
            method = "read",
            at = @At(value = "INVOKE",
                    target = "Lcom/simibubi/create/content/equipment/armor/BacktankUtil;maxAir(I)I"))
    private int mbga$readMaxAir(int capacityEnchantLevel) {
        return mbga$maxAir(this, capacityEnchantLevel);
    }

    /** 余烬背罐 → 2 倍上限；其它背罐 → Create 原值。 */
    @Unique
    private static int mbga$maxAir(Object blockEntity, int capacityEnchantLevel) {
        return blockEntity instanceof EmberBacktankBlockEntity
                ? EmberBacktankItem.maxAir(capacityEnchantLevel)
                : BacktankUtil.maxAir(capacityEnchantLevel);
    }
}
