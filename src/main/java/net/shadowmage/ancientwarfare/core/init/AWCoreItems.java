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
    /** @deprecated use the fixed-id backpack fields. Kept as a source compatibility alias to SMALL_BACKPACK. */
    @Deprecated
    public static Item BACKPACK;
    public static Item LEGACY_BACKPACK;
    public static Item SMALL_BACKPACK;
    public static Item TRAVEL_BACKPACK;
    public static Item MEDIUM_BACKPACK;
    public static Item LARGE_BACKPACK;
    public static Item IRON_QUILL;
    public static Item STEEL_INGOT;
    /** Hidden compatibility item for old ancientwarfare:component stacks. */
    public static Item LEGACY_COMPONENT;
    public static Item WOODEN_GEAR_SET;
    public static Item IRON_GEAR_SET;
    public static Item STEEL_GEAR_SET;
    public static Item WOODEN_BEARINGS;
    public static Item IRON_BEARINGS;
    public static Item STEEL_BEARINGS;
    public static Item WOODEN_TORQUE_SHAFT;
    public static Item IRON_TORQUE_SHAFT;
    public static Item STEEL_TORQUE_SHAFT;

    public static void load() {
        LegacyOreDictionary.registerOre("ingotSteel", STEEL_INGOT);
    }

    @SubscribeEvent
    public static void register(RegisterEvent event) {
        event.register(ForgeRegistries.Keys.ITEMS, helper -> {
            INFO_TOOL = LegacyRegistryHelper.register(helper, new ItemInfoTool());
            RESEARCH_BOOK = LegacyRegistryHelper.register(helper, new ItemResearchBook());
            LegacyRegistryHelper.register(helper, new ItemResearchNotes());
            LEGACY_BACKPACK = LegacyRegistryHelper.register(helper, new ItemLegacyBackpack());
            SMALL_BACKPACK = LegacyRegistryHelper.register(helper, new ItemBackpack("backpack_small", 1));
            TRAVEL_BACKPACK = LegacyRegistryHelper.register(helper, new ItemBackpack("backpack_travel", 2));
            MEDIUM_BACKPACK = LegacyRegistryHelper.register(helper, new ItemBackpack("backpack_medium", 3));
            LARGE_BACKPACK = LegacyRegistryHelper.register(helper, new ItemBackpack("backpack_large", 4));
            BACKPACK = SMALL_BACKPACK;

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

            // Keep the old id only as an automatic save migration target.
            LEGACY_COMPONENT = LegacyRegistryHelper.register(helper, new ItemLegacyComponent());

            WOODEN_GEAR_SET = registerComponent(helper, "component_wooden_gear", "component_wooden_gear", "gearWood");
            IRON_GEAR_SET = registerComponent(helper, "component_iron_gear", "component_iron_gear", "gearIron");
            STEEL_GEAR_SET = registerComponent(helper, "component_steel_gear", "component_steel_gear", "gearSteel");
            WOODEN_BEARINGS = registerComponent(helper, "component_wooden_bearings", "component_wooden_bearings", "bearingWood");
            IRON_BEARINGS = registerComponent(helper, "component_iron_bearings", "component_iron_bearings", "bearingIron");
            STEEL_BEARINGS = registerComponent(helper, "component_steel_bearings", "component_steel_bearings", "bearingSteel");
            WOODEN_TORQUE_SHAFT = registerComponent(helper, "component_wooden_shaft", "component_wooden_shaft", "shaftWood");
            IRON_TORQUE_SHAFT = registerComponent(helper, "component_iron_shaft", "component_iron_shaft", "shaftIron");
            STEEL_TORQUE_SHAFT = registerComponent(helper, "component_steel_shaft", "component_steel_shaft", "shaftSteel");

            STEEL_INGOT = LegacyRegistryHelper.register(helper, new ItemBaseCore("steel_ingot"));
        });
    }

    private static Item registerComponent(RegisterEvent.RegisterHelper<Item> helper, String id, String model, String oreName) {
        Item item = LegacyRegistryHelper.register(helper, new ItemComponent(id, model));
        LegacyOreDictionary.registerOre(oreName, item);
        return item;
    }

    public static Item getComponentByLegacyMeta(int metadata) {
        return switch (metadata) {
            case 0 -> WOODEN_GEAR_SET;
            case 1 -> IRON_GEAR_SET;
            case 2 -> STEEL_GEAR_SET;
            case 3 -> WOODEN_BEARINGS;
            case 4 -> IRON_BEARINGS;
            case 5 -> STEEL_BEARINGS;
            case 6 -> WOODEN_TORQUE_SHAFT;
            case 7 -> IRON_TORQUE_SHAFT;
            case 8 -> STEEL_TORQUE_SHAFT;
            default -> null;
        };
    }
    public static Item getBackpackByLegacyMeta(int metadata) {
        return switch (metadata) {
            case 0 -> SMALL_BACKPACK;
            case 1 -> TRAVEL_BACKPACK;
            case 2 -> MEDIUM_BACKPACK;
            case 3 -> LARGE_BACKPACK;
            default -> SMALL_BACKPACK;
        };
    }

}
