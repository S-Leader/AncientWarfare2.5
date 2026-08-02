package net.shadowmage.ancientwarfare.structure.tile;

import net.minecraft.world.level.block.state.BlockState;
import net.shadowmage.ancientwarfare.core.tile.IBlockBreakHandler;
import net.shadowmage.ancientwarfare.core.util.InventoryTools;

public class TileLootBasket extends TileAdvancedLootChest implements IBlockBreakHandler {
    @Override
    public void onBlockBroken(BlockState state) {
        InventoryTools.getItemHandlerFrom(this, null).ifPresent(inv -> InventoryTools.dropItemsInWorld(level, inv, worldPosition));
    }
}
