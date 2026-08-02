package net.shadowmage.ancientwarfare.automation.container;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.shadowmage.ancientwarfare.automation.tile.worksite.WorkSiteQuarry;
import net.shadowmage.ancientwarfare.core.container.ContainerTileBase;
import net.shadowmage.ancientwarfare.core.interfaces.IBoundedSite;
import net.shadowmage.ancientwarfare.core.util.BlockTools;

public class ContainerWorksiteQuarryBounds extends ContainerTileBase<WorkSiteQuarry> {

    public int maxHeight;

    public ContainerWorksiteQuarryBounds(Player player, int x, int y, int z) {
        super(player, x, y, z);
        maxHeight = tileEntity.height;
    }

    @Override
    public void sendInitData() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("height", maxHeight);
        sendDataToClient(tag);
    }

    @Override
    public void handlePacketData(CompoundTag tag) {
        maxHeight = tag.getInt("height");
        if (tag.contains("guiClosed")) {
            getWorksite().onBoundsAdjusted();
            getWorksite().onPostBoundsAdjusted();
            BlockTools.notifyBlockUpdate(player.level(), getPos());
        }
        if (!player.level().isClientSide) {
            tileEntity.height = maxHeight;
            tileEntity.setChanged();//mark dirty so it get saved to nbt
        }
        refreshGui();
    }

    public void sendSettingsToServer() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("height", maxHeight);
        sendDataToServer(tag);
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        boolean send = false;
        if (maxHeight != tileEntity.height) {
            maxHeight = tileEntity.height;
            send = true;
        }
        if (send) {
            sendInitData();
        }
    }

    public void onClose(boolean boundsAdjusted) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("guiClosed", true);
        if (boundsAdjusted) {
            tag.putInt("height", maxHeight);
        }
        sendDataToServer(tag);
    }

    public BlockPos getPos() {
        return tileEntity.getPos();
    }

    public int getX() {
        return tileEntity.getPos().getX();
    }

    public int getY() {
        return tileEntity.getPos().getY();
    }

    public int getZ() {
        return tileEntity.getPos().getZ();
    }

    public IBoundedSite getWorksite() {
        return tileEntity;
    }
}
