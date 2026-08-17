package net.shadowmage.ancientwarfare.core.tile;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.shadowmage.ancientwarfare.core.owner.IOwnable;
import net.shadowmage.ancientwarfare.core.owner.Owner;

public class TileOwned extends TileUpdatable implements IOwnable {
    public TileOwned(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    private Owner owner = Owner.EMPTY;

    @Override
    public final void setOwner(Player player) {
        owner = new Owner(player);
    }

    @Override
    public final void setOwner(Owner owner) {
        this.owner = owner;
    }

    @Override
    public Owner getOwner() {
        return owner;
    }

    @Override
    public final boolean isOwner(Player player) {
        return owner.isOwnerOrSameTeamOrFriend(player);
    }

    @Override
    protected void writeUpdateNBT(CompoundTag tag) {
        owner.serializeToNBT(tag);
    }

    @Override
    protected void handleUpdateNBT(CompoundTag tag) {
        owner = Owner.deserializeFromNBT(tag);
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
        super.readFromNBT(tag);
        owner = Owner.deserializeFromNBT(tag);
    }

    @Override
    public CompoundTag writeToNBT(CompoundTag tag) {
        super.writeToNBT(tag);
        owner.serializeToNBT(tag);
        return tag;
    }
}
