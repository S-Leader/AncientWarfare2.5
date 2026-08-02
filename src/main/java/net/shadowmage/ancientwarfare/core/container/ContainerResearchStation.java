package net.shadowmage.ancientwarfare.core.container;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import net.shadowmage.ancientwarfare.core.config.AWCoreStatics;
import net.shadowmage.ancientwarfare.core.item.ItemResearchBook;
import net.shadowmage.ancientwarfare.core.research.ResearchTracker;
import net.shadowmage.ancientwarfare.core.tile.TileResearchStation;
import net.shadowmage.ancientwarfare.core.util.Constants;
import net.shadowmage.ancientwarfare.core.util.StreamUtils;

import java.util.ArrayList;
import java.util.List;

public class ContainerResearchStation extends ContainerTileBase<TileResearchStation> {

    public boolean useAdjacentInventory;
    public String researcherName;
    public String currentGoal = "";
    public int progress = 0;
    public List<String> queuedResearch = new ArrayList<>();

    public ContainerResearchStation(Player player, int x, int y, int z) {
        super(player, x, y, z);
        researcherName = tileEntity.getCrafterName();
        useAdjacentInventory = tileEntity.useAdjacentInventory;
        if (!player.level().isClientSide) {
            if (researcherName != null) {
                currentGoal = ResearchTracker.INSTANCE.getCurrentGoal(player.level(), researcherName).orElse("");
                progress = ResearchTracker.INSTANCE.getProgress(player.level(), researcherName);
                queuedResearch.addAll(ResearchTracker.INSTANCE.getResearchQueueFor(player.level(), researcherName));
            }
        }

        Slot slot = new SlotItemHandler(tileEntity.bookInventory, 0, 8, 18 + 4) {
            @Override
            public boolean mayPlace(ItemStack par1ItemStack) {
                return ItemResearchBook.getResearcherName(par1ItemStack) != null;
            }
        };
        addSlotToContainer(slot);

        int yBase = 8 + 3 * 18 + 10 + 12 + 4 + 10;
        int slotNum = 0;
        for (int y1 = 0; y1 < 3; y1++) {
            int y2 = y1 * 18 + yBase;
            for (int x1 = 0; x1 < 3; x1++) {
                int x2 = x1 * 18 + 8 + 18;
                slot = new SlotItemHandler(tileEntity.resourceInventory, slotNum, x2, y2);
                addSlotToContainer(slot);
                slotNum++;
            }
        }

        this.addPlayerSlots();
    }

