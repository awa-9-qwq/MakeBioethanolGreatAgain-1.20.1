package com.mbga.integration.oreexcavation;

import com.mojang.datafixers.util.Pair;
import com.tom.createores.Config;
import com.tom.createores.OreVeinGenerator;
import com.tom.createores.network.NetworkHandler;
import com.tom.createores.network.OreVeinInfoPacket;
import com.tom.createores.recipe.VeinRecipe;
import com.tom.createores.util.RandomSpreadGenerator;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * 下界合金探矿杖（{@code mbga:netherite_prospector}）。
 *
 * <p>右键探测周围最近的 Create Ore Excavation 矿脉，并把「距离最近的矿脉还有多远」以<b>区块</b>为单位
 * 报告给玩家。工作方式与 Create Ore Excavation 自带的「矿脉探测器」一致：
 * 用 {@link OreVeinGenerator#getPicker(ServerWorld)} 取出分布生成器，再用
 * {@link RandomSpreadGenerator#locate(BlockPos, ServerWorld, int, java.util.function.Predicate)}
 * 在 {@link Config#veinFinderFar} 个区块范围内寻找最近的矿脉。{@code distance2d} 返回的是<b>格</b>，
 * 因此除以 16 换算成区块。
 *
 * <p>与余烬探矿杖的区别：本物品会像原版探测器那样进入 {@link Config#veinFinderCd} 冷却。
 */
public class NetheriteProspectorItem extends Item {

    /** Create Ore Excavation 中 1 区块 = 16 格。 */
    private static final float BLOCKS_PER_CHUNK = 16.0F;

    public NetheriteProspectorItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        // 只在服务端做事：客户端直接返回，避免 OreDataCapability 在客户端区块上抛异常。
        if (world.isClient) {
            return TypedActionResult.success(stack);
        }
        if (!(world instanceof ServerWorld) || !(user instanceof ServerPlayerEntity)) {
            return TypedActionResult.success(stack);
        }
        ServerWorld serverWorld = (ServerWorld) world;
        ServerPlayerEntity player = (ServerPlayerEntity) user;

        BlockPos origin = player.getBlockPos();
        int radius = searchRadiusChunks();
        Pair<BlockPos, VeinRecipe> found = findNearest(serverWorld, origin, radius);

        if (found == null) {
            player.sendMessage(Text.translatable("message.mbga.netherite_prospector.none", radius)
                    .formatted(Formatting.GRAY), false);
        } else {
            int chunks = distanceInChunks(origin, found.getFirst());
            player.sendMessage(Text.translatable("message.mbga.netherite_prospector.found",
                    veinName(found.getSecond()), chunks).formatted(Formatting.AQUA), false);
            sendVeinInfo(player, origin, found, chunks);
        }

        // 与原版矿脉探测器一致的冷却；余烬探矿杖刻意不调用这里。
        int cooldown = Config.veinFinderCd;
        if (cooldown > 0) {
            player.getItemCooldownManager().set(this, cooldown);
        }
        return TypedActionResult.success(stack);
    }

    /** 搜索半径（区块）。直接沿用 Create Ore Excavation 的配置值。 */
    static int searchRadiusChunks() {
        return Math.max(1, Config.veinFinderFar);
    }

    /** 找到范围内最近的矿脉；找不到时返回 {@code null}。 */
    static Pair<BlockPos, VeinRecipe> findNearest(ServerWorld world, BlockPos origin, int radiusChunks) {
        RandomSpreadGenerator picker = OreVeinGenerator.getPicker(world);
        if (picker == null) {
            return null;
        }
        return picker.locate(origin, world, radiusChunks, recipe -> recipe != null);
    }

    /** 两点之间的区块距离（{@code distance2d} 给的是格）。 */
    static int distanceInChunks(BlockPos origin, BlockPos veinPos) {
        float blocks = RandomSpreadGenerator.distance2d(origin, veinPos);
        return Math.max(0, Math.round(blocks / BLOCKS_PER_CHUNK));
    }

    /** 矿脉名称；数据缺失时退化为可翻译的占位文本（{@code VeinRecipe#getName} 可能为 null）。 */
    static Text veinName(VeinRecipe recipe) {
        if (recipe == null || recipe.getName() == null) {
            return Text.translatable("message.mbga.prospector.unknown_vein");
        }
        return recipe.getName();
    }

    /**
     * 附带把结果发给 Create Ore Excavation 自己的 {@code OreVeinInfoPacket}（旅程地图矿物标记）。
     * 这是锦上添花的功能，失败也绝不能影响上面已经发出的聊天消息。
     */
    static void sendVeinInfo(ServerPlayerEntity player, BlockPos origin, Pair<BlockPos, VeinRecipe> found, int chunks) {
        NbtCompound tag = new NbtCompound();
        tag.putInt("x", origin.getX());
        tag.putInt("z", origin.getZ());
        if (found != null && found.getSecond() != null) {
            Identifier id = found.getSecond().getId();
            if (id != null) {
                tag.putString("found", id.toString());
                tag.putString("nearby", id.toString());
                tag.putInt("dist", chunks);
            }
        }
        try {
            NetworkHandler.sendTo(player, new OreVeinInfoPacket(tag));
        } catch (RuntimeException ignored) {
            // 数据包只是额外效果（需要客户端装了对应的地图模组），忽略任何发送失败。
        }
    }
}
