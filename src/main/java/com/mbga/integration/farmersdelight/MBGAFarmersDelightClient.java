package com.mbga.integration.farmersdelight;

import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import vectorwing.farmersdelight.client.renderer.CuttingBoardRenderer;

/**
 * 余烬砧板的客户端渲染注册（可选）。
 *
 * <p>农夫乐事把 {@code CuttingBoardRenderer} 只注册给了自己的方块实体类型，
 * 我们的 {@code mbga:ember_cutting_board} 是新的类型，因此需要在客户端再注册一次，
 * 砧板上摆放的物品才会显示出来。
 *
 * <p>调用位置：{@code com.mbga.client.MBGAClient#onInitializeClient()} 里加一行
 * {@code MBGAFarmersDelightClient.registerBlockEntityRenderers();}
 * （本类只在客户端环境加载，切勿在公共初始化里调用）。
 */
public final class MBGAFarmersDelightClient {

    private MBGAFarmersDelightClient() {
    }

    /** 为余烬砧板的方块实体类型注册农夫乐事的砧板渲染器。 */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void registerBlockEntityRenderers() {
        BlockEntityRendererFactories.register(MBGAFarmersDelight.EMBER_CUTTING_BOARD_ENTITY,
                (BlockEntityRendererFactory) CuttingBoardRenderer::new);
    }
}
