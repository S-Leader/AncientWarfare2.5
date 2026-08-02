package net.shadowmage.ancientwarfare.automation.tile.worksite.fruitfarm;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.IItemHandler;
import net.shadowmage.ancientwarfare.core.util.BlockTools;
import net.shadowmage.ancientwarfare.core.util.InventoryTools;
import net.shadowmage.ancientwarfare.core.util.parsing.BlockStateMatcher;
import net.shadowmage.ancientwarfare.core.util.parsing.PropertyState;
import net.shadowmage.ancientwarfare.core.util.parsing.PropertyStateMatcher;

public class FruitPicked implements IFruit {
    private BlockStateMatcher stateMatcher;
    private PropertyStateMatcher ripeStateMatcher;
    private PropertyState newState;

    public FruitPicked(BlockStateMatcher stateMatcher, PropertyStateMatcher ripeStateMatcher, PropertyState newState) {
        this.stateMatcher = stateMatcher;
        this.ripeStateMatcher = ripeStateMatcher;
        this.newState = newState;
    }

    @Override
    public boolean matches(BlockState state) {
        return stateMatcher.test(state);
    }

    @Override
    public boolean isRipe(BlockState state) {
        return ripeStateMatcher.test(state);
    }

    @Override
    public boolean pick(Level world, BlockState state, BlockPos pos, int fortune, IItemHandler inventory) {
        NonNullList<ItemStack> drops = getDrops(world, state, pos, fortune);

        if (drops.isEmpty() || !InventoryTools.canInventoryHold(inventory, drops)) {
            return false;
        }

        world.setBlock(pos, newState.update(state), 3);

        putInInventory(world, pos, inventory, drops);

        return true;
    }

    protected NonNullList<ItemStack> getDrops(Level world, BlockState state, BlockPos pos, int fortune) {
        //using deprecated getDrops here just because of pam's harvestcraft, change to proper one in the future
        return BlockTools.getDrops(world, pos, state, fortune);
    }

    protected void putInInventory(Level world, BlockPos pos, IItemHandler inventory, NonNullList<ItemStack> drops) {
        InventoryTools.insertOrDropItems(inventory, drops, world, pos);
    }

    @Override
    public boolean isPlantable() {
        return false;
    }
}
