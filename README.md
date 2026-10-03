# MBGA

一个 **Create Crafts & Additions (1.2.6)** 的附属模组，适用于 Minecraft **1.20.1** + Fabric Loader **0.17.2** + Create Fabric **0.5.1-j-build.1631+mc1.20.1**。

## 方块

| 方块 | ID | 机制 |
| --- | --- | --- |
| 空间超压器 | `space_superpressor` | 坚固的工业方块（抗爆 1200，需要镐）；也是多种「压块塑形」配方的原料 / 催化剂。 |
| 固态生物乙醇 | `solid_bioethanol` | **燃料**：可烧制 8192 个物品（`8192 × 200 = 1,638,400` tick）。 |
| 紫冰 | `purple_ice` | 类似蓝冰：透明、**完全不减速**（`slipperiness = 1 / 0.91`，使原版速度乘数 `slipperiness × 0.91` 恰好为 1.0）；与蓝冰一致，只有「精准采集」才会掉落。 |
| 强化深板岩 | `reinforced_deepslate` | 需要**钻石**级工具才会掉落（见下方「挖掘等级」）。 |
| 奇点块 | `singularity_block` | **无碰撞体积**；接触它的生物每 5 tick 受到 5 点虚空伤害并获得「缓慢Ⅱ」「失明」；需要**下界合金**级工具才会掉落，掉落物免疫火焰、爆炸、虚空伤害。鼓风机气流穿过它时触发「时移」。 |
| 发光圆石 | `glowing_cobblestone` | 15 级光源。 |
| 有机泥土 | `organic_dirt` | 泥土类方块（可用 1B 生物乙醇注液得到；时移可变成富饶泥土）。 |
| 富饶泥土 | `fertile_dirt` | 见下方「富饶泥土」。 |
| 固态石油 | `solid_oil` | **燃料**：可烧制 6400 个物品（`6400 × 200 = 1,280,000` tick）。 |
| 余烬金属块 | `ember_metal_block` | 3×3 余烬金属合成；用于「余烬升级」锻造模板配方，需要钻石级工具。 |

### 富饶泥土

- **不能被锄头耕成耕地**（本条机制已按要求删除：富饶泥土只作为「超级土壤」使用，锄头对它无效）。
- 可以**直接种植作物**而无需耕地：小麦 / 胡萝卜 / 马铃薯 / 甜菜（`CropBlock`）、南瓜 / 西瓜茎（`StemBlock`）、**甘蔗**（无需水源）、**海泡菜**、**仙人掌**都能直接种在富饶泥土上。
- **海泡菜与仙人掌正常生长**：仙人掌的生长逻辑本身不检查下方方块，富饶泥土只需通过 `CactusBlockMixin` 放行放置判定；海泡菜原本唯一的生长途径是骨粉，而原版要求下方是珊瑚块，`SeaPickleBlockMixin` 为「下方是富饶泥土」补上了分支（数量 +1，最多 4 个），富饶泥土每 10 tick 的催熟会让它们持续生长。
- **无视亮度**：在富饶泥土上种植作物不受亮度限制，且作物在黑暗中也会继续生长（`CropBlockMixin` / `StemBlockMixin` 把随机刻的亮度门槛替换掉）。
- **每 10 tick 有 50% 概率处理上方作物**：
  - 普通作物：优先使用原版骨粉逻辑（`Fertilizable#grow`）；无法催熟时退化为「直接增加生长进度」（`age` 属性 +1）；
  - **甘蔗：改为让它向上生长一格**（每 10 tick 50% 概率，最高 3 格，与原版一致）。

### 挖掘等级

- **强化深板岩**：只需要**钻石**级——加入原版 `minecraft:needs_diamond_tool` 标签，配合 `requiresTool()`。
- **奇点块**：需要**下界合金**级。原版 1.20.1 没有「下界合金」挖掘等级标签，因此它同时使用两套机制：
  1. 加入原版 `minecraft:needs_diamond_tool` 标签（钻石级起步、不使用正确工具不掉落）；
  2. 加入自定义标签 `mbga:needs_netherite_tool`，由 `MiningToolItemMixin` 在 `MiningToolItem#isSuitableFor` 上追加第四档判定：挖掘等级低于下界合金（4）的工具一律视为「不合适」，即不会掉落。

