package net.shadowmage.ancientwarfare.structure.tile;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.shadowmage.ancientwarfare.core.tile.IBlockBreakHandler;
import net.shadowmage.ancientwarfare.core.util.InventoryTools;

public class TileLootBasket extends TileAdvancedLootChest implements IBlockBreakHandler {
    public TileLootBasket(BlockEntityType<? extends ChestBlockEntity> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void onBlockBroken(BlockState state) {
        InventoryTools.getItemHandlerFrom(this, null).ifPresent(inv -> InventoryTools.dropItemsInWorld(level, inv, worldPosition));
    }
}
