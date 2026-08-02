package net.shadowmage.ancientwarfare.structure.tile;

import com.google.common.collect.ImmutableSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.shadowmage.ancientwarfare.core.block.BlockRotationHandler;
import net.shadowmage.ancientwarfare.core.render.property.CoreProperties;
import net.shadowmage.ancientwarfare.structure.util.BlockStateProperties;

import java.util.Set;

public class TileChair extends TileMulti implements BlockRotationHandler.IRotatableTile {
    private Direction facing = Direction.NORTH;

    @Override
    protected void readNBT(CompoundTag compound) {
        super.readNBT(compound);
        facing = Direction.byName(compound.getString("facing"));
    }

    @Override
    protected void writeNBT(CompoundTag compound) {
        super.writeNBT(compound);
        compound.putString("facing", facing.getName());
    }

    @Override
    public Set<BlockPos> getAdditionalPositions(BlockState state) {
        return ImmutableSet.of(pos.above());
    }

    @Override
    public Direction getPrimaryFacing() {
        return facing;
    }

    @Override
    public void setPrimaryFacing(Direction face) {
        facing = face;
    }

    public boolean shouldRefresh(Level world, BlockPos pos, BlockState oldState, BlockState newState) {
        return oldState.getBlock() != newState.getBlock()
                || oldState.getValue(BlockStateProperties.VARIANT) != newState.getValue(BlockStateProperties.VARIANT)
                || !oldState.getValue(CoreProperties.VISIBLE).equals(newState.getValue(CoreProperties.VISIBLE));
    }
}
