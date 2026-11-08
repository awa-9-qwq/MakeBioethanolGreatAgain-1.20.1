package com.mbga.recipe;

import com.mbga.MBGA;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.FlintAndSteelItem;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.ShapelessRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;

/**
 * 瓶中火的无序合成：喷溅式生物乙醇 + 打火石（打火石消耗 1 点耐久）。
 */
public class BottleOfFireRecipe extends ShapelessRecipe {
    public BottleOfFireRecipe(Identifier id, String group, CraftingRecipeCategory category, ItemStack output,
                              DefaultedList<Ingredient> input) {
        super(id, group, category, output, input);
    }

    @Override
    public net.minecraft.recipe.RecipeType<?> getType() {
        return net.minecraft.recipe.RecipeType.CRAFTING;
    }

    @Override
    public net.minecraft.recipe.RecipeSerializer<?> getSerializer() {
        return MBGA.BOTTLE_OF_FIRE_RECIPE_SERIALIZER;
    }

}
