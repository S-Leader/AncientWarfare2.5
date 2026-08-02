package net.shadowmage.ancientwarfare.structure.util;

import java.util.Map;

/**
 * Maps vanilla 1.12 biome registry names used by bundled templates to 1.20 names.
 */
public final class LegacyBiomeNames {
    private static final Map<String, String> VANILLA_RENAMES = Map.ofEntries(
            Map.entry("minecraft:beaches", "minecraft:beach"),
            Map.entry("minecraft:birch_forest_hills", "minecraft:birch_forest"),
            Map.entry("minecraft:cold_beach", "minecraft:snowy_beach"),
            Map.entry("minecraft:desert_hills", "minecraft:desert"),
            Map.entry("minecraft:extreme_hills", "minecraft:windswept_hills"),
            Map.entry("minecraft:extreme_hills_with_trees", "minecraft:windswept_forest"),
            Map.entry("minecraft:forest_hills", "minecraft:forest"),
            Map.entry("minecraft:ice_flats", "minecraft:snowy_plains"),
            Map.entry("minecraft:ice_mountains", "minecraft:snowy_slopes"),
            Map.entry("minecraft:jungle_edge", "minecraft:sparse_jungle"),
            Map.entry("minecraft:jungle_hills", "minecraft:jungle"),
            Map.entry("minecraft:mesa", "minecraft:badlands"),
            Map.entry("minecraft:mesa_clear_rock", "minecraft:badlands"),
            Map.entry("minecraft:mesa_rock", "minecraft:wooded_badlands"),
            Map.entry("minecraft:mushroom_island", "minecraft:mushroom_fields"),
            Map.entry("minecraft:mushroom_island_shore", "minecraft:mushroom_fields"),
            Map.entry("minecraft:mutated_birch_forest", "minecraft:old_growth_birch_forest"),
            Map.entry("minecraft:mutated_birch_forest_hills", "minecraft:old_growth_birch_forest"),
            Map.entry("minecraft:mutated_desert", "minecraft:desert"),
            Map.entry("minecraft:mutated_extreme_hills", "minecraft:windswept_gravelly_hills"),
            Map.entry("minecraft:mutated_forest", "minecraft:flower_forest"),
            Map.entry("minecraft:mutated_ice_flats", "minecraft:ice_spikes"),
            Map.entry("minecraft:mutated_mesa", "minecraft:eroded_badlands"),
            Map.entry("minecraft:mutated_mesa_clear_rock", "minecraft:badlands"),
            Map.entry("minecraft:mutated_mesa_rock", "minecraft:wooded_badlands"),
            Map.entry("minecraft:mutated_plains", "minecraft:sunflower_plains"),
            Map.entry("minecraft:mutated_redwood_taiga", "minecraft:old_growth_spruce_taiga"),
            Map.entry("minecraft:mutated_redwood_taiga_hills", "minecraft:old_growth_spruce_taiga"),
            Map.entry("minecraft:mutated_roofed_forest", "minecraft:dark_forest"),
            Map.entry("minecraft:mutated_swampland", "minecraft:swamp"),
            Map.entry("minecraft:mutated_taiga", "minecraft:taiga"),
            Map.entry("minecraft:mutated_taiga_cold", "minecraft:snowy_taiga"),
            Map.entry("minecraft:redwood_taiga", "minecraft:old_growth_pine_taiga"),
            Map.entry("minecraft:redwood_taiga_hills", "minecraft:old_growth_pine_taiga"),
            Map.entry("minecraft:roofed_forest", "minecraft:dark_forest"),
            Map.entry("minecraft:savanna_rock", "minecraft:windswept_savanna"),
            Map.entry("minecraft:smaller_extreme_hills", "minecraft:windswept_hills"),
            Map.entry("minecraft:stone_beach", "minecraft:stony_shore"),
            Map.entry("minecraft:swampland", "minecraft:swamp"),
            Map.entry("minecraft:taiga_cold", "minecraft:snowy_taiga"),
            Map.entry("minecraft:taiga_cold_hills", "minecraft:snowy_taiga"),
            Map.entry("minecraft:taiga_hills", "minecraft:taiga")
    );

    private LegacyBiomeNames() {
    }

    public static String remap(String biomeName) {
        String normalized = biomeName == null ? "" : biomeName.trim().toLowerCase(java.util.Locale.ROOT);
        return VANILLA_RENAMES.getOrDefault(normalized, normalized);
    }
}
