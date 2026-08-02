package net.shadowmage.ancientwarfare.automation.container;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.shadowmage.ancientwarfare.automation.tile.worksite.WorkSiteAnimalFarm;
import net.shadowmage.ancientwarfare.core.container.ContainerTileBase;

public class ContainerWorksiteAnimalControl extends ContainerTileBase<WorkSiteAnimalFarm> {

    public int maxPigs;
    public int maxSheep;
    public int maxCows;
    public int maxChickens;

    public ContainerWorksiteAnimalControl(Player player, int x, int y, int z) {
        super(player, x, y, z);
        maxPigs = tileEntity.maxPigCount;
        maxSheep = tileEntity.maxSheepCount;
        maxCows = tileEntity.maxCowCount;
        maxChickens = tileEntity.maxChickenCount;
    }

    @Override
    public void sendInitData() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("pigs", maxPigs);
        tag.putInt("cows", maxCows);
        tag.putInt("sheep", maxSheep);
        tag.putInt("chickens", maxChickens);
        sendDataToClient(tag);
    }

    @Override
    public void handlePacketData(CompoundTag tag) {
        maxCows = tag.getInt("cows");
        maxPigs = tag.getInt("pigs");
        maxChickens = tag.getInt("chickens");
        maxSheep = tag.getInt("sheep");
        if (!player.level().isClientSide) {
            tileEntity.maxCowCount = maxCows;
            tileEntity.maxPigCount = maxPigs;
            tileEntity.maxChickenCount = maxChickens;
            tileEntity.maxSheepCount = maxSheep;
            tileEntity.setChanged();//mark dirty so it get saved to nbt
        }
        refreshGui();
    }

    public void sendSettingsToServer() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("cows", maxCows);
        tag.putInt("pigs", maxPigs);
        tag.putInt("chickens", maxChickens);
        tag.putInt("sheep", maxSheep);
        sendDataToServer(tag);
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        boolean send = false;
        if (maxPigs != tileEntity.maxPigCount) {
            maxPigs = tileEntity.maxPigCount;
            send = true;
        }
        if (maxChickens != tileEntity.maxChickenCount) {
            maxChickens = tileEntity.maxChickenCount;
            send = true;
        }
        if (maxSheep != tileEntity.maxSheepCount) {
            maxSheep = tileEntity.maxSheepCount;
            send = true;
        }
        if (maxCows != tileEntity.maxCowCount) {
            maxCows = tileEntity.maxCowCount;
            send = true;
        }

        if (send) {
            sendInitData();
        }
    }

}
