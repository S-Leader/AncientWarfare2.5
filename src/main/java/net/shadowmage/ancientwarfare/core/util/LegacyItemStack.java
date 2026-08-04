package net.shadowmage.ancientwarfare.core.util;

import com.mojang.serialization.Dynamic;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import javax.annotation.Nullable;

/**
 * Preserves the old 1.12 metadata-based subtype value in ItemStack's legacy
 * Damage NBT field. AW2 still reads these values through getDamageValue().
 */
public final class LegacyItemStack {
    private static final int MINECRAFT_1_12_2_DATA_VERSION = 1343;

    private LegacyItemStack() {
    }

    public static ItemStack of(ItemLike item, int count, int legacyMeta) {
        ItemStack stack = new ItemStack(item, count);
        if (legacyMeta >= 0) {
            stack.setDamageValue(legacyMeta);
        }
        return stack;
    }

    /**
     * Converts a registry name plus 1.12 metadata through Mojang's item data fixer.
     */
    public static ItemStack of(String legacyItemName, int count, int legacyMeta, @Nullable CompoundTag itemTag) {
        if ("ancientwarfare:component".equals(legacyItemName)) {
            legacyItemName = switch (legacyMeta) {
                case 0 -> "ancientwarfare:component_wooden_gear";
                case 1 -> "ancientwarfare:component_iron_gear";
                case 2 -> "ancientwarfare:component_steel_gear";
                case 3 -> "ancientwarfare:component_wooden_bearings";
                case 4 -> "ancientwarfare:component_iron_bearings";
                case 5 -> "ancientwarfare:component_steel_bearings";
                case 6 -> "ancientwarfare:component_wooden_shaft";
                case 7 -> "ancientwarfare:component_iron_shaft";
                case 8 -> "ancientwarfare:component_steel_shaft";
                default -> legacyItemName;
            };
            legacyMeta = 0;
        }
        if ("ancientwarfareautomation:worksite_upgrade".equals(legacyItemName)) {
            legacyItemName = switch (legacyMeta) {
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
                default -> legacyItemName;
            };
            legacyMeta = 0;
        }
        if (legacyItemName != null && legacyItemName.startsWith("minecraft:")) {
            CompoundTag oldStack = new CompoundTag();
            oldStack.putString("id", legacyItemName);
            oldStack.putByte("Count", (byte) count);
            if (legacyMeta >= 0) {
                oldStack.putShort("Damage", (short) legacyMeta);
            }
            if (itemTag != null) {
                oldStack.put("tag", itemTag.copy());
            }

            Dynamic<Tag> fixed = DataFixers.getDataFixer().update(
                    References.ITEM_STACK,
                    new Dynamic<>(NbtOps.INSTANCE, oldStack),
                    MINECRAFT_1_12_2_DATA_VERSION,
                    SharedConstants.getCurrentVersion().getDataVersion().getVersion());
            if (fixed.getValue() instanceof CompoundTag fixedStack) {
                ItemStack converted = ItemStack.of(fixedStack);
                if (!converted.isEmpty()) {
                    converted.setCount(count);
                    return converted;
                }
            }
        }

        ItemStack stack = of(RegistryTools.getItem(legacyItemName), count, legacyMeta);
        stack.setTag(itemTag == null ? null : itemTag.copy());
        return stack;
    }
}
