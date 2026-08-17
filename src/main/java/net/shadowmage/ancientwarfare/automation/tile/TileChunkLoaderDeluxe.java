package net.shadowmage.ancientwarfare.automation.tile;


import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.shadowmage.ancientwarfare.automation.container.ContainerChunkLoaderDeluxe;
import net.shadowmage.ancientwarfare.core.init.AWMenuTypes;
import net.shadowmage.ancientwarfare.core.interfaces.IInteractableTile;
import net.shadowmage.ancientwarfare.core.network.NetworkHandler;
import net.shadowmage.ancientwarfare.core.util.Constants;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TileChunkLoaderDeluxe extends TileChunkLoaderSimple implements IInteractableTile {

    private final Set<ChunkPos> ccipSet = new HashSet<>();

    private final List<ContainerChunkLoaderDeluxe> viewers = new ArrayList<>();

    public TileChunkLoaderDeluxe(BlockEntityType<?> type, BlockPos pos, BlockState state) {

        super(type, pos, state);

    }

    @Override
    public boolean onBlockClicked(Player player, @Nullable InteractionHand hand) {
        if (!player.level().isClientSide) {
            AWMenuTypes.open(player, NetworkHandler.GUI_CHUNK_LOADER_DELUXE, pos);
        }
        return true;
    }

    public void addViewer(ContainerChunkLoaderDeluxe viewer) {
        viewers.add(viewer);
    }

    public void removeViewer(ContainerChunkLoaderDeluxe viewer) {
        viewers.remove(viewer);
    }

    public void addOrRemoveChunk(ChunkPos ccip) {
        if (!ccipSet.remove(ccip)) {
            ccipSet.add(ccip);
        }
        refreshForcedChunks();
        markDirty();
        informViewers();
    }

    private void informViewers() {
        for (ContainerChunkLoaderDeluxe viewer : viewers) {
            viewer.onChunkLoaderSetUpdated(ccipSet);
        }
    }

    public Set<ChunkPos> getForcedChunks() {
        return new HashSet<>(ccipSet);
    }

    @Override
    protected Set<ChunkPos> getChunksToForce() {
        Set<ChunkPos> chunks = new HashSet<>(ccipSet);
        chunks.add(new ChunkPos(worldPosition));
        return chunks;
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
        super.readFromNBT(tag);
        ListTag list = tag.getList("chunkList", Constants.NBT.TAG_COMPOUND);
        CompoundTag ccipTag;
        ChunkPos ccip;
        ccipSet.clear();
        for (int i = 0; i < list.size(); i++) {
            ccipTag = list.getCompound(i);
            ccip = new ChunkPos(ccipTag.getInt("x"), ccipTag.getInt("z"));
            ccipSet.add(ccip);
        }
    }

    @Override
    public CompoundTag writeToNBT(CompoundTag tag) {
        super.writeToNBT(tag);
        ListTag list = new ListTag();
        CompoundTag ccipTag;
        for (ChunkPos chunkPos : this.ccipSet) {
            ccipTag = new CompoundTag();
            ccipTag.putInt("x", chunkPos.x);
            ccipTag.putInt("z", chunkPos.z);
            list.add(ccipTag);
        }
        tag.put("chunkList", list);
        return tag;
    }
}