### 奇点块的接触判定

奇点块使用 `noCollision()`，但原版 `Entity#checkBlockCollision` 是按「实体碰撞箱覆盖到的方块坐标」回调 `onEntityCollision` 的，因此接触判定照常工作；同一实体每 5 tick 只结算一次（按世界刻记录）。

## 物品

| 物品 | ID | 说明 |
| --- | --- | --- |
| 晶洞 | `geode` | 材料；序列组装可产出宝石。 |
| 红宝石 | `ruby` | 材料 / 护符基底。 |
| 黄玉 | `topaz` | 材料 / 护符基底。 |
| 蓝宝石 | `sapphire` | 材料 / 护符基底。 |
| 翡翠 | `jade` | 材料 / 护符基底。 |
| 余烬金属 | `ember_metal` | 材料；锻造余烬装备的核心材料。 |
| 锻造模板（余烬升级） | `ember_upgrade_smithing_template` | 用「奇点 + 下界合金升级模板 + 余烬金属块」锻造得到，用于把下界合金装备升级为余烬装备。 |
| 红宝石护符 | `ruby_charm` | 免疫火焰伤害；每次免疫有 10% 概率恢复 1 点生命值。 |
| 黄玉护符 | `topaz_charm` | 免疫雷电伤害；持续获得「力量Ⅰ」。 |
| 蓝宝石护符 | `sapphire_charm` | 在水中时获得「潮涌能量」。 |
| 翡翠护符 | `jade_charm` | 持续获得「村庄英雄」；受伤时有 25% 概率完全免疫并获得 10 秒「生命恢复Ⅰ」。 |
| 生物乙醇护符 | `bioethanol_charm` | 消除「醉酒」状态；持有「超级燃烧」时额外获得「生命恢复Ⅰ」「抗性提升Ⅰ」「力量Ⅰ」「跳跃提升Ⅰ」。 |
| Tap / Flick / Drag / Hold | `tap` `flick` `drag` `hold` | 四把武器，各自独特的命中音效（Hold 与 Tap 同音效）。 |
| 余烬剑 / 镐 / 斧 / 锹 / 锄 | `ember_sword` `ember_pickaxe` `ember_axe` `ember_shovel` `ember_hoe` | 余烬工具（数值与下界合金同级）。 |
| 余烬头盔 / 胸甲 / 护腿 / 靴子 | `ember_helmet` `ember_chestplate` `ember_leggings` `ember_boots` | 余烬盔甲（护甲 3/8/6/3、韧性 3、击退抗性 0.1）。 |
| 半成品 | `incomplete_space_superpressor` `incomplete_diamond` `incomplete_gemstone` `incomplete_bioethanol_charm` | 序列组装的中间产物。 |

（原有物品：生物乙醇茶饮、喷溅式生物乙醇、瓶中火、火箭、奇点。）

### 护符

- **生效位置：主手、副手、物品栏右下角**（快捷栏第 9 格，槽位索引 `8`）。
- 护符可以**完全免疫**对应伤害：被护符挡下的伤害不会结算（不扣血、不触发受击效果），**也不会**触发「超级燃烧 → 爆燃」的转换。
- 翡翠护符的 25% 闪避对「无敌类」伤害（`minecraft:bypasses_invulnerability`，例如 `/kill`、虚空伤害）不生效。
- 护符堆叠上限为 1。

### 余烬装备 / 工具

- 工具提示里有**两行橙色（GOLD）说明**：
  - **弑主**：装备者每损失 **5** 点生命值，恢复 1 点耐久（不足 5 点的损失会累积）；
  - **重铸**：装备者在火或熔岩中时，每 tick 恢复 1 点耐久。
