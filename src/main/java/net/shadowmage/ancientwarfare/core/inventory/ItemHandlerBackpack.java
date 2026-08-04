package net.shadowmage.ancientwarfare.core.inventory;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.shadowmage.ancientwarfare.core.item.ItemBackpack;
import net.shadowmage.ancientwarfare.core.item.ItemLegacyBackpack;

public class ItemHandlerBackpack implements IItemHandlerModifiable {
    private static final String BACKPACK_ITEMS_TAG = "backpackItems";
    private final ItemStackHandler backpackInventory;
    private final ItemStack backpackStack;

    public ItemHandlerBackpack(ItemStack backpackStack) {
        backpackInventory = getHandler(backpackStack);
        this.backpackStack = backpackStack;
    }

    @Override
    public int getSlots() {
        return backpackInventory.getSlots();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return backpackInventory.getStackInSlot(slot);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (ItemBackpack.isBackpack(stack)) {
            return stack;
        }
        ItemStack ret = backpackInventory.insertItem(slot, stack, simulate);
        if (!simulate && ret.getCount() < stack.getCount()) {
            saveToStack(backpackInventory);
        }
        return ret;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        ItemStack ret = backpackInventory.extractItem(slot, amount, simulate);
        if (!simulate && !ret.isEmpty()) {
            saveToStack(backpackInventory);
        }
        return ret;
    }

    @Override
    public int getSlotLimit(int slot) {
        return backpackInventory.getSlotLimit(slot);
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return !ItemBackpack.isBackpack(stack) && backpackInventory.isItemValid(slot, stack);
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        backpackInventory.setStackInSlot(slot, stack);
        saveToStack(backpackInventory);
    }

    private ItemStackHandler getHandler(ItemStack stack) {
        int slots;
        if (stack.getItem() instanceof ItemBackpack backpack) {
            slots = backpack.getSlotCount();
        } else if (stack.getItem() instanceof ItemLegacyBackpack) {
            // Old saves may still contain legacy backpacks inside NPC equipment
            // or non-player inventories where inventoryTick cannot migrate them.
            slots = (Math.max(0, Math.min(3, stack.getDamageValue())) + 1) * 9;
        } else {
            return new ItemStackHandler();
        }

        ItemStackHandler handler = new ItemStackHandler(slots);
        if (stack.hasTag() && stack.getTag().contains(BACKPACK_ITEMS_TAG)) {
            handler.deserializeNBT(stack.getTag().getCompound(BACKPACK_ITEMS_TAG));
        }
        return handler;
    }

    private void saveToStack(ItemStackHandler handler) {
        CompoundTag invTag = handler.serializeNBT();
        backpackStack.getOrCreateTag().put(BACKPACK_ITEMS_TAG, invTag);
    }
}
