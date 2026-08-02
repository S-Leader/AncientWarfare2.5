package net.shadowmage.ancientwarfare.automation.tile.worksite.cropfarm;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.IItemHandler;
import net.shadowmage.ancientwarfare.core.util.InventoryTools;
import net.shadowmage.ancientwarfare.core.util.parsing.BlockStateMatcher;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class CropKeepBottom extends CropDefault {
    private static final int MAX_HEIGHT = 10;
    private BlockStateMatcher stateMatcher;

    public CropKeepBottom(BlockStateMatcher stateMatcher) {
        this.stateMatcher = stateMatcher;
    }

    @Override
    public boolean harvest(Level world, BlockState state, BlockPos pos, int fortune, IItemHandler inventory) {
        NonNullList<ItemStack> stacks = NonNullList.create();

        //figuring out positions here instead of in getPositionsToHarvest because if growing quickly we could be harvesting block that's not the top one.
        // so instead just pass in the bottom one as the one to harvest and then figure out all of the blocks to harvest at once here
        Map<BlockPos, BlockState> harvestPositions = getHarvestPositions(world, pos);

        getDrops(harvestPositions, stacks, world, fortune);

        if (!InventoryTools.canInventoryHold(inventory, stacks)) {
            return false;
        }

        if (!breakCrop(harvestPositions, world)) {
            return false;
        }

        InventoryTools.insertOrDropItems(inventory, stacks, world, pos);
        return true;
    }

    private void getDrops(Map<BlockPos, BlockState> harvestPositions, NonNullList<ItemStack> stacks, Level world, int fortune) {
        for (Map.Entry<BlockPos, BlockState> entry : harvestPositions.entrySet()) {
            getDrops(stacks, world, entry.getKey(), entry.getValue(), fortune);
        }
    }

    private boolean breakCrop(Map<BlockPos, BlockState> harvestPositions, Level world) {
        for (Map.Entry<BlockPos, BlockState> entry : harvestPositions.entrySet()) {
            if (!breakCrop(world, entry.getKey(), entry.getValue())) {
                return false;
            }
        }
        return true;
    }

    private Map<BlockPos, BlockState> getHarvestPositions(Level world, BlockPos pos) {
        Map<BlockPos, BlockState> ret = new TreeMap<>(Collections.reverseOrder());
        for (BlockPos currentPos = pos.above(); currentPos.getY() < pos.getY() + MAX_HEIGHT; currentPos = currentPos.above()) {
            BlockState state = world.getBlockState(currentPos);
            if (!stateMatcher.test(state)) {
                break;
            }
            ret.put(currentPos, state);
        }
        return ret;
    }

    @Override
    public List<BlockPos> getPositionsToHarvest(Level world, BlockPos pos, BlockState state) {
        if (stateMatcher.test(world.getBlockState(pos.above()))) {
            return Collections.singletonList(pos);
        }
        return Collections.emptyList();
    }

    @Override
    public boolean canBeFertilized(BlockState state, Level world, BlockPos pos) {
        return false;
    }

    @Override
    public boolean matches(BlockState state) {
        return stateMatcher.test(state);
    }
}
