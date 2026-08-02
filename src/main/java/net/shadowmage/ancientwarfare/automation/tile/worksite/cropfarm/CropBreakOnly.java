package net.shadowmage.ancientwarfare.automation.tile.worksite.cropfarm;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.IItemHandler;
import net.shadowmage.ancientwarfare.core.util.BlockTools;
import net.shadowmage.ancientwarfare.core.util.InventoryTools;
import net.shadowmage.ancientwarfare.core.util.parsing.BlockStateMatcher;
import net.shadowmage.ancientwarfare.core.util.parsing.ItemStackMatcher;

import java.util.Collections;
import java.util.List;

public class CropBreakOnly implements ICrop {

    private BlockStateMatcher stateMatcher;
    private ItemStackMatcher stackMatcher;

    CropBreakOnly() {
    }

    public CropBreakOnly(BlockStateMatcher stateMatcher, ItemStackMatcher stackMatcher) {
        this.stateMatcher = stateMatcher;
        this.stackMatcher = stackMatcher;
    }

    @Override
    public List<BlockPos> getPositionsToHarvest(Level world, BlockPos pos, BlockState state) {
        return Collections.singletonList(pos);
    }

    @Override
    public boolean canBeFertilized(BlockState state, Level world, BlockPos pos) {
        return false;
    }

    @Override
    public boolean harvest(Level world, BlockState state, BlockPos pos, int fortune, IItemHandler inventory) {
        Block block = state.getBlock();
        NonNullList<ItemStack> stacks = BlockTools.getDrops(world, pos, state, fortune);

        if (!InventoryTools.canInventoryHold(inventory, stacks)) {
            return false;
        }

        if (!BlockTools.breakBlockNoDrops(world, pos, state)) {
            return false;
        }

        InventoryTools.insertOrDropItems(inventory, stacks, world, pos);
        return true;
    }

    @Override
    public boolean matches(BlockState state) {
        return stateMatcher.test(state);
    }

    @Override
    public boolean matches(ItemStack stack) {
        return stackMatcher.test(stack);
    }

    @Override
    public boolean isPlantable(ItemStack stack) {
        return false;
    }
}
