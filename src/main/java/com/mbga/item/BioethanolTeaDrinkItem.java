package com.mbga.item;

import com.mbga.MBGA;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;

/**
 * 可饮用的“生物乙醇茶饮”。
 *
 * <p>饮用效果链：
 * <ol>
 *     <li>无效果时饮用：获得 4 分钟“超级燃烧”；</li>
 *     <li>持有“超级燃烧”时饮用：变为“醉酒”，时长为剩余超级燃烧时长的一半；</li>
 *     <li>持有“醉酒”时饮用：直接死亡（死于酒精中毒）。</li>
 * </ol>
 */
public class BioethanolTeaDrinkItem extends Item {
    /** 超级燃烧效果的持续时长：4 分钟（4800 tick）。 */
    public static final int SUPER_BURN_DURATION = 4 * 60 * 20;

    private static final int MAX_USE_TIME = 40;

    public BioethanolTeaDrinkItem(Settings settings) {
        super(settings);
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.DRINK;
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return MAX_USE_TIME;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        return ItemUsage.consumeHeldItem(world, user, hand);
    }

    @Override
    public SoundEvent getDrinkSound() {
        // 水瓶的饮用声音（原版通用饮用声）
        return SoundEvents.ENTITY_GENERIC_DRINK;
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        ItemStack result = super.finishUsing(stack, world, user);

        if (!world.isClient && user instanceof PlayerEntity player) {
            applyDrinkEffects(player);
        }

        // 创造模式或已死亡时不处理空瓶返回。
        if (user instanceof PlayerEntity player && (player.getAbilities().creativeMode || player.getHealth() <= 0.0F)) {
            return result;
        }

        if (result.isEmpty()) {
            return new ItemStack(Items.GLASS_BOTTLE);
        }

        // 手中还有剩余茶饮：把空瓶放回背包。
        if (user instanceof PlayerEntity player) {
            ItemStack bottle = new ItemStack(Items.GLASS_BOTTLE);
            if (!player.getInventory().insertStack(bottle)) {
                player.dropItem(bottle, false);
            }
        }
        return result;
    }

    private void applyDrinkEffects(PlayerEntity player) {
        if (player.hasStatusEffect(MBGA.DRUNK)) {
            // 醉酒后再次饮用：直接死亡（酒精中毒）
            player.damage(player.getDamageSources().create(MBGA.ALCOHOL_POISONING), Float.MAX_VALUE);
        } else if (player.hasStatusEffect(MBGA.SUPER_BURN)) {
            // 超级燃烧期间饮用：变为醉酒，时长为剩余超级燃烧时长的一半
            int remaining = player.getStatusEffect(MBGA.SUPER_BURN).getDuration();
            player.removeStatusEffect(MBGA.SUPER_BURN);
            player.addStatusEffect(new StatusEffectInstance(MBGA.DRUNK, remaining / 2, 0));
        } else {
            // 首次饮用：获得 4 分钟超级燃烧
            player.addStatusEffect(new StatusEffectInstance(MBGA.SUPER_BURN, SUPER_BURN_DURATION, 0));
        }
    }
}
