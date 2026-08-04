package net.shadowmage.ancientwarfare.core.datafixes;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

/** Converts the old metadata-based ancientwarfare:component stack to a real item id. */
public final class ComponentItemFixer implements ILegacyDataFixer {
    @Override
    public int getFixVersion() {
        return 12;
    }

    @Override
    public CompoundTag fixTagCompound(CompoundTag tag) {
        return fixStackTag(tag);
    }

    public static CompoundTag fixStackTag(CompoundTag tag) {
        if (!"ancientwarfare:component".equals(tag.getString("id"))) {
            return tag;
        }
        String replacement = switch (tag.getShort("Damage")) {
            case 0 -> "ancientwarfare:component_wooden_gear";
            case 1 -> "ancientwarfare:component_iron_gear";
            case 2 -> "ancientwarfare:component_steel_gear";
            case 3 -> "ancientwarfare:component_wooden_bearings";
            case 4 -> "ancientwarfare:component_iron_bearings";
            case 5 -> "ancientwarfare:component_steel_bearings";
            case 6 -> "ancientwarfare:component_wooden_shaft";
            case 7 -> "ancientwarfare:component_iron_shaft";
            case 8 -> "ancientwarfare:component_steel_shaft";
            default -> null;
        };
        if (replacement != null) {
            tag.putString("id", replacement);
            tag.putShort("Damage", (short) 0);
        }
        return tag;
    }

    /** Fixes old component stacks nested in inventories, equipment and entity NBT. */
    public static CompoundTag fixRecursively(CompoundTag tag) {
        fixStackTag(tag);
        for (String key : tag.getAllKeys().toArray(String[]::new)) {
            Tag child = tag.get(key);
            if (child instanceof CompoundTag compound) {
                fixRecursively(compound);
            } else if (child instanceof ListTag list) {
                for (Tag element : list) {
                    if (element instanceof CompoundTag compound) {
                        fixRecursively(compound);
                    }
                }
            }
        }
        return tag;
    }
}
