package net.shadowmage.ancientwarfare.structure.container;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.shadowmage.ancientwarfare.core.network.NetworkHandler;
import net.shadowmage.ancientwarfare.core.util.BlockTools;
import net.shadowmage.ancientwarfare.core.util.WorldTools;
import net.shadowmage.ancientwarfare.structure.tile.TileAdvancedSpawner;

import java.util.Optional;

public class ContainerSpawnerAdvancedBlock extends ContainerSpawnerAdvancedBase {
    private static final String SPAWNER_SETTINGS_TAG = "spawnerSettings";
    private final TileAdvancedSpawner spawner;

    public ContainerSpawnerAdvancedBlock(Player player, int x, int y, int z) {
        super(player);
        Optional<TileAdvancedSpawner> te = WorldTools.getTile(player.level(), new BlockPos(x, y, z), TileAdvancedSpawner.class);
        if (te.isPresent()) {
            spawner = te.get();
            settings = spawner.getSettings();
        } else {
            throw new IllegalArgumentException("Spawner not found");
        }
    }

    @Override
    public void sendInitData() {
        if (!spawner.getWorld().isClientSide) {
            NetworkHandler.sendToPlayer((ServerPlayer) player, getSettingPacket());
        }
    }

    @Override
    public void handlePacketData(CompoundTag tag) {
        if (tag.contains(SPAWNER_SETTINGS_TAG)) {
            if (spawner.getWorld().isClientSide) {
                settings.readFromNBT(tag.getCompound(SPAWNER_SETTINGS_TAG));
                this.refreshGui();
            } else {
                spawner.getSettings().readFromNBT(tag.getCompound(SPAWNER_SETTINGS_TAG));
                spawner.setChanged();
                BlockTools.notifyBlockUpdate(spawner);
            }
        }
    }
}
