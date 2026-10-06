package com.mbga.mixin;

import com.google.gson.JsonElement;
import com.mbga.integration.delightfulcreation.MBGADelightfulCreation;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

/**
 * 支持「与其他模组同时存在时移除指定配方」。
 *
 * <p>{@code RecipeManager#apply} 是数据包 / 资源重载解析所有配方 JSON 的入口。这里在它的
 * <b>开头</b>把 {@link MBGADelightfulCreation} 列出的配方从待解析表中删掉，于是这些配方
 * 根本不会被反序列化，也就不会出现在配方管理器（以及由服务端同步出去的配方列表）里。
 *
 * <p>删除动作本身受模组加载状态保护（见 {@link MBGADelightfulCreation#removeRecipes}），
 * 目标模组不存在时这里等价于空操作。
 */
@Mixin(RecipeManager.class)
public abstract class RecipeManagerMixin {

    @Inject(
            method = "apply(Ljava/util/Map;Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/util/profiler/Profiler;)V",
            at = @At("HEAD"))
    private void mbga$removeConflictRecipes(Map<Identifier, JsonElement> recipeJson,
                                            ResourceManager resourceManager,
                                            Profiler profiler,
                                            CallbackInfo ci) {
        MBGADelightfulCreation.removeRecipes(recipeJson);
    }
}
