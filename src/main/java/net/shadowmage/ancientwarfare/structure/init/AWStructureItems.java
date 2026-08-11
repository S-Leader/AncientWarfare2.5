package net.shadowmage.ancientwarfare.structure.init;

import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import net.shadowmage.ancientwarfare.core.util.LegacyRegistryHelper;
import net.shadowmage.ancientwarfare.structure.AncientWarfareStructure;
import net.shadowmage.ancientwarfare.structure.item.*;

@Mod.EventBusSubscriber(modid = AncientWarfareStructure.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class AWStructureItems {
    private AWStructureItems() {
    }

    /** Legacy metadata placer kept only for old saves/templates. */
    @Deprecated
    public static ItemGateSpawner GATE_SPAWNER;
    public static ItemGateSpawner GATE_VERTICAL_WOODEN;
    public static ItemGateSpawner GATE_VERTICAL_IRON;
    public static ItemGateSpawner GATE_SINGLE_WOOD;
    public static ItemGateSpawner GATE_SINGLE_IRON;
    public static ItemGateSpawner GATE_DOUBLE_WOOD;
    public static ItemGateSpawner GATE_DOUBLE_IRON;
    public static ItemGateSpawner GATE_DRAWBRIDGE;
    public static Item STRUCTURE_SCANNER;
    public static Item TOTEM_PART;
    public static ItemBlockStructureBuilder STRUCTURE_BUILDER_TICKED;
    public static ItemBlockColored ALTAR_CANDLE;
    public static ItemBlockColored ALTAR_SHORT_CLOTH;
    public static ItemBlockColored ALTAR_LONG_CLOTH;

    @SubscribeEvent
    public static void register(RegisterEvent event) {
        event.register(ForgeRegistries.Keys.ITEMS, helper -> {
            STRUCTURE_SCANNER = LegacyRegistryHelper.register(helper, new ItemStructureScanner("structure_scanner"));
            LegacyRegistryHelper.register(helper, new ItemStructureBuilder("structure_builder"));
            LegacyRegistryHelper.register(helper, new ItemStructureBuilderWorldGen("structure_builder_world_gen"));
            LegacyRegistryHelper.register(helper, new ItemTownBuilder("town_builder"));
            LegacyRegistryHelper.register(helper, new ItemSpawnerPlacer("spawner_placer"));
            GATE_SPAWNER = LegacyRegistryHelper.register(helper, new ItemGateSpawner("gate_spawner"));
            GATE_VERTICAL_WOODEN = LegacyRegistryHelper.register(helper, new ItemGateSpawner("gate_vertical_wooden", 0));
            GATE_VERTICAL_IRON = LegacyRegistryHelper.register(helper, new ItemGateSpawner("gate_vertical_iron", 1));
            GATE_SINGLE_WOOD = LegacyRegistryHelper.register(helper, new ItemGateSpawner("gate_single_wood", 4));
            GATE_SINGLE_IRON = LegacyRegistryHelper.register(helper, new ItemGateSpawner("gate_single_iron", 5));
            GATE_DOUBLE_WOOD = LegacyRegistryHelper.register(helper, new ItemGateSpawner("gate_double_wood", 8));
            GATE_DOUBLE_IRON = LegacyRegistryHelper.register(helper, new ItemGateSpawner("gate_double_iron", 9));
            GATE_DRAWBRIDGE = LegacyRegistryHelper.register(helper, new ItemGateSpawner("gate_drawbridge", 12));
            LegacyRegistryHelper.register(helper, new ItemConstructionTool("construction_tool"));
            LegacyRegistryHelper.register(helper, new ItemConstructionToolLakes("construction_tool_lakes"));
            LegacyRegistryHelper.register(helper, new ItemBlockInfo("block_info_clicker"));
            LegacyRegistryHelper.register(helper, new ItemLootChestPlacer());
        });
    }
    public static ItemGateSpawner getGateSpawnerItem(int gateId) {
        ItemGateSpawner item = switch (gateId) {
            case 0 -> GATE_VERTICAL_WOODEN;
            case 1 -> GATE_VERTICAL_IRON;
            case 4 -> GATE_SINGLE_WOOD;
            case 5 -> GATE_SINGLE_IRON;
            case 8 -> GATE_DOUBLE_WOOD;
            case 9 -> GATE_DOUBLE_IRON;
            case 12 -> GATE_DRAWBRIDGE;
            default -> null;
        };
        return item != null ? item : GATE_SPAWNER;
    }
}
