package com.mbga.client;

import com.mbga.MBGA;
import com.mbga.block.MBGABlocks;
import com.mbga.integration.farmersdelight.EmberCuttingBoardBlockEntity;
import com.mbga.integration.farmersdelight.MBGAFarmersDelight;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;
import vectorwing.farmersdelight.client.renderer.CuttingBoardRenderer;

public class MBGAClient implements ClientModInitializer {
    @Override
    @SuppressWarnings("unchecked")
    public void onInitializeClient() {
        EntityRendererRegistry.register(MBGA.SPLASH_BIOETHANOL_ENTITY, FlyingItemEntityRenderer::new);
        EntityRendererRegistry.register(MBGA.BOTTLE_OF_FIRE_ENTITY, FlyingItemEntityRenderer::new);

        // 余烬砧板：直接复用农夫乐事的砧板渲染器，因此外观（含砧板上放置的物品）与原版一致。
        BlockEntityRendererRegistry.register(MBGAFarmersDelight.EMBER_CUTTING_BOARD_ENTITY,
                dispatcher -> (BlockEntityRenderer<EmberCuttingBoardBlockEntity>) (Object)
                        new CuttingBoardRenderer(dispatcher));

        // 紫冰与蓝冰一致：透明方块，使用半透明渲染层。
        BlockRenderLayerMap.INSTANCE.putBlock(MBGABlocks.PURPLE_ICE, RenderLayer.getTranslucent());
    }
}
