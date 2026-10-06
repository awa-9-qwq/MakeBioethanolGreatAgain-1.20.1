package com.mbga.block.entity;

import com.mbga.block.MBGABlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Fertilizable;
import net.minecraft.block.SugarCaneBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;

/**
 * 富饶泥土的方块实体：每 10 tick 有 50% 概率催熟一次上方作物。
 *
 * <p>「催熟」优先走原版 {@link Fertilizable}（与骨粉/随机刻完全一致）；如果上方方块无法被催熟
 * （例如已经成熟、或者本身不支持骨粉），则退化为「直接增加生长进度」——即把它的 {@code age}
 * 属性 +1（例如甜浆果丛等）。
 *
 * <p>特例：上方是甘蔗时不做催熟，而是每 10 tick 有 50% 概率让它向上长高一格。
 */
public class FertileDirtBlockEntity extends BlockEntity {
    /** 触发间隔：10 tick。 */
    private static final int INTERVAL_TICKS = 10;
    /** 触发概率：50%。 */
    private static final float GROWTH_CHANCE = 0.5F;
    /** 甘蔗最大高度（与原版一致）。 */
    private static final int MAX_SUGAR_CANE_HEIGHT = 3;

    private int tickCounter;

    public FertileDirtBlockEntity(BlockPos pos, BlockState state) {
        super(MBGABlocks.FERTILE_DIRT_BLOCK_ENTITY, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, FertileDirtBlockEntity blockEntity) {
        if (world.isClient) {
            return;
        }
        if (++blockEntity.tickCounter < INTERVAL_TICKS) {
            return;
        }
        blockEntity.tickCounter = 0;

        if (world.random.nextFloat() >= GROWTH_CHANCE) {
            return;
        }
        if (!(world instanceof ServerWorld serverWorld)) {
            return;
        }
        tryRipen(serverWorld, pos.up());
    }

    private static void tryRipen(ServerWorld world, BlockPos cropPos) {
        BlockState cropState = world.getBlockState(cropPos);
        Block cropBlock = cropState.getBlock();

        // 甘蔗：富饶泥土改为让它「向上生长一格」（每 10 tick 50% 概率，见 tick）。
        if (cropBlock instanceof SugarCaneBlock) {
            growSugarCaneUpward(world, cropPos);
            return;
        }

        if (cropBlock instanceof Fertilizable fertilizable
                && fertilizable.isFertilizable(world, cropPos, cropState, false)) {
            if (fertilizable.canGrow(world, world.random, cropPos, cropState)) {
                fertilizable.grow(world, world.random, cropPos, cropState);
            }
            return;
        }

        // 无法催熟：直接增加生长进度。
        IntProperty ageProperty = findAgeProperty(cropState);
        if (ageProperty == null) {
            return;
        }
        int age = cropState.get(ageProperty);
        int maxAge = Collections.max(ageProperty.getValues());
        if (age < maxAge) {
            world.setBlockState(cropPos, cropState.with(ageProperty, age + 1), Block.NOTIFY_LISTENERS);
        }
    }

    /**
     * 让富饶泥土上方的整列甘蔗向上长高一格（与原版一致：最高 3 格）。
     */
    private static void growSugarCaneUpward(ServerWorld world, BlockPos canePos) {
        BlockPos top = canePos;
        int height = 1;
        while (height < MAX_SUGAR_CANE_HEIGHT && world.getBlockState(top.up()).isOf(Blocks.SUGAR_CANE)) {
            top = top.up();
            height++;
        }
        if (height >= MAX_SUGAR_CANE_HEIGHT || !world.getBlockState(top.up()).isAir()) {
            return;
        }

        world.setBlockState(top.up(), Blocks.SUGAR_CANE.getDefaultState(), Block.NOTIFY_LISTENERS);
        BlockState topState = world.getBlockState(top);
        if (topState.contains(SugarCaneBlock.AGE)) {
            world.setBlockState(top, topState.with(SugarCaneBlock.AGE, 0), Block.NOTIFY_LISTENERS);
        }
    }

    @Nullable
    private static IntProperty findAgeProperty(BlockState state) {
        for (Property<?> property : state.getProperties()) {
            if (property instanceof IntProperty intProperty && "age".equals(property.getName())) {
                return intProperty;
            }
        }
        return null;
    }
}
