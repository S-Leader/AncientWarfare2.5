package net.shadowmage.ancientwarfare.structure.container;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.items.SlotItemHandler;
import net.shadowmage.ancientwarfare.core.util.Constants;
import net.shadowmage.ancientwarfare.core.util.InventoryTools;
import net.shadowmage.ancientwarfare.core.util.WorldTools;
import net.shadowmage.ancientwarfare.structure.tile.TileDraftingStation;

import java.util.Optional;

public class ContainerDraftingStation extends ContainerStructureSelectionBase {
    private static final String IS_STARTED_TAG = "isStarted";
    private static final String IS_FINISHED_TAG = "isFinished";
    private static final String REMAINING_TIME_TAG = "remainingTime";
    private static final String TOTAL_TIME_TAG = "totalTime";
    private static final String STRUCT_NAME_TAG = "structName";
    private static final String RESOURCE_LIST_TAG = "resourceList";
    public boolean isStarted = false;
    private boolean isFinished = false;
    private int remainingTime;
    private int totalTime;
    public final NonNullList<ItemStack> neededResources = NonNullList.create();

    private final TileDraftingStation tile;

    public ContainerDraftingStation(Player player, int x, int y, int z) {
        super(player);
        Optional<TileDraftingStation> te = WorldTools.getTile(player.level(), new BlockPos(x, y, z), TileDraftingStation.class);
        if (!te.isPresent()) {
            throw new IllegalArgumentException("No drafting station");
        }
        tile = te.get();
        structureName = tile.getCurrentTemplateName();
        neededResources.addAll(InventoryTools.copyStacks(tile.getNeededResources()));
        isStarted = tile.isStarted();
        isFinished = tile.isFinished();
        remainingTime = tile.getRemainingTime();
        totalTime = tile.getTotalTime();

        int y2 = 94;

        int xp;
        int yp;
        int slotNum;
        for (int y1 = 0; y1 < 3; y1++) {
            for (int x1 = 0; x1 < 9; x1++) {
                slotNum = y1 * 9 + x1;
                xp = 8 + x1 * 18;
                yp = y2 + y1 * 18;
                addSlotToContainer(new SlotItemHandler(tile.inputSlots, slotNum, xp, yp));
            }
        }

        addSlotToContainer(new SlotItemHandler(tile.outputSlot, 0, 8 + 4 * 18, 94 - 16 - 18) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });

