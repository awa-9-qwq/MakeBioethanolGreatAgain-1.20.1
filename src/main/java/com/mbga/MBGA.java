package com.mbga;

import com.mbga.block.MBGABlocks;
import com.mbga.block.windmill.MBGAWindmills;
import com.mbga.effect.DeflagrationEffect;
import com.mbga.effect.DrunkEffect;
import com.mbga.effect.SuperBurnEffect;
import com.mbga.entity.BottleOfFireEntity;
import com.mbga.entity.SplashBioethanolEntity;
import com.mbga.event.MBGAEventHandlers;
import com.mbga.integration.farmersdelight.MBGAFarmersDelight;
import com.mbga.integration.oreexcavation.MBGAProspectors;
import com.mbga.item.BioethanolTeaDrinkItem;
import com.mbga.item.BottleOfFireItem;
import com.mbga.item.FireArrowItem;
import com.mbga.item.MBGAItems;
import com.mbga.item.SingularityItem;
import com.mbga.item.SplashBioethanolItem;
import com.mbga.recipe.BottleOfFireRecipe;
import com.mbga.recipe.BottleOfFireRecipeSerializer;
import com.mbga.recipe.MBGARecipeTypes;
import com.mbga.recipe.TimeShiftProcessingType;
import com.mbga.sound.MBGASounds;
import com.simibubi.create.content.kinetics.fan.processing.FanProcessingTypeRegistry;
import net.fabricmc.api.ModInitializer;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class MBGA implements ModInitializer {
    public static final String MOD_ID = "mbga";

    // ---- 状态效果 ----
    public static final StatusEffect SUPER_BURN = new SuperBurnEffect();
    public static final StatusEffect DRUNK = new DrunkEffect();
    public static final StatusEffect DEFLAGRATION = new DeflagrationEffect();

    // ---- 伤害类型 ----
    public static final RegistryKey<DamageType> ALCOHOL_POISONING =
            RegistryKey.of(RegistryKeys.DAMAGE_TYPE, new Identifier(MOD_ID, "alcohol_poisoning"));
    public static final RegistryKey<DamageType> SINGULARITY =
            RegistryKey.of(RegistryKeys.DAMAGE_TYPE, new Identifier(MOD_ID, "singularity"));

    // ---- 物品 ----
    public static final Item BIOETHANOL_TEA_DRINK = new BioethanolTeaDrinkItem(
            new Item.Settings()
                    .maxCount(16)
                    .recipeRemainder(Items.GLASS_BOTTLE)
                    .food(new FoodComponent.Builder()
                            .hunger(2)
                            .saturationModifier(0.4F)
                            .alwaysEdible()
                            .build()));

    public static final Item SPLASH_BIOETHANOL = new SplashBioethanolItem(
            new Item.Settings().maxCount(16));

    public static final Item BOTTLE_OF_FIRE = new BottleOfFireItem(
            new Item.Settings().maxCount(16));

    public static final Item FIRE_ARROW = new FireArrowItem(
            new Item.Settings());

    public static final Item SINGULARITY_ITEM = new SingularityItem(
            new Item.Settings().maxCount(64));

    // ---- 实体 ----
    public static final EntityType<SplashBioethanolEntity> SPLASH_BIOETHANOL_ENTITY = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier(MOD_ID, "splash_bioethanol"),
            FabricEntityTypeBuilder.<SplashBioethanolEntity>create(SpawnGroup.MISC, SplashBioethanolEntity::new)
                    .dimensions(EntityDimensions.fixed(0.25F, 0.25F))
                    .trackRangeChunks(4)
                    .trackedUpdateRate(10)
                    .build());

    public static final EntityType<BottleOfFireEntity> BOTTLE_OF_FIRE_ENTITY = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier(MOD_ID, "bottle_of_fire"),
            FabricEntityTypeBuilder.<BottleOfFireEntity>create(SpawnGroup.MISC, BottleOfFireEntity::new)
                    .dimensions(EntityDimensions.fixed(0.25F, 0.25F))
                    .trackRangeChunks(4)
                    .trackedUpdateRate(10)
                    .build());

    // ---- 配方（瓶中火：打火石消耗耐久）----
    public static final RecipeType<BottleOfFireRecipe> BOTTLE_OF_FIRE_RECIPE_TYPE = Registry.register(
            Registries.RECIPE_TYPE,
            new Identifier(MOD_ID, "bottle_of_fire"),
            new RecipeType<BottleOfFireRecipe>() {
                @Override
                public String toString() {
                    return MOD_ID + ":bottle_of_fire";
                }
            });

    public static final RecipeSerializer<BottleOfFireRecipe> BOTTLE_OF_FIRE_RECIPE_SERIALIZER = Registry.register(
            Registries.RECIPE_SERIALIZER,
            new Identifier(MOD_ID, "bottle_of_fire"),
            new BottleOfFireRecipeSerializer());

    @Override
    public void onInitialize() {
        Registry.register(Registries.STATUS_EFFECT, new Identifier(MOD_ID, "super_burn"), SUPER_BURN);
        Registry.register(Registries.STATUS_EFFECT, new Identifier(MOD_ID, "drunk"), DRUNK);
        Registry.register(Registries.STATUS_EFFECT, new Identifier(MOD_ID, "deflagration"), DEFLAGRATION);

        // 自定义音效（武器命中音效）必须先注册。
        MBGASounds.register();

        // 新增方块 / 物品（含方块实体、燃料、护符、武器与余烬装备等）。
        MBGABlocks.register();
        MBGAItems.register();

        // 自定义处理配方类型（时移）+ 鼓风机处理方式（鼓风机 -> 奇点块）。
        MBGARecipeTypes.register();
        FanProcessingTypeRegistry.register(new Identifier(MOD_ID, "time_shift"), new TimeShiftProcessingType());

        // 联动内容（对应模组由 fabric.mod.json 的 depends 声明）。
        MBGAProspectors.register();
        MBGAFarmersDelight.register();
        MBGAFarmersDelight.registerEmberKnife();
        MBGAWindmills.register();

        Registry.register(Registries.ITEM, new Identifier(MOD_ID, "bioethanol_tea_drink"), BIOETHANOL_TEA_DRINK);
        Registry.register(Registries.ITEM, new Identifier(MOD_ID, "splash_bioethanol"), SPLASH_BIOETHANOL);
        Registry.register(Registries.ITEM, new Identifier(MOD_ID, "bottle_of_fire"), BOTTLE_OF_FIRE);
        Registry.register(Registries.ITEM, new Identifier(MOD_ID, "fire_arrow"), FIRE_ARROW);
        Registry.register(Registries.ITEM, new Identifier(MOD_ID, "singularity"), SINGULARITY_ITEM);

        // ---- 统一的 MBGA 创造模式选项卡（半成品不显示）----
        Registry.register(Registries.ITEM_GROUP, new Identifier(MOD_ID, "main"),
                FabricItemGroup.builder()
                        .icon(() -> new ItemStack(BIOETHANOL_TEA_DRINK))
                        .displayName(Text.translatable("itemGroup.mbga.main"))
                        .entries((context, entries) -> {
                            entries.add(BIOETHANOL_TEA_DRINK);
                            entries.add(SPLASH_BIOETHANOL);
                            entries.add(BOTTLE_OF_FIRE);
                            entries.add(FIRE_ARROW);
                            entries.add(SINGULARITY_ITEM);
                            entries.add(MBGABlocks.SPACE_SUPERPRESSOR);
                            entries.add(MBGABlocks.SOLID_BIOETHANOL);
                            entries.add(MBGABlocks.PURPLE_ICE);
                            entries.add(MBGABlocks.REINFORCED_DEEPSLATE);
                            entries.add(MBGABlocks.SINGULARITY_BLOCK);
                            entries.add(MBGABlocks.GLOWING_COBBLESTONE);
                            entries.add(MBGABlocks.ORGANIC_DIRT);
                            entries.add(MBGABlocks.FERTILE_DIRT);
                            entries.add(MBGABlocks.SOLID_OIL);
                            entries.add(MBGABlocks.EMBER_METAL_BLOCK);
                            entries.add(MBGAFarmersDelight.EMBER_CUTTING_BOARD);
                            MBGAWindmills.allBlocks().forEach(entries::add);
                            entries.add(MBGAItems.GEODE);
                            entries.add(MBGAItems.RUBY);
                            entries.add(MBGAItems.TOPAZ);
                            entries.add(MBGAItems.SAPPHIRE);
                            entries.add(MBGAItems.JADE);
                            entries.add(MBGAItems.EMBER_METAL);
                            entries.add(MBGAItems.EMBER_UPGRADE_SMITHING_TEMPLATE);
                            entries.add(MBGAItems.RUBY_CHARM);
                            entries.add(MBGAItems.TOPAZ_CHARM);
                            entries.add(MBGAItems.SAPPHIRE_CHARM);
                            entries.add(MBGAItems.JADE_CHARM);
                            entries.add(MBGAItems.BIOETHANOL_CHARM);
                            entries.add(MBGAItems.TAP);
                            entries.add(MBGAItems.FLICK);
                            entries.add(MBGAItems.DRAG);
                            entries.add(MBGAItems.HOLD);
                            entries.add(MBGAItems.EMBER_SWORD);
                            entries.add(MBGAItems.EMBER_PICKAXE);
                            entries.add(MBGAItems.EMBER_AXE);
                            entries.add(MBGAItems.EMBER_SHOVEL);
                            entries.add(MBGAItems.EMBER_HOE);
                            entries.add(MBGAItems.EMBER_HELMET);
                            entries.add(MBGAItems.EMBER_CHESTPLATE);
                            entries.add(MBGAItems.EMBER_LEGGINGS);
                            entries.add(MBGAItems.EMBER_BOOTS);
                            entries.add(MBGAItems.RING_DOUGH);
                            entries.add(MBGAItems.EARTH);
                            entries.add(MBGAItems.DONUT);
                            entries.add(MBGAItems.CREAM_DONUT);
                            entries.add(MBGAItems.CHOCOLATE_DONUT);
                            entries.add(MBGAItems.SWEET_ROLL_RING);
                            entries.add(MBGAItems.CHROME);
                            entries.add(MBGAItems.ONION_RINGS);
                            entries.add(MBGAItems.EXPERIENCE_DONUT);
                            entries.add(MBGAItems.WHAT_DOES_IT_MEAN);
                            entries.add(MBGAFarmersDelight.EMBER_KNIFE);
                            entries.add(MBGAProspectors.NETHERITE_PROSPECTOR);
                            entries.add(MBGAProspectors.EMBER_PROSPECTOR);
                        })
                        .build());
        MBGAEventHandlers.register();
    }
}