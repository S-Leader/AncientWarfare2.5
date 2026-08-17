package net.shadowmage.ancientwarfare.automation.tile.warehouse2;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.shadowmage.ancientwarfare.core.interfaces.ITickable;
import net.shadowmage.ancientwarfare.core.tile.TileUpdatable;
import net.shadowmage.ancientwarfare.core.util.BlockTools;
import net.shadowmage.ancientwarfare.core.util.WorldTools;

import javax.annotation.Nullable;
import java.util.Optional;

public abstract class TileControlled extends TileUpdatable implements IControlledTile, ITickable {
    protected TileControlled(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    private static final String CONTROLLER_POSITION_TAG = "controllerPosition";
    private boolean init;
    private TileWarehouseBase controller;
    private BlockPos controllerPosition;

    @Override
    public final void update() {
        if (!init) {
            init = true;
            if (!loadController()) {
                searchForController();
            }
        }
        updateTile();
    }

    private boolean loadController() {
        BlockPos pos = controllerPosition;
        controllerPosition = null;
        if (pos != null && controller == null) {
            WorldTools.getTile(world, pos, IControllerTile.class).filter(this::isValidController).ifPresent(t -> t.addControlledTile(this));
        }
        return controller != null;
    }

    protected abstract void updateTile();

    public void searchForController() {
        BlockPos min = pos.offset(-16, -4, -16);
        BlockPos max = pos.offset(16, 4, 16);
        for (BlockEntity te : WorldTools.getTileEntitiesInArea(world, min.getX(), min.getY(), min.getZ(), max.getX(), max.getY(), max.getZ())) {
            if (te instanceof IControllerTile && isValidController((IControllerTile) te)) {
                ((IControllerTile) te).addControlledTile(this);
                break;
            }
        }
    }

    @Override
    public boolean isValidController(IControllerTile tile) {
        return BlockTools.isPositionWithinBounds(getPos(), tile.getWorkBoundsMin(), tile.getWorkBoundsMax());
    }

    @Override
    public final void invalidate() {
        if (controller != null) {
            controller.removeControlledTile(this);
        }
        controller = null;
        init = false;
        super.invalidate();
    }

    @Override
    public final void clearRemoved() {
        super.clearRemoved();
        if (controller != null) {
            controller.addControlledTile(this);
        }
    }

    @Override
    public final void setController(@Nullable TileWarehouseBase tile) {
        this.controller = tile;
        this.controllerPosition = tile == null ? null : tile.getPosisition();
        onControllerChanged();
    }

    protected void onControllerChanged() {
    }

    @Override
    public final Optional<TileWarehouseBase> getController() {
        return Optional.ofNullable(controller);
    }

    @Override
    public BlockPos getPosition() {
        return getPos();
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
        super.readFromNBT(tag);
        if (tag.contains(CONTROLLER_POSITION_TAG)) {
            controllerPosition = BlockPos.of(tag.getLong(CONTROLLER_POSITION_TAG));
        }
    }

    @Override
    public CompoundTag writeToNBT(CompoundTag tag) {
        super.writeToNBT(tag);
        if (controllerPosition != null) {
            tag.putLong(CONTROLLER_POSITION_TAG, controllerPosition.asLong());
        }
        return tag;
    }

    @Override
    public final boolean equals(Object obj) {
        return this == obj
                || (obj instanceof TileControlled && this.world == ((TileControlled) obj).getWorld() && this.getPos().equals(((TileControlled) obj).getPos()));
    }

    @Override
    public final int hashCode() {
        return this.getPos().hashCode();
    }
}