- 结算在 `com.mbga.event.EmberGearHandler`，只对**装备中**的余烬物品生效（主手 / 副手 / 四个盔甲槽）。
- 数值**高于**下界合金：工具提示伤害 +2（剑 9 / 镐 7 / 斧 11 / 锹 7.5 / 锄 2）、耐久 +1000（3031）、盔甲护甲与韧性各 +2（护甲 5/10/8/5、韧性 5.0、击退抗性 0.1）；物品本身免疫火焰与熔岩（`fireproof`），但**玩家不会**因此免疫火焰伤害。
- 盔甲贴图使用自绘的 `assets/minecraft/textures/models/armor/mbga_ember_layer_1.png` / `_layer_2.png`（`ArmorMaterial#getName()` 返回 `mbga_ember`，因此在 `assets/minecraft/` 命名空间下）；物品图标同样使用自绘贴图。

### 武器 Tap / Flick / Drag / Hold

| 武器 | 提示攻击伤害 | 攻速 | 命中音效 |
| --- | --- | --- | --- |
| Tap | 7 | 1.6（与剑相同） | `mbga:weapon.tap` |
| Flick | 6 | 1.8（略快于剑） | `mbga:weapon.flick` |
| Drag | 5 | 2.2（更快于剑） | `mbga:weapon.drag` |
| Hold | 8 | 1.8（略快于剑） | `mbga:weapon.tap`（与 Tap 一致） |

- 「提示攻击伤害」指物品栏里显示的攻击伤害数值（玩家实际命中伤害会再加上 1 点基础伤害，与剑一致）。
- 音效在命中时播放（`postHit`，服务端广播）。音频文件来自 `libs/`，已转换为 OGG Vorbis 并放入 `assets/mbga/sounds/weapon/{tap,flick,drag}.ogg`，由 `assets/mbga/sounds.json` 声明。

## 新增加工方式：时移

- **鼓风机气流穿过奇点块时处理物品**，效果类似批量熔炼：把鼓风机对着奇点块，气流中的掉落物、传送带 / 置物台上的物品都会被「时移」处理。
- 实现方式：`TimeShiftProcessingType implements FanProcessingType`，通过 Create 的 `FanProcessingTypeRegistry` 注册为 `mbga:time_shift`（优先级 **250**——Create 的烟熏/高炉/haunting/洗涤分别是 200/100/300/400，而该注册表**不允许优先级重复**，重复会抛异常）。
- 时移配方是 MBGA 自己的配方类型 `mbga:time_shift`（`TimeShiftRecipe` + `TimeShiftRecipeSerializer`），JSON 与 Create 的处理配方同构：

```json
{
  "type": "mbga:time_shift",
  "ingredients": [ { "item": "mbga:glowing_cobblestone" } ],
  "results": [ { "item": "minecraft:glowstone" } ]
}
```

`results` 支持 `count` 与 `chance`（0~1 小数，与 Create 的研磨 / 序列组装一致）。

## 效果

- **超级燃烧**（正面效果，蓝紫色粒子）：饮用后获得 4 分钟；持续期间给予隐藏的「速度Ⅱ」「急迫Ⅱ」「夜视」。
- **醉酒**：持有「超级燃烧」时再次饮用，变为「醉酒」（时长为剩余超级燃烧时长的一半）；持续期间给予隐藏的「反胃Ⅱ」「缓慢Ⅰ」。
- **酒精中毒**：持有「醉酒」时再次饮用直接死亡（死于酒精中毒）。
- **爆燃**：持有「超级燃烧」时受到火焰伤害，会将「超级燃烧」转换成「爆燃」；持续期间给予隐藏的「缓慢Ⅰ」，且一旦被点燃就无法熄灭。
- **烈焰人燃烧室**：持有至少 1 分钟「超级燃烧」的玩家，**空手**右击烈焰人燃烧室：未沸腾时通过 Create 公开 API（`BlazeBurnerBlock.tryInsert`）喂一块烈焰蛋糕使其进入沸腾状态；已沸腾时通过 Mixin 增加 1 分钟燃烧时长（每次转移消耗 1 分钟超级燃烧）。

