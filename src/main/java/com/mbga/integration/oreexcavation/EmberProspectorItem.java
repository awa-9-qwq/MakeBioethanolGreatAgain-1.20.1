package com.mbga.integration.oreexcavation;

import com.mojang.datafixers.util.Pair;
import com.tom.createores.Config;
import com.tom.createores.OreVeinGenerator;
import com.tom.createores.recipe.VeinRecipe;
import com.tom.createores.util.RandomSpreadGenerator;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 余烬探矿杖（{@code mbga:ember_prospector}）。
 *
 * <p>右键打印附近矿脉的<b>区块坐标</b>（照着坐标就能直接挖过去），并且<b>完全没有冷却</b>：
 * 既不调用 {@code player.getItemCooldownManager().set(...)}（原版矿脉探测器与下界合金探矿杖的冷却来源），
 * 也不使用任何原版 {@code use()} 冷却。
 *
 * <p>只用 Create Ore Excavation 的「分布生成」API：
 * {@link OreVeinGenerator#getPicker(ServerWorld)} +
 * {@link RandomSpreadGenerator#locate(BlockPos, ServerWorld, int, java.util.function.Predicate)}。
 * 反复用「排除已找到的矿脉」的过滤器调用 {@code locate}，即可在
 * {@link Config#veinFinderFar} 个区块范围内按距离由近到远列出若干条矿脉（最多 {@value #MAX_VEINS} 条）。
 * {@code distance2d} 返回的是<b>格</b>，除以 16 换算成区块；区块坐标由 {@code BlockPos >> 4} 得到。
 */
public class EmberProspectorItem extends Item {

    /** 一次最多报告多少条矿脉，避免无冷却右键被滥用。 */
    private static final int MAX_VEINS = 4;

    public EmberProspectorItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        // 只在服务端做事（矿脉生成数据只存在于服务端）。
        if (world.isClient) {
            return TypedActionResult.success(stack);
        }
        if (!(world instanceof ServerWorld) || !(user instanceof ServerPlayerEntity)) {
            return TypedActionResult.success(stack);
        }
        ServerWorld serverWorld = (ServerWorld) world;
        ServerPlayerEntity player = (ServerPlayerEntity) user;

        BlockPos origin = player.getBlockPos();

        // 当前区块所含的矿脉（OreVeinGenerator.pick 对每个区块是确定性的）。
        VeinRecipe currentVein = OreVeinGenerator.pick(serverWorld.getWorldChunk(origin));
        if (currentVein != null) {
            player.sendMessage(Text.translatable("message.mbga.ember_prospector.current",
                    origin.getX() >> 4, origin.getZ() >> 4,
                    NetheriteProspectorItem.veinName(currentVein))
                    .formatted(Formatting.YELLOW), false);
        }
        int radius = NetheriteProspectorItem.searchRadiusChunks();
        List<Pair<BlockPos, VeinRecipe>> veins = findNearbyVeins(serverWorld, origin, radius);

        if (veins.isEmpty()) {
            player.sendMessage(Text.translatable("message.mbga.ember_prospector.none")
                    .formatted(Formatting.GRAY), false);
        } else {
            player.sendMessage(Text.translatable("message.mbga.ember_prospector.header", veins.size())
                    .formatted(Formatting.GOLD), false);
            for (Pair<BlockPos, VeinRecipe> vein : veins) {
                BlockPos veinPos = vein.getFirst();
                int chunks = NetheriteProspectorItem.distanceInChunks(origin, veinPos);
                player.sendMessage(Text.translatable("message.mbga.ember_prospector.vein",
                        veinPos.getX() >> 4, veinPos.getZ() >> 4,
                        NetheriteProspectorItem.veinName(vein.getSecond()), chunks)
                        .formatted(Formatting.AQUA), false);
            }
            // 附带把最近的一条同步给 Create Ore Excavation 自己的数据包（旅程地图标记）。
            NetheriteProspectorItem.sendVeinInfo(player, origin, veins.get(0),
                    NetheriteProspectorItem.distanceInChunks(origin, veins.get(0).getFirst()));
        }

        // 注意：这里刻意不设置任何冷却（余烬探矿杖可以无限次右键）。
        return TypedActionResult.success(stack);
    }

    /**
     * 按距离由近到远找出范围内若干条不同的矿脉（最多 {@link #MAX_VEINS} 条）。
     *
     * <p>{@code locate} 的过滤器作用在 {@link VeinRecipe} 上，所以把已找到的矿脉 id 排除掉再调用一次，
     * 就会返回「次近的另一种矿脉」。同一区块坐标重复出现时立即停止，保证循环一定收敛。
     */
    private static List<Pair<BlockPos, VeinRecipe>> findNearbyVeins(ServerWorld world, BlockPos origin, int radius) {
        List<Pair<BlockPos, VeinRecipe>> result = new ArrayList<>(MAX_VEINS);
        RandomSpreadGenerator picker = OreVeinGenerator.getPicker(world);
        if (picker == null) {
            return result;
        }
        Set<Identifier> seenTypes = new HashSet<>();
        Set<String> seenChunks = new HashSet<>();
        for (int i = 0; i < MAX_VEINS; i++) {
            Pair<BlockPos, VeinRecipe> hit = picker.locate(origin, world, radius, recipe -> {
                if (recipe == null) {
                    return false;
                }
                Identifier id = recipe.getId();
                return id == null || !seenTypes.contains(id);
            });
            if (hit == null || hit.getFirst() == null) {
                break;
            }
            BlockPos veinPos = hit.getFirst();
            if (!seenChunks.add((veinPos.getX() >> 4) + ":" + (veinPos.getZ() >> 4))) {
                break;
            }
            VeinRecipe recipe = hit.getSecond();
            if (recipe != null && recipe.getId() != null) {
                seenTypes.add(recipe.getId());
            }
            result.add(hit);
        }
        return result;
    }
}
