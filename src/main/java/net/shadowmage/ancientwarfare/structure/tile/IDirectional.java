package net.shadowmage.ancientwarfare.structure.tile;

import net.minecraft.core.Direction;

public interface IDirectional {
    Direction getFacing();

    void setFacing(Direction facing);
}