## 配方

### 原有配方

- **生物乙醇茶饮**：注液——玻璃瓶 + 200 mB 生物乙醇（`createaddition:bioethanol`）。
- **喷溅式生物乙醇**：混合搅拌——生物乙醇茶饮 + 火药。
- **瓶中火**：无序合成——喷溅式生物乙醇 + 打火石（消耗 1 点耐久）。
- **火箭**：有序合成——8 支箭围绕 1 个瓶中火。

### 新增配方（对应新任务清单）

| # | 类型 | 内容 |
| --- | --- | --- |
| 1 | 序列组装 | 潜影盒为基底，机械手（潜影盒）+ 辊压，重复 3 次 → 空间超压器 |
| 2 | 序列组装 | 煤炭 / 木炭（`minecraft:coals` 标签）为基底，注液 1 mB 生物乙醇 + 辊压，重复 **1000** 次 → 钻石 |
| 3 | 时移 | 发光圆石 → 萤石 |
| 4 | 研磨 | 发光圆石 → 圆石 + 萤石粉 + 萤石粉（10%） |
| 5 | 压块塑形 | 空间超压器×9 → 奇点 + 空间超压器 |
| 6 | 压块塑形 | 空间超压器 + 1B 生物乙醇 → 固态生物乙醇 + 空间超压器 |
| 7 | 压块塑形 | 空间超压器 + 1B 水 → 蓝冰 + 空间超压器 |
| 8 | 压块塑形 | 空间超压器 + 蓝冰×8 → 紫冰 + 空间超压器 |
| 9 | 压块塑形 | 空间超压器 + 深板岩×8 → 强化深板岩 + 空间超压器 |
| 10 | 机械手使用 | 圆石 + 发光浆果 → 发光圆石 |
| 11 | 压块塑形 | 空间超压器 + 紫水晶块×8 → 晶洞 + 空间超压器 |
| 12 | 序列组装 | 晶洞为基底，注液 100 mB 生物乙醇 + 辊压，重复 5 次 → 红宝石 2% / 黄玉 2% / 蓝宝石 2% / 翡翠 2% |
| 13 | 时移 | 粗矿块 / 矿石（铁 / 铜 / 金，含深板岩变种与下界金矿）→ 对应**矿石方块** ×3 |
| 14 | 注液 | 泥土 + 1B 生物乙醇 → 有机泥土 |
| 15 | 时移 | 有机泥土 → 富饶泥土 |
| 16 | 序列组装 | 坚固板为基底，依次用机械手使用四种护符 + 注液 1B 生物乙醇 → 生物乙醇护符 |
| 17 | 时移 | 固态生物乙醇 → 固态石油 |
| 18 | 压块塑形 | 固态石油×3 + 瓶中火 + 下界合金锭，**需要超级加热** → 余烬金属 |
| 19 | 有序合成 | 2×2 奇点 → 奇点块；3×3 余烬金属 → 余烬金属块；皓蓝石/绯红岩/赭金砂 ×2 + 木棍（竖排）→ Tap / Flick / Drag；皓蓝石 ×3（竖排）→ Hold |
| 20 | 锻造 | 奇点 + {四种宝石} + 光辉石 → 对应宝石护符；奇点 + 下界合金升级模板 + 余烬金属块 → 余烬升级模板；余烬升级模板 + {下界合金装备/工具} + 余烬金属 → 对应余烬装备/工具 |

> **Create 的流体单位**：配方的 `amount` 并不是 mB，而是 **mB × 81**（对照 Create 自带数据：250 mB 牛奶写作 `20250`，一瓶药水写作 `2025`）。本模组所有含流体的配方都按此换算：1000 mB → `81000`、250 mB → `20250`、100 mB → `8100`、10 mB → `810`、1 mB → `81`。
>
> **已知偏差（Create 引擎限制）**：Create 的盆配方（压块塑形 / 混合搅拌）**最多只能写 9 个物品原料，并且不支持 `count`**（`Ingredient` 不含数量）。因此配方 8 / 9 / 11 按「保留空间超压器作为原料、材料数量降为 8」实现；配方 5 使用 9 个空间超压器（上限）。其余条目均按原文实现。
>
> 配方 2 的 1000 次循环是任务原文要求（每次循环 = 一次注液 + 一次辊压），实际游戏内会非常耗时。

