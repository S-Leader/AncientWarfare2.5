package net.shadowmage.ancientwarfare.automation.init;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import net.shadowmage.ancientwarfare.automation.AncientWarfareAutomation;
import net.shadowmage.ancientwarfare.automation.item.ItemWorksiteUpgrade;
import net.shadowmage.ancientwarfare.core.item.ItemMulti;
import net.shadowmage.ancientwarfare.core.upgrade.WorksiteUpgrade;
import net.shadowmage.ancientwarfare.core.util.LegacyRegistryHelper;

@Mod.EventBusSubscriber(modid = AncientWarfareAutomation.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class AWAutomationItems {
    private AWAutomationItems() {
    }

    public static ItemMulti WORKSITE_UPGRADE;

    @SubscribeEvent
    public static void register(RegisterEvent event) {
        event.register(ForgeRegistries.Keys.ITEMS, helper -> {
            ItemMulti item = new ItemWorksiteUpgrade().listenToProxy(AncientWarfareAutomation.proxy);
            item.addSubItem(WorksiteUpgrade.SIZE_MEDIUM.ordinal(), "automation/worksite_upgrade#variant=bounds_medium");
            item.addSubItem(WorksiteUpgrade.SIZE_LARGE.ordinal(), "automation/worksite_upgrade#variant=bounds_large");
            item.addSubItem(WorksiteUpgrade.QUARRY_MEDIUM.ordinal(), "automation/worksite_upgrade#variant=quarry_medium");
            item.addSubItem(WorksiteUpgrade.QUARRY_LARGE.ordinal(), "automation/worksite_upgrade#variant=quarry_large");
            item.addSubItem(WorksiteUpgrade.ENCHANTED_TOOLS_1.ordinal(), "automation/worksite_upgrade#variant=enchanted_tools_1");
            item.addSubItem(WorksiteUpgrade.ENCHANTED_TOOLS_2.ordinal(), "automation/worksite_upgrade#variant=enchanted_tools_2");
            item.addSubItem(WorksiteUpgrade.TOOL_QUALITY_1.ordinal(), "automation/worksite_upgrade#variant=quality_tools_1");
            item.addSubItem(WorksiteUpgrade.TOOL_QUALITY_2.ordinal(), "automation/worksite_upgrade#variant=quality_tools_2");
            item.addSubItem(WorksiteUpgrade.TOOL_QUALITY_3.ordinal(), "automation/worksite_upgrade#variant=quality_tools_3");
            item.addSubItem(WorksiteUpgrade.BASIC_CHUNK_LOADER.ordinal(), "automation/worksite_upgrade#variant=chunkloader_basic");
            item.addSubItem(WorksiteUpgrade.QUARRY_CHUNK_LOADER.ordinal(), "automation/worksite_upgrade#variant=chunkloader_quarry");
            WORKSITE_UPGRADE = LegacyRegistryHelper.register(helper, item);
        });
    }
}
