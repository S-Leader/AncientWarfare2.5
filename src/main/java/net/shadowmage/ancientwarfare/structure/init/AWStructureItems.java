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

    public static ItemGateSpawner GATE_SPAWNER;
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
            LegacyRegistryHelper.register(helper, new ItemConstructionTool("construction_tool"));
            LegacyRegistryHelper.register(helper, new ItemConstructionToolLakes("construction_tool_lakes"));
            LegacyRegistryHelper.register(helper, new ItemBlockInfo("block_info_clicker"));
            LegacyRegistryHelper.register(helper, new ItemLootChestPlacer());
        });
    }
}
