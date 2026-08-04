package net.shadowmage.ancientwarfare.automation.init;

import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import net.shadowmage.ancientwarfare.automation.AncientWarfareAutomation;
import net.shadowmage.ancientwarfare.automation.item.ItemLegacyWorksiteUpgrade;
import net.shadowmage.ancientwarfare.automation.item.ItemWorksiteUpgrade;
import net.shadowmage.ancientwarfare.core.item.ItemMulti;
import net.shadowmage.ancientwarfare.core.upgrade.WorksiteUpgrade;
import net.shadowmage.ancientwarfare.core.util.LegacyRegistryHelper;

@Mod.EventBusSubscriber(modid = AncientWarfareAutomation.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class AWAutomationItems {
    private AWAutomationItems() {
    }

    /** Hidden old metadata item. Kept only so existing saves can be migrated. */
    public static ItemLegacyWorksiteUpgrade LEGACY_WORKSITE_UPGRADE;
    /** @deprecated use the fixed-id fields or getWorksiteUpgradeItem. */
    @Deprecated
    public static ItemMulti WORKSITE_UPGRADE;

    public static ItemWorksiteUpgrade WORKSITE_UPGRADE_SIZE_MEDIUM;
    public static ItemWorksiteUpgrade WORKSITE_UPGRADE_SIZE_LARGE;
    public static ItemWorksiteUpgrade WORKSITE_UPGRADE_QUARRY_MEDIUM;
    public static ItemWorksiteUpgrade WORKSITE_UPGRADE_QUARRY_LARGE;
    public static ItemWorksiteUpgrade WORKSITE_UPGRADE_ENCHANTED_TOOLS_1;
    public static ItemWorksiteUpgrade WORKSITE_UPGRADE_ENCHANTED_TOOLS_2;
    public static ItemWorksiteUpgrade WORKSITE_UPGRADE_TOOL_QUALITY_1;
    public static ItemWorksiteUpgrade WORKSITE_UPGRADE_TOOL_QUALITY_2;
    public static ItemWorksiteUpgrade WORKSITE_UPGRADE_TOOL_QUALITY_3;
    public static ItemWorksiteUpgrade WORKSITE_UPGRADE_BASIC_CHUNK_LOADER;
    public static ItemWorksiteUpgrade WORKSITE_UPGRADE_QUARRY_CHUNK_LOADER;

    @SubscribeEvent
    public static void register(RegisterEvent event) {
        event.register(ForgeRegistries.Keys.ITEMS, helper -> {
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
            LEGACY_WORKSITE_UPGRADE = LegacyRegistryHelper.register(helper, legacy);
            WORKSITE_UPGRADE = LEGACY_WORKSITE_UPGRADE;

            WORKSITE_UPGRADE_SIZE_MEDIUM = registerUpgrade(helper,
                    "worksite_upgrade_size_medium", WorksiteUpgrade.SIZE_MEDIUM);
            WORKSITE_UPGRADE_SIZE_LARGE = registerUpgrade(helper,
                    "worksite_upgrade_size_large", WorksiteUpgrade.SIZE_LARGE);
            WORKSITE_UPGRADE_QUARRY_MEDIUM = registerUpgrade(helper,
                    "worksite_upgrade_quarry_medium", WorksiteUpgrade.QUARRY_MEDIUM);
            WORKSITE_UPGRADE_QUARRY_LARGE = registerUpgrade(helper,
                    "worksite_upgrade_quarry_large", WorksiteUpgrade.QUARRY_LARGE);
            WORKSITE_UPGRADE_ENCHANTED_TOOLS_1 = registerUpgrade(helper,
                    "worksite_upgrade_enchanted_tools_1", WorksiteUpgrade.ENCHANTED_TOOLS_1);
            WORKSITE_UPGRADE_ENCHANTED_TOOLS_2 = registerUpgrade(helper,
                    "worksite_upgrade_enchanted_tools_2", WorksiteUpgrade.ENCHANTED_TOOLS_2);
            WORKSITE_UPGRADE_TOOL_QUALITY_1 = registerUpgrade(helper,
                    "worksite_upgrade_tool_quality_1", WorksiteUpgrade.TOOL_QUALITY_1);
            WORKSITE_UPGRADE_TOOL_QUALITY_2 = registerUpgrade(helper,
                    "worksite_upgrade_tool_quality_2", WorksiteUpgrade.TOOL_QUALITY_2);
            WORKSITE_UPGRADE_TOOL_QUALITY_3 = registerUpgrade(helper,
                    "worksite_upgrade_tool_quality_3", WorksiteUpgrade.TOOL_QUALITY_3);
            WORKSITE_UPGRADE_BASIC_CHUNK_LOADER = registerUpgrade(helper,
                    "worksite_upgrade_basic_chunk_loader", WorksiteUpgrade.BASIC_CHUNK_LOADER);
            WORKSITE_UPGRADE_QUARRY_CHUNK_LOADER = registerUpgrade(helper,
                    "worksite_upgrade_quarry_chunk_loader", WorksiteUpgrade.QUARRY_CHUNK_LOADER);
        });
    }

    private static ItemWorksiteUpgrade registerUpgrade(RegisterEvent.RegisterHelper<Item> helper,
                                                        String registryName,
                                                        WorksiteUpgrade upgrade) {
        return LegacyRegistryHelper.register(helper,
                new ItemWorksiteUpgrade(registryName, upgrade));
    }

    public static Item getWorksiteUpgradeItem(WorksiteUpgrade upgrade) {
        if (upgrade == null) {
            return null;
        }
        return switch (upgrade) {
            case SIZE_MEDIUM -> WORKSITE_UPGRADE_SIZE_MEDIUM;
            case SIZE_LARGE -> WORKSITE_UPGRADE_SIZE_LARGE;
            case QUARRY_MEDIUM -> WORKSITE_UPGRADE_QUARRY_MEDIUM;
            case QUARRY_LARGE -> WORKSITE_UPGRADE_QUARRY_LARGE;
            case ENCHANTED_TOOLS_1 -> WORKSITE_UPGRADE_ENCHANTED_TOOLS_1;
            case ENCHANTED_TOOLS_2 -> WORKSITE_UPGRADE_ENCHANTED_TOOLS_2;
            case TOOL_QUALITY_1 -> WORKSITE_UPGRADE_TOOL_QUALITY_1;
            case TOOL_QUALITY_2 -> WORKSITE_UPGRADE_TOOL_QUALITY_2;
            case TOOL_QUALITY_3 -> WORKSITE_UPGRADE_TOOL_QUALITY_3;
            case BASIC_CHUNK_LOADER -> WORKSITE_UPGRADE_BASIC_CHUNK_LOADER;
            case QUARRY_CHUNK_LOADER -> WORKSITE_UPGRADE_QUARRY_CHUNK_LOADER;
        };
    }

    public static Item getWorksiteUpgradeByLegacyMeta(int metadata) {
        WorksiteUpgrade[] values = WorksiteUpgrade.values();
        return metadata >= 0 && metadata < values.length
                ? getWorksiteUpgradeItem(values[metadata])
                : null;
    }

    public static boolean isLegacyVariantItem(Item item) {
        return item == LEGACY_WORKSITE_UPGRADE;
    }
}
