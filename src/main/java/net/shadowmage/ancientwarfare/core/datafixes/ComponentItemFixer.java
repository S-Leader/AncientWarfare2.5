package net.shadowmage.ancientwarfare.core.datafixes;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

/** Converts old metadata-based component and automation item stacks to real item ids. */
public final class ComponentItemFixer implements ILegacyDataFixer {
    @Override
    public int getFixVersion() {
        return 13;
    }

    @Override
    public CompoundTag fixTagCompound(CompoundTag tag) {
        return fixStackTag(tag);
    }

    public static CompoundTag fixStackTag(CompoundTag tag) {
        String itemId = tag.getString("id");
        short metadata = tag.getShort("Damage");
        String replacement = null;

        if ("ancientwarfare:component".equals(itemId)) {
            replacement = switch (metadata) {
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
        } else if ("ancientwarfareautomation:worksite_upgrade".equals(itemId)) {
            replacement = switch (metadata) {
                case 0 -> "ancientwarfareautomation:worksite_upgrade_size_medium";
                case 1 -> "ancientwarfareautomation:worksite_upgrade_size_large";
                case 2 -> "ancientwarfareautomation:worksite_upgrade_quarry_medium";
                case 3 -> "ancientwarfareautomation:worksite_upgrade_quarry_large";
                case 4 -> "ancientwarfareautomation:worksite_upgrade_enchanted_tools_1";
                case 5 -> "ancientwarfareautomation:worksite_upgrade_enchanted_tools_2";
                case 6 -> "ancientwarfareautomation:worksite_upgrade_tool_quality_1";
                case 7 -> "ancientwarfareautomation:worksite_upgrade_tool_quality_2";
                case 8 -> "ancientwarfareautomation:worksite_upgrade_tool_quality_3";
                case 9 -> "ancientwarfareautomation:worksite_upgrade_basic_chunk_loader";
                case 10 -> "ancientwarfareautomation:worksite_upgrade_quarry_chunk_loader";
                default -> null;
            };
        }

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