        this.addPlayerSlots(156);
    }

    @Override
    public boolean stillValid(Player var1) {
        return var1.distanceToSqr(Vec3.atCenterOf(tile.getBlockPos())) <= 64D;
    }

    @Override
    public ItemStack quickMoveStack(Player par1EntityPlayer, int slotClickedIndex) {
        ItemStack slotStackCopy = ItemStack.EMPTY;
        Slot theSlot = this.getSlot(slotClickedIndex);
        if (theSlot != null && theSlot.hasItem()) {
            ItemStack slotStack = theSlot.getItem();
            slotStackCopy = slotStack.copy();

            int playerSlotStart = tile.inputSlots.getSlots();
            int playerSlotEnd = playerSlotStart + playerSlots;
            if (slotClickedIndex < playerSlotStart)//storage slots
            {
                if (!this.mergeItemStack(slotStack, playerSlotStart, playerSlotEnd, false))//merge into player inventory
                {
                    return ItemStack.EMPTY;
                }
            } else if (slotClickedIndex < playerSlotEnd)//player slots, merge into storage
            {
                if (!this.mergeItemStack(slotStack, 0, playerSlotStart, false))//merge into storage
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
        tag.putBoolean(IS_STARTED_TAG, isStarted);
        tag.putBoolean(IS_FINISHED_TAG, isFinished);
        tag.putInt(REMAINING_TIME_TAG, remainingTime);
        tag.putInt(TOTAL_TIME_TAG, totalTime);
        if (structureName != null) {
            tag.putString(STRUCT_NAME_TAG, structureName);
        }
        tag.put(RESOURCE_LIST_TAG, getResourceListTag(neededResources));
        this.sendDataToClient(tag);
    }

    private ListTag getResourceListTag(NonNullList<ItemStack> resources) {
        ListTag list = new ListTag();
        CompoundTag tag;
        for (ItemStack item : resources) {
            tag = new CompoundTag();
            item.save(tag);
            list.add(tag);
        }
        return list;
    }

    private void readResourceList(ListTag list, NonNullList<ItemStack> resources) {
        CompoundTag tag;
        ItemStack stack;
        for (int i = 0; i < list.size(); i++) {
            tag = list.getCompound(i);
            stack = ItemStack.of(tag);
            if (!stack.isEmpty()) {
                resources.add(stack);
            }
        }
    }

    public void handleStopInput() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("stop", true);
        this.sendDataToServer(tag);
    }

    public void handleStartInput() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("start", true);
        this.sendDataToServer(tag);
    }

    @Override
    public void handlePacketData(CompoundTag tag) {
        if (tag.contains(STRUCT_NAME_TAG)) {
            if (player.level().isClientSide) {
                this.structureName = tag.getString(STRUCT_NAME_TAG);
            } else {
                tile.setTemplate(tag.getString(STRUCT_NAME_TAG));
            }
        } else if (tag.contains("clearName")) {
            structureName = null;
        }
        if (tag.contains(IS_STARTED_TAG)) {
            isStarted = tag.getBoolean(IS_STARTED_TAG);
        }
        if (tag.contains(IS_FINISHED_TAG)) {
            isFinished = tag.getBoolean(IS_FINISHED_TAG);
        }
        if (tag.contains(REMAINING_TIME_TAG)) {
            remainingTime = tag.getInt(REMAINING_TIME_TAG);
        }
        if (tag.contains(TOTAL_TIME_TAG)) {
            totalTime = tag.getInt(TOTAL_TIME_TAG);
        }
        if (tag.contains(RESOURCE_LIST_TAG)) {
            neededResources.clear();
            readResourceList(tag.getList(RESOURCE_LIST_TAG, Constants.NBT.TAG_COMPOUND), neededResources);
        }
        if (tag.contains("stop")) {
            tile.stopCurrentWork();
        } else if (tag.contains("start")) {
            tile.tryStart();
        }
        refreshGui();
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        String tileName = tile.getCurrentTemplateName();
        CompoundTag tag = null;
        if ((structureName == null && tileName != null) || (tileName == null && structureName != null)) {
            tag = new CompoundTag();
            this.structureName = tileName;
            if (this.structureName == null) {
                tag.putBoolean("clearName", true);
            } else {
                tag.putString(STRUCT_NAME_TAG, structureName);
            }
        } else if (structureName != null && !structureName.equals(tileName)) {
            structureName = tileName;
            tag = new CompoundTag();
            tag.putString(STRUCT_NAME_TAG, structureName);
        }
        if (tile.isFinished() != isFinished) {
            if (tag == null) {
                tag = new CompoundTag();
            }
            isFinished = tile.isFinished();
            tag.putBoolean(IS_FINISHED_TAG, isFinished);
        }
        if (tile.isStarted() != isStarted) {
            if (tag == null) {
                tag = new CompoundTag();
            }
            isStarted = tile.isStarted();
            tag.putBoolean(IS_STARTED_TAG, isStarted);
        }
        if (tile.getRemainingTime() != remainingTime) {
            if (tag == null) {
                tag = new CompoundTag();
            }
            remainingTime = tile.getRemainingTime();
            tag.putInt(REMAINING_TIME_TAG, remainingTime);
        }
        if (tile.getTotalTime() != totalTime) {
            if (tag == null) {
                tag = new CompoundTag();
            }
            totalTime = tile.getTotalTime();
            tag.putInt(TOTAL_TIME_TAG, totalTime);
        }
        if (!neededResources.equals(tile.getNeededResources())) {
            if (tag == null) {
                tag = new CompoundTag();
            }
            neededResources.clear();
            neededResources.addAll(InventoryTools.copyStacks(tile.getNeededResources()));
            ListTag list = getResourceListTag(neededResources);
            tag.put(RESOURCE_LIST_TAG, list);
        }
        if (tag != null) {
            sendDataToClient(tag);
        }
    }

}
