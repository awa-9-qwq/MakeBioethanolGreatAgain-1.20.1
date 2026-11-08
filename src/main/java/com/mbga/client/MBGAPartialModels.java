package com.mbga.client;

import com.jozufozu.flywheel.core.PartialModel;
import com.mbga.MBGA;
import net.minecraft.util.Identifier;

/**
 * MBGA 用到的 Flywheel「部分模型」（不占方块状态、由渲染器旋转的模型）。
 *
 * <p>参照 Create 的 {@code AllPartialModels}：{@link PartialModel} 在构造时把自己登记到
 * Flywheel 的静态表里，随后由 {@code PartialModel#onModelRegistry} / {@code #onModelBake} 统一烘焙。
 * 因此本类必须在模型加载之前完成类初始化 —— 由 {@code MBGAClient#onInitializeClient} 调用 {@link #init()}
 * 触发。
 *
 * <p>模型文件与 Create 的背罐同构，只是贴图换成 {@code mbga:block/ember_backtank}：
 * <ul>
 *     <li>{@code mbga:block/ember_backtank/block_shaft_input}：背面接传动杆的那截轴（随转速旋转）；</li>
 *     <li>{@code mbga:block/ember_backtank/block_cogs}：侧面两个小齿轮（随转速旋转）。</li>
 * </ul>
 */
public final class MBGAPartialModels {

    /** 背罐背面的传动轴。 */
    public static final PartialModel EMBER_BACKTANK_SHAFT =
            new PartialModel(new Identifier(MBGA.MOD_ID, "block/ember_backtank/block_shaft_input"));

    /** 背罐侧面的小齿轮。 */
    public static final PartialModel EMBER_BACKTANK_COGS =
            new PartialModel(new Identifier(MBGA.MOD_ID, "block/ember_backtank/block_cogs"));

    private MBGAPartialModels() {
    }

    /** 仅仅用于触发静态初始化（必须在模型烘焙之前调用）。 */
    public static void init() {
        // 静态字段的初始化就是全部工作。
    }
}
