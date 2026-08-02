package net.shadowmage.ancientwarfare.npc.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.Optional;

public interface IKeepFood {

    int getUpkeepAmount();

    Direction getUpkeepBlockSide();

    int getUpkeepDimensionId();

    void setUpkeepAutoPosition(BlockPos pos);

    Optional<BlockPos> getUpkeepPoint();
}