## 第三轮更新（甜甜圈 / 联动 / 风车 / 进度）

### 修复

- **批量时移**：`TimeShiftProcessingType#process` 现在按<b>输入堆叠数量</b>逐个结算配方（并合并同类产出），一整叠原料不再只产出一份。
- **流体量**：所有含流体的配方按 Create 的真实单位（mB × 81）重写，详见上方「Create 的流体单位」。
- **配方 13** 的产出改为对应的**矿石方块** ×3。

### 新增食物（10 种）

| 物品 | ID | 饱食 / 饱和 | 获取方式 |
| --- | --- | --- | --- |
| 环形面团 | `ring_dough` | — | 8 个面团围一圈 → 8 个 |
| “地球” | `earth` | 5 / 5 | 机械手：环形面团 + 草方块 |
| 甜甜圈 | `donut` | 5 / 6 | **烟熏**环形面团（烟熏炉，或鼓风机**批量烟熏**） |
| 奶油甜甜圈 | `cream_donut` | 6 / 9.5 | 注液：甜甜圈 + 250 mB 牛奶 |
| 巧克力甜甜圈 | `chocolate_donut` | 8 / 12 | 注液：甜甜圈 + 250 mB 巧克力 |
| 甜甜甜甜卷圈 | `sweet_roll_ring` | 13 / 20 | 机械手：甜甜圈 + 甜卷（`create:sweet_roll`） |
| Chrome | `chrome` | 6 / 8 | 混合搅拌：甜甜圈 + 红/黄/蓝/绿/白染料 |
| 洋葱圈 | `onion_rings` | 3 / 10 | 序列组装：甜甜圈 → 注液 10 mB 种子油 → 机械手（洋葱）→ 动力锯，产出 7 个 |
| 经验甜甜圈 | `experience_donut` | 1 / 22 | 注液：甜甜圈 + 250 mB 液态经验（附魔工业） |
| 何意味 | `what_does_it_mean` | 15 / 19 + 5 分钟滋养 | 农夫乐事厨锅：意面 + 洋葱 + 肉 + 番茄酱 |

### 联动内容

- **余烬砧板**（农夫乐事）：放置在砧板上的**余烬装备每秒恢复 5 点耐久**；砧板会**自动烧制**放在上面的生食物（按原版熔炉配方，每 200 tick 处理 1 个）。
- **余烬小刀**（农夫乐事）：`KnifeItem` 子类，使用余烬材质（耐久 3031、挖掘等级 4、`fireproof`），并加入 `c:tools/knives` 标签。
- **下界合金探矿杖**（矿石开掘）：锻造（下界合金升级模板 + `createoreexcavation:vein_finder` + 下界合金锭）；使用后显示最近矿脉的名称与**区块距离**（保留原版探矿杖的冷却）。
- **余烬探矿杖**（矿石开掘）：锻造（余烬升级模板 + 下界合金探矿杖 + 余烬金属）；使用后列出附近最多 4 条矿脉的**区块坐标**与距离，**无任何冷却**。
- **JEI 时移配方支持**：通过 `jei_mod_plugin` 客户端入口注册 `mbga:time_shift` 配方类别（输入 → 多个带概率的产出），催化剂为奇点块。JEI 为**可选**依赖（`recommends`），专用服务器不会加载该入口。
- **经验甜甜圈**（附魔工业）：见上表。

### 新增应力源（6 档风车，均为完整方块）

