package net.shadowmage.ancientwarfare.structure.tile;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.AABB;
import net.shadowmage.ancientwarfare.core.block.BlockRotationHandler;
import net.shadowmage.ancientwarfare.core.tile.TileUpdatable;

public class TileStatue extends TileUpdatable implements BlockRotationHandler.IRotatableTile {
    private EntityStatueInfo entityStatueInfo = new EntityStatueInfo();
    private Direction facing;

    public TileStatue(BlockEntityType<?> type, BlockPos pos, BlockState state) {

        super(type, pos, state);
        entityStatueInfo.setRenderType(EntityStatueInfo.RenderType.MODEL);
    }

    public EntityStatueInfo getEntityStatueInfo() {
        return entityStatueInfo;
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(pos.offset(-2, -2, -2), pos.offset(3, 3, 3));
    }

    @Override
    public Direction getPrimaryFacing() {
        return facing;
    }

    @Override
    public void setPrimaryFacing(Direction facing) {
        this.facing = facing;
    }

    @Override
    protected void writeUpdateNBT(CompoundTag tag) {
        super.writeUpdateNBT(tag);
        writeNBT(tag);
    }

    @Override
    protected void handleUpdateNBT(CompoundTag tag) {
        super.handleUpdateNBT(tag);
        readNBT(tag);
    }

    @Override
    public void readFromNBT(CompoundTag compound) {
        super.readFromNBT(compound);
        readNBT(compound);
    }

    private void readNBT(CompoundTag compound) {
        facing = Direction.from2DDataValue(compound.getByte("facing"));
        entityStatueInfo.deserializeNBT(compound.getCompound("entityStatueInfo"));
    }

    @Override
    public CompoundTag writeToNBT(CompoundTag compound) {
        return writeNBT(super.writeToNBT(compound));
    }

    private CompoundTag writeNBT(CompoundTag ret) {
        ret.putByte("facing", (byte) facing.get2DDataValue());
        ret.put("entityStatueInfo", entityStatueInfo.serializeNBT(new CompoundTag()));
        return ret;
    }
}
