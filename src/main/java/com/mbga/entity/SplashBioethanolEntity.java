package com.mbga.entity;

import com.mbga.MBGA;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.List;

/**
 * 喷溅式生物乙醇的投掷实体：落地后向周围生物施加「超级燃烧」。
 */
public class SplashBioethanolEntity extends ThrownItemEntity {
    public SplashBioethanolEntity(EntityType<? extends SplashBioethanolEntity> type, World world) {
        super(type, world);
    }

    public SplashBioethanolEntity(World world, LivingEntity owner) {
        super(MBGA.SPLASH_BIOETHANOL_ENTITY, owner, world);
    }

    @Override
    protected Item getDefaultItem() {
        return MBGA.SPLASH_BIOETHANOL;
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);
        if (!this.getWorld().isClient) {
            this.applySplash();
            this.discard();
        }
    }

    private void applySplash() {
        Box box = this.getBoundingBox().expand(4.0, 2.0, 4.0);
        List<LivingEntity> list = this.getWorld().getNonSpectatingEntities(LivingEntity.class, box);
        for (LivingEntity entity : list) {
            if (entity.isAffectedBySplashPotions() && this.squaredDistanceTo(entity) < 16.0) {
                entity.addStatusEffect(new StatusEffectInstance(MBGA.SUPER_BURN, 4 * 60 * 20, 0));
            }
        }
    }
}
