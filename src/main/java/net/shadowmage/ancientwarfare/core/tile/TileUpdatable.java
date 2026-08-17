package net.shadowmage.ancientwarfare.core.tile;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Shared 1.12-to-1.20 block-entity compatibility base.
 * <p>
 * It keeps the legacy AW lifecycle entry points available while delegating
 * them to the modern BlockEntity API. This removes duplicate migration code
 * from every worksite, warehouse and structure tile.
 */
public abstract class TileUpdatable extends BlockEntity {
    /**
     * Transitional aliases used by legacy subclasses; kept synchronized by setLevel.
     */
    @Deprecated
    protected Level world;
    @Deprecated
    protected final BlockPos pos;

    protected TileUpdatable(BlockEntityType<?> type, BlockPos pos, net.minecraft.world.level.block.state.BlockState state) {
        super(type, pos, state);
        this.pos = pos;
    }

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);
        this.world = level;
    }

    /**
     * Legacy invalidation hook used throughout the 1.12 codebase.
     */
    public void invalidate() {
    }

    @Override
    public void setRemoved() {
        if (!isRemoved()) {
            invalidate();
        }
        super.setRemoved();
    }

    protected final boolean hasWorld() {
        return level != null;
    }

    protected final void markDirty() {
        setChanged();
    }

    public final BlockPos getPos() {
        return worldPosition;
    }

    public final Level getWorld() {
        return level;
    }

    /**
     * Legacy metadata accessor bridging to BlockBase#getMetaFromState.
     */
    public int getBlockMetadata() {
        net.minecraft.world.level.block.state.BlockState state = getBlockState();
        return state.getBlock() instanceof net.shadowmage.ancientwarfare.core.block.BlockBase blockBase ? blockBase.getMetaFromState(state) : 0;
    }

    /**
     * Legacy save hook.
     */
    public CompoundTag writeToNBT(CompoundTag tag) {
        super.saveAdditional(tag);
        return tag;
    }

    /**
     * Legacy load hook.
     */
    public void readFromNBT(CompoundTag tag) {
        super.load(tag);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        writeToNBT(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        readFromNBT(tag);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this, blockEntity -> {
            CompoundTag tag = new CompoundTag();
            writeUpdateNBT(tag);
            return tag;
        });
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet) {
        CompoundTag tag = packet.getTag();
        if (tag != null) handleUpdateNBT(tag);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        writeUpdateNBT(tag);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        handleUpdateNBT(tag);
    }

    protected void writeUpdateNBT(CompoundTag tag) {
    }

    protected void handleUpdateNBT(CompoundTag tag) {
    }
}
