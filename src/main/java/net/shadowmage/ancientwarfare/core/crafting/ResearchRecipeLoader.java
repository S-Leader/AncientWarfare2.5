package net.shadowmage.ancientwarfare.core.crafting;

import com.google.gson.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.ModList;
import net.shadowmage.ancientwarfare.core.AncientWarfareCore;
import net.shadowmage.ancientwarfare.core.config.AWCoreStatics;
import net.shadowmage.ancientwarfare.core.util.ModResourceHelper;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Loads the legacy AW research-recipe tree into the 1.20.1 crafting bridge.
 */
final class ResearchRecipeLoader {
    private static final String CONSTANTS_FILE = "_constants.json";
    private static final String LEGACY_ORE_TYPE = "forge:ore_dict";
    private static final String ORE_COUNT_TYPE = AncientWarfareCore.MOD_ID + ":ore_dict_count";

    private ResearchRecipeLoader() {
    }

    static int load() {
        Path bundledRoot = ModResourceHelper.findModResource(
                AncientWarfareCore.MOD_ID, "assets", AncientWarfareCore.MOD_ID, "research_recipes");
        Path overrideRoot = Paths.get(AWCoreStatics.configPathForFiles, "research_recipes");

        Map<String, JsonObject> constants = new HashMap<>();
        loadConstants(bundledRoot, constants);
        // User constants intentionally replace bundled constants.
        loadConstants(overrideRoot, constants);

        int loaded = 0;
        // Overrides load first; bundled recipes with the same id are then skipped.
        loaded += loadRoot(overrideRoot, constants, true);
        loaded += loadRoot(bundledRoot, constants, true);
        return loaded;
    }

    private static void loadConstants(Path root, Map<String, JsonObject> constants) {
        if (root == null) {
            return;
        }
        Path file = root.resolve(CONSTANTS_FILE);
        if (!Files.isRegularFile(file)) {
            return;
        }
        try (Reader reader = Files.newBufferedReader(file)) {
            JsonElement parsed = JsonParser.parseReader(reader);
            if (!parsed.isJsonArray()) {
                throw new JsonParseException("Research constants must be a JSON array");
            }
            for (JsonElement entryElement : parsed.getAsJsonArray()) {
                JsonObject entry = entryElement.getAsJsonObject();
                String name = entry.get("name").getAsString();
                JsonObject ingredient = entry.getAsJsonObject("ingredient");
                if (name == null || name.isBlank() || ingredient == null) {
                    throw new JsonParseException("Invalid research recipe constant in " + file);
                }
                constants.put(name, ingredient.deepCopy());
            }
        } catch (IOException | RuntimeException exception) {
            AncientWarfareCore.LOG.error("Unable to load research recipe constants from {}", file, exception);
        }
    }

    private static int loadRoot(Path root, Map<String, JsonObject> constants, boolean skipExisting) {
        if (root == null || !Files.isDirectory(root)) {
            return 0;
        }

        int loaded = 0;
        try (Stream<Path> files = Files.walk(root)) {
            for (Path file : files.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".json"))
                    .filter(path -> !CONSTANTS_FILE.equals(path.getFileName().toString()))
                    .sorted().toList()) {
                ResourceLocation id = recipeId(root, file);
                try (Reader reader = Files.newBufferedReader(file)) {
                    JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                    if (isDisabled(json) || !isRequiredModLoaded(json)) {
                        continue;
                    }
                    JsonObject expanded = expandObject(json, constants);
                    ResearchRecipeBase recipe = new ResearchRecipeFactory().parse(expanded);
                    recipe.setId(id);
                    if (AWCraftingManager.addRecipe(recipe, skipExisting)) {
                        loaded++;
                    }
                } catch (IOException | RuntimeException exception) {
                    AncientWarfareCore.LOG.error("Unable to load research recipe {} from {}", id, file, exception);
                }
            }
        } catch (IOException exception) {
            AncientWarfareCore.LOG.error("Unable to scan research recipes in {}", root, exception);
        }
        return loaded;
    }

    private static ResourceLocation recipeId(Path root, Path file) {
        String relative = root.relativize(file).toString().replace('\\', '/');
        relative = relative.substring(0, relative.length() - ".json".length());
        return new ResourceLocation(AncientWarfareCore.MOD_ID, relative);
    }

    private static boolean isDisabled(JsonObject json) {
        return json.has("disabled") && json.get("disabled").getAsBoolean();
    }

    private static boolean isRequiredModLoaded(JsonObject json) {
        return !json.has("mod") || ModList.get().isLoaded(json.get("mod").getAsString());
    }

    private static JsonObject expandObject(JsonObject source, Map<String, JsonObject> constants) {
        if (source.has("item") && source.get("item").isJsonPrimitive()) {
            String item = source.get("item").getAsString();
            if (item.startsWith("#")) {
                JsonObject constant = constants.get(item.substring(1));
                if (constant == null) {
                    throw new JsonParseException("Undefined research recipe constant " + item);
                }
                return expandObject(constant.deepCopy(), constants);
            }
        }

        JsonObject result = new JsonObject();
        for (Map.Entry<String, JsonElement> entry : source.entrySet()) {
            result.add(entry.getKey(), expand(entry.getValue(), constants));
        }
        if (result.has("type") && LEGACY_ORE_TYPE.equals(result.get("type").getAsString())) {
            result.addProperty("type", ORE_COUNT_TYPE);
        }
        return result;
    }

    private static JsonElement expand(JsonElement source, Map<String, JsonObject> constants) {
        if (source == null || source.isJsonNull() || source.isJsonPrimitive()) {
            return source == null ? null : source.deepCopy();
        }
        if (source.isJsonObject()) {
            return expandObject(source.getAsJsonObject(), constants);
        }
        JsonArray result = new JsonArray();
        for (JsonElement value : source.getAsJsonArray()) {
            result.add(expand(value, constants));
        }
        return result;
    }
}
