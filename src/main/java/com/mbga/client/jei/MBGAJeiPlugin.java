package com.mbga.client.jei;

import com.mbga.MBGA;
import com.mbga.block.MBGABlocks;
import com.mbga.recipe.MBGARecipeTypes;
import com.mbga.recipe.TimeShiftRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;

import java.util.List;

/**
 * MBGA 的 JEI 插件：把「时移」配方接入 JEI 配方界面。
 *
 * <p>只通过 {@code jei_mod_plugin} 入口点加载，而该入口点仅存在于客户端，
 * 因此这里可以安全地引用客户端类。
 */
@JeiPlugin
public class MBGAJeiPlugin implements IModPlugin {
    private static final Identifier PLUGIN_UID = new Identifier(MBGA.MOD_ID, "jei_plugin");

    @Override
    public Identifier getPluginUid() {
        return PLUGIN_UID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(
                new TimeShiftCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        // 只在客户端世界可用时查询配方表（JEI 在进入世界后才会调用本方法）。
        MinecraftClient client = MinecraftClient.getInstance();
        ClientWorld world = client == null ? null : client.world;
        if (world == null) {
            return;
        }

        // MBGARecipeTypes.getType() 是泛型方法，这里显式指定类型参数。
        RecipeType<TimeShiftRecipe> vanillaType = MBGARecipeTypes.TIME_SHIFT.<RecipeType<TimeShiftRecipe>>getType();
        List<TimeShiftRecipe> recipes = world.getRecipeManager().listAllOfType(vanillaType);
        registration.addRecipes(TimeShiftCategory.RECIPE_TYPE, recipes);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        // 奇点块是时移处理的催化剂。
        registration.addRecipeCatalyst(MBGABlocks.SINGULARITY_BLOCK, TimeShiftCategory.RECIPE_TYPE);
    }
}
