package net.shadowmage.ancientwarfare.automation.datafixes;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.shadowmage.ancientwarfare.core.datafixes.LegacyDataFixerRegistry;
import net.shadowmage.ancientwarfare.core.util.Constants;

/**
 * Applies legacy item fixers to item-map entries embedded in warehouse NBT.
 */
public final class ItemMapDataWalker {
    private final String[] tagPathElements;

    public ItemMapDataWalker(String tagPath) {
        this.tagPathElements = tagPath.split("/");
    }

    public CompoundTag process(CompoundTag compound, int storedVersion) {
        ListTag itemMap = getItemMapNBT(compound);
        for (int i = 0; i < itemMap.size(); i++) {
            CompoundTag entryTag = itemMap.getCompound(i);
            entryTag.put("item", LegacyDataFixerRegistry.apply(
                    LegacyDataFixerRegistry.Target.ITEM,
                    entryTag.getCompound("item"),
                    storedVersion));
        }
        return compound;
    }

    private ListTag getItemMapNBT(CompoundTag compound) {
        CompoundTag tag = compound;
        for (int i = 0; i < tagPathElements.length - 1; i++) {
            tag = tag.getCompound(tagPathElements[i]);
        }
        return tag.getList(tagPathElements[tagPathElements.length - 1], Constants.NBT.TAG_COMPOUND);
    }
}
