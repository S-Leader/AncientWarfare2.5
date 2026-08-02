package net.shadowmage.ancientwarfare.automation.container;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.items.wrapper.PlayerInvWrapper;
import net.shadowmage.ancientwarfare.automation.tile.warehouse2.TileWarehouseBase;
import net.shadowmage.ancientwarfare.automation.tile.warehouse2.TileWarehouseCraftingStation;
import net.shadowmage.ancientwarfare.core.container.ContainerCraftingRecipeMemory;
import net.shadowmage.ancientwarfare.core.container.ContainerTileBase;
import net.shadowmage.ancientwarfare.core.container.ICraftingContainer;
import net.shadowmage.ancientwarfare.core.crafting.AWCraftingManager;
import net.shadowmage.ancientwarfare.core.crafting.ICraftingRecipe;
import net.shadowmage.ancientwarfare.core.inventory.ItemHashEntry;
import net.shadowmage.ancientwarfare.core.inventory.ItemQuantityMap;
import net.shadowmage.ancientwarfare.core.util.InventoryTools;

import java.util.ArrayList;
import java.util.List;

public class ContainerWarehouseCraftingStation extends ContainerTileBase<TileWarehouseCraftingStation> implements ICraftingContainer {
    private static final int CRAFTING_SLOT = 0;
    private static final int BOOK_SLOT = 1;
    private static final String CHANGE_LIST_TAG = "changeList";

    public final ContainerCraftingRecipeMemory containerCrafting;

    private final ItemQuantityMap itemMap = new ItemQuantityMap();
    private final ItemQuantityMap cache = new ItemQuantityMap();
    private boolean shouldUpdate = true;
    private int currentCraftTotalSize;
    private IItemHandlerModifiable warehouseItemHandler;

    public ContainerWarehouseCraftingStation(Player player, int x, int y, int z) {
        super(player, x, y, z);

        this.containerCrafting = new ContainerCraftingRecipeMemory(this.tileEntity.craftingRecipeMemory, player) {
            @Override
            protected OnTakeResult handleOnTake(Player player, ItemStack stack) {
                var warehouse = ContainerWarehouseCraftingStation.this.tileEntity.getWarehouse();
                Level level = ContainerWarehouseCraftingStation.this.tileEntity.getLevel();
                ICraftingRecipe recipe = ContainerWarehouseCraftingStation.this.tileEntity.craftingRecipeMemory.getRecipe();

                if (warehouse == null || level == null || recipe == null || ContainerWarehouseCraftingStation.this.warehouseItemHandler == null) {
                    return new OnTakeResult(InteractionResult.PASS, stack);
                }

                NonNullList<ItemStack> reusableStacks = AWCraftingManager.getReusableStacks(
                        recipe,
                        ContainerWarehouseCraftingStation.this.tileEntity.craftingRecipeMemory.craftMatrix
                );

                CombinedInvWrapper combinedHandler = new CombinedInvWrapper(
                        new ItemStackHandler(reusableStacks),
                        ContainerWarehouseCraftingStation.this.warehouseItemHandler
                );

                NonNullList<ItemStack> resources = AWCraftingManager.getRecipeInventoryMatch(
                        recipe,
                        ContainerWarehouseCraftingStation.this.containerCrafting.getCraftingStacks(),
                        requested -> warehouse.getCountOf(requested) >= requested.getCount(),
                        combinedHandler
                );

                if (resources.isEmpty()) {
                    return new OnTakeResult(InteractionResult.PASS, stack);
                }

                resources = InventoryTools.removeItems(resources, reusableStacks);
                InventoryTools.removeItems(ContainerWarehouseCraftingStation.this.warehouseItemHandler, resources);

                NonNullList<ItemStack> remainingItems = InventoryTools.removeItems(
                        ContainerWarehouseCraftingStation.this.tileEntity.craftingRecipeMemory.getRemainingItems(
                                AWCraftingManager.fillCraftingMatrixFromInventory(resources)
                        ),
                        reusableStacks
                );

                InventoryTools.insertOrDropItems(
                        ContainerWarehouseCraftingStation.this.warehouseItemHandler,
                        remainingItems,
                        level,
                        ContainerWarehouseCraftingStation.this.tileEntity.getBlockPos()
                );

                return new OnTakeResult(InteractionResult.SUCCESS, stack);
            }

            @Override
            protected boolean canTakeStackFromOutput(Player player) {
                return true;
            }
        };

        for (Slot slot : this.containerCrafting.getSlots()) {
            this.addSlot(slot);
        }

        int playerInventoryY = 8 + 3 * 18 + 8;
        this.addPlayerSlots(playerInventoryY);

        TileWarehouseBase warehouse = this.tileEntity.getWarehouse();
        if (warehouse != null) {
            warehouse.addCraftingViewer(this);
        }
    }

