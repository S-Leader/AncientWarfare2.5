package net.shadowmage.ancientwarfare.automation.tile.worksite.fruitfarm;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.shadowmage.ancientwarfare.core.util.parsing.BlockStateMatcher;
import net.shadowmage.ancientwarfare.core.util.parsing.PropertyState;
import net.shadowmage.ancientwarfare.core.util.parsing.PropertyStateMatcher;

public class FruitPickedDrop extends FruitPicked {
    private ItemStack drop;

    public FruitPickedDrop(BlockStateMatcher stateMatcher, PropertyStateMatcher ripeStateMatcher, PropertyState newState, ItemStack drop) {
        super(stateMatcher, ripeStateMatcher, newState);
        this.drop = drop;
    }

    @Override
    protected NonNullList<ItemStack> getDrops(Level world, BlockState state, BlockPos pos, int fortune) {
        return NonNullList.of(ItemStack.EMPTY, drop.copy());
    }
}
