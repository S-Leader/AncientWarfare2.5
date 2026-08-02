package net.shadowmage.ancientwarfare.core.init;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tiers;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import net.shadowmage.ancientwarfare.core.AncientWarfareCore;
import net.shadowmage.ancientwarfare.core.item.*;
import net.shadowmage.ancientwarfare.core.util.LegacyOreDictionary;
import net.shadowmage.ancientwarfare.core.util.LegacyRegistryHelper;

@Mod.EventBusSubscriber(modid = AncientWarfareCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class AWCoreItems {
    private AWCoreItems() {
    }

    public static ItemInfoTool INFO_TOOL;
    public static Item IRON_HAMMER;
    public static Item MANUAL;
    public static Item RESEARCH_BOOK;
    public static Item BACKPACK;
    public static Item IRON_QUILL;
    public static Item STEEL_INGOT;
    public static ItemMulti COMPONENT;

    public static void load() {
        LegacyOreDictionary.registerOre("ingotSteel", STEEL_INGOT);
    }

    @SubscribeEvent
    public static void register(RegisterEvent event) {
        event.register(ForgeRegistries.Keys.ITEMS, helper -> {
            INFO_TOOL = LegacyRegistryHelper.register(helper, new ItemInfoTool());
            RESEARCH_BOOK = LegacyRegistryHelper.register(helper, new ItemResearchBook());
            LegacyRegistryHelper.register(helper, new ItemResearchNotes());
            BACKPACK = LegacyRegistryHelper.register(helper, new ItemBackpack());

            LegacyRegistryHelper.register(helper, new ItemQuill("wooden_quill", Tiers.WOOD));
            LegacyRegistryHelper.register(helper, new ItemQuill("stone_quill", Tiers.STONE));
            IRON_QUILL = LegacyRegistryHelper.register(helper, new ItemQuill("iron_quill", Tiers.IRON));
            LegacyRegistryHelper.register(helper, new ItemQuill("gold_quill", Tiers.GOLD));
            LegacyRegistryHelper.register(helper, new ItemQuill("diamond_quill", Tiers.DIAMOND));

            LegacyRegistryHelper.register(helper, new ItemHammer("wooden_hammer", Tiers.WOOD));
            LegacyRegistryHelper.register(helper, new ItemHammer("stone_hammer", Tiers.STONE));
            IRON_HAMMER = LegacyRegistryHelper.register(helper, new ItemHammer("iron_hammer", Tiers.IRON));
            LegacyRegistryHelper.register(helper, new ItemHammer("gold_hammer", Tiers.GOLD));
            LegacyRegistryHelper.register(helper, new ItemHammer("diamond_hammer", Tiers.DIAMOND));

            MANUAL = LegacyRegistryHelper.register(helper, new ItemManual());

            COMPONENT = new ItemComponent().listenToProxy(AncientWarfareCore.proxy);
            LegacyRegistryHelper.register(helper, COMPONENT);
            COMPONENT.addSubItem(ItemComponent.WOODEN_GEAR_SET, "automation/component_wooden_gear#inventory", "gearWood");
            COMPONENT.addSubItem(ItemComponent.IRON_GEAR_SET, "automation/component_iron_gear#inventory", "gearIron");
            COMPONENT.addSubItem(ItemComponent.STEEL_GEAR_SET, "automation/component_steel_gear#inventory", "gearSteel");
            COMPONENT.addSubItem(ItemComponent.WOODEN_BEARINGS, "automation/component_wooden_bearings#inventory", "bearingWood");
            COMPONENT.addSubItem(ItemComponent.IRON_BEARINGS, "automation/component_iron_bearings#inventory", "bearingIron");
            COMPONENT.addSubItem(ItemComponent.STEEL_BEARINGS, "automation/component_steel_bearings#inventory", "bearingSteel");
            COMPONENT.addSubItem(ItemComponent.WOODEN_TORQUE_SHAFT, "automation/component_wooden_shaft#inventory", "shaftWood");
            COMPONENT.addSubItem(ItemComponent.IRON_TORQUE_SHAFT, "automation/component_iron_shaft#inventory", "shaftIron");
            COMPONENT.addSubItem(ItemComponent.STEEL_TORQUE_SHAFT, "automation/component_steel_shaft#inventory", "shaftSteel");

            STEEL_INGOT = LegacyRegistryHelper.register(helper, new ItemBaseCore("steel_ingot"));
        });
    }
}
