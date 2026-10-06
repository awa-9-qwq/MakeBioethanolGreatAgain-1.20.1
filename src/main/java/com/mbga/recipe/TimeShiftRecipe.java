package com.mbga.recipe;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

/**
 * 「时移」配方：鼓风机气流穿过<b>奇点块</b>时处理物品（类似批量熔炼）。
 *
 * <p>JSON 与 Create 的处理配方保持同构，例如：
 * <pre>
 * {
 *   "type": "mbga:time_shift",
 *   "ingredients": [ { "item": "mbga:glowing_cobblestone" } ],
 *   "results": [ { "item": "minecraft:glowstone" } ]
 * }
 * </pre>
 * 结果支持 {@code count} 与 {@code chance}（0~1 的小数，与 Create 的研磨/序列组装一致）。
 */
public class TimeShiftRecipe implements Recipe<Inventory> {
    /** 默认处理时间（tick）。 */
    public static final int DEFAULT_PROCESSING_TIME = 100;

    private final Identifier id;
    private final Ingredient input;
    private final List<Output> outputs;
    private final int processingTime;

    public TimeShiftRecipe(Identifier id, Ingredient input, List<Output> outputs, int processingTime) {
        this.id = id;
        this.input = input;
        this.outputs = List.copyOf(outputs);
        this.processingTime = processingTime;
    }

    public Ingredient getInput() {
        return this.input;
    }

    public List<Output> getOutputs() {
        return this.outputs;
    }

    public int getOutputCount() {
        return this.outputs.size();
    }

    public int getProcessingTime() {
        return this.processingTime;
    }

    /** 依次判定每个产出（{@code chance >= 1} 必定产出）。 */
    public List<ItemStack> rollResults(Random random) {
        List<ItemStack> results = new ArrayList<>();
        for (Output output : this.outputs) {
            ItemStack stack = output.roll(random);
            if (!stack.isEmpty()) {
                results.add(stack);
            }
        }
        return results;
    }

    @Override
    public boolean matches(Inventory inventory, World world) {
        return this.input.test(inventory.getStack(0));
    }

    @Override
    public ItemStack craft(Inventory inventory, DynamicRegistryManager registryManager) {
        return this.outputs.isEmpty() ? ItemStack.EMPTY : this.outputs.get(0).stack().copy();
    }

    @Override
    public boolean fits(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getOutput(DynamicRegistryManager registryManager) {
        return this.outputs.isEmpty() ? ItemStack.EMPTY : this.outputs.get(0).stack().copy();
    }

    @Override
    public DefaultedList<Ingredient> getIngredients() {
        DefaultedList<Ingredient> ingredients = DefaultedList.of();
        ingredients.add(this.input);
        return ingredients;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean isIgnoredInRecipeBook() {
        return true;
    }

    @Override
    public Identifier getId() {
        return this.id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return MBGARecipeTypes.TIME_SHIFT.getSerializer();
    }

    @Override
    public RecipeType<?> getType() {
        return MBGARecipeTypes.TIME_SHIFT.getType();
    }

    /** 单个产出：物品 + 概率。 */
    public record Output(ItemStack stack, float chance) {
        public ItemStack roll(Random random) {
            if (this.chance >= 1.0F || random.nextFloat() < this.chance) {
                return this.stack.copy();
            }
            return ItemStack.EMPTY;
        }
    }
}
