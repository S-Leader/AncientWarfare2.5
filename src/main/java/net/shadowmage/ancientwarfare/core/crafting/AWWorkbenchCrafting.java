package net.shadowmage.ancientwarfare.core.crafting;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import net.shadowmage.ancientwarfare.core.AncientWarfareCore;

import java.util.HashMap;
import java.util.Map;

/**
 * Restores the ordinary crafting-table recipes which were still stored under
 * assets/ in the 1.12 layout. The serializers deliberately retain legacy item
 * metadata and result NBT, both of which vanilla 1.20 recipe serializers drop.
 */
@Mod.EventBusSubscriber(modid = AncientWarfareCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class AWWorkbenchCrafting {
    private static RecipeSerializer<LegacyShapedRecipe> shapedSerializer;
    private static RecipeSerializer<LegacyShapelessRecipe> shapelessSerializer;

    private AWWorkbenchCrafting() {
    }

    @SubscribeEvent
    public static void register(RegisterEvent event) {
        event.register(ForgeRegistries.Keys.RECIPE_SERIALIZERS, helper -> {
            shapedSerializer = new ShapedSerializer();
            shapelessSerializer = new ShapelessSerializer();
            helper.register(new ResourceLocation(AncientWarfareCore.MOD_ID, "legacy_shaped"), shapedSerializer);
            helper.register(new ResourceLocation(AncientWarfareCore.MOD_ID, "legacy_shapeless"), shapelessSerializer);
        });
    }

    private static CraftingBookCategory category(JsonObject json) {
        String name = GsonHelper.getAsString(json, "category", "misc");
        try {
            return CraftingBookCategory.valueOf(name.toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return CraftingBookCategory.MISC;
        }
    }

    private static ItemStack result(JsonObject json) {
        return ResearchRecipeFactory.parseItemStack(GsonHelper.getAsJsonObject(json, "result"));
    }

    private abstract static class LegacyRecipe extends CustomRecipe {
        protected final NonNullList<Ingredient> ingredients;
        protected final ItemStack result;
        protected final CraftingBookCategory recipeCategory;

        protected LegacyRecipe(ResourceLocation id, CraftingBookCategory category,
                               NonNullList<Ingredient> ingredients, ItemStack result) {
            super(id, category);
            this.ingredients = ingredients;
            this.result = result;
            this.recipeCategory = category;
        }

        @Override
        public NonNullList<Ingredient> getIngredients() {
            return ingredients;
        }

        @Override
        public ItemStack assemble(CraftingContainer container, RegistryAccess registryAccess) {
            return result.copy();
        }

        @Override
        public ItemStack getResultItem(RegistryAccess registryAccess) {
            return result.copy();
        }
    }

    private static final class LegacyShapedRecipe extends LegacyRecipe {
        private final int width;
        private final int height;

        private LegacyShapedRecipe(ResourceLocation id, CraftingBookCategory category, int width, int height,
                                   NonNullList<Ingredient> ingredients, ItemStack result) {
            super(id, category, ingredients, result);
            this.width = width;
            this.height = height;
        }

        @Override
        public boolean matches(CraftingContainer container, Level level) {
            if (container.getWidth() < width || container.getHeight() < height) {
                return false;
            }
            for (int x = 0; x <= container.getWidth() - width; x++) {
                for (int y = 0; y <= container.getHeight() - height; y++) {
                    if (matchesAt(container, x, y, false) || matchesAt(container, x, y, true)) {
                        return true;
                    }
                }
            }
            return false;
        }

        private boolean matchesAt(CraftingContainer container, int startX, int startY, boolean mirror) {
            for (int x = 0; x < container.getWidth(); x++) {
                for (int y = 0; y < container.getHeight(); y++) {
                    int subX = x - startX;
                    int subY = y - startY;
                    Ingredient expected = Ingredient.EMPTY;
                    if (subX >= 0 && subY >= 0 && subX < width && subY < height) {
                        int recipeX = mirror ? width - subX - 1 : subX;
                        expected = ingredients.get(recipeX + subY * width);
                    }
                    if (!expected.test(container.getItem(x + y * container.getWidth()))) {
                        return false;
                    }
                }
            }
            return true;
        }

        @Override
        public boolean canCraftInDimensions(int width, int height) {
            return width >= this.width && height >= this.height;
        }

        @Override
        public RecipeSerializer<?> getSerializer() {
            return shapedSerializer;
        }
    }

    private static final class LegacyShapelessRecipe extends LegacyRecipe {
        private LegacyShapelessRecipe(ResourceLocation id, CraftingBookCategory category,
                                      NonNullList<Ingredient> ingredients, ItemStack result) {
            super(id, category, ingredients, result);
        }

        @Override
        public boolean matches(CraftingContainer container, Level level) {
            java.util.List<Ingredient> remaining = new java.util.ArrayList<>(ingredients);
            for (int slot = 0; slot < container.getContainerSize(); slot++) {
                ItemStack stack = container.getItem(slot);
                if (stack.isEmpty()) {
                    continue;
                }
                int matched = -1;
                for (int index = 0; index < remaining.size(); index++) {
                    if (remaining.get(index).test(stack)) {
                        matched = index;
                        break;
                    }
                }
                if (matched < 0) {
                    return false;
                }
                remaining.remove(matched);
            }
            return remaining.isEmpty();
        }

        @Override
        public boolean canCraftInDimensions(int width, int height) {
            return width * height >= ingredients.size();
        }

        @Override
        public RecipeSerializer<?> getSerializer() {
            return shapelessSerializer;
        }
    }

    private static final class ShapedSerializer implements RecipeSerializer<LegacyShapedRecipe> {
        @Override
        public LegacyShapedRecipe fromJson(ResourceLocation id, JsonObject json) {
            JsonArray patternJson = GsonHelper.getAsJsonArray(json, "pattern");
            if (patternJson.size() == 0) {
                throw new JsonParseException("Empty legacy crafting pattern");
            }
            String[] pattern = new String[patternJson.size()];
            int width = -1;
            for (int row = 0; row < pattern.length; row++) {
                pattern[row] = GsonHelper.convertToString(patternJson.get(row), "pattern[" + row + "]");
                if (width < 0) {
                    width = pattern[row].length();
                } else if (pattern[row].length() != width) {
                    throw new JsonParseException("Inconsistent legacy crafting pattern width");
                }
            }
            if (width <= 0 || width > 3 || pattern.length > 3) {
                throw new JsonParseException("Invalid legacy crafting pattern size " + width + "x" + pattern.length);
            }

            Map<Character, Ingredient> keys = new HashMap<>();
            for (Map.Entry<String, JsonElement> entry : GsonHelper.getAsJsonObject(json, "key").entrySet()) {
                if (entry.getKey().length() != 1 || entry.getKey().charAt(0) == ' ') {
                    throw new JsonParseException("Invalid legacy crafting key " + entry.getKey());
                }
                keys.put(entry.getKey().charAt(0), ResearchRecipeFactory.parseIngredient(entry.getValue()));
            }

            NonNullList<Ingredient> ingredients = NonNullList.withSize(width * pattern.length, Ingredient.EMPTY);
            for (int y = 0; y < pattern.length; y++) {
                for (int x = 0; x < width; x++) {
                    char symbol = pattern[y].charAt(x);
                    Ingredient ingredient = symbol == ' ' ? Ingredient.EMPTY : keys.get(symbol);
                    if (ingredient == null) {
                        throw new JsonParseException("Undefined legacy crafting symbol " + symbol);
                    }
                    ingredients.set(x + y * width, ingredient);
                }
            }
            return new LegacyShapedRecipe(id, category(json), width, pattern.length, ingredients, result(json));
        }

        @Override
        public LegacyShapedRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            int width = buffer.readVarInt();
            int height = buffer.readVarInt();
            CraftingBookCategory category = buffer.readEnum(CraftingBookCategory.class);
            NonNullList<Ingredient> ingredients = NonNullList.withSize(width * height, Ingredient.EMPTY);
            for (int i = 0; i < ingredients.size(); i++) {
                ingredients.set(i, Ingredient.fromNetwork(buffer));
            }
            return new LegacyShapedRecipe(id, category, width, height, ingredients, buffer.readItem());
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, LegacyShapedRecipe recipe) {
            buffer.writeVarInt(recipe.width);
            buffer.writeVarInt(recipe.height);
            buffer.writeEnum(recipe.recipeCategory);
            recipe.ingredients.forEach(ingredient -> ingredient.toNetwork(buffer));
            buffer.writeItem(recipe.result);
        }
    }

    private static final class ShapelessSerializer implements RecipeSerializer<LegacyShapelessRecipe> {
        @Override
        public LegacyShapelessRecipe fromJson(ResourceLocation id, JsonObject json) {
            JsonArray array = GsonHelper.getAsJsonArray(json, "ingredients");
            NonNullList<Ingredient> ingredients = NonNullList.create();
            for (JsonElement element : array) {
                ingredients.add(ResearchRecipeFactory.parseIngredient(element));
            }
            if (ingredients.isEmpty() || ingredients.size() > 9) {
                throw new JsonParseException("Invalid legacy shapeless recipe ingredient count " + ingredients.size());
            }
            return new LegacyShapelessRecipe(id, category(json), ingredients, result(json));
        }

        @Override
        public LegacyShapelessRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            CraftingBookCategory category = buffer.readEnum(CraftingBookCategory.class);
            int size = buffer.readVarInt();
            NonNullList<Ingredient> ingredients = NonNullList.withSize(size, Ingredient.EMPTY);
            for (int i = 0; i < size; i++) {
                ingredients.set(i, Ingredient.fromNetwork(buffer));
            }
            return new LegacyShapelessRecipe(id, category, ingredients, buffer.readItem());
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, LegacyShapelessRecipe recipe) {
            buffer.writeEnum(recipe.recipeCategory);
            buffer.writeVarInt(recipe.ingredients.size());
            recipe.ingredients.forEach(ingredient -> ingredient.toNetwork(buffer));
            buffer.writeItem(recipe.result);
        }
    }
}
