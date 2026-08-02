package net.shadowmage.ancientwarfare.automation.tile.worksite.fruitfarm;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.IItemHandler;
import net.shadowmage.ancientwarfare.core.util.InventoryTools;
import net.shadowmage.ancientwarfare.core.util.parsing.BlockStateMatcher;
import net.shadowmage.ancientwarfare.core.util.parsing.PropertyState;
import net.shadowmage.ancientwarfare.core.util.parsing.PropertyStateMatcher;

public class FruitPickedRemoveOne extends FruitPicked {
    public FruitPickedRemoveOne(BlockStateMatcher stateMatcher, PropertyStateMatcher ripeStateMatcher, PropertyState newState) {
        super(stateMatcher, ripeStateMatcher, newState);
    }

    @Override
    protected void putInInventory(Level world, BlockPos pos, IItemHandler inventory, NonNullList<ItemStack> drops) {
        InventoryTools.removeItem(drops, s -> InventoryTools.doItemStacksMatchRelaxed(s, drops.get(0)), 1);

        InventoryTools.insertOrDropItems(inventory, drops, world, pos);
    }
}
