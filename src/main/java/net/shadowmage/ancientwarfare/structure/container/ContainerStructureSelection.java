package net.shadowmage.ancientwarfare.structure.container;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.shadowmage.ancientwarfare.core.util.EntityTools;
import net.shadowmage.ancientwarfare.structure.item.ItemStructureBuilder;
import net.shadowmage.ancientwarfare.structure.item.ItemStructureBuilderWorldGen;
import net.shadowmage.ancientwarfare.structure.item.ItemStructureSettings;

public class ContainerStructureSelection extends ContainerStructureSelectionBase {

    private ItemStructureSettings buildSettings;

    public ContainerStructureSelection(Player player, int x, int y, int z) {
        super(player);
        buildSettings = ItemStructureSettings.getSettingsFor(EntityTools.getItemFromEitherHand(player, ItemStructureBuilder.class, ItemStructureBuilderWorldGen.class));
        structureName = buildSettings.hasName() ? buildSettings.name() : null;
        addPlayerSlots();
        removeSlots();
    }

    @Override
    public void handlePacketData(CompoundTag tag) {
        if (!player.level().isClientSide && tag.contains("structName")) {
            ItemStack stack = EntityTools.getItemFromEitherHand(player, ItemStructureBuilder.class, ItemStructureBuilderWorldGen.class);
            buildSettings = ItemStructureSettings.getSettingsFor(stack);
            buildSettings.setName(tag.getString("structName"));
            ItemStructureSettings.setSettingsFor(stack, buildSettings);
        }
    }

}