与创造马达同类的应力源：**传动杆从 `FACING` 面接出**（旋转轴 = `FACING` 的轴），朝向可以用**扳手**像其他机械动力装置一样旋转（见下）。`calculateAddedStressCapacity()` 返回的是「每 RPM 的 SU」，因此网络总应力 = 每 RPM 应力 × 转速。

朝向与旋转方向：

- **放置**：优先与相邻的动能方块对接（`getPreferredFacing`），与创造电机一致。
- **扳手右键 = 旋转朝向**：`WindmillBlock` 继承 `DirectionalKineticBlock`（带 `FACING`），Create 的 `IWrenchable#onWrenched` 默认实现会按「点击的那个面」决定绕哪根轴转 90°，并在旋转后把发电者重新激活（`GeneratingKineticBlockEntity#reActivateSource`）。**潜行 + 扳手**则是拆下（按战利品表掉落），这也是 Create 的通用行为。
- **空手（或非扳手物品）右键 = 只翻转旋转方向**：切换自定义的 `REVERSED` 状态，不改转速、不改朝向。
- **模型约定**：贴图里的**底面**（`windmill_bottom`，接口那张）永远朝着 `FACING`——也就是接传动杆的那一面看到的一定是接口贴图，叶片（`_top`）在相反的一面。blockstate 因此用的是「模型底面朝向 FACING」的旋转（原版 `end_rod` 那套映射取反），由 `.mbga-tools/gen-windmill-blockstates.ps1` 生成 12 个变体。

| 方块 | ID | 转速 | 总应力（SU） | 每 RPM | 配方 |
| --- | --- | --- | --- | --- | --- |
| 小型风车 | `small_windmill` | 16 | 8,192 | 512 | 序列组装：风车轴承 + 羊毛 ×128 |
| 大型风车 | `large_windmill` | 256 | 1,048,576 | 4,096 | 小型风车 + 小型风车 ×127 |
| 风车组 | `windmill_group` | 256 | 134,217,728 | 524,288 | 大型风车 ×2 ×127 |
| 中型风车组 | `medium_windmill_group` | 256 | 17,179,869,184 | 67,108,864 | 风车组 ×2 ×127 |
| 大型风车组 | `large_windmill_group` | 256 | 2,199,023,255,552 | 8,589,934,592 | 中型风车组 ×2 ×127 |
| 巨型风车组 | `giant_windmill_group` | 256 | 281,474,976,710,656 | 1,099,511,627,776 | 大型风车组 ×2 ×127 |

### 进度 / 成就（共 25 个）

- **进度 9 个**（普通）：燃起来了、那是烈焰人吗、精确定位（下界合金探矿杖）、燃烧瓶！、“火箭”、芜湖起飞、沃土plus++、甜甜甜甜甜！、精确定位（余烬探矿杖）。
- **隐藏进度 8 个**：给你一半、赤壁之战、何意味、飞龙一般的、Microsoft、地球明明是甜甜圈形的、咕噜咕噜、Firefox。
- **成就 4 个**（challenge 边框）：我纵茕茕孑立…、还要啥创造马达啊、永世不竭、躬耕陇亩。
- **隐藏成就 4 个**（challenge + 隐藏）：超越瞬间的极限、迷失、目不能追，耳未可及、欲火焚身。
- 物品 / 状态效果类的进度完全由数据包触发；需要代码判定的（右键转移超级燃烧、火箭射船、狐狸爆燃、效率Ⅵ挖黑曜石、吃奇点 / 被时移、340 m/s、全套余烬挨火焰伤害）由 `MBGAAdvancementHandler` 调用 `grantCriterion` 授予（criteria 使用 `minecraft:impossible`，名字为 `code`）。

## 第四轮修订（贴图 / 渲染 / 配方）

