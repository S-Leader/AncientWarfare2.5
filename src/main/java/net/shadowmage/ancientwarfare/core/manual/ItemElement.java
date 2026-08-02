package net.shadowmage.ancientwarfare.core.manual;

import com.google.gson.JsonObject;
import net.minecraft.world.item.ItemStack;
import net.shadowmage.ancientwarfare.core.AncientWarfareCore;
import net.shadowmage.ancientwarfare.core.util.parsing.JsonHelper;

import java.util.ArrayList;
import java.util.List;

public class ItemElement implements IContentElement {
    private ItemStack[] itemStack;

    private ItemElement(ItemStack... itemStack) {
        this.itemStack = itemStack;
    }

    public ItemStack[] getItemStacks() {
        return itemStack;
    }

    public static IContentElement parse(JsonObject elementJson) {
        if (elementJson.has("items")) {
            List<ItemStack> stacks = new ArrayList<>();
            elementJson.getAsJsonArray("items").forEach(e -> {
                ItemStack stack = parseManualStack(e, elementJson);
                if (!stack.isEmpty()) {
                    stacks.add(stack);
                }
            });
            return new ItemElement(stacks.toArray(new ItemStack[stacks.size()]));
        }
        return new ItemElement(parseManualStack(elementJson, elementJson));
    }

    private static ItemStack parseManualStack(com.google.gson.JsonElement stackJson, JsonObject elementJson) {
        try {
            return JsonHelper.getItemStack(stackJson);
        } catch (RuntimeException exception) {
            // Localized/manual data is display-only. A translated registry name or
            // optional missing item must not abort the entire mod loading sequence.
            AncientWarfareCore.LOG.error("Skipping invalid manual item entry {}: {}",
                    elementJson, exception.getMessage());
            return ItemStack.EMPTY;
        }
    }
}
