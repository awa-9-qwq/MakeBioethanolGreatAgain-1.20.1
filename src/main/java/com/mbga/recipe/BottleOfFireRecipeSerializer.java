package com.mbga.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.collection.DefaultedList;

public class BottleOfFireRecipeSerializer implements RecipeSerializer<BottleOfFireRecipe> {
    @Override
    public BottleOfFireRecipe read(Identifier id, JsonObject json) {
        String group = JsonHelper.getString(json, "group", "");
        DefaultedList<Ingredient> ingredients = readIngredients(JsonHelper.getArray(json, "ingredients"));
        ItemStack output = ShapedRecipe.outputFromJson(JsonHelper.getObject(json, "result"));
        return new BottleOfFireRecipe(id, group, CraftingRecipeCategory.MISC, output, ingredients);
    }

    @Override
    public BottleOfFireRecipe read(Identifier id, PacketByteBuf buf) {
        String group = buf.readString();
        int size = buf.readVarInt();
        DefaultedList<Ingredient> ingredients = DefaultedList.ofSize(size, Ingredient.EMPTY);
        for (int i = 0; i < size; i++) {
            ingredients.set(i, Ingredient.fromPacket(buf));
        }
        ItemStack output = buf.readItemStack();
        return new BottleOfFireRecipe(id, group, CraftingRecipeCategory.MISC, output, ingredients);
    }

    @Override
    public void write(PacketByteBuf buf, BottleOfFireRecipe recipe) {
        buf.writeString(recipe.getGroup());
        buf.writeVarInt(recipe.getIngredients().size());
        for (Ingredient ingredient : recipe.getIngredients()) {
            ingredient.write(buf);
        }
        buf.writeItemStack(recipe.getOutput(null));
    }

    private static DefaultedList<Ingredient> readIngredients(JsonArray array) {
        DefaultedList<Ingredient> ingredients = DefaultedList.of();
        for (JsonElement element : array) {
            ingredients.add(Ingredient.fromJson(element));
        }
        return ingredients;
    }
}
