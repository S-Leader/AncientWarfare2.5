package net.shadowmage.ancientwarfare.core.crafting;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.shadowmage.ancientwarfare.core.util.LegacyItemStack;

import java.util.HashMap;
import java.util.Map;

public class ResearchRecipeFactory {
    public ResearchRecipeBase parse(JsonObject json) {
        String research = GsonHelper.getAsString(json, "research");
        ItemStack result = parseItemStack(GsonHelper.getAsJsonObject(json, "result"));
        String type = GsonHelper.getAsString(json, "type");
        if (type.endsWith("shapeless_research_recipe")) {
            JsonArray array = GsonHelper.getAsJsonArray(json, "ingredients");
            NonNullList<Ingredient> ingredients = NonNullList.create();
            for (JsonElement value : array) {
                ingredients.add(parseIngredient(value));
            }
            return new ShapelessResearchRecipe(research, ingredients, result);
        }

        JsonArray patternJson = GsonHelper.getAsJsonArray(json, "pattern");
        String[] pattern = new String[patternJson.size()];
        for (int i = 0; i < pattern.length; i++) {
            pattern[i] = GsonHelper.convertToString(patternJson.get(i), "pattern[" + i + "]");
        }
        if (pattern.length == 0 || pattern[0].isEmpty()) {
            throw new JsonParseException("Empty research recipe pattern");
        }
        int width = pattern[0].length();
        for (String row : pattern) {
            if (row.length() != width) {
                throw new JsonParseException("Inconsistent research recipe pattern width");
            }
        }

        Map<Character, Ingredient> key = new HashMap<>();
        for (Map.Entry<String, JsonElement> entry : GsonHelper.getAsJsonObject(json, "key").entrySet()) {
            if (entry.getKey().length() != 1 || entry.getKey().charAt(0) == ' ') {
                throw new JsonParseException("Invalid research recipe key " + entry.getKey());
            }
            key.put(entry.getKey().charAt(0), parseIngredient(entry.getValue()));
        }
        NonNullList<Ingredient> ingredients = NonNullList.withSize(width * pattern.length, Ingredient.EMPTY);
        for (int y = 0; y < pattern.length; y++) {
            for (int x = 0; x < width; x++) {
                char symbol = pattern[y].charAt(x);
                Ingredient ingredient = symbol == ' ' ? Ingredient.EMPTY : key.get(symbol);
                if (ingredient == null) {
                    throw new JsonParseException("Undefined research recipe symbol " + symbol);
                }
                ingredients.set(x + y * width, ingredient);
            }
        }
        return new ShapedResearchRecipe(research, result, width, pattern.length, ingredients);
    }

    static Ingredient parseIngredient(JsonElement element) {
        if (!element.isJsonObject()) {
            return Ingredient.fromJson(element);
        }

        JsonObject json = element.getAsJsonObject();
        String type = GsonHelper.getAsString(json, "type", "");
        return switch (type) {
            case "forge:ore_dict", "ancientwarfare:ore_dict_count" ->
                    new IngredientOreCount(GsonHelper.getAsString(json, "ore"), GsonHelper.getAsInt(json, "count", 1));
            case "ancientwarfare:item_count" -> new IngredientCount(parseItemStack(json));
            case "ancientwarfare:item_nbt_relaxed", "minecraft:item_nbt", "forge:nbt" ->
                    new IngredientNBTRelaxed(parseItemStack(json));
            default -> {
                if (json.has("item")) {
                    ItemStack stack = parseItemStack(json);
                    if (json.has("data") || json.has("nbt")) {
                        yield new IngredientNBTRelaxed(stack);
                    }
                    yield Ingredient.of(stack.copyWithCount(1));
                }
                yield Ingredient.fromJson(element);
            }
        };
    }

    static ItemStack parseItemStack(JsonObject json) {
        String itemName = GsonHelper.getAsString(json, "item");
        int count = GsonHelper.getAsInt(json, "count", 1);
        int legacyMeta = GsonHelper.getAsInt(json, "data", -1);
        CompoundTag tag = parseNbt(json.get("nbt"));
        ItemStack stack = LegacyItemStack.of(itemName, count, legacyMeta, tag);
        if (stack.isEmpty()) {
            throw new JsonParseException("Unknown or empty item stack " + itemName);
        }
        return stack;
    }

    private static CompoundTag parseNbt(JsonElement nbt) {
        if (nbt == null || nbt.isJsonNull()) {
            return null;
        }
        String snbt = nbt.isJsonPrimitive() ? nbt.getAsString() : nbt.toString();
        try {
            return TagParser.parseTag(snbt);
        } catch (CommandSyntaxException exception) {
            throw new JsonParseException("Invalid item NBT: " + snbt, exception);
        }
    }
}
