package net.shadowmage.ancientwarfare.npc.datafixes;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.shadowmage.ancientwarfare.core.datafixes.ILegacyDataFixer;
import net.shadowmage.ancientwarfare.core.util.Constants;
import net.shadowmage.ancientwarfare.core.util.RegistryTools;
import net.shadowmage.ancientwarfare.npc.init.AWNPCItems;

import java.util.Arrays;

public class RoutingOrderFilterCountsFixer implements ILegacyDataFixer {
    @Override
    public int getFixVersion() {
        return 6;
    }

    @Override
    public CompoundTag fixTagCompound(CompoundTag compound) {
        String id = compound.getString("id");

        //noinspection ConstantConditions
        if (id.equals(RegistryTools.getRegistryName(AWNPCItems.ROUTING_ORDER).toString())) {
            CompoundTag tag = compound.getCompound("tag");
            ListTag entryList = tag.getCompound("orders").getList("entryList", Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < entryList.size(); i++) {
                CompoundTag routePointNBT = entryList.getCompound(i);
                ListTag filterList = routePointNBT.getList("filterList", Constants.NBT.TAG_COMPOUND);
                int[] filterCounts = new int[12];
                Arrays.fill(filterCounts, 0);
                for (int j = 0; j < filterList.size(); j++) {
                    CompoundTag itemTag = filterList.getCompound(j);
                    int slot = itemTag.getInt("slot");

                    ItemStack filterStack = ItemStack.of(itemTag);
                    filterCounts[slot] = filterStack.getCount();
                }
                routePointNBT.putIntArray("filterCounts", filterCounts);
            }
        }
        return compound;
    }
}
