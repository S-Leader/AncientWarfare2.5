package net.shadowmage.ancientwarfare.vehicle.registry;

import com.google.gson.JsonObject;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.shadowmage.ancientwarfare.vehicle.AncientWarfareVehicles;

import javax.annotation.Nullable;

/**
 * Multi-output furnace recipe serializer used by the four recyclable iron-shot recipes.
 * Vanilla 1.20.1 smelting JSON only exposes a single-item result, so the legacy 3/6/13/19
 * nugget outputs require a tiny custom serializer. The recipe itself deliberately extends
 * SmeltingRecipe rather than only AbstractCookingRecipe: anything returned from
 * RecipeManager#getAllRecipesFor(RecipeType.SMELTING) must be safely castable to the
 * vanilla smelting class by compatibility mods.
 */
public final class SmeltingRecipeRegistry {
    private static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, AncientWarfareVehicles.MOD_ID);
    public static final RegistryObject<RecipeSerializer<MultiOutputSmeltingRecipe>> SERIALIZER =
            SERIALIZERS.register("multi_output_smelting", Serializer::new);

    private SmeltingRecipeRegistry() {
    }
    public static void register(IEventBus modBus) {
        SERIALIZERS.register(modBus);
    }

    public static final class MultiOutputSmeltingRecipe extends SmeltingRecipe {
        public MultiOutputSmeltingRecipe(ResourceLocation id, String group, Ingredient ingredient,
                                         ItemStack result, float experience, int cookingTime) {
            super(id, group, CookingBookCategory.MISC, ingredient, result, experience, cookingTime);
        }

        @Override
        public RecipeSerializer<?> getSerializer() {
            return SERIALIZER.get();
        }
    }

    private static final class Serializer implements RecipeSerializer<MultiOutputSmeltingRecipe> {
        @Override
        public MultiOutputSmeltingRecipe fromJson(ResourceLocation id, JsonObject json) {
            String group = GsonHelper.getAsString(json, "group", "");
            Ingredient ingredient = Ingredient.fromJson(json.get("ingredient"));
            JsonObject resultJson = GsonHelper.getAsJsonObject(json, "result");
            ResourceLocation itemId = new ResourceLocation(GsonHelper.getAsString(resultJson, "item"));
            Item item = ForgeRegistries.ITEMS.getValue(itemId);
            if (item == null) {
                throw new IllegalStateException("Unknown smelting result item: " + itemId);
            }
            int count = GsonHelper.getAsInt(resultJson, "count", 1);
            float experience = GsonHelper.getAsFloat(json, "experience", 0.0F);
            int cookingTime = GsonHelper.getAsInt(json, "cookingtime", 200);
            return new MultiOutputSmeltingRecipe(id, group, ingredient,
                    new ItemStack(item, count), experience, cookingTime);
        }

        @Override
        @Nullable
        public MultiOutputSmeltingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            String group = buffer.readUtf();
            Ingredient ingredient = Ingredient.fromNetwork(buffer);
            ItemStack result = buffer.readItem();
            float experience = buffer.readFloat();
            int cookingTime = buffer.readVarInt();
            return new MultiOutputSmeltingRecipe(id, group, ingredient, result, experience, cookingTime);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, MultiOutputSmeltingRecipe recipe) {
            buffer.writeUtf(recipe.getGroup());
            recipe.getIngredients().get(0).toNetwork(buffer);
            buffer.writeItem(recipe.getResultItem(RegistryAccess.EMPTY));
            buffer.writeFloat(recipe.getExperience());
            buffer.writeVarInt(recipe.getCookingTime());
        }
    }
}
