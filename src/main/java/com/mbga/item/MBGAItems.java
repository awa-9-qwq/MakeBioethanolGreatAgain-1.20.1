package com.mbga.item;

import com.mbga.MBGA;
import com.mbga.item.charm.BioethanolCharmItem;
import com.mbga.item.charm.CharmItem;
import com.mbga.item.charm.JadeCharmItem;
import com.mbga.item.charm.RubyCharmItem;
import com.mbga.item.charm.SapphireCharmItem;
import com.mbga.item.charm.TopazCharmItem;
import com.mbga.item.equipment.EmberArmorItem;
import com.mbga.item.equipment.EmberAxeItem;
import com.mbga.item.equipment.EmberHoeItem;
import com.mbga.item.equipment.EmberPickaxeItem;
import com.mbga.item.equipment.EmberShovelItem;
import com.mbga.item.equipment.EmberSwordItem;
import com.mbga.item.equipment.EmberUpgradeTemplateItem;
import com.mbga.item.weapon.MBGAWeaponItem;
import com.mbga.item.weapon.WeaponMaterial;
import com.mbga.sound.MBGASounds;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SmithingTemplateItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * MBGA 的物品注册表（宝石类材料、余烬升级锻造模板、五种护符、四把武器与整套余烬装备）。
 */
public final class MBGAItems {
    // ---- 材料 ----
    /** 晶洞。 */
    public static Item GEODE;
    /** 红宝石。 */
    public static Item RUBY;
    /** 黄玉。 */
    public static Item TOPAZ;
    /** 蓝宝石。 */
    public static Item SAPPHIRE;
    /** 翡翠。 */
    public static Item JADE;
    /** 余烬金属。 */
    public static Item EMBER_METAL;
    /** 锻造模板（余烬升级）。 */
    public static Item EMBER_UPGRADE_SMITHING_TEMPLATE;

    // ---- 护符 ----
    public static CharmItem RUBY_CHARM;
    public static CharmItem TOPAZ_CHARM;
    public static CharmItem SAPPHIRE_CHARM;
    public static CharmItem JADE_CHARM;
    public static CharmItem BIOETHANOL_CHARM;

    // ---- 武器（各有独特命中音效；Hold 与 Tap 音效一致）----
    public static Item TAP;
    public static Item FLICK;
    public static Item DRAG;
    public static Item HOLD;

    // ---- 余烬装备 / 工具 ----
    public static Item EMBER_SWORD;
    public static Item EMBER_PICKAXE;
    public static Item EMBER_AXE;
    public static Item EMBER_SHOVEL;
    public static Item EMBER_HOE;
    public static Item EMBER_HELMET;
    public static Item EMBER_CHESTPLATE;
    public static Item EMBER_LEGGINGS;
    public static Item EMBER_BOOTS;

    // ---- 序列组装用的半成品 ----
    public static Item INCOMPLETE_SPACE_SUPERPRESSOR;
    public static Item INCOMPLETE_DIAMOND;
    public static Item INCOMPLETE_GEMSTONE;
    public static Item INCOMPLETE_BIOETHANOL_CHARM;
    public static Item INCOMPLETE_ONION_RINGS;

    // ---- 甜甜圈系列食物 ----
    /** 环形面团（材料，不可食用）。 */
    public static Item RING_DOUGH;
    /** "地球"：5 饱食 5 饱和。 */
    public static Item EARTH;
    /** 甜甜圈：5 饱食 6 饱和。 */
    public static Item DONUT;
    /** 奶油甜甜圈：6 饱食 9.5 饱和。 */
    public static Item CREAM_DONUT;
    /** 巧克力甜甜圈：8 饱食 12 饱和。 */
    public static Item CHOCOLATE_DONUT;
    /** 甜甜甜甜卷圈：13 饱食 20 饱和。 */
    public static Item SWEET_ROLL_RING;
    /** Chrome：6 饱食 8 饱和。 */
    public static Item CHROME;
    /** 洋葱圈：3 饱食 10 饱和。 */
    public static Item ONION_RINGS;
    /** 经验甜甜圈：1 饱食 22 饱和。 */
    public static Item EXPERIENCE_DONUT;
    /** 何意味：15 饱食 19 饱和 + 5 分钟滋养。 */
    public static Item WHAT_DOES_IT_MEAN;

    /** 所有护符；事件处理器按此列表逐个检查玩家是否「生效」。 */
    public static final List<CharmItem> ALL_CHARMS = new ArrayList<>();

    private MBGAItems() {
    }

