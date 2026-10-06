package com.mbga.item;

import com.mbga.entity.FireArrowEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/**
 * 火箭（fire arrow）：可被弓/弩发射，命中目标施加「爆燃」并点燃。
 */
public class FireArrowItem extends ArrowItem {
    public FireArrowItem(Settings settings) {
        super(settings);
    }

    @Override
    public PersistentProjectileEntity createArrow(World world, ItemStack stack, LivingEntity shooter) {
        return new FireArrowEntity(world, shooter);
    }
}