    @Override
    public ItemStack quickMoveStack(Player par1EntityPlayer, int slotClickedIndex) {
        ItemStack slotStackCopy = ItemStack.EMPTY;
        Slot theSlot = this.getSlot(slotClickedIndex);
        if (theSlot != null && theSlot.hasItem()) {
            ItemStack slotStack = theSlot.getItem();
            slotStackCopy = slotStack.copy();

            int playerSlotStart = tileEntity.bookInventory.getSlots() + tileEntity.resourceInventory.getSlots();
            int playerSlotEnd = playerSlotStart + playerSlots;
            if (slotClickedIndex < playerSlotStart)//book , storage slot
            {
                if (!this.mergeItemStack(slotStack, playerSlotStart, playerSlotEnd, false))//merge into player inventory
                {
                    return ItemStack.EMPTY;
                }
            } else if (slotClickedIndex < playerSlotEnd)//player slots, merge into storage
            {
                if (!this.mergeItemStack(slotStack, 1, playerSlotStart, false))//merge into storage
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

    @Override
    public void sendInitData() {
        CompoundTag tag = new CompoundTag();
        if (researcherName != null) {
            tag.putString("researcherName", researcherName);
        } else {
            tag.putBoolean("clearResearcher", true);
        }
        tag.putString("currentGoal", currentGoal);

        tag.putInt("progress", progress);
        if (!queuedResearch.isEmpty()) {
            tag.put("queuedResearch", queuedResearch.stream().map(StringTag::valueOf).collect(StreamUtils.toNBTTagList));
        }
        tag.putBoolean("useAdjacentInventory", useAdjacentInventory);
        tag.putInt("inventoryDirection", tileEntity.inventoryDirection.ordinal());
        tag.putInt("inventorySide", tileEntity.inventorySide.ordinal());
        this.sendDataToClient(tag);
    }

    @Override
    public void handlePacketData(CompoundTag tag) {
        if (tag.contains("researcherName")) {
            researcherName = tag.getString("researcherName");
        } else if (tag.contains("clearResearcher")) {
            researcherName = null;
        }
        if (tag.contains("currentGoal")) {
            currentGoal = tag.getString("currentGoal");
        }
        if (tag.contains("progress")) {
            progress = tag.getInt("progress");
        }
        if (tag.contains("queuedResearch")) {
            queuedResearch.clear();
            tag.getList("queuedResearch", Constants.NBT.TAG_STRING).forEach(t -> queuedResearch.add(((StringTag) t).getAsString()));
        }
        if (this.researcherName == null) {
            this.queuedResearch.clear();
            this.progress = 0;
            this.currentGoal = "";
        }
        if (tag.contains("useAdjacentInventory")) {
            this.useAdjacentInventory = tag.getBoolean("useAdjacentInventory");
            if (!player.level().isClientSide) {
                tileEntity.useAdjacentInventory = useAdjacentInventory;
            }
        }
        if (tag.contains("inventoryDirection")) {
            tileEntity.inventoryDirection = Direction.values()[tag.getInt("inventoryDirection")];
        }
        if (tag.contains("inventorySide")) {
            tileEntity.inventorySide = Direction.values()[tag.getInt("inventorySide")];
        }
        if (!player.level().isClientSide) {
            tileEntity.setChanged();
        }
        this.refreshGui();
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        if (player.level().isClientSide) {
            return;
        }
        tileEntity.addTorque(null, AWCoreStatics.researchPerTick);//do research whenever the GUI is open
        CompoundTag tag = null;
        String name = tileEntity.getCrafterName();
        /*
         * synch researcher name
         */
        if (name == null && researcherName == null) {

        } else if (name == null) {
            tag = new CompoundTag();
            researcherName = null;
            tag.putBoolean("clearResearcher", true);
            tag.putInt("currentGoal", -1);
            tag.putInt("progress", 0);
        } else if (researcherName == null || !name.equals(researcherName)) {
            tag = new CompoundTag();
            researcherName = name;
            tag.putString("researcherName", name);
            currentGoal = ResearchTracker.INSTANCE.getCurrentGoal(player.level(), researcherName).orElse("");
            tag.putString("currentGoal", currentGoal);
            progress = ResearchTracker.INSTANCE.getProgress(player.level(), researcherName);
            tag.putInt("progress", progress);
        } else {
            String g = ResearchTracker.INSTANCE.getCurrentGoal(player.level(), researcherName).orElse("");
            if (!g.equals(currentGoal)) {
                tag = new CompoundTag();
                currentGoal = g;
                tag.putString("currentGoal", currentGoal);
            }
            int p = ResearchTracker.INSTANCE.getProgress(player.level(), researcherName);
            if (p != progress) {
                if (tag == null) {
                    tag = new CompoundTag();
                }
                progress = p;
                tag.putInt("progress", progress);
            }
        }

        /*
         * synch queued research
         */
        if (researcherName != null) {
            List<String> queue = ResearchTracker.INSTANCE.getResearchQueueFor(player.level(), researcherName);
            if (!queue.equals(queuedResearch)) {
                if (tag == null) {
                    tag = new CompoundTag();
                }
                queuedResearch.clear();
                queuedResearch.addAll(queue);
                tag.put("queuedResearch", queuedResearch.stream().map(StringTag::valueOf).collect(StreamUtils.toNBTTagList));
            }
        } else {
            queuedResearch.clear();
        }

        /*
         * synch use-adjacent inventory status
         */
        if (tileEntity.useAdjacentInventory != useAdjacentInventory) {
            if (tag == null) {
                tag = new CompoundTag();
            }
            this.useAdjacentInventory = tileEntity.useAdjacentInventory;
            tag.putBoolean("useAdjacentInventory", useAdjacentInventory);
        }

        if (tag != null) {
            this.sendDataToClient(tag);
        }

    }

    /*
     * should be called from client-side to send update packet to server
     */
    public void toggleUseAdjacentInventory() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("useAdjacentInventory", !useAdjacentInventory);
        useAdjacentInventory = !useAdjacentInventory;
        sendDataToServer(tag);
    }

    public void onSidePressed() {
        int o = (tileEntity.inventorySide.ordinal() + 1) % Direction.values().length;
        CompoundTag tag = new CompoundTag();
        tag.putInt("inventorySide", o);
        sendDataToServer(tag);
        tileEntity.inventorySide = Direction.values()[o];
    }

    public void onDirPressed() {
        int o = (tileEntity.inventoryDirection.ordinal() + 1) % Direction.values().length;
        CompoundTag tag = new CompoundTag();
        tag.putInt("inventoryDirection", o);
        sendDataToServer(tag);
        tileEntity.inventoryDirection = Direction.values()[o];
    }
}
