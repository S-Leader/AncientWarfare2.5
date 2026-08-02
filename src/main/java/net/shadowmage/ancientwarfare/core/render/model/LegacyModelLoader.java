package net.shadowmage.ancientwarfare.core.render.model;

import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Collects old model declarations for modern ModelEvent registration.
 */
public final class LegacyModelLoader {
    static final Map<Block, LegacyStateMapperBase> STATE_MAPPERS = new ConcurrentHashMap<>();
    static final Map<Item, Function<ItemStack, ModelResourceLocation>> MESH_DEFINITIONS = new ConcurrentHashMap<>();
    static final Map<ItemModelKey, ModelResourceLocation> ITEM_MODELS = new ConcurrentHashMap<>();
    static final Set<ResourceLocation> ITEM_VARIANTS = ConcurrentHashMap.newKeySet();
    static final Map<ResourceLocation, Set<Item>> ITEM_VARIANT_OWNERS = new ConcurrentHashMap<>();

    private LegacyModelLoader() {
    }

    public static void setCustomStateMapper(Block block, LegacyStateMapperBase mapper) {
        STATE_MAPPERS.put(block, mapper);
    }

    public static void setCustomMeshDefinition(Item item, Function<ItemStack, ModelResourceLocation> definition) {
        MESH_DEFINITIONS.put(item, definition);
    }

    public static void setCustomModelResourceLocation(Item item, int metadata, ModelResourceLocation location) {
        ITEM_MODELS.put(new ItemModelKey(item, metadata), location);
        ITEM_VARIANTS.add(location);
        registerVariantOwner(item, location);
    }

    public static void registerItemVariants(Item item, ResourceLocation... locations) {
        ITEM_VARIANTS.addAll(Arrays.asList(locations));
        Arrays.stream(locations).forEach(location -> registerVariantOwner(item, location));
    }

    private static void registerVariantOwner(Item item, ResourceLocation location) {
        ITEM_VARIANT_OWNERS.computeIfAbsent(location, ignored -> ConcurrentHashMap.newKeySet()).add(item);
    }

    record ItemModelKey(Item item, int metadata) {
    }
}
