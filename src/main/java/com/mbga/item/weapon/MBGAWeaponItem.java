package com.mbga.item.weapon;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;

/**
 * Tap / Flick / Drag / Hold 四种武器：剑类行为 + 各自独特的命中音效。
 */
public class MBGAWeaponItem extends SwordItem {
    private final SoundEvent attackSound;

    public MBGAWeaponItem(ToolMaterial material, int attackDamage, float attackSpeed,
                          SoundEvent attackSound, Settings settings) {
        super(material, attackDamage, attackSpeed, settings);
        this.attackSound = attackSound;
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!attacker.getWorld().isClient) {
            attacker.getWorld().playSound(null, target.getX(), target.getY(), target.getZ(),
                    this.attackSound, SoundCategory.PLAYERS, 1.0F, 1.0F);
        }
        return super.postHit(stack, target, attacker);
    }
}
