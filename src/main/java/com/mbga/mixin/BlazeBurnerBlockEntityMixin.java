package com.mbga.mixin;

import com.mbga.duck.BlazeBurnerAccessor;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * 向 Create 的 {@code BlazeBurnerBlockEntity} 注入一个公开方法，
 * 用于在燃烧室已处于沸腾（SEETHING）状态时继续增加燃烧时长。
 */
@Mixin(BlazeBurnerBlockEntity.class)
public abstract class BlazeBurnerBlockEntityMixin implements BlazeBurnerAccessor {

    @Shadow(remap = false)
    protected int remainingBurnTime;

    @Override
    public void mbga$addBurnTime(int ticks) {
        this.remainingBurnTime = Math.min(this.remainingBurnTime + ticks, BlazeBurnerBlockEntity.MAX_HEAT_CAPACITY);
    }
}
