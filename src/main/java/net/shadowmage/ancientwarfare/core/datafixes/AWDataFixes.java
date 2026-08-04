package net.shadowmage.ancientwarfare.core.datafixes;

import net.shadowmage.ancientwarfare.npc.datafixes.*;
import net.shadowmage.ancientwarfare.structure.datafixes.LootSettingsPotionRegistryNameFixer;
import net.shadowmage.ancientwarfare.structure.datafixes.TileLootFixer;
import net.shadowmage.ancientwarfare.structure.datafixes.WoodenCoffinFixer;

import static net.shadowmage.ancientwarfare.core.datafixes.LegacyDataFixerRegistry.Target.*;

/**
 * Registers the original Ancient Warfare NBT transformations without depending on
 * the removed Forge 1.12 CompoundDataFixer API.
 */
public final class AWDataFixes {
    public static final int DATA_FIXER_VERSION = 12;

    private AWDataFixes() {
    }

    public static void registerDataFixes() {
        LegacyDataFixerRegistry.clear();

        LegacyDataFixerRegistry.register(ENTITY, new VehicleOwnerFixer());
        LegacyDataFixerRegistry.register(BLOCK_ENTITY, new TileOwnerFixer());
        LegacyDataFixerRegistry.register(BLOCK_ENTITY, new TileIdFixer());
        LegacyDataFixerRegistry.register(ENTITY, new FactionEntityFixer());
        LegacyDataFixerRegistry.register(ITEM, new FactionSpawnerItemFixer());
        LegacyDataFixerRegistry.register(ITEM, new ResearchNoteFixer());
        LegacyDataFixerRegistry.register(ENTITY, new FactionExpansionEntityFixer());
        LegacyDataFixerRegistry.register(ITEM, new FactionExpansionItemFixer());
        LegacyDataFixerRegistry.register(ITEM, new RoutingOrderFilterCountsFixer());
        LegacyDataFixerRegistry.register(ITEM, new FoodBundleDataFixer());
        LegacyDataFixerRegistry.register(ITEM, new ComponentItemFixer());
        LegacyDataFixerRegistry.register(BLOCK_ENTITY, new TileLootFixer());
        LegacyDataFixerRegistry.register(ENTITY, new NpcSkinFixer());
        LegacyDataFixerRegistry.register(BLOCK_ENTITY, new LootSettingsPotionRegistryNameFixer());
        LegacyDataFixerRegistry.register(BLOCK_ENTITY, new WoodenCoffinFixer());
    }
}
