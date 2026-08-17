package net.shadowmage.ancientwarfare.core.init;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tiers;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.shadowmage.ancientwarfare.core.AncientWarfareCore;
import net.shadowmage.ancientwarfare.core.item.*;
import net.shadowmage.ancientwarfare.core.util.LegacyOreDictionary;

public final class AWCoreItems {
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, AncientWarfareCore.MOD_ID);

    public static final RegistryObject<ItemInfoTool> INFO_TOOL = ITEMS.register("info_tool", ItemInfoTool::new);
    public static final RegistryObject<ItemResearchBook> RESEARCH_BOOK = ITEMS.register("research_book", ItemResearchBook::new);
    public static final RegistryObject<ItemResearchNotes> RESEARCH_NOTES = ITEMS.register("research_notes", ItemResearchNotes::new);

    public static final RegistryObject<ItemLegacyBackpack> LEGACY_BACKPACK = ITEMS.register("backpack", ItemLegacyBackpack::new);
    public static final RegistryObject<ItemBackpack> SMALL_BACKPACK = ITEMS.register("backpack_small", () -> new ItemBackpack("backpack_small", 1));
    public static final RegistryObject<ItemBackpack> TRAVEL_BACKPACK = ITEMS.register("backpack_travel", () -> new ItemBackpack("backpack_travel", 2));
    public static final RegistryObject<ItemBackpack> MEDIUM_BACKPACK = ITEMS.register("backpack_medium", () -> new ItemBackpack("backpack_medium", 3));
    public static final RegistryObject<ItemBackpack> LARGE_BACKPACK = ITEMS.register("backpack_large", () -> new ItemBackpack("backpack_large", 4));
    /** @deprecated fixed-id alias; use SMALL_BACKPACK. */
    @Deprecated public static final RegistryObject<ItemBackpack> BACKPACK = SMALL_BACKPACK;

    public static final RegistryObject<ItemQuill> WOODEN_QUILL = ITEMS.register("wooden_quill", () -> new ItemQuill("wooden_quill", Tiers.WOOD));
    public static final RegistryObject<ItemQuill> STONE_QUILL = ITEMS.register("stone_quill", () -> new ItemQuill("stone_quill", Tiers.STONE));
    public static final RegistryObject<ItemQuill> IRON_QUILL = ITEMS.register("iron_quill", () -> new ItemQuill("iron_quill", Tiers.IRON));
    public static final RegistryObject<ItemQuill> GOLD_QUILL = ITEMS.register("gold_quill", () -> new ItemQuill("gold_quill", Tiers.GOLD));
    public static final RegistryObject<ItemQuill> DIAMOND_QUILL = ITEMS.register("diamond_quill", () -> new ItemQuill("diamond_quill", Tiers.DIAMOND));

    public static final RegistryObject<ItemHammer> WOODEN_HAMMER = ITEMS.register("wooden_hammer", () -> new ItemHammer("wooden_hammer", Tiers.WOOD));
    public static final RegistryObject<ItemHammer> STONE_HAMMER = ITEMS.register("stone_hammer", () -> new ItemHammer("stone_hammer", Tiers.STONE));
    public static final RegistryObject<ItemHammer> IRON_HAMMER = ITEMS.register("iron_hammer", () -> new ItemHammer("iron_hammer", Tiers.IRON));
    public static final RegistryObject<ItemHammer> GOLD_HAMMER = ITEMS.register("gold_hammer", () -> new ItemHammer("gold_hammer", Tiers.GOLD));
    public static final RegistryObject<ItemHammer> DIAMOND_HAMMER = ITEMS.register("diamond_hammer", () -> new ItemHammer("diamond_hammer", Tiers.DIAMOND));

    public static final RegistryObject<ItemManual> MANUAL = ITEMS.register("manual", ItemManual::new);
    public static final RegistryObject<ItemLegacyComponent> LEGACY_COMPONENT = ITEMS.register("component", ItemLegacyComponent::new);

    public static final RegistryObject<ItemComponent> WOODEN_GEAR_SET = component("component_wooden_gear", "gearWood");
    public static final RegistryObject<ItemComponent> IRON_GEAR_SET = component("component_iron_gear", "gearIron");
    public static final RegistryObject<ItemComponent> STEEL_GEAR_SET = component("component_steel_gear", "gearSteel");
    public static final RegistryObject<ItemComponent> WOODEN_BEARINGS = component("component_wooden_bearings", "bearingWood");
    public static final RegistryObject<ItemComponent> IRON_BEARINGS = component("component_iron_bearings", "bearingIron");
    public static final RegistryObject<ItemComponent> STEEL_BEARINGS = component("component_steel_bearings", "bearingSteel");
    public static final RegistryObject<ItemComponent> WOODEN_TORQUE_SHAFT = component("component_wooden_shaft", "shaftWood");
    public static final RegistryObject<ItemComponent> IRON_TORQUE_SHAFT = component("component_iron_shaft", "shaftIron");
    public static final RegistryObject<ItemComponent> STEEL_TORQUE_SHAFT = component("component_steel_shaft", "shaftSteel");
    public static final RegistryObject<ItemBaseCore> STEEL_INGOT = ITEMS.register("steel_ingot", () -> new ItemBaseCore("steel_ingot"));

    private AWCoreItems() {}

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }

    private static RegistryObject<ItemComponent> component(String id, String oreName) {
        RegistryObject<ItemComponent> object = ITEMS.register(id, () -> new ItemComponent(id, id));
        // Ore/tag bridge must run after the item registry is populated.
        return object;
    }

    public static void load() {
        LegacyOreDictionary.registerOre("ingotSteel", STEEL_INGOT.get());
        LegacyOreDictionary.registerOre("gearWood", WOODEN_GEAR_SET.get());
        LegacyOreDictionary.registerOre("gearIron", IRON_GEAR_SET.get());
        LegacyOreDictionary.registerOre("gearSteel", STEEL_GEAR_SET.get());
        LegacyOreDictionary.registerOre("bearingWood", WOODEN_BEARINGS.get());
        LegacyOreDictionary.registerOre("bearingIron", IRON_BEARINGS.get());
        LegacyOreDictionary.registerOre("bearingSteel", STEEL_BEARINGS.get());
        LegacyOreDictionary.registerOre("shaftWood", WOODEN_TORQUE_SHAFT.get());
        LegacyOreDictionary.registerOre("shaftIron", IRON_TORQUE_SHAFT.get());
        LegacyOreDictionary.registerOre("shaftSteel", STEEL_TORQUE_SHAFT.get());
    }

    public static Item getComponentByLegacyMeta(int metadata) {
        return switch (metadata) {
            case 0 -> WOODEN_GEAR_SET.get();
            case 1 -> IRON_GEAR_SET.get();
            case 2 -> STEEL_GEAR_SET.get();
            case 3 -> WOODEN_BEARINGS.get();
            case 4 -> IRON_BEARINGS.get();
            case 5 -> STEEL_BEARINGS.get();
            case 6 -> WOODEN_TORQUE_SHAFT.get();
            case 7 -> IRON_TORQUE_SHAFT.get();
            case 8 -> STEEL_TORQUE_SHAFT.get();
            default -> null;
        };
    }

    public static Item getBackpackByLegacyMeta(int metadata) {
        return switch (metadata) {
            case 0 -> SMALL_BACKPACK.get();
            case 1 -> TRAVEL_BACKPACK.get();
            case 2 -> MEDIUM_BACKPACK.get();
            case 3 -> LARGE_BACKPACK.get();
            default -> SMALL_BACKPACK.get();
        };
    }
}