    @Override
    public void initializeContents(int stateId, List<ItemStack> stacks, ItemStack carried) {
        this.containerCrafting.setOpening(true);
        try {
            super.initializeContents(stateId, stacks, carried);
        } finally {
            this.containerCrafting.setOpening(false);
        }
    }

    @Override
    public void setItem(int slotId, int stateId, ItemStack stack) {
        this.containerCrafting.setOpening(true);
        try {
            super.setItem(slotId, stateId, stack);
        } finally {
            this.containerCrafting.setOpening(false);
        }
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (slotId == CRAFTING_SLOT) {
            var warehouse = this.tileEntity.getWarehouse();
            this.warehouseItemHandler = warehouse == null ? null : warehouse.getItemHandler();
        }

        try {
            super.clicked(slotId, button, clickType, player);
        } finally {
            this.currentCraftTotalSize = 0;
            this.warehouseItemHandler = null;
        }
    }

    @Override
    public void removed(Player player) {
        TileWarehouseBase warehouse = this.tileEntity.getWarehouse();
        if (warehouse != null) {
            warehouse.removeCraftingViewer(this);
        }
        super.removed(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        if (slotIndex < 0 || slotIndex >= this.slots.size()) {
            return ItemStack.EMPTY;
        }

        Slot slot = this.slots.get(slotIndex);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        boolean temporaryHandler = false;
        if (slotIndex == CRAFTING_SLOT && this.warehouseItemHandler == null) {
            var warehouse = this.tileEntity.getWarehouse();
            this.warehouseItemHandler = warehouse == null ? null : warehouse.getItemHandler();
            temporaryHandler = true;
        }

        try {
            if (slotIndex == CRAFTING_SLOT && !this.updateAndCheckCraftStackOrLessInTotal()) {
                return ItemStack.EMPTY;
            }

            ItemStack stack = slot.getItem();
            ItemStack original = stack.copy();
            int playerSlotStart = 2 + this.tileEntity.craftingRecipeMemory.craftMatrix.getContainerSize();
            int playerSlotEnd = Math.min(this.slots.size(), playerSlotStart + this.playerSlots);
            int hotbarStart = playerSlotEnd - 9;

            if (slotIndex < playerSlotStart) {
                if (!this.moveItemStackTo(stack, playerSlotStart, playerSlotEnd, true)) {
                    return ItemStack.EMPTY;
                }

                if (slotIndex == CRAFTING_SLOT) {
                    slot.onQuickCraft(stack, original);
                }
            } else if (slotIndex < playerSlotEnd) {
                if (!this.moveItemStackTo(stack, BOOK_SLOT, BOOK_SLOT + 1, false)) {
                    if (slotIndex < hotbarStart) {
                        if (!this.moveItemStackTo(stack, hotbarStart, playerSlotEnd, false)) {
                            return ItemStack.EMPTY;
                        }
                    } else if (!this.moveItemStackTo(stack, playerSlotStart, hotbarStart, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            } else {
                return ItemStack.EMPTY;
            }

            if (stack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (stack.getCount() == original.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, stack);
            return original;
        } finally {
            if (temporaryHandler) {
                this.warehouseItemHandler = null;
            }
        }
    }

    private boolean updateAndCheckCraftStackOrLessInTotal() {
        ItemStack craftedStack = this.getSlot(CRAFTING_SLOT).getItem();
        this.currentCraftTotalSize += craftedStack.getCount();
        return this.currentCraftTotalSize <= craftedStack.getMaxStackSize();
    }

    @Override
    public void handlePacketData(CompoundTag tag) {
        if (tag.contains(CHANGE_LIST_TAG, Tag.TAG_LIST) && this.player.level().isClientSide) {
            this.handleChangeList(tag.getList(CHANGE_LIST_TAG, Tag.TAG_COMPOUND));
        } else if (tag.contains("recipe")) {
            this.containerCrafting.handleRecipeUpdate(tag);
        }

        this.refreshGui();
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();

        if (!this.player.level().isClientSide && this.shouldUpdate) {
            this.synchItemMaps();
            this.shouldUpdate = false;
        }
    }

    private void handleChangeList(ListTag changeList) {
        for (int i = 0; i < changeList.size(); i++) {
            this.itemMap.putEntryFromNBT(changeList.getCompound(i));
        }

        TileWarehouseBase warehouse = this.tileEntity.getWarehouse();
        if (warehouse != null) {
            warehouse.clearItemCache();
            warehouse.addItemsToCache(this.itemMap);
        }
    }

    private void synchItemMaps() {
        this.cache.clear();

        TileWarehouseBase warehouse = this.tileEntity.getWarehouse();
        if (warehouse != null) {
            warehouse.getItems(this.cache);
        }

        ListTag changeList = new ListTag();

        for (ItemHashEntry entry : new ArrayList<>(this.itemMap.keySet())) {
            int warehouseCount = this.cache.getCount(entry);
            if (this.itemMap.getCount(entry) != warehouseCount) {
                changeList.add(this.cache.writeEntryToNBT(entry));
                if (warehouseCount == 0) {
                    this.itemMap.remove(entry);
                } else {
                    this.itemMap.put(entry, warehouseCount);
                }
            }
        }

        for (ItemHashEntry entry : this.cache.keySet()) {
            if (!this.itemMap.contains(entry)) {
                int quantity = this.cache.getCount(entry);
                changeList.add(this.cache.writeEntryToNBT(entry));
                this.itemMap.put(entry, quantity);
            }
        }

        if (!changeList.isEmpty()) {
            CompoundTag tag = new CompoundTag();
            tag.put(CHANGE_LIST_TAG, changeList);
            this.sendDataToClient(tag);
        }
    }

    public void onWarehouseInventoryUpdated() {
        this.shouldUpdate = true;
    }

    @Override
    public ContainerCraftingRecipeMemory getCraftingMemoryContainer() {
        return this.containerCrafting;
    }

    @Override
    public IItemHandlerModifiable[] getInventories() {
        var warehouse = this.tileEntity.getWarehouse();
        if (warehouse == null) {
            return new IItemHandlerModifiable[]{
                    new PlayerInvWrapper(this.player.getInventory())
            };
        }

        return new IItemHandlerModifiable[]{
                warehouse.getItemHandler(),
                new PlayerInvWrapper(this.player.getInventory())
        };
    }

    @Override
    public boolean pushCraftingMatrixToInventories() {
        IItemHandler craftMatrix = new InvWrapper(this.tileEntity.craftingRecipeMemory.craftMatrix);
        NonNullList<ItemStack> craftingItems = InventoryTools.getItems(craftMatrix);
        var warehouse = this.tileEntity.getWarehouse();

        IItemHandler inventories = warehouse == null
                ? new PlayerInvWrapper(this.player.getInventory())
                : new CombinedInvWrapper(
                warehouse.getItemHandler(),
                new PlayerInvWrapper(this.player.getInventory())
        );

        if (!InventoryTools.insertItems(inventories, craftingItems, true).isEmpty()) {
            return false;
        }

        List<ItemStack> remainingItems = InventoryTools.insertItems(inventories, craftingItems, false);
        InventoryTools.emptyInventory(craftMatrix);

        if (!remainingItems.isEmpty()) {
            InventoryTools.insertItems(craftMatrix, remainingItems, false);
            return false;
        }

        return true;
    }
}