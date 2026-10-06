package com.mbga.integration.delightfulcreation;

import com.google.gson.JsonElement;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;

/**
 * 与「Delightful Creations Tweaker」（模组 ID：{@code delightful_creation}）的联动。
 *
 * <p>该模组自带注液配方 {@code delightful_creation:bioethanol_wine_filling}
 * （玻璃瓶 + 27,000 mB {@code createaddition:bioethanol} → {@code delightful_creation:bioethanol_wine}）。
 * MBGA 与它**同时存在**时，这个配方需要被移除，因此这里在配方数据包加载阶段把它剔除。
 *
 * <p>该模组是**可选**依赖：没有加载时 {@link #isLoaded()} 为 {@code false}，
 * {@link #removeRecipes(Map)} 什么也不做，MBGA 单独运行不受任何影响。
 *
 * <p>调用方是 {@code com.mbga.mixin.RecipeManagerMixin}（注入 {@code RecipeManager#apply}）。
 */
public final class MBGADelightfulCreation {

    /** 联动目标模组的 mod ID。 */
    public static final String MOD_ID = "delightful_creation";

    private static final Logger LOGGER = LoggerFactory.getLogger("mbga/delightful_creation");

    /**
     * 与 MBGA 同时存在时需要移除的配方 ID。
     *
     * <p>{@code delightful_creation:bioethanol_wine_filling}（create:filling）：
     * 玻璃瓶 + 27,000 mB 生物乙醇 → 生物乙醇酒。
     *
     * <p>要再屏蔽别的配方，直接往这个集合里加 ID 即可（同一 mod ID 亦可）。
     */
    private static final Set<Identifier> REMOVED_RECIPES = Set.of(
            new Identifier(MOD_ID, "bioethanol_wine_filling"));

    private MBGADelightfulCreation() {
    }

    /** 联动目标模组是否已加载。 */
    public static boolean isLoaded() {
        return FabricLoader.getInstance().isModLoaded(MOD_ID);
    }

    /**
     * 从「配方 ID → 配方 JSON」表里剔除 {@link #REMOVED_RECIPES} 中的配方。
     *
     * <p>由 {@code RecipeManagerMixin} 在 {@code RecipeManager#apply}（数据包 / 资源重载时由
     * {@code JsonDataLoader} 调用）的<b>开头</b>调用：条目在反序列化之前就被丢掉，因此
     * 既不会留下解析失败的日志，也不会进入 {@code RecipeManager} 的缓存——机械动力的注液机、
     * 配方书与 JEI（配方由服务端同步）都不会再显示或使用它。
     *
     * @param recipeJson 资源重载准备阶段收集到的配方 JSON 表（{@code HashMap}，可修改）
     */
    public static void removeRecipes(Map<Identifier, JsonElement> recipeJson) {
        if (!isLoaded()) {
            return;
        }
        for (Identifier id : REMOVED_RECIPES) {
            if (recipeJson.remove(id) != null) {
                LOGGER.info("Removed recipe {} ({} is loaded)", id, MOD_ID);
            }
        }
    }
}
