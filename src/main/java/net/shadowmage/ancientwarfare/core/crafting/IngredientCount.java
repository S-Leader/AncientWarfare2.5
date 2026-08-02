package net.shadowmage.ancientwarfare.core.crafting;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import net.shadowmage.ancientwarfare.core.util.LegacyItemStack;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.stream.Stream;

public class IngredientCount extends Ingredient implements IIngredientCount {
    public static final IIngredientSerializer<IngredientCount> SERIALIZER = new Serializer();
    private final int count;

    public IngredientCount(ItemStack stack) {
        super(Stream.of(new ItemValue(stack.copyWithCount(1))));
        count = stack.getCount();
    }

    @Override
    public ItemStack[] getItems() {
        return Arrays.stream(super.getItems()).map(stack -> stack.copyWithCount(count)).toArray(ItemStack[]::new);
    }

    @Override
    public boolean test(@Nullable ItemStack input) {
        return input != null && input.getCount() >= count && super.test(input);
    }

    @Override
    public int getCount() {
        return count;
    }

    @Override
    public IIngredientSerializer<? extends Ingredient> getSerializer() {
        return SERIALIZER;
    }

    private static class Serializer implements IIngredientSerializer<IngredientCount> {
        @Override
        public IngredientCount parse(FriendlyByteBuf buffer) {
            return new IngredientCount(buffer.readItem());
        }

        @Override
        public IngredientCount parse(JsonObject json) {
            String itemName = GsonHelper.getAsString(json, "item");
            int count = GsonHelper.getAsInt(json, "count", 1);
            int legacyMeta = GsonHelper.getAsInt(json, "data", -1);
            return new IngredientCount(LegacyItemStack.of(itemName, count, legacyMeta, null));
        }

        @Override
        public void write(FriendlyByteBuf buffer, IngredientCount ingredient) {
            ItemStack[] items = ingredient.getItems();
            buffer.writeItem(items.length == 0 ? ItemStack.EMPTY : items[0].copyWithCount(ingredient.count));
        }
    }
}
