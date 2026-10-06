package com.mbga.item;

import com.mbga.MBGA;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/**
 * 奇点：可食用（100 饱食度 / 100 饱和度），食用后立即受到 2 亿虚空伤害。
 */
public class SingularityItem extends Item {
    public SingularityItem(Settings settings) {
        super(settings.food(new FoodComponent.Builder()
                .hunger(100)
                .saturationModifier(1.0F)
                .alwaysEdible()
                .build()));
    }

    @Override
    public boolean isFireproof() {
        return true;
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (!world.isClient && user instanceof PlayerEntity player) {
            // 2 亿（200,000,000）虚空伤害
            player.damage(player.getDamageSources().create(MBGA.SINGULARITY), 200_000_000.0F);
            // 隐藏成就「迷失」：迷失在时间的长河（吃下奇点 / 被时移）。
            if (player instanceof net.minecraft.server.network.ServerPlayerEntity serverPlayer) {
                com.mbga.event.MBGAAdvancementHandler.grant(serverPlayer, "lost_in_time");
            }
        }
        return super.finishUsing(stack, world, user);
    }
}
