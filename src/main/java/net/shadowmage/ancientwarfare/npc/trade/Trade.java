package net.shadowmage.ancientwarfare.npc.trade;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.shadowmage.ancientwarfare.core.util.Constants;
import net.shadowmage.ancientwarfare.core.util.InventoryTools;

import javax.annotation.Nullable;
import java.util.List;

/*
 * Created by Olivier on 12/05/2015.
 */
public abstract class Trade {
    protected NonNullList<ItemStack> input = NonNullList.withSize(size(), ItemStack.EMPTY);
    protected NonNullList<ItemStack> output = NonNullList.withSize(size(), ItemStack.EMPTY);

    public int size() {
        return 9;
    }

    public ItemStack getInputStack(int index) {
        return input.get(index);
    }

    public List<ItemStack> getInput() {
        return input;
    }

    public ItemStack getOutputStack(int index) {
        return output.get(index);
    }

    public List<ItemStack> getOutput() {
        return output;
    }

    public void setInputStack(int index, ItemStack stack) {
        input.set(index, stack);
    }

    public void setOutputStack(int index, ItemStack stack) {
        output.set(index, stack);
    }

    /*
     * If items are all present in trade grid, delegate to #doTrade
     */
    public boolean performTrade(Player player, @Nullable IItemHandler storage) {
        NonNullList<ItemStack> list = NonNullList.create();
        for (ItemStack temp : input) {
            if (!temp.isEmpty()) {
                list.add(temp.copy());
            }
        }
        list = InventoryTools.compactStackList(list);
        for (ItemStack stack : list) {
            if (!hasEnoughOf(player, stack)) {
                return false;
            }
        }
        doTrade(player, storage);
        return true;
    }

    private boolean hasEnoughOf(Player player, ItemStack stack) {
        return InventoryTools.getItemHandlerFrom(player, null).map(handler -> InventoryTools.getCountOf(handler, stack) >= stack.getCount())
                .orElse(false);
    }

    /*
     * will remove necessary items from player inventory and add to storage
     * and remove result from storage and merge into player inventory/drop on ground<br>
     */
    protected void doTrade(Player player, @Nullable IItemHandler storage) {
        InventoryTools.getItemHandlerFrom(player, null).ifPresent(playerInventory -> {
            for (ItemStack inputStack : input) {
                if (inputStack.isEmpty()) {
                    continue;
                }
                ItemStack result = InventoryTools.removeItems(playerInventory, inputStack, inputStack.getCount());//remove from trade grid
                if (!result.isEmpty() && storage != null) {
                    InventoryTools.mergeItemStack(storage, result);//merge into storage
                }
            }
            for (ItemStack outputStack : output) {
                if (outputStack.isEmpty()) {
                    continue;
                }
                if (storage != null)
                    outputStack = InventoryTools.removeItems(storage, outputStack, outputStack.getCount());//remove from storage
                else
                    outputStack = outputStack.copy();
                outputStack = InventoryTools.mergeItemStack(playerInventory, outputStack);//merge into player inventory, drop any unused portion on next line
                if (!outputStack.isEmpty() && !player.level().isClientSide) {//only drop into world if on server!
                    Containers.dropItemStack(player.level(), player.getX(), player.getY(), player.getZ(), outputStack);
                }
            }
        });
    }

    public CompoundTag writeToNBT(CompoundTag tag) {
        ListTag list = new ListTag();
        CompoundTag itemTag;

        for (int i = 0; i < input.size(); i++) {
            if (input.get(i).isEmpty()) {
                continue;
            }
            itemTag = input.get(i).save(new CompoundTag());
            itemTag.putInt("slot", i);
            list.add(itemTag);
        }
        tag.put("inputItems", list);

        list = new ListTag();
        for (int i = 0; i < output.size(); i++) {
            if (output.get(i).isEmpty()) {
                continue;
            }
            itemTag = output.get(i).save(new CompoundTag());
            itemTag.putInt("slot", i);
            list.add(itemTag);
        }
        tag.put("outputItems", list);
        return tag;
    }

    public void readFromNBT(CompoundTag tag) {
        CompoundTag itemTag;

        ListTag inputList = tag.getList("inputItems", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < inputList.size(); i++) {
            itemTag = inputList.getCompound(i);
            input.set(itemTag.getInt("slot"), ItemStack.of(itemTag));
        }

        ListTag outputList = tag.getList("outputItems", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < outputList.size(); i++) {
            itemTag = outputList.getCompound(i);
            output.set(itemTag.getInt("slot"), ItemStack.of(itemTag));
        }
    }

    public boolean isValid() {
        return input.stream().anyMatch(s -> !s.isEmpty()) && output.stream().anyMatch(s -> !s.isEmpty());
    }
}
