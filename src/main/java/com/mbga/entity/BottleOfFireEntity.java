package com.mbga.entity;

import com.mbga.MBGA;
import com.mbga.event.MBGAAdvancementHandler;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.List;

/**
 * 瓶中火的投掷实体：落地后向周围生物施加「爆燃」，并点燃以落地点为中心的 4x4 区域。
 */
public class BottleOfFireEntity extends ThrownItemEntity {
    public BottleOfFireEntity(EntityType<? extends BottleOfFireEntity> type, World world) {
        super(type, world);
    }

    public BottleOfFireEntity(World world, LivingEntity owner) {
        super(MBGA.BOTTLE_OF_FIRE_ENTITY, owner, world);
    }

    @Override
    protected Item getDefaultItem() {
        return MBGA.BOTTLE_OF_FIRE;
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);
        if (!this.getWorld().isClient) {
            this.applySplash();
            this.igniteArea(this.getBlockPos());
            this.discard();
        }
    }

    private void applySplash() {
        Box box = this.getBoundingBox().expand(4.0, 2.0, 4.0);
        List<LivingEntity> list = this.getWorld().getNonSpectatingEntities(LivingEntity.class, box);
        for (LivingEntity entity : list) {
            if (entity.isAffectedBySplashPotions() && this.squaredDistanceTo(entity) < 16.0) {
                entity.addStatusEffect(new StatusEffectInstance(MBGA.DEFLAGRATION, 4 * 60 * 20, 0));
                // 隐藏进度「Firefox」：让一只狐狸获得爆燃状态。
                MBGAAdvancementHandler.onDeflagrationApplied(entity, this.getOwner());
            }
        }
    }

    private void igniteArea(BlockPos center) {
        World world = this.getWorld();
        for (int x = -1; x <= 2; x++) {
            for (int z = -1; z <= 2; z++) {
                BlockPos pos = center.add(x, 0, z);
                if (world.getBlockState(pos).isAir()) {
                    BlockState fire = AbstractFireBlock.getState(world, pos);
                    if (fire != null) {
                        world.setBlockState(pos, fire, 3);
                    }
                }
            }
        }
    }
}
