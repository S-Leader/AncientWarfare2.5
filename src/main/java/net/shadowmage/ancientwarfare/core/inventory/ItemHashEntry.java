package net.shadowmage.ancientwarfare.core.inventory;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.stream.Collectors;

/*
 * Lightweight wrapper for an item stack as a hashable object suitable for use as keys in maps.<br>
 * Uses item, item damage, and nbt-tag for hash-code.<br>
 * Ignores quantity.<br>
 * Immutable.
 *
 * @author Shadowmage
 */
public final class ItemHashEntry {
    private final CompoundTag itemTag;
    private ItemStack cacheStack = ItemStack.EMPTY;
    private String cachedNameAndTooltip = "";

    /*
     * @param item MUST NOT BE NULL
     */
    public ItemHashEntry(ItemStack item) {
        ItemStack copy = item.copy();
        copy.setCount(1);
        itemTag = copy.save(new CompoundTag());
    }

    @Override
    public int hashCode() {
        return itemTag.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        //noinspection SimplifiableIfStatement
        if (!(obj instanceof ItemHashEntry)) {
            return false;
        }
        return itemTag.equals(((ItemHashEntry) obj).itemTag);
    }

    public ItemStack getItemStack() {
        if (cacheStack.isEmpty()) {
            cacheStack = ItemStack.of(itemTag.copy());
        }
        return cacheStack;
    }

    @OnlyIn(Dist.CLIENT)
    public String getNameAndTooltip() {
        if (cachedNameAndTooltip.isEmpty()) {
            String stackText = cacheStack.getDisplayName().getString().toLowerCase() + " ";
            stackText += cacheStack.getTooltipLines(Minecraft.getInstance().player, TooltipFlag.Default.NORMAL).stream()
                    .map(Component::getString).collect(Collectors.joining(" ")).toLowerCase();

            cachedNameAndTooltip = stackText;
        }

        return cachedNameAndTooltip;
    }

    public CompoundTag writeToNBT() {
        return itemTag.copy();
    }

    public static ItemHashEntry readFromNBT(CompoundTag tag) {
        return new ItemHashEntry(ItemStack.of(tag));
    }
}