- **修复：富饶泥土放置后不渲染**（表现为「贴图缺失」）。`FertileDirtBlock` 继承自 `BlockWithEntity`，而后者把渲染类型写死成 `BlockRenderType.INVISIBLE`（其设计前提是「外观交给方块实体渲染器」）。现在显式覆盖 `getRenderType()` 返回 `BlockRenderType.MODEL`。物品栏里的图标一直正常，所以这个 bug 只在**放置到世界后**出现。
- **修复：6 档风车放置后是紫黑缺失模型**，与上一条同一类问题但成因不同——`WindmillBlock` 原本继承 Create 的 `DirectionalKineticBlock`（自带 6 向 `FACING`）并额外声明了 `REVERSED`，方块状态因此有 `6 × 2 = 12` 种组合，而 blockstate 里只写了 `""`：变体匹配是**精确匹配**，`""` 匹配不到任何真实状态，于是所有风车都落到「缺失模型」上。
- **风车输出面 / 朝向（最终形态）**：`WindmillBlock` 继承 Create 的 `DirectionalKineticBlock`，传动杆从 `FACING` 面接出、旋转轴 = `FACING` 的轴，朝向可用扳手旋转（`IWrenchable` 默认行为）；模型方面约定「**贴图的底面**（`windmill_bottom`）朝向 `FACING`」，所以 blockstate 用「模型底面朝向 FACING」的旋转（原版 `end_rod` 映射取反），共 `6 × 2 = 12` 个变体，由 `.mbga-tools/gen-windmill-blockstates.ps1` 生成。
  > 中间曾短暂改成「固定底面出力、没有朝向」，随后按要求回滚为带 `FACING` 的版本。
- **甜甜圈改为批量烟熏**：配方由 `data/mbga/recipes/smelting/donut.json`（`minecraft:smelting`）改为 `data/mbga/recipes/smoking/donut.json`（`minecraft:smoking`，`cookingtime` 100）。Create 的**批量熔炼**鼓风机认的是 `minecraft:smelting` / `minecraft:blasting`（见 `AllFanProcessingTypes$BlastingType`），**批量烟熏**只认 `minecraft:smoking`，所以原来的配方会被鼓风机当熔炼处理。改动后：烟熏炉 / 鼓风机批量烟熏可产出甜甜圈，普通熔炉不再产出（避免同时存在两种配方时又被鼓风机按熔炼处理）。
- **分面贴图**：贴图名带 `_top` / `_side` / `_bottom` 的方块改用 `minecraft:block/cube_bottom_top`——强化深板岩 3 面各自独立；6 档风车各有自己的 `_top` / `_side`，底面共用 `windmill_bottom`。
- **半成品贴图复用基底材料**：`incomplete_*` 不再需要单独画图，`layer0` 直接指向配方基底材料的贴图（潜影盒 / 煤炭 / 晶洞 / 坚固板 / 甜甜圈 / 风车轴承）。

## 构建

需要 JDK 17 与 Fabric 开发环境（Gradle + Fabric Loom，本仓库已配置 `fabric-loom 1.10.5` / `Gradle 8.14`）。

Create Fabric 0.5.1-j（1.20.1）没有可靠的公共 Maven 源，因此本仓库直接使用 `libs/create-fabric-0.5.1-j-build.1631+mc1.20.1.jar` 作为编译依赖（`build.gradle` 中通过 `modImplementation files(...)` 引入，Loom 会自动把它从 intermediary 重映射到 yarn）。

```bash
./gradlew build
```

产物位于 `build/libs/mbga-1.0.0.jar`。

## 说明

