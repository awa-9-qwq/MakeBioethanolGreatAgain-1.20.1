package com.mbga.recipe;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * 「时移」配方的序列化器，JSON 结构与 Create 的处理配方一致
 * （{@code ingredients} 数组取第一个作为输入，{@code results} 支持 {@code count} / {@code chance}）。
 */
public class TimeShiftRecipeSerializer implements RecipeSerializer<TimeShiftRecipe> {

    @Override
    public TimeShiftRecipe read(Identifier id, JsonObject json) {
        // 兼容两种写法：{"ingredients": [ ... ]}（Create 风格）与 {"ingredient": {...}}。
        Ingredient input;
        if (json.has("ingredients")) {
            JsonElement first = JsonHelper.getArray(json, "ingredients").get(0);
            input = Ingredient.fromJson(first);
        } else {
            input = Ingredient.fromJson(JsonHelper.getObject(json, "ingredient"));
        }

        List<TimeShiftRecipe.Output> outputs = new ArrayList<>();
        for (JsonElement element : JsonHelper.getArray(json, "results")) {
            JsonObject result = element.getAsJsonObject();
            ItemStack stack = ShapedRecipe.outputFromJson(result);
            float chance = JsonHelper.getFloat(result, "chance", 1.0F);
            outputs.add(new TimeShiftRecipe.Output(stack, chance));
        }

        int processingTime = JsonHelper.getInt(json, "processingTime", TimeShiftRecipe.DEFAULT_PROCESSING_TIME);
        return new TimeShiftRecipe(id, input, outputs, processingTime);
    }

    @Override
    public TimeShiftRecipe read(Identifier id, PacketByteBuf buf) {
        Ingredient input = Ingredient.fromPacket(buf);
        int size = buf.readVarInt();
        List<TimeShiftRecipe.Output> outputs = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            ItemStack stack = buf.readItemStack();
            float chance = buf.readFloat();
            outputs.add(new TimeShiftRecipe.Output(stack, chance));
        }
        int processingTime = buf.readVarInt();
        return new TimeShiftRecipe(id, input, outputs, processingTime);
    }

    @Override
    public void write(PacketByteBuf buf, TimeShiftRecipe recipe) {
        recipe.getInput().write(buf);
        buf.writeVarInt(recipe.getOutputCount());
        for (TimeShiftRecipe.Output output : recipe.getOutputs()) {
            buf.writeItemStack(output.stack());
            buf.writeFloat(output.chance());
        }
        buf.writeVarInt(recipe.getProcessingTime());
    }
}
