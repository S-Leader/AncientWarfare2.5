package net.shadowmage.ancientwarfare.core.util;

import com.mojang.datafixers.DSL;
import com.mojang.serialization.Dynamic;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.shadowmage.ancientwarfare.core.AncientWarfareCore;

import java.util.Map;
import java.util.MissingResourceException;
import java.util.concurrent.ConcurrentHashMap;

public final class RegistryTools {
    public static final ResourceLocation EMPTY_REGISTRY_NAME = new ResourceLocation("minecraft", "air");
    private static final int MINECRAFT_1_12_2_DATA_VERSION = 1343;
    private static final Map<String, String> LEGACY_BLOCK_NAMES = new ConcurrentHashMap<>();
    private static final Map<String, String> LEGACY_ITEM_NAMES = new ConcurrentHashMap<>();

    private RegistryTools() {
    }

    public static Block getBlock(String registryName) {
        return getRegistryEntry(fixLegacyName(registryName, References.BLOCK_NAME, LEGACY_BLOCK_NAMES), ForgeRegistries.BLOCKS, Blocks.AIR);
    }

    public static Item getItem(String registryName) {
        // The old skull item required metadata to choose its variant. NPC default
        // equipment only stores the registry name, where variant 0 was a skeleton skull.
        if ("minecraft:skull".equals(registryName)) {
            return Items.SKELETON_SKULL;
        }
        return getRegistryEntry(fixLegacyName(registryName, References.ITEM_NAME, LEGACY_ITEM_NAMES), ForgeRegistries.ITEMS, Items.AIR);
    }

    private static String fixLegacyName(String registryName, DSL.TypeReference type, Map<String, String> cache) {
        if (registryName == null || !registryName.startsWith("minecraft:")) {
            return registryName;
        }
        return cache.computeIfAbsent(registryName, name -> {
            Dynamic<Tag> fixed = DataFixers.getDataFixer().update(
                    type,
                    new Dynamic<>(NbtOps.INSTANCE, StringTag.valueOf(name)),
                    MINECRAFT_1_12_2_DATA_VERSION,
                    SharedConstants.getCurrentVersion().getDataVersion().getVersion());
            return fixed.getValue().getAsString();
        });
    }

    public static ResourceLocation getRegistryName(Item item) {
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(item);
        return key == null ? EMPTY_REGISTRY_NAME : key;
    }

    public static ResourceLocation getRegistryName(Block block) {
        ResourceLocation key = ForgeRegistries.BLOCKS.getKey(block);
        return key == null ? EMPTY_REGISTRY_NAME : key;
    }

    private static <T> T getRegistryEntry(String registryName, IForgeRegistry<T> registry, T defaultValue) {
        ResourceLocation key = new ResourceLocation(registryName);
        if (!registry.containsKey(key)) {
            if (!ModList.get().isLoaded(key.getNamespace())) {
                ResourceLocation fallbackKey = registry.getKey(defaultValue);
                AncientWarfareCore.LOG.debug("Mod {} is not loaded. Replacing {} with {}",
                        key.getNamespace(), key, fallbackKey);
                return defaultValue;
            }
            throw new MissingResourceException("Unable to find entry with registry name \"" + registryName + "\"",
                    registry.getClass().getName(), registryName);
        }
        T value = registry.getValue(key);
        return value == null ? defaultValue : value;
    }
}
