package net.shadowmage.ancientwarfare.core.crafting;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import javax.annotation.Nullable;
import java.util.stream.Stream;

public class IngredientNBTRelaxed extends Ingredient {
    private final ItemStack stack;

    protected IngredientNBTRelaxed(ItemStack stack) {
        super(Stream.of(new ItemValue(stack.copyWithCount(1))));
        this.stack = stack;
    }

    @Override
    public boolean test(@Nullable ItemStack input) {
        if (input == null)
            return false;
        //Can't use areItemStacksEqualUsingNBTShareTag because it compares stack size as well
        return this.stack.getItem() == input.getItem() && this.stack.getDamageValue() == input.getDamageValue() && nbtTagsMatch(input);
    }

    private boolean nbtTagsMatch(ItemStack input) {
        if (!stack.hasTag()) {
            return true;
        }

        if (!input.hasTag()) {
            return false;
        }
        //noinspection ConstantConditions
        CompoundTag original = input.getTag().copy();
        CompoundTag merged = original.copy();

        //noinspection ConstantConditions
        merged.merge(stack.getTag());

        //if all the NBT values of the ingredient were in the input stack's NBT the merged one will match the one before merge
        return original.equals(merged);
    }

    @Override
    public boolean isSimple() {
        return false;
    }
}
