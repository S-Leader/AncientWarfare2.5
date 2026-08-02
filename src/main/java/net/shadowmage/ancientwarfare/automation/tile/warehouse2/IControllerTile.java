package net.shadowmage.ancientwarfare.automation.tile.warehouse2;

import net.minecraft.core.BlockPos;

public interface IControllerTile {

    void addControlledTile(IControlledTile tile);

    void removeControlledTile(IControlledTile tile);

    BlockPos getPosisition();

    BlockPos getWorkBoundsMin();

    BlockPos getWorkBoundsMax();
}
