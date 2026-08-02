package net.shadowmage.ancientwarfare.vehicle.registry;

import com.google.gson.JsonObject;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import net.shadowmage.ancientwarfare.vehicle.AncientWarfareVehicles;

import javax.annotation.Nullable;

/**
 * Multi-output furnace recipe serializer used by the four recyclable iron-shot recipes.
 * Vanilla 1.20.1 smelting JSON only exposes a single-item result, so the legacy 3/6/13/19
 * nugget outputs require a tiny custom serializer.
 */
@Mod.EventBusSubscriber(modid = AncientWarfareVehicles.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class SmeltingRecipeRegistry {
    public static RecipeSerializer<MultiOutputSmeltingRecipe> SERIALIZER;

    private SmeltingRecipeRegistry() {
    }

    @SubscribeEvent
    public static void register(RegisterEvent event) {
        event.register(ForgeRegistries.Keys.RECIPE_SERIALIZERS, helper -> {
            SERIALIZER = new Serializer();
            helper.register(new ResourceLocation(AncientWarfareVehicles.MOD_ID, "multi_output_smelting"), SERIALIZER);
        });
    }

    public static final class MultiOutputSmeltingRecipe extends AbstractCookingRecipe {
        public MultiOutputSmeltingRecipe(ResourceLocation id, String group, Ingredient ingredient,
                                         ItemStack result, float experience, int cookingTime) {
            super(RecipeType.SMELTING, id, group, CookingBookCategory.MISC, ingredient, result, experience, cookingTime);
        }

        @Override
        public RecipeSerializer<?> getSerializer() {
            return SERIALIZER;
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
