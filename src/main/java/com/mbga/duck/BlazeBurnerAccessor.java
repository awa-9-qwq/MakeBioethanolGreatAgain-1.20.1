package com.mbga.duck;

/**
 * 由 Mixin 注入到 {@code BlazeBurnerBlockEntity} 上的鸭子类型接口，
 * 用于在燃烧室已处于沸腾状态时继续增加燃烧时长。
 */
public interface BlazeBurnerAccessor {
    void mbga$addBurnTime(int ticks);
}