    public static void register() {
        GEODE = register("geode", new Item(new Item.Settings()));
        RUBY = register("ruby", new Item(new Item.Settings()));
        TOPAZ = register("topaz", new Item(new Item.Settings()));
        SAPPHIRE = register("sapphire", new Item(new Item.Settings()));
        JADE = register("jade", new Item(new Item.Settings()));
        EMBER_METAL = register("ember_metal", new Item(new Item.Settings().fireproof()));
        EMBER_UPGRADE_SMITHING_TEMPLATE = register("ember_upgrade_smithing_template", createEmberUpgradeTemplate());

        RUBY_CHARM = registerCharm("ruby_charm", new RubyCharmItem(new Item.Settings().maxCount(1)));
        TOPAZ_CHARM = registerCharm("topaz_charm", new TopazCharmItem(new Item.Settings().maxCount(1)));
        SAPPHIRE_CHARM = registerCharm("sapphire_charm", new SapphireCharmItem(new Item.Settings().maxCount(1)));
        JADE_CHARM = registerCharm("jade_charm", new JadeCharmItem(new Item.Settings().maxCount(1)));
        BIOETHANOL_CHARM = registerCharm("bioethanol_charm", new BioethanolCharmItem(new Item.Settings().maxCount(1)));

        // 四把武器：提示伤害 7 / 6 / 5 / 8，攻速 1.6 / 1.8 / 2.2 / 1.8（剑 = 1.6，对应修饰符 -2.4）。
        TAP = register("tap", new MBGAWeaponItem(
                new WeaponMaterial(1200, createItem("asurine")), 7, -2.4F, MBGASounds.WEAPON_TAP,
                new Item.Settings()));
        FLICK = register("flick", new MBGAWeaponItem(
                new WeaponMaterial(1200, createItem("crimsite")), 6, -2.2F, MBGASounds.WEAPON_FLICK,
                new Item.Settings()));
        DRAG = register("drag", new MBGAWeaponItem(
                new WeaponMaterial(1200, createItem("ochrum")), 5, -1.8F, MBGASounds.WEAPON_DRAG,
                new Item.Settings()));
        HOLD = register("hold", new MBGAWeaponItem(
                new WeaponMaterial(1500, createItem("asurine")), 8, -2.2F, MBGASounds.WEAPON_TAP,
                new Item.Settings()));

        // 余烬装备 / 工具（数值高于下界合金：伤害 +2、耐久 +1000、护甲与韧性 +2；
        // 物品本身免疫火焰与熔岩，即 fireproof）。
        EMBER_SWORD = register("ember_sword", new EmberSwordItem(new Item.Settings().fireproof()));
        EMBER_PICKAXE = register("ember_pickaxe", new EmberPickaxeItem(new Item.Settings().fireproof()));
        EMBER_AXE = register("ember_axe", new EmberAxeItem(new Item.Settings().fireproof()));
        EMBER_SHOVEL = register("ember_shovel", new EmberShovelItem(new Item.Settings().fireproof()));
        EMBER_HOE = register("ember_hoe", new EmberHoeItem(new Item.Settings().fireproof()));
        EMBER_HELMET = register("ember_helmet",
                new EmberArmorItem(ArmorItem.Type.HELMET, new Item.Settings().maxCount(1).fireproof()));
        EMBER_CHESTPLATE = register("ember_chestplate",
                new EmberArmorItem(ArmorItem.Type.CHESTPLATE, new Item.Settings().maxCount(1).fireproof()));
        EMBER_LEGGINGS = register("ember_leggings",
                new EmberArmorItem(ArmorItem.Type.LEGGINGS, new Item.Settings().maxCount(1).fireproof()));
        EMBER_BOOTS = register("ember_boots",
                new EmberArmorItem(ArmorItem.Type.BOOTS, new Item.Settings().maxCount(1).fireproof()));

        // 序列组装中间产物。
        INCOMPLETE_SPACE_SUPERPRESSOR = register("incomplete_space_superpressor", new Item(new Item.Settings()));
        INCOMPLETE_DIAMOND = register("incomplete_diamond", new Item(new Item.Settings()));
        INCOMPLETE_GEMSTONE = register("incomplete_gemstone", new Item(new Item.Settings()));
        INCOMPLETE_BIOETHANOL_CHARM = register("incomplete_bioethanol_charm", new Item(new Item.Settings()));
        INCOMPLETE_ONION_RINGS = register("incomplete_onion_rings", new Item(new Item.Settings()));

        // 甜甜圈系列：食饱/饱和按任务给定数值换算（饱和 = 饱食 × 饱和度系数 × 2）。
        RING_DOUGH = register("ring_dough", new Item(new Item.Settings()));
        EARTH = register("earth", new Item(food(5, 5.0F)));
        DONUT = register("donut", new Item(food(5, 6.0F)));
        CREAM_DONUT = register("cream_donut", new Item(food(6, 9.5F)));
        CHOCOLATE_DONUT = register("chocolate_donut", new Item(food(8, 12.0F)));
        SWEET_ROLL_RING = register("sweet_roll_ring", new Item(food(13, 20.0F)));
        CHROME = register("chrome", new Item(food(6, 8.0F)));
        ONION_RINGS = register("onion_rings", new Item(food(3, 10.0F)));
        EXPERIENCE_DONUT = register("experience_donut", new Item(food(1, 22.0F)));
        WHAT_DOES_IT_MEAN = register("what_does_it_mean", new Item(nourishingFood(15, 19.0F)));
    }

