package net.shadowmage.ancientwarfare.structure.container;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.shadowmage.ancientwarfare.core.container.ContainerBase;
import net.shadowmage.ancientwarfare.core.util.EntityTools;
import net.shadowmage.ancientwarfare.structure.item.ItemTownBuilder;

public class ContainerTownSelection extends ContainerBase {
    public String townName;
    private int width;
    private int length;

    public ContainerTownSelection(Player player, int x, int y, int z) {
        super(player);

        ItemStack townBuilder = EntityTools.getItemFromEitherHand(player, ItemTownBuilder.class);
        if (townBuilder.isEmpty()) {
            return;
        }

        townName = ItemTownBuilder.getTownName(townBuilder);
        width = ItemTownBuilder.getWidth(townBuilder);
        length = ItemTownBuilder.getLength(townBuilder);
    }

    public void handleNameSelection(String name) {
        sendDataToServer("townName", StringTag.valueOf(name));
    }

    public void handleWidthUpdate(int width) {
        sendDataToServer("width", IntTag.valueOf(width));
    }

    public void handleLengthUpdate(int length) {
        sendDataToServer("length", IntTag.valueOf(length));
    }

    @Override
    public void handlePacketData(CompoundTag tag) {
        if (player.level().isClientSide) {
            return;
        }
        ItemStack townBuilder = EntityTools.getItemFromEitherHand(player, ItemTownBuilder.class);
        if (tag.contains("townName")) {
            ItemTownBuilder.setTownName(townBuilder, tag.getString("townName"));
        }

        if (tag.contains("width")) {
            ItemTownBuilder.setWidth(townBuilder, tag.getInt("width"));
        }

        if (tag.contains("length")) {
            ItemTownBuilder.setLength(townBuilder, tag.getInt("length"));
        }
    }

    public int getWidth() {
        return width;
    }

    public int getLength() {
        return length;
    }
}
