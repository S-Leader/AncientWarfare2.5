package net.shadowmage.ancientwarfare.structure.container;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.shadowmage.ancientwarfare.core.container.ContainerBase;
import net.shadowmage.ancientwarfare.core.network.NetworkHandler;
import net.shadowmage.ancientwarfare.core.network.PacketGui;
import net.shadowmage.ancientwarfare.structure.tile.SpawnerSettings;

public abstract class ContainerSpawnerAdvancedBase extends ContainerBase {

    public SpawnerSettings settings;

    public ContainerSpawnerAdvancedBase(Player player) {
        super(player);
    }

    public void sendSettingsToServer() {
        NetworkHandler.sendToServer(getSettingPacket());
    }

    public PacketGui getSettingPacket() {
        CompoundTag tag = new CompoundTag();
        settings.writeToNBT(tag);

        PacketGui pkt = new PacketGui();
        pkt.setTag("spawnerSettings", tag);
        pkt.setMenuTarget(containerId);
        return pkt;
    }

}
