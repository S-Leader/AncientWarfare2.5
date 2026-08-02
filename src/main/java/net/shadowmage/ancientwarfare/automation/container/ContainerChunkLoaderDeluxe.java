package net.shadowmage.ancientwarfare.automation.container;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.shadowmage.ancientwarfare.automation.tile.TileChunkLoaderDeluxe;
import net.shadowmage.ancientwarfare.core.container.ContainerTileBase;
import net.shadowmage.ancientwarfare.core.util.Constants;

import java.util.HashSet;
import java.util.Set;

public class ContainerChunkLoaderDeluxe extends ContainerTileBase<TileChunkLoaderDeluxe> {

    public Set<ChunkPos> ccipSet = new HashSet<>();

    public ContainerChunkLoaderDeluxe(Player player, int x, int y, int z) {
        super(player, x, y, z);
        if (!player.level().isClientSide) {
            ccipSet = tileEntity.getForcedChunks();
            tileEntity.addViewer(this);
        }
    }

    @Override
    public void onContainerClosed(Player par1EntityPlayer) {
        super.onContainerClosed(par1EntityPlayer);
        tileEntity.removeViewer(this);
    }

    @Override
    public void handlePacketData(CompoundTag tag) {
        if (tag.contains("chunkList")) {
            ccipSet.clear();
            ListTag list = tag.getList("chunkList", Constants.NBT.TAG_COMPOUND);
            CompoundTag ccipTag;
            ChunkPos ccip;
            for (int i = 0; i < list.size(); i++) {
                ccipTag = list.getCompound(i);
                ccip = new ChunkPos(ccipTag.getInt("x"), ccipTag.getInt("z"));
                ccipSet.add(ccip);
            }
            refreshGui();
        } else if (tag.contains("forced")) {
            ChunkPos ccip = new ChunkPos(tag.getInt("x"), tag.getInt("z"));
            tileEntity.addOrRemoveChunk(ccip);
            //should trigger an updateViewers and then a re-send of forced chunk list from tile
        }
    }

    @Override
    public void sendInitData() {
        sendChunkList();
    }

    private void sendChunkList() {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        CompoundTag ccipTag;
        for (ChunkPos chunkPos : this.ccipSet) {
            ccipTag = new CompoundTag();
            ccipTag.putInt("x", chunkPos.x);
            ccipTag.putInt("z", chunkPos.z);
            list.add(ccipTag);
        }
        tag.put("chunkList", list);
        sendDataToClient(tag);
    }

    public void force(ChunkPos ccip) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("forced", true);
        tag.putInt("x", ccip.x);
        tag.putInt("z", ccip.z);
        sendDataToServer(tag);
    }

    public void onChunkLoaderSetUpdated(Set<ChunkPos> ccipSet) {
        this.ccipSet.clear();
        this.ccipSet.addAll(ccipSet);
        sendChunkList();
    }

}