    /** 按「饱食度 / 饱和度」构造食物属性（原版饱和 = 饱食 × 系数 × 2）。 */
    private static Item.Settings food(int hunger, float saturation) {
        return new Item.Settings().food(new FoodComponent.Builder()
                .hunger(hunger)
                .saturationModifier(saturation / (hunger * 2.0F))
                .build());
    }

    /** 何意味：额外给予农夫乐事的「滋养」效果 5 分钟。 */
    private static Item.Settings nourishingFood(int hunger, float saturation) {
        FoodComponent.Builder builder = new FoodComponent.Builder()
                .hunger(hunger)
                .saturationModifier(saturation / (hunger * 2.0F));
        StatusEffect nourishment = Registries.STATUS_EFFECT.get(new Identifier("farmersdelight", "nourishment"));
        if (nourishment != null) {
            builder.statusEffect(new StatusEffectInstance(nourishment, 5 * 60 * 20, 0), 1.0F);
        }
        return new Item.Settings().food(builder.build());
    }

    /**
     * 余烬升级锻造模板。与「下界合金升级」模板同构。
     */
    private static SmithingTemplateItem createEmberUpgradeTemplate() {
        return new EmberUpgradeTemplateItem(
                Text.translatable("item.mbga.smithing_template.ember_upgrade.applies_to").formatted(Formatting.BLUE),
                Text.translatable("item.mbga.smithing_template.ember_upgrade.ingredients").formatted(Formatting.BLUE),
                Text.translatable("upgrade.mbga.ember_upgrade").formatted(Formatting.GRAY),
                Text.translatable("item.mbga.smithing_template.ember_upgrade.base_slot_description"),
                Text.translatable("item.mbga.smithing_template.ember_upgrade.additions_slot_description"),
                List.of(
                        new Identifier("item/empty_armor_slot_helmet"),
                        new Identifier("item/empty_armor_slot_chestplate"),
                        new Identifier("item/empty_armor_slot_leggings"),
                        new Identifier("item/empty_armor_slot_boots"),
                        new Identifier("item/empty_slot_sword"),
                        new Identifier("item/empty_slot_pickaxe"),
                        new Identifier("item/empty_slot_axe"),
                        new Identifier("item/empty_slot_shovel"),
                        new Identifier("item/empty_slot_hoe")),
                List.of(new Identifier("item/empty_slot_ingot")));
    }

    /** 读取 Create 的物品（缺失时退化为铁锭，避免出现「空气」材料）。 */
    private static Item createItem(String path) {
        Item item = Registries.ITEM.get(new Identifier("create", path));
        return item == Items.AIR ? Items.IRON_INGOT : item;
    }

    private static Item register(String name, Item item) {
        return Registry.register(Registries.ITEM, new Identifier(MBGA.MOD_ID, name), item);
    }

    private static CharmItem registerCharm(String name, CharmItem charm) {
        register(name, charm);
        ALL_CHARMS.add(charm);
        return charm;
    }

    /** 已注册的护符（只读）。 */
    public static List<CharmItem> allCharms() {
        return Collections.unmodifiableList(ALL_CHARMS);
    }
    /**
     * 该物品是否属于「余烬系列」（本模组内 id 以 ember 开头的物品）。
     *
     * <p>用于让它们的掉落物免疫火焰与熔岩伤害，并让余烬装备的掉落物在火 / 熔岩中恢复耐久。
     */
    public static boolean isEmberSeries(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        Identifier id = Registries.ITEM.getId(stack.getItem());
        return MBGA.MOD_ID.equals(id.getNamespace()) && id.getPath().startsWith("ember");
    }}
