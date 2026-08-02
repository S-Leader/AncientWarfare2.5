package net.shadowmage.ancientwarfare.npc.container;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.shadowmage.ancientwarfare.core.compat.CapabilityItemHandler;
import net.shadowmage.ancientwarfare.core.container.ContainerTileBase;
import net.shadowmage.ancientwarfare.core.util.Constants;
import net.shadowmage.ancientwarfare.npc.tile.TileTownHall;
import net.shadowmage.ancientwarfare.npc.tile.TileTownHall.NpcDeathEntry;

import java.util.ArrayList;
import java.util.List;

public class ContainerTownHall extends ContainerTileBase<TileTownHall> {

    List<NpcDeathEntry> deathList = new ArrayList<>();

    public ContainerTownHall(Player player, int x, int y, int z) {
        super(player, x, y, z);
        int xPos, yPos;
        IItemHandler handler = tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).orElse(null);
        for (int i = 0; i < handler.getSlots(); i++) {
            xPos = (i % 9) * 18 + 8;
            yPos = (i / 9) * 18 + 8 + 16;
            addSlotToContainer(new SlotItemHandler(handler, i, xPos, yPos));
        }
        addPlayerSlots(8 + 3 * 18 + 8 + 16);
        if (!player.level().isClientSide) {
            deathList.addAll(tileEntity.getDeathList());
            tileEntity.addViewer(this);
        }
    }

    @Override
    public void handlePacketData(CompoundTag tag) {
        if (tag.contains("deathList")) {
            deathList.clear();
            ListTag list = tag.getList("deathList", Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                deathList.add(new NpcDeathEntry(list.getCompound(i)));
            }
            refreshGui();
        } else if (tag.contains("clear")) {
            tileEntity.clearDeathNotices();
        }

        if (tag.contains("range")) {
            tileEntity.setRange(tag.getInt("range"));
            refreshGui();
        }

        if (tag.contains("name")) {
            tileEntity.setName(tag.getString("name"));
            refreshGui();
        }

        if (!tileEntity.getWorld().isClientSide) {
            tileEntity.setChanged();
        }
    }

    @Override
    public void sendInitData() {
        sendTownHallDataToClient(false);
    }

    @Override
    public void onContainerClosed(Player par1EntityPlayer) {
        super.onContainerClosed(par1EntityPlayer);
        tileEntity.removeViewer(this);
    }

    public void onTownHallDeathListUpdated() {
        this.deathList.clear();
        this.deathList.addAll(tileEntity.getDeathList());
        sendTownHallDataToClient(true);
    }

    public void setRange(int value) {
        tileEntity.setRange(value);
        CompoundTag tag = new CompoundTag();
        tag.putInt("range", value);
        sendDataToServer(tag);
    }

    public void setName(String name) {
        tileEntity.setName(name);
        CompoundTag tag = new CompoundTag();
        tag.putString("name", name);
        sendDataToServer(tag);
    }

    public void teleportPlayer(String playerName) {
        CompoundTag tag = new CompoundTag();
        tag.putString("playerName", playerName);
        sendDataToServer(tag);
    }

    public void clearList() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("clear", true);
        sendDataToServer(tag);
    }

    private void sendTownHallDataToClient(boolean onlyDeathList) {
        ListTag list = new ListTag();
        for (NpcDeathEntry entry : deathList) {
            list.add(entry.writeToNBT(new CompoundTag()));
        }
        CompoundTag tag = new CompoundTag();
        tag.put("deathList", list);
        if (!onlyDeathList) {
            tag.putInt("range", tileEntity.getRange());
            tag.putString("name", tileEntity.getName());
        }
        sendDataToClient(tag);
    }

    public List<NpcDeathEntry> getDeathList() {
        return deathList;
    }

    @Override
    public ItemStack quickMoveStack(Player par1EntityPlayer, int slotClickedIndex) {
        ItemStack slotStackCopy = ItemStack.EMPTY;
        Slot theSlot = this.getSlot(slotClickedIndex);
        if (theSlot.hasItem()) {
            ItemStack slotStack = theSlot.getItem();
            slotStackCopy = slotStack.copy();
            IItemHandler handler = tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).orElse(null);
            if (slotClickedIndex < handler.getSlots())//book slot
            {
                if (!this.mergeItemStack(slotStack, handler.getSlots(), handler.getSlots() + playerSlots, false))//merge into player inventory
                {
                    return ItemStack.EMPTY;
                }
            } else {
                if (!this.mergeItemStack(slotStack, 0, handler.getSlots(), false))//merge into player inventory
                {
                    return ItemStack.EMPTY;
                }
            }
            if (slotStack.getCount() == 0) {
                theSlot.set(ItemStack.EMPTY);
            } else {
                theSlot.setChanged();
            }
            if (slotStack.getCount() == slotStackCopy.getCount()) {
                return ItemStack.EMPTY;
            }
            theSlot.onTake(par1EntityPlayer, slotStack);
        }
        return slotStackCopy;
    }

}