- 运行时依赖（均已声明在 `src/main/resources/fabric.mod.json`）：`create`（>=0.5.1-j-build.1631）、`createaddition`（>=1.2.6），以及第三轮联动所需的 `farmersdelight`（>=1.20.1-2.4.0）、`createoreexcavation`（>=1.5.4）、`create_enchantment_industry`（>=1.2.16）——这三者属于硬依赖；**JEI**（>=15.20.0）为可选（`recommends`，仅在客户端加载 `jei_mod_plugin` 入口）。配方还引用了 Create 的 `create:asurine` / `create:crimsite` / `create:ochrum` / `create:refined_radiance` / `create:sturdy_sheet` / `create:sweet_roll` / `create:chocolate`。
- 编译期依赖 Create 通过 `libs/` 下的本地 jar 提供；如需更换版本，请替换 `libs/` 中的 jar 并同步修改 `gradle.properties` 里的 `create_version`。
- **贴图状态**（可用 `.mbga-tools/audit-textures.ps1` 复核）：物品贴图、方块贴图（含奇点块的 `assets/mbga/textures/block/singularity_block.png`）、3 张状态效果图标（`textures/mob_effect/`）、2 张余烬盔甲层（`assets/minecraft/textures/models/armor/mbga_ember_layer_*.png`）**均已就位，没有缺失的贴图引用**。约定：
  - 贴图名带 `_top` / `_side` / `_bottom` 的方块使用 `minecraft:block/cube_bottom_top` 分面模型（强化深板岩 3 面；6 档风车各有 `_top` / `_side`，底面共用 `windmill_bottom`）；6 档风车因为只从底面出力，模型不做任何旋转，blockstate 只有 `REVERSED` 两个变体（由 `.mbga-tools/gen-windmill-blockstates.ps1` 生成）；
  - 序列组装半成品（`incomplete_*`）**不单独画贴图**，直接复用配方基底材料的贴图（潜影盒 / 煤炭 / 晶洞 / 坚固板 / 甜甜圈 / 风车轴承）；
  - 画好新贴图后运行 `.mbga-tools/wire-textures.ps1` 会自动把模型指到 `mbga:` 路径。细节见 [`TEXTURES.md`](TEXTURES.md)。
- **燃烧时长的原版限制**：原版熔炉把 `BurnTime` 以 short 存进 NBT，因此超过 32767 的燃烧时长（固态生物乙醇 1,638,400 tick、固态石油 1,280,000 tick）在存档 / 重新加载后会被截断；Fabric 的 `FuelRegistry` 也会为超过 32767 的值打印警告。
- **`mbga:singularity` 伤害类型**加入了 `minecraft:bypasses_armor`、`bypasses_invulnerability`、`bypasses_resistance` 三个伤害类型标签（与「虚空伤害」`out_of_world` 一致）。
- Create 在加载时会为流体量正好为 1000 的配方打印 `Suspicious fluid amount` 警告（本模组的注液 / 压块配方按任务原文使用 1B = 1000 mB），这只是提示，不影响配方生效。
- Mixin 列表（`src/main/resources/mbga.mixins.json`）：
  - `BlazeBurnerBlockEntityMixin`：向 Create 的 `BlazeBurnerBlockEntity` 注入 `mbga$addBurnTime(int)`；
  - `ItemEntityMixin`：奇点 / 奇点块掉落物免疫爆炸与虚空，奇点掉落物不会自然消失；
  - `MiningToolItemMixin`：为 `mbga:needs_netherite_tool` 标签补上「下界合金」挖掘等级；
  - `CropBlockMixin` / `StemBlockMixin`：允许作物直接种在富饶泥土上并忽略亮度要求；
  - `SugarCaneBlockMixin`：允许甘蔗直接种在富饶泥土上而无需水源；
  - `CactusBlockMixin`：允许仙人掌种在富饶泥土上并正常生长；
  - `SeaPickleBlockMixin`：让海泡菜能在富饶泥土上生长。
- `.mbga-tools/` 下是开发辅助脚本（仅在工作区内运行，不影响构建产物）：`compile.ps1`（受限环境下 Gradle 无法写 `~/.gradle` 时用 javac 做快速编译自检）、`gen-resources.ps1` / `gen-resources2.ps1`（生成方块状态 / 模型 / 战利品表 / 标签 / 配方的脚本）、`gen-windmill-blockstates.ps1`（生成 6 档风车的 `FACING × REVERSED` 共 12 个 blockstate 变体）、`wire-textures.ps1`（贴图放好后把模型 JSON 的贴图键自动指向 `mbga:` 路径）、`audit-textures.ps1`（只读审计：JSON 可解析性、贴图引用是否存在、blockstate 变体是否覆盖全部状态、有没有白画的贴图、还有哪些模型在用外部占位贴图）。
