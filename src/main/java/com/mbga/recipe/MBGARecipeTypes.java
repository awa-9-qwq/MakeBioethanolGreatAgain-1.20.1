package com.mbga.recipe;

import com.mbga.MBGA;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

/**
 * MBGA 自己的「处理配方」类型注册表。
 *
 * <p>实现 Create 的 {@link IRecipeTypeInfo}，因此可以直接被 Create 的
 * {@code ProcessingRecipeSerializer} / JEI 集成体系识别（这里用的是自建的轻量配方实现）。
 */
public enum MBGARecipeTypes implements IRecipeTypeInfo {
    /** 时移（鼓风机 + 奇点块）。 */
    TIME_SHIFT;

    private final Identifier id;
    private final RecipeSerializer<?> serializer;
    private final RecipeType<?> type;

    private MBGARecipeTypes() {
        String name = name().toLowerCase(java.util.Locale.ROOT);
        this.id = new Identifier(MBGA.MOD_ID, name);
        this.serializer = Registry.register(
                Registries.RECIPE_SERIALIZER, this.id, new TimeShiftRecipeSerializer());
        this.type = Registry.register(
                Registries.RECIPE_TYPE, this.id,
                new RecipeType<TimeShiftRecipe>() {
                    @Override
                    public String toString() {
                        return MBGA.MOD_ID + ":" + name;
                    }
                });
    }

    /** 触发枚举类初始化（从而注册配方类型与序列化器）。 */
    public static void register() {
        values();
    }

    @Override
    public Identifier getId() {
        return this.id;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends RecipeSerializer<?>> T getSerializer() {
        return (T) this.serializer;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends RecipeType<?>> T getType() {
        return (T) this.type;
    }
}
