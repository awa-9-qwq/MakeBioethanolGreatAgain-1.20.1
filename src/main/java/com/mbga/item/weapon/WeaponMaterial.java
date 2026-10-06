package com.mbga.item.weapon;

import net.minecraft.item.Item;
import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.Ingredient;

/**
 * 四种武器（Tap / Flick / Drag / Hold）共用的工具材料。
 *
 * <p>{@code getAttackDamage()} 为 0，因此每把武器在物品栏里显示的「攻击伤害」
 * 就是构造函数传入的数值（7 / 6 / 5 / 8）。
 */
public class WeaponMaterial implements ToolMaterial {
    private final int durability;
    private final Item repairItem;

    public WeaponMaterial(int durability, Item repairItem) {
        this.durability = durability;
        this.repairItem = repairItem;
    }

    @Override
    public int getDurability() {
        return this.durability;
    }

    @Override
    public float getMiningSpeedMultiplier() {
        return 6.0F;
    }

    @Override
    public float getAttackDamage() {
        return 0.0F;
    }

    @Override
    public int getMiningLevel() {
        return 1;
    }

    @Override
    public int getEnchantability() {
        return 14;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.ofItems(this.repairItem);
    }
}
