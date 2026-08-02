package net.shadowmage.ancientwarfare.structure.container;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.shadowmage.ancientwarfare.core.container.ContainerBase;

public class ContainerStructureSelectionBase extends ContainerBase {

    public String structureName;

    public ContainerStructureSelectionBase(Player player) {
        super(player);
    }

    public void handleNameSelection(String name) {
        CompoundTag tag = new CompoundTag();
        tag.putString("structName", name);
        sendDataToServer(tag);
    }

}
