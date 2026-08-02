package net.shadowmage.ancientwarfare.automation.container;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.shadowmage.ancientwarfare.automation.tile.worksite.WorkSiteFishFarm;
import net.shadowmage.ancientwarfare.core.container.ContainerTileBase;

public class ContainerWorksiteFishControl extends ContainerTileBase<WorkSiteFishFarm> {

    public boolean harvestFish;
    public boolean harvestInk;

    public ContainerWorksiteFishControl(Player player, int x, int y, int z) {
        super(player, x, y, z);
        this.harvestFish = tileEntity.harvestFish();
        this.harvestInk = tileEntity.harvestInk();
    }

    @Override
    public void sendInitData() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("fish", harvestFish);
        tag.putBoolean("ink", harvestInk);
        this.sendDataToClient(tag);
    }

    @Override
    public void handlePacketData(CompoundTag tag) {
        if (tag.contains("fish") && tag.contains("ink")) {
            harvestFish = tag.getBoolean("fish");
            harvestInk = tag.getBoolean("ink");
            tileEntity.setHarvest(harvestFish, harvestInk);
        }
        refreshGui();
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        if (harvestFish != tileEntity.harvestFish() || harvestInk != tileEntity.harvestInk()) {
            sendInitData();
        }
    }

    public void sendSettingsToServer() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("fish", harvestFish);
        tag.putBoolean("ink", harvestInk);
        this.sendDataToServer(tag);
    }

}
