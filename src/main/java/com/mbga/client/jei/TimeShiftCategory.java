package com.mbga.client.jei;

import com.mbga.MBGA;
import com.mbga.block.MBGABlocks;
import com.mbga.recipe.TimeShiftRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Locale;

/**
 * JEI 的「时移」配方分类。
 *
 * <p>左侧为输入槽，右侧 2×2 网格展示最多 4 个产出；产出概率小于 100% 时，
 * 既会在物品提示（tooltip）里追加百分比，也会把百分比文本常驻绘制在槽位下方，
 * 让玩家一眼就能看出这是概率产出。
 */
public class TimeShiftCategory implements IRecipeCategory<TimeShiftRecipe> {
    /** 时移配方的 JEI 配方类型（uid 为 {@code mbga:time_shift}）。 */
    public static final RecipeType<TimeShiftRecipe> RECIPE_TYPE =
            RecipeType.create(MBGA.MOD_ID, "time_shift", TimeShiftRecipe.class);

    /** 分类界面尺寸。 */
    public static final int WIDTH = 160;
    public static final int HEIGHT = 68;

    /** 输入槽位置。 */
    private static final int INPUT_X = 6;
    private static final int INPUT_Y = 25;
    /** 箭头位置。 */
    private static final int ARROW_X = 28;
    private static final int ARROW_Y = 28;
    /** 产出网格起点与间距。 */
    private static final int OUTPUT_BASE_X = 52;
    private static final int OUTPUT_BASE_Y = 6;
    private static final int OUTPUT_COLUMN_SPACING = 42;
    private static final int OUTPUT_ROW_SPACING = 30;
    private static final int OUTPUT_COLUMNS = 2;
    /** 一屏最多展示的产出数量（与 2×2 网格一致）。 */
    private static final int MAX_OUTPUTS = 4;
    /** 概率文本相对产出槽的偏移（槽位高 18，文本落在其正下方）。 */
    private static final int CHANCE_TEXT_DY = 19;
    /** 概率文本颜色（黄色）。 */
    private static final int CHANCE_TEXT_COLOR = 0xFFFF55;

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable arrow;

    public TimeShiftCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(WIDTH, HEIGHT);
        this.icon = guiHelper.createDrawableItemLike(MBGABlocks.SINGULARITY_BLOCK);
        this.arrow = guiHelper.getRecipeArrow();
    }

    @Override
    public RecipeType<TimeShiftRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Text getTitle() {
        return Text.translatable("jei.mbga.category.time_shift");
    }

    @Override
    @SuppressWarnings("removal")
    public IDrawable getBackground() {
        return this.background;
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, TimeShiftRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, INPUT_X, INPUT_Y)
                .setStandardSlotBackground()
                .addIngredients(recipe.getInput());

        List<TimeShiftRecipe.Output> outputs = recipe.getOutputs();
        int shown = Math.min(outputs.size(), MAX_OUTPUTS);
        for (int i = 0; i < shown; i++) {
            TimeShiftRecipe.Output output = outputs.get(i);
            IRecipeSlotBuilder slot = builder.addSlot(RecipeIngredientRole.OUTPUT, outputX(i), outputY(i))
                    .setOutputSlotBackground()
                    .addItemStack(output.stack());
            if (output.chance() < 1.0F) {
                String percent = formatChance(output.chance());
                slot.addRichTooltipCallback((view, tooltip) -> tooltip.add(Text.literal(percent)));
            }
        }
    }

    @Override
    public void draw(TimeShiftRecipe recipe, IRecipeSlotsView recipeSlotsView, DrawContext context,
                     double mouseX, double mouseY) {
        this.arrow.draw(context, ARROW_X, ARROW_Y);

        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) {
            return;
        }
        TextRenderer textRenderer = client.textRenderer;

        List<TimeShiftRecipe.Output> outputs = recipe.getOutputs();
        int shown = Math.min(outputs.size(), MAX_OUTPUTS);
        for (int i = 0; i < shown; i++) {
            TimeShiftRecipe.Output output = outputs.get(i);
            if (output.chance() >= 1.0F) {
                continue;
            }
            String percent = formatChance(output.chance());
            context.drawTextWithShadow(textRenderer, Text.literal(percent),
                    outputX(i) + 1, outputY(i) + CHANCE_TEXT_DY, CHANCE_TEXT_COLOR);
        }
    }

    /** 产出槽 X 坐标（按行优先铺满 {@value #OUTPUT_COLUMNS} 列）。 */
    private static int outputX(int index) {
        return OUTPUT_BASE_X + (index % OUTPUT_COLUMNS) * OUTPUT_COLUMN_SPACING;
    }

    /** 产出槽 Y 坐标。 */
    private static int outputY(int index) {
        return OUTPUT_BASE_Y + (index / OUTPUT_COLUMNS) * OUTPUT_ROW_SPACING;
    }

    /**
     * 把 0~1 的概率格式化成百分比文本：
     * {@code 0.02F -> "2%"}、{@code 0.5F -> "50%"}、{@code 0.125F -> "12.5%"}。
     */
    private static String formatChance(float chance) {
        float percent = chance * 100.0F;
        int rounded = Math.round(percent);
        if (Math.abs(percent - rounded) < 0.05F) {
            return rounded + "%";
        }
        return String.format(Locale.ROOT, "%.1f%%", percent);
    }
}
