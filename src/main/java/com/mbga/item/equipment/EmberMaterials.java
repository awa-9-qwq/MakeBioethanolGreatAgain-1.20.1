package com.mbga.item.equipment;

import com.mbga.item.MBGAItems;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.Ingredient;
import net.minecraft.sound.SoundEvents;

/**
 * 「余烬」装备 / 工具的材料。
 *
 * <p>数值与原版下界合金同级（余烬是下界合金装备的升级品，特色在于两条耐久机制，
 * 而不是更高的基础数值）。工具材料的 {@code getAttackDamage()} 为 0，
 * 因此每种工具的伤害完全由各自构造函数里的数值决定。
 */
public final class EmberMaterials {
    /** 挖掘等级 4 = 下界合金（与 {@code net.fabricmc.yarn.constants.MiningLevels.NETHERITE} 一致）。 */
    private static final int NETHERITE_MINING_LEVEL = 4;

    public static final ToolMaterial TOOL = new ToolMaterial() {
        @Override
        public int getDurability() {
            // 下界合金 2031 + 1000。
            return 3031;
        }

        @Override
        public float getMiningSpeedMultiplier() {
            return 9.0F;
        }

        @Override
        public float getAttackDamage() {
            return 0.0F;
        }

        @Override
        public int getMiningLevel() {
            return NETHERITE_MINING_LEVEL;
        }

        @Override
        public int getEnchantability() {
            return 15;
        }

        @Override
        public Ingredient getRepairIngredient() {
            return Ingredient.ofItems(MBGAItems.EMBER_METAL);
        }
    };

    public static final ArmorMaterial ARMOR = new ArmorMaterial() {
        private static final int DURABILITY_MULTIPLIER = 37;
        /** 每种余烬盔甲比对应下界合金盔甲多出的耐久。 */
        private static final int ARMOR_DURABILITY_BONUS = 1000;

        @Override
        public int getDurability(ArmorItem.Type type) {
            // 下界合金盔甲耐久（37 × 11/16/15/13）+ 1000。
            return DURABILITY_MULTIPLIER * switch (type) {
                case HELMET -> 11;
                case CHESTPLATE -> 16;
                case LEGGINGS -> 15;
                case BOOTS -> 13;
            } + ARMOR_DURABILITY_BONUS;
        }

        @Override
        public int getProtection(ArmorItem.Type type) {
            // 下界合金护甲（3/8/6/3）+ 2。
            return switch (type) {
                case HELMET -> 5;
                case CHESTPLATE -> 10;
                case LEGGINGS -> 8;
                case BOOTS -> 5;
            };
        }

        @Override
        public int getEnchantability() {
            return 15;
        }

        @Override
        public net.minecraft.sound.SoundEvent getEquipSound() {
            return SoundEvents.ITEM_ARMOR_EQUIP_NETHERITE;
        }

        @Override
        public Ingredient getRepairIngredient() {
            return Ingredient.ofItems(MBGAItems.EMBER_METAL);
        }

       @Override
        public String getName() {
            return "mbga_ember";
        }

        @Override
        public float getToughness() {
            // 下界合金韧性 3 + 2。
            return 5.0F;
        }

        @Override
        public float getKnockbackResistance() {
            return 0.1F;
        }
    };

    private EmberMaterials() {
    }
}
