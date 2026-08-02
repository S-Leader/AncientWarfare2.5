package net.shadowmage.ancientwarfare.structure.registry;

import com.google.gson.JsonObject;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.shadowmage.ancientwarfare.core.registry.IRegistryDataParser;
import net.shadowmage.ancientwarfare.core.util.JsonUtils;
import net.shadowmage.ancientwarfare.core.util.parsing.BlockStateMatcher;
import net.shadowmage.ancientwarfare.core.util.parsing.JsonHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class StructureBlockRegistry {
    private StructureBlockRegistry() {
    }

    private static final Map<BlockStateMatcher, ItemStack> STATE_TO_ITEM = new HashMap<>();
    private static final Map<BlockStateMatcher, ItemStack> STATE_TO_REMAINING_ITEM = new HashMap<>();
    private static final Map<BlockStateMatcher, Integer> STATE_PASS = new HashMap<>();

    @SuppressWarnings({"squid:CallToDeprecatedMethod", "squid:S4449"})
    //null passed in intentionally and any exceptions caused by that are caught upstream
    public static Optional<ItemStack> getItemStackFrom(BlockState state) {
        for (Map.Entry<BlockStateMatcher, ItemStack> stateItem : STATE_TO_ITEM.entrySet()) {
            if (stateItem.getKey().test(state)) {
                return Optional.of(stateItem.getValue());
            }
        }

        ItemStack stack = new ItemStack(state.getBlock().asItem());
        updateCountForSpecialBlocks(state, stack);
        return stack.isEmpty() ? Optional.empty() : Optional.of(stack);
    }

    private static void updateCountForSpecialBlocks(BlockState state, ItemStack stack) {
        if (state.getBlock() instanceof SlabBlock) {
            stack.setCount(2);
        } else if (state.getBlock() instanceof SnowLayerBlock) {
            stack.setCount(state.getValue(SnowLayerBlock.LAYERS));
        }
    }

    public static ItemStack getRemainingStackFrom(BlockState state) {
        for (Map.Entry<BlockStateMatcher, ItemStack> stateItem : STATE_TO_REMAINING_ITEM.entrySet()) {
            if (stateItem.getKey().test(state)) {
                return stateItem.getValue();
            }
        }
        return ItemStack.EMPTY;
    }

    public static int getBuildPass(BlockState state) {
        for (Map.Entry<BlockStateMatcher, Integer> statePass : STATE_PASS.entrySet()) {
            if (statePass.getKey().test(state)) {
                return statePass.getValue();
            }
        }
        return 0;
    }

    public static class Parser implements IRegistryDataParser {

        private static final String BLOCK_PROPERTY = "block";

        @Override
        public String getName() {
            return "structure_blocks";
        }

        @Override
        public void parse(JsonObject json) {
            STATE_TO_ITEM.putAll(JsonHelper.mapFromObjectArray(JsonUtils.getJsonArray(json, "blockstate_to_item"),
                    BLOCK_PROPERTY, "item", JsonHelper::getBlockStateMatcher, JsonHelper::getItemStack));
            STATE_TO_REMAINING_ITEM.putAll(JsonHelper.mapFromObjectArray(JsonUtils.getJsonArray(json, "blockstate_to_item"),
                    BLOCK_PROPERTY, "remaining_item", JsonHelper::getBlockStateMatcher, e -> e != null ? JsonHelper.getItemStack(e) : ItemStack.EMPTY));
            STATE_PASS.putAll(JsonHelper.mapFromObjectArray(JsonUtils.getJsonArray(json, "block_passes"),
                    BLOCK_PROPERTY, "build_pass", JsonHelper::getBlockStateMatcher, e -> Integer.parseInt(JsonUtils.getString(e, ""))));
        }
    }
}
