package net.shadowmage.ancientwarfare.structure.container;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.shadowmage.ancientwarfare.core.network.NetworkHandler;
import net.shadowmage.ancientwarfare.core.network.PacketGui;
import net.shadowmage.ancientwarfare.core.util.BlockTools;
import net.shadowmage.ancientwarfare.core.util.WorldTools;
import net.shadowmage.ancientwarfare.structure.tile.SpawnerSettings;
import net.shadowmage.ancientwarfare.structure.tile.TileAdvancedSpawner;

import java.util.Optional;

public class ContainerSpawnerAdvancedInventoryBlock extends ContainerSpawnerAdvancedInventoryBase {
    private static final String SPAWNER_SETTINGS_TAG = "spawnerSettings";
    private TileAdvancedSpawner spawner;

    public ContainerSpawnerAdvancedInventoryBlock(Player player, int x, int y, int z) {
        super(player, x, y, z);

        Optional<TileAdvancedSpawner> te = WorldTools.getTile(player.level(), new BlockPos(x, y, z), TileAdvancedSpawner.class);
        if (!player.level().isClientSide && te.isPresent()) {
            spawner = te.get();
            settings = spawner.getSettings();
        } else {
            settings = SpawnerSettings.getDefaultSettings();
        }
        inventory = settings.getInventory();
        this.addSettingsInventorySlots();
        this.addPlayerSlots(8, 70, 8);
    }

    @Override
    public boolean stillValid(Player var1) {
        return var1.distanceToSqr(Vec3.atCenterOf(spawner.getBlockPos())) <= 64D;
    }

    @Override
    public void sendInitData() {
        if (!player.level().isClientSide) {
            sendSettingsToClient();
        }
    }

    private void sendSettingsToClient() {
        CompoundTag tag = new CompoundTag();
        settings.writeToNBT(tag);

        PacketGui pkt = new PacketGui();
        pkt.setTag(SPAWNER_SETTINGS_TAG, tag);
        pkt.setMenuTarget(containerId);
        NetworkHandler.sendToPlayer((ServerPlayer) player, pkt);
    }

    @Override
    public void handlePacketData(CompoundTag tag) {
        if (tag.contains(SPAWNER_SETTINGS_TAG)) {
            if (player.level().isClientSide) {
                settings.readFromNBT(tag.getCompound(SPAWNER_SETTINGS_TAG));
                this.refreshGui();
            } else {
                spawner.readFromNBT(tag);
                BlockTools.notifyBlockUpdate(spawner);
            }
        }
    }

}
