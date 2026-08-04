package net.shadowmage.ancientwarfare.structure.tile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.shadowmage.ancientwarfare.core.tile.IBlockBreakHandler;
import net.shadowmage.ancientwarfare.core.tile.TileUpdatable;
import net.shadowmage.ancientwarfare.core.util.BlockTools;
import net.shadowmage.ancientwarfare.core.util.WorldTools;

import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class TileMulti extends TileUpdatable implements IBlockBreakHandler {
    private static final String MAIN_BLOCK_POS_TAG = "mainBlockPos";
    private BlockPos mainBlockPos = null;

    @Override
    public final void readFromNBT(CompoundTag compound) {
        super.readFromNBT(compound);
        readNBT(compound);
    }

    protected void readNBT(CompoundTag compound) {
        if (compound.contains(MAIN_BLOCK_POS_TAG)) {
            mainBlockPos = BlockPos.of(compound.getLong(MAIN_BLOCK_POS_TAG));
        }
    }

    @Override
    public final CompoundTag writeToNBT(CompoundTag compound) {
        compound = super.writeToNBT(compound);
        writeNBT(compound);
        return compound;
    }

    protected void writeNBT(CompoundTag compound) {
        if (mainBlockPos != null) {
            compound.putLong(MAIN_BLOCK_POS_TAG, mainBlockPos.asLong());
        }
    }

    @Override
    protected final void writeUpdateNBT(CompoundTag tag) {
        super.writeUpdateNBT(tag);
        writeNBT(tag);
    }

    @Override
    protected final void handleUpdateNBT(CompoundTag tag) {
        super.handleUpdateNBT(tag);
        readNBT(tag);
    }

    public void setMainBlockPos(BlockPos mainBlockPos) {
        if (mainBlockPos != null && mainBlockPos.equals(this.mainBlockPos)) {
            return;
        }
        this.mainBlockPos = mainBlockPos;
        setChanged();
        if (level != null && !level.isClientSide) {
            BlockTools.notifyBlockUpdate(this);
        }
    }

    public Optional<BlockPos> getMainBlockPos() {
        return Optional.ofNullable(mainBlockPos);
    }

    @Override
    public void onBlockBroken(BlockState state) {
        Optional<BlockPos> mainPos = getMainBlockPos();
        if (mainPos.isPresent() && !mainPos.get().equals(pos)) {
            BlockState mainState = world.getBlockState(mainPos.get());
            if (mainState.getBlock() != Blocks.AIR) {
                WorldTools.getTile(world, mainPos.get(), TileMulti.class).ifPresent(tileMulti -> tileMulti.onBlockBroken(mainState));
                world.removeBlock(mainPos.get(), false);
            }
            return;
        }

        getAdditionalPositions(state).forEach(position -> world.removeBlock(position, false));
    }

    public abstract Set<BlockPos> getAdditionalPositions(BlockState state);

    protected <T, U extends TileMulti> T getValueFromMain(Class<U> teClass, Function<U, T> getValue, T value, Supplier<T> getDefaultValue) {
        Optional<BlockPos> mainPos = getMainBlockPos();
        if (!mainPos.isPresent() || mainPos.get().equals(pos)) {
            return value;
        }
        return WorldTools.getTile(world, mainPos.get(), teClass).map(getValue).orElse(getDefaultValue.get());
    }

    public void setPlacementDirection(Level world, BlockPos pos, BlockState state, Direction horizontalFacing, float rotationYaw) {
        //noop by default
    }

    public void setMainPosOnAdditionalBlocks() {
        getAdditionalPositions(world.getBlockState(pos)).forEach(additionalPos -> WorldTools.getTile(world, additionalPos, TileMulti.class)
                .ifPresent(teAdditional -> teAdditional.setMainBlockPos(pos)));
    }
}
