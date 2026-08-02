package net.shadowmage.ancientwarfare.structure.container;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.shadowmage.ancientwarfare.core.util.EntityTools;
import net.shadowmage.ancientwarfare.structure.item.ItemBlockAdvancedSpawner;
import net.shadowmage.ancientwarfare.structure.item.ItemSpawnerPlacer;
import net.shadowmage.ancientwarfare.structure.tile.SpawnerSettings;

public class ContainerSpawnerAdvanced extends ContainerSpawnerAdvancedBase {
    private static final String SPAWNER_SETTINGS_TAG = "spawnerSettings";

    @SuppressWarnings("unused") //used in reflection
    public ContainerSpawnerAdvanced(Player player, int x, int y, int z) {
        super(player);
        settings = new SpawnerSettings();
        ItemStack item = EntityTools.getItemFromEitherHand(player, ItemBlockAdvancedSpawner.class);
        if (!item.isEmpty()) {
            //noinspection ConstantConditions
            if (!item.hasTag() || !item.getTag().contains(SPAWNER_SETTINGS_TAG)) {
                throw new IllegalArgumentException("stack must have correct data!!");
            }
            settings.readFromNBT(item.getTag().getCompound(SPAWNER_SETTINGS_TAG));
        } else {
            item = EntityTools.getItemFromEitherHand(player, ItemSpawnerPlacer.class);
            if (!ItemSpawnerPlacer.hasSpawnerData(item)) {
                throw new IllegalArgumentException("stack must have correct data!!");
            }
            settings.readFromNBT(ItemSpawnerPlacer.getSpawnerData(item));
        }
    }

    @Override
    public void handlePacketData(CompoundTag tag) {
        if (tag.contains(SPAWNER_SETTINGS_TAG)) {
            ItemStack item = EntityTools.getItemFromEitherHand(player, ItemBlockAdvancedSpawner.class);
            if (!item.isEmpty()) {
                item.getOrCreateTag().put(SPAWNER_SETTINGS_TAG, tag.getCompound(SPAWNER_SETTINGS_TAG));
            } else {
                item = EntityTools.getItemFromEitherHand(player, ItemSpawnerPlacer.class);
                ItemSpawnerPlacer.setSpawnerData(item, tag.getCompound(SPAWNER_SETTINGS_TAG));
            }
        }
    }
}
