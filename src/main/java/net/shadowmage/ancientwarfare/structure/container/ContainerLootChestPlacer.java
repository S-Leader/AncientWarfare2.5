package net.shadowmage.ancientwarfare.structure.container;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.shadowmage.ancientwarfare.core.container.ContainerBase;
import net.shadowmage.ancientwarfare.core.util.EntityTools;
import net.shadowmage.ancientwarfare.core.util.NBTBuilder;
import net.shadowmage.ancientwarfare.structure.config.AWStructureStatics;
import net.shadowmage.ancientwarfare.structure.item.ItemLootChestPlacer;
import net.shadowmage.ancientwarfare.structure.tile.LootSettings;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ContainerLootChestPlacer extends ContainerBase {
    private static final String LOOT_CONTAINER_NAME_TAG = "lootContainerName";
    private final ItemStack placer;

    public ContainerLootChestPlacer(Player player, int x, int y, int z) {
        super(player);
        placer = EntityTools.getItemFromEitherHand(player, ItemLootChestPlacer.class);
    }

    public List<String> getLootTableNames() {
        MinecraftServer server = player.level().getServer();
        if (server == null) {
            return Collections.emptyList();
        }
        return server.getLootData().getKeys(LootDataType.TABLE).stream().map(ResourceLocation::toString).filter(rl -> !AWStructureStatics.lootTableExclusions.contains(rl))
                .collect(Collectors.toList());
    }

    @Override
    public void handlePacketData(CompoundTag tag) {
        if (tag.contains(LOOT_CONTAINER_NAME_TAG)) {
            setContainer(tag.getString(LOOT_CONTAINER_NAME_TAG));
            return;
        }

        setLootSettings(LootSettings.deserializeNBT(tag));
    }

    public void setLootSettings(LootSettings lootSettings) {
        if (player.level().isClientSide) {
            sendDataToServer(lootSettings.serializeNBT());
        }

        ItemLootChestPlacer.setLootSettings(placer, lootSettings);
    }

    public Optional<LootSettings> getLootSettings() {
        return ItemLootChestPlacer.getLootSettings(placer);
    }

    public ItemLootChestPlacer.LootContainerInfo getLootContainerInfo() {
        return ItemLootChestPlacer.getLootContainerInfo(placer);
    }

    public void setContainer(String blockName) {
        if (player.level().isClientSide) {
            sendDataToServer(new NBTBuilder().setString(LOOT_CONTAINER_NAME_TAG, blockName).build());
        }

        ItemLootChestPlacer.setContainerName(placer, blockName);
    }
}
