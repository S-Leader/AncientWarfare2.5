package net.shadowmage.ancientwarfare.automation.tile.warehouse2;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.BlockPos;

public final class TileWarehouseStorageLarge extends TileWarehouseStorage {

    public TileWarehouseStorageLarge(BlockEntityType<?> type, BlockPos pos, BlockState state) {

        super(type, pos, state);

    }

    @Override
    public int getStorageAdditionSize() {
        return 3 * super.getStorageAdditionSize();
    }
}
