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

public class FruitBreakOnly implements IFruit {
    private BlockStateMatcher stateMatcher;

    public FruitBreakOnly(BlockStateMatcher stateMatcher) {
        this.stateMatcher = stateMatcher;
    }

    @Override
    public boolean matches(BlockState state) {
        return stateMatcher.test(state);
    }

    @Override
    public boolean isRipe(BlockState state) {
        return true;
    }

    @Override
    public boolean pick(Level world, BlockState state, BlockPos pos, int fortune, IItemHandler inventory) {
        NonNullList<ItemStack> drops = BlockTools.getDrops(world, pos, state, fortune);

        if (drops.isEmpty() || !InventoryTools.canInventoryHold(inventory, drops)) {
            return false;
        }

        BlockTools.breakBlock(world, pos, fortune, false);

        InventoryTools.insertOrDropItems(inventory, drops, world, pos);

        return true;
    }

    @Override
    public boolean isPlantable() {
        return false;
    }
}
