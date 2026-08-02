package net.shadowmage.ancientwarfare.automation.registry;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.MushroomBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.shadowmage.ancientwarfare.automation.init.AWAutomationBlocks;
import net.shadowmage.ancientwarfare.automation.tile.worksite.treefarm.*;
import net.shadowmage.ancientwarfare.core.compat.LegacyMaterial;
import net.shadowmage.ancientwarfare.core.registry.IRegistryDataParser;
import net.shadowmage.ancientwarfare.core.util.JsonUtils;
import net.shadowmage.ancientwarfare.core.util.parsing.BlockStateMatcher;
import net.shadowmage.ancientwarfare.core.util.parsing.JsonHelper;

import java.util.*;
import java.util.function.Predicate;

public class TreeFarmRegistry {
    private TreeFarmRegistry() {
    }

    private static final ITreeScanner DEFAULT_TREE_SCANNER =
            new DefaultTreeScanner(st -> LegacyMaterial.of(st) == LegacyMaterial.WOOD && st.getBlock() != AWAutomationBlocks.TREE_FARM
                    , sl -> LegacyMaterial.of(sl) == LegacyMaterial.LEAVES);

    private static Set<ISapling> saplings = new HashSet<>();
    private static Set<BlockStateMatcher> soilBlocks = new HashSet<>();
    private static Set<IBlockExtraDrop> extraBlockDrops = new HashSet<>();
    private static List<ITreeScanner> treeScanners = new ArrayList<>();

    static {
        saplings.add(new Sapling(s -> s.getItem() instanceof BlockItem && ((BlockItem) s.getItem()).getBlock() instanceof SaplingBlock, false));
        saplings.add(new Sapling(s -> s.getItem() instanceof BlockItem && ((BlockItem) s.getItem()).getBlock() instanceof MushroomBlock, false));

        extraBlockDrops.add(new ChorusFlowerDrop());
    }

    private static void registerTreeScanner(ITreeScanner treeScanner) {
        treeScanners.add(0, treeScanner);
    }

    static {
        registerTreeScanner(new ChorusScanner());
    }

    public static ITreeScanner getTreeScanner(BlockState state) {
        return getRegisteredTreeScanner(state).orElse(DEFAULT_TREE_SCANNER);
    }

    public static Optional<ITreeScanner> getRegisteredTreeScanner(BlockState state) {
        return treeScanners.stream().filter(ts -> ts.matches(state)).findFirst();
    }

    public static Optional<ISapling> getSapling(ItemStack stack) {
        return saplings.stream().filter(s -> s.matches(stack)).findFirst();
    }

    public static boolean isPlantable(ItemStack stack) {
        return saplings.stream().anyMatch(s -> s.matches(stack));
    }

    public static boolean isSoil(BlockState state) {
        return soilBlocks.stream().anyMatch(m -> m.test(state));
    }

    public static IBlockExtraDrop getBlockExtraDrop(BlockState state) {
        return extraBlockDrops.stream().filter(b -> b.matches(state)).findFirst().orElse(EMPTY_EXTRA_DROP);
    }

    public static class PlantableParser implements IRegistryDataParser {

        @Override
        public String getName() {
            return "saplings";
        }

        @Override
        public void parse(JsonObject json) {
            JsonArray saplings = JsonUtils.getJsonArray(json, "saplings");

            for (JsonElement e : saplings) {
                JsonObject saplingDefinition = JsonUtils.getJsonObject(e, "");
                TreeFarmRegistry.saplings.add(new Sapling(JsonHelper.getItemStackMatcher(JsonUtils.getJsonObject(saplingDefinition, "sapling")),
                        saplingDefinition.has("right_click") && JsonUtils.getBoolean(saplingDefinition, "right_click")));
            }
        }
    }

    public static class SoilParser implements IRegistryDataParser {

        @Override
        public String getName() {
            return "tree_soil_blocks";
        }

        @Override
        public void parse(JsonObject json) {
            JsonArray soils = JsonUtils.getJsonArray(json, "soils");

            for (JsonElement t : soils) {
                soilBlocks.add(JsonHelper.getBlockStateMatcher(JsonUtils.getJsonObject(t, "")));
            }
        }
    }

    public static class TreeScannerParser implements IRegistryDataParser {
        @Override
        public String getName() {
            return "tree_scanners";
        }

        @Override
        public void parse(JsonObject json) {
            JsonArray treeScanners = JsonUtils.getJsonArray(json, "tree_scanners");

            for (JsonElement ts : treeScanners) {
                JsonObject treeScanner = JsonUtils.getJsonObject(ts, "");
                switch (JsonUtils.getString(treeScanner, "type")) {
                    case "default":
                    default:
                        DefaultSearchParser.parse(treeScanner);
                }
            }
        }

        private static class DefaultSearchParser {
            private DefaultSearchParser() {
            }

            public static void parse(JsonObject treeScanner) {
                Predicate<BlockState> trunkMatcher = JsonHelper.getBlockStateMatcher(treeScanner, "trunks", "trunk");
                Predicate<BlockState> leafMatcher = JsonHelper.getBlockStateMatcher(treeScanner, "leaves", "leaf");

                int maxLeafDistance = JsonUtils.getInt(treeScanner, "max_leaf_distance");

                DefaultTreeScanner.INextPositionGetter nextPosGetter = parseNextPositionGetter(treeScanner);

                Optional<DefaultTreeScanner> currentScanner = treeScanners.stream().filter(m -> m instanceof DefaultTreeScanner && ((DefaultTreeScanner) m).getTrunkMatcher().hashCode() == trunkMatcher.hashCode()).map(m -> (DefaultTreeScanner) m).findFirst();

                if (currentScanner.isPresent()) {
                    currentScanner.get().addLeafMatcher(leafMatcher);
                } else {
                    registerTreeScanner(new DefaultTreeScanner(trunkMatcher, leafMatcher, nextPosGetter, maxLeafDistance));
                }
            }

            private static DefaultTreeScanner.INextPositionGetter parseNextPositionGetter(JsonObject treeScanner) {
                switch (JsonUtils.getString(treeScanner, "next_block_search")) {
                    case "all_up_or_level":
                        return DefaultTreeScanner.ALL_UP_OR_LEVEL;
                    case "all_around":
                        return DefaultTreeScanner.ALL_AROUND;
                    case "connected_around":
                        return DefaultTreeScanner.CONNECTED_AROUND;
                    case "connected_down_or_level":
                        return DefaultTreeScanner.CONNECTED_DOWN_OR_LEVEL;
                    case "connected_up_or_level":
                    default:
                        return DefaultTreeScanner.CONNECTED_UP_OR_LEVEL;
                }
            }
        }
    }

    private static final IBlockExtraDrop EMPTY_EXTRA_DROP = new IBlockExtraDrop() {
        @Override
        public boolean matches(BlockState state) {
            return true;
        }

        @Override
        public NonNullList<ItemStack> getDrops(BlockGetter world, BlockPos pos, BlockState state, int fortune) {
            return NonNullList.create();
        }
    };
}
