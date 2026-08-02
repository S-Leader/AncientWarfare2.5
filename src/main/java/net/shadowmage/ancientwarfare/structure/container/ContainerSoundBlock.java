package net.shadowmage.ancientwarfare.structure.container;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.shadowmage.ancientwarfare.core.container.ContainerTileBase;
import net.shadowmage.ancientwarfare.core.util.BlockTools;
import net.shadowmage.ancientwarfare.structure.tile.TileSoundBlock;
import net.shadowmage.ancientwarfare.structure.util.BlockSongPlayData;

public class ContainerSoundBlock extends ContainerTileBase<TileSoundBlock> {
    private static final String TUNE_DATA_TAG = "tuneData";
    public BlockSongPlayData data;

    public ContainerSoundBlock(Player player, int x, int y, int z) {
        super(player, x, y, z);
        data = tileEntity.getSongs();
    }

    @Override
    public void sendInitData() {
        CompoundTag tag = new CompoundTag();
        tag.put(TUNE_DATA_TAG, data.writeToNBT(new CompoundTag()));
        sendDataToClient(tag);
    }

    @Override
    public void handlePacketData(CompoundTag tag) {
        if (tag.contains(TUNE_DATA_TAG)) {
            tileEntity.getSongs().readFromNBT(tag.getCompound(TUNE_DATA_TAG));
            data = tileEntity.getSongs();
        }
        if (!tileEntity.getWorld().isClientSide) {
            tileEntity.resetStateValues();
            tileEntity.setChanged();
            BlockTools.notifyBlockUpdate(tileEntity);
        }
        refreshGui();
    }

    public void sendTuneDataToServer(Player player) {
        if (player.level().isClientSide)//handles sending new/updated/changed data back to server on GUI close.  the last GUI to close will be the one whose data 'sticks'
        {
            CompoundTag tag = new CompoundTag();
            tag.put(TUNE_DATA_TAG, data.writeToNBT(new CompoundTag()));
            sendDataToServer(tag);
        }
    }
}
