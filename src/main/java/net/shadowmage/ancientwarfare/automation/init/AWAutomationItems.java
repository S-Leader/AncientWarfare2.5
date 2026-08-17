package net.shadowmage.ancientwarfare.automation.init;

import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.shadowmage.ancientwarfare.automation.AncientWarfareAutomation;
import net.shadowmage.ancientwarfare.automation.item.ItemLegacyWorksiteUpgrade;
import net.shadowmage.ancientwarfare.automation.item.ItemWorksiteUpgrade;
import net.shadowmage.ancientwarfare.core.upgrade.WorksiteUpgrade;

public final class AWAutomationItems {
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, AncientWarfareAutomation.MOD_ID);

    public static final RegistryObject<ItemLegacyWorksiteUpgrade> LEGACY_WORKSITE_UPGRADE = ITEMS.register("worksite_upgrade", () -> {
        ItemLegacyWorksiteUpgrade legacy = new ItemLegacyWorksiteUpgrade();
        legacy.addSubItem(WorksiteUpgrade.SIZE_MEDIUM.ordinal(), "ancientwarfare:automation/worksite_upgrade#variant=bounds_medium");
        legacy.addSubItem(WorksiteUpgrade.SIZE_LARGE.ordinal(), "ancientwarfare:automation/worksite_upgrade#variant=bounds_large");
        legacy.addSubItem(WorksiteUpgrade.QUARRY_MEDIUM.ordinal(), "ancientwarfare:automation/worksite_upgrade#variant=quarry_medium");
        legacy.addSubItem(WorksiteUpgrade.QUARRY_LARGE.ordinal(), "ancientwarfare:automation/worksite_upgrade#variant=quarry_large");
        legacy.addSubItem(WorksiteUpgrade.ENCHANTED_TOOLS_1.ordinal(), "ancientwarfare:automation/worksite_upgrade#variant=enchanted_tools_1");
        legacy.addSubItem(WorksiteUpgrade.ENCHANTED_TOOLS_2.ordinal(), "ancientwarfare:automation/worksite_upgrade#variant=enchanted_tools_2");
        legacy.addSubItem(WorksiteUpgrade.TOOL_QUALITY_1.ordinal(), "ancientwarfare:automation/worksite_upgrade#variant=quality_tools_1");
        legacy.addSubItem(WorksiteUpgrade.TOOL_QUALITY_2.ordinal(), "ancientwarfare:automation/worksite_upgrade#variant=quality_tools_2");
        legacy.addSubItem(WorksiteUpgrade.TOOL_QUALITY_3.ordinal(), "ancientwarfare:automation/worksite_upgrade#variant=quality_tools_3");
        legacy.addSubItem(WorksiteUpgrade.BASIC_CHUNK_LOADER.ordinal(), "ancientwarfare:automation/worksite_upgrade#variant=chunkloader_basic");
        legacy.addSubItem(WorksiteUpgrade.QUARRY_CHUNK_LOADER.ordinal(), "ancientwarfare:automation/worksite_upgrade#variant=chunkloader_quarry");
        legacy.listenToProxy(AncientWarfareAutomation.proxy);
        return legacy;
    });
    @Deprecated public static final RegistryObject<ItemLegacyWorksiteUpgrade> WORKSITE_UPGRADE = LEGACY_WORKSITE_UPGRADE;

    public static final RegistryObject<ItemWorksiteUpgrade> WORKSITE_UPGRADE_SIZE_MEDIUM = upgrade("worksite_upgrade_size_medium", WorksiteUpgrade.SIZE_MEDIUM);
    public static final RegistryObject<ItemWorksiteUpgrade> WORKSITE_UPGRADE_SIZE_LARGE = upgrade("worksite_upgrade_size_large", WorksiteUpgrade.SIZE_LARGE);
    public static final RegistryObject<ItemWorksiteUpgrade> WORKSITE_UPGRADE_QUARRY_MEDIUM = upgrade("worksite_upgrade_quarry_medium", WorksiteUpgrade.QUARRY_MEDIUM);
    public static final RegistryObject<ItemWorksiteUpgrade> WORKSITE_UPGRADE_QUARRY_LARGE = upgrade("worksite_upgrade_quarry_large", WorksiteUpgrade.QUARRY_LARGE);
    public static final RegistryObject<ItemWorksiteUpgrade> WORKSITE_UPGRADE_ENCHANTED_TOOLS_1 = upgrade("worksite_upgrade_enchanted_tools_1", WorksiteUpgrade.ENCHANTED_TOOLS_1);
    public static final RegistryObject<ItemWorksiteUpgrade> WORKSITE_UPGRADE_ENCHANTED_TOOLS_2 = upgrade("worksite_upgrade_enchanted_tools_2", WorksiteUpgrade.ENCHANTED_TOOLS_2);
    public static final RegistryObject<ItemWorksiteUpgrade> WORKSITE_UPGRADE_TOOL_QUALITY_1 = upgrade("worksite_upgrade_tool_quality_1", WorksiteUpgrade.TOOL_QUALITY_1);
    public static final RegistryObject<ItemWorksiteUpgrade> WORKSITE_UPGRADE_TOOL_QUALITY_2 = upgrade("worksite_upgrade_tool_quality_2", WorksiteUpgrade.TOOL_QUALITY_2);
    public static final RegistryObject<ItemWorksiteUpgrade> WORKSITE_UPGRADE_TOOL_QUALITY_3 = upgrade("worksite_upgrade_tool_quality_3", WorksiteUpgrade.TOOL_QUALITY_3);
    public static final RegistryObject<ItemWorksiteUpgrade> WORKSITE_UPGRADE_BASIC_CHUNK_LOADER = upgrade("worksite_upgrade_basic_chunk_loader", WorksiteUpgrade.BASIC_CHUNK_LOADER);
    public static final RegistryObject<ItemWorksiteUpgrade> WORKSITE_UPGRADE_QUARRY_CHUNK_LOADER = upgrade("worksite_upgrade_quarry_chunk_loader", WorksiteUpgrade.QUARRY_CHUNK_LOADER);

    private AWAutomationItems() {}

    public static void register(IEventBus modBus) { ITEMS.register(modBus); }

    private static RegistryObject<ItemWorksiteUpgrade> upgrade(String id, WorksiteUpgrade upgrade) {
        return ITEMS.register(id, () -> new ItemWorksiteUpgrade(id, upgrade));
    }

    public static Item getWorksiteUpgradeItem(WorksiteUpgrade upgrade) {
        if (upgrade == null) return null;
        return switch (upgrade) {
            case SIZE_MEDIUM -> WORKSITE_UPGRADE_SIZE_MEDIUM.get();
            case SIZE_LARGE -> WORKSITE_UPGRADE_SIZE_LARGE.get();
            case QUARRY_MEDIUM -> WORKSITE_UPGRADE_QUARRY_MEDIUM.get();
            case QUARRY_LARGE -> WORKSITE_UPGRADE_QUARRY_LARGE.get();
            case ENCHANTED_TOOLS_1 -> WORKSITE_UPGRADE_ENCHANTED_TOOLS_1.get();
            case ENCHANTED_TOOLS_2 -> WORKSITE_UPGRADE_ENCHANTED_TOOLS_2.get();
            case TOOL_QUALITY_1 -> WORKSITE_UPGRADE_TOOL_QUALITY_1.get();
            case TOOL_QUALITY_2 -> WORKSITE_UPGRADE_TOOL_QUALITY_2.get();
            case TOOL_QUALITY_3 -> WORKSITE_UPGRADE_TOOL_QUALITY_3.get();
            case BASIC_CHUNK_LOADER -> WORKSITE_UPGRADE_BASIC_CHUNK_LOADER.get();
            case QUARRY_CHUNK_LOADER -> WORKSITE_UPGRADE_QUARRY_CHUNK_LOADER.get();
        };
    }

    public static Item getWorksiteUpgradeByLegacyMeta(int metadata) {
        WorksiteUpgrade[] values = WorksiteUpgrade.values();
        return metadata >= 0 && metadata < values.length ? getWorksiteUpgradeItem(values[metadata]) : null;
    }

    public static boolean isLegacyVariantItem(Item item) { return item == LEGACY_WORKSITE_UPGRADE.get(); }
}
