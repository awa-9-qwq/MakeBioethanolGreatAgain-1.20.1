package com.mbga.entity;

import com.mbga.MBGA;
import com.mbga.event.MBGAAdvancementHandler;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * 火箭（fire arrow）：命中目标施加「爆燃」（10 秒）并点燃；落地则点燃方块。
 */
public class FireArrowEntity extends ArrowEntity {
    public FireArrowEntity(World world, LivingEntity owner) {
        super(world, owner);
    }

    @Override
    protected void onHit(LivingEntity target) {
        super.onHit(target);
        if (!this.getWorld().isClient) {
            target.addStatusEffect(new StatusEffectInstance(MBGA.DEFLAGRATION, 10 * 20, 0));
            target.setOnFireFor(5);
            // 隐藏进度「Firefox」：让一只狐狸获得爆燃状态。
            MBGAAdvancementHandler.onDeflagrationApplied(target, this.getOwner());
        }
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        super.onEntityHit(entityHitResult);
        // 隐藏进度「赤壁之战」：使用火箭射中一只船。
        if (!this.getWorld().isClient
                && entityHitResult.getEntity() instanceof BoatEntity
                && this.getOwner() instanceof ServerPlayerEntity player) {
            MBGAAdvancementHandler.grant(player, "fire_arrow_boat");
        }
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        super.onBlockHit(blockHitResult);
        if (!this.getWorld().isClient) {
            BlockPos pos = blockHitResult.getBlockPos().offset(blockHitResult.getSide());
            if (this.getWorld().getBlockState(pos).isAir()) {
                BlockState fire = AbstractFireBlock.getState(this.getWorld(), pos);
                if (fire != null) {
                    this.getWorld().setBlockState(pos, fire, 3);
                }
            }
        }
    }

    @Override
    protected ItemStack asItemStack() {
        return new ItemStack(MBGA.FIRE_ARROW);
    }
}
