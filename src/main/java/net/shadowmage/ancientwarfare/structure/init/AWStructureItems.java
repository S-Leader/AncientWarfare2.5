package net.shadowmage.ancientwarfare.structure.init;

import net.minecraft.core.Vec3i;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.shadowmage.ancientwarfare.core.item.ItemBlockBase;
import net.shadowmage.ancientwarfare.structure.AncientWarfareStructure;
import net.shadowmage.ancientwarfare.structure.item.*;

import java.util.function.Supplier;

/** Native Forge item registration for the structure module. */
public final class AWStructureItems {
    private static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, AncientWarfareStructure.MOD_ID);

    public static final RegistryObject<ItemStructureScanner> STRUCTURE_SCANNER = item("structure_scanner",
            () -> new ItemStructureScanner("structure_scanner"));
    public static final RegistryObject<ItemStructureBuilder> STRUCTURE_BUILDER = item("structure_builder",
            () -> new ItemStructureBuilder("structure_builder"));
    public static final RegistryObject<ItemStructureBuilderWorldGen> STRUCTURE_BUILDER_WORLD_GEN = item("structure_builder_world_gen",
            () -> new ItemStructureBuilderWorldGen("structure_builder_world_gen"));
    public static final RegistryObject<ItemTownBuilder> TOWN_BUILDER = item("town_builder", () -> new ItemTownBuilder("town_builder"));
    public static final RegistryObject<ItemSpawnerPlacer> SPAWNER_PLACER = item("spawner_placer", () -> new ItemSpawnerPlacer("spawner_placer"));

    public static final RegistryObject<ItemGateSpawner> GATE_SPAWNER = item("gate_spawner", () -> new ItemGateSpawner("gate_spawner"));
    public static final RegistryObject<ItemGateSpawner> GATE_VERTICAL_WOODEN = item("gate_vertical_wooden", () -> new ItemGateSpawner("gate_vertical_wooden", 0));
    public static final RegistryObject<ItemGateSpawner> GATE_VERTICAL_IRON = item("gate_vertical_iron", () -> new ItemGateSpawner("gate_vertical_iron", 1));
    public static final RegistryObject<ItemGateSpawner> GATE_SINGLE_WOOD = item("gate_single_wood", () -> new ItemGateSpawner("gate_single_wood", 4));
    public static final RegistryObject<ItemGateSpawner> GATE_SINGLE_IRON = item("gate_single_iron", () -> new ItemGateSpawner("gate_single_iron", 5));
    public static final RegistryObject<ItemGateSpawner> GATE_DOUBLE_WOOD = item("gate_double_wood", () -> new ItemGateSpawner("gate_double_wood", 8));
    public static final RegistryObject<ItemGateSpawner> GATE_DOUBLE_IRON = item("gate_double_iron", () -> new ItemGateSpawner("gate_double_iron", 9));
    public static final RegistryObject<ItemGateSpawner> GATE_DRAWBRIDGE = item("gate_drawbridge", () -> new ItemGateSpawner("gate_drawbridge", 12));

    public static final RegistryObject<ItemConstructionTool> CONSTRUCTION_TOOL = item("construction_tool", () -> new ItemConstructionTool("construction_tool"));
    public static final RegistryObject<ItemConstructionToolLakes> CONSTRUCTION_TOOL_LAKES = item("construction_tool_lakes",
            () -> new ItemConstructionToolLakes("construction_tool_lakes"));
    public static final RegistryObject<ItemBlockInfo> BLOCK_INFO_CLICKER = item("block_info_clicker", () -> new ItemBlockInfo("block_info_clicker"));
    public static final RegistryObject<ItemLootChestPlacer> LOOT_CHEST_PLACER = item("loot_chest_placer", ItemLootChestPlacer::new);

    // Block items are registered here as normal item RegistryObjects instead of being back-filled from a block registry callback.
    public static final RegistryObject<ItemBlockAdvancedSpawner> ADVANCED_SPAWNER = item("advanced_spawner",
            () -> new ItemBlockAdvancedSpawner(AWStructureBlocks.ADVANCED_SPAWNER.get()));
    public static final RegistryObject<ItemBlockBase> GATE_PROXY = item("gate_proxy", () -> new ItemBlockBase(AWStructureBlocks.GATE_PROXY.get()));
    public static final RegistryObject<ItemBlockBase> DRAFTING_STATION = item("drafting_station", () -> new ItemBlockBase(AWStructureBlocks.DRAFTING_STATION.get()));
    public static final RegistryObject<ItemBlockStructureBuilder> STRUCTURE_BUILDER_TICKED = item("structure_builder_ticked",
            () -> new ItemBlockStructureBuilder(AWStructureBlocks.STRUCTURE_BUILDER_TICKED.get()));
    public static final RegistryObject<ItemBlockBase> SOUND_BLOCK = item("sound_block", () -> new ItemBlockBase(AWStructureBlocks.SOUND_BLOCK.get()));
    public static final RegistryObject<ItemBlockBase> STRUCTURE_SCANNER_BLOCK = item("structure_scanner_block",
            () -> new ItemBlockBase(AWStructureBlocks.STRUCTURE_SCANNER_BLOCK.get()));
    public static final RegistryObject<ItemBlockBase> ADVANCED_LOOT_CHEST = item("advanced_loot_chest",
            () -> new ItemBlockBase(AWStructureBlocks.ADVANCED_LOOT_CHEST.get()));
    public static final RegistryObject<ItemBlockFirePit> FIRE_PIT = item("fire_pit", () -> new ItemBlockFirePit(AWStructureBlocks.FIRE_PIT.get()));
    public static final RegistryObject<ItemBlockTotemPart> TOTEM_PART = item("totem_part", () -> new ItemBlockTotemPart(AWStructureBlocks.TOTEM_PART.get()));
    public static final RegistryObject<ItemBlockBrazierEmber> BRAZIER_EMBER = item("brazier_ember", () -> new ItemBlockBrazierEmber(AWStructureBlocks.BRAZIER_EMBER.get()));
    public static final RegistryObject<ItemBlockBrazierFlame> BRAZIER_FLAME = item("brazier_flame", () -> new ItemBlockBrazierFlame(AWStructureBlocks.BRAZIER_FLAME.get()));
    public static final RegistryObject<ItemBlockColored> ALTAR_SHORT_CLOTH = item("altar_short_cloth", () -> new ItemBlockColored(AWStructureBlocks.ALTAR_SHORT_CLOTH.get()));
    public static final RegistryObject<ItemBlockColored> ALTAR_LONG_CLOTH = item("altar_long_cloth", () -> new ItemBlockColored(AWStructureBlocks.ALTAR_LONG_CLOTH.get()));
    public static final RegistryObject<ItemBlockColored> ALTAR_CANDLE = item("altar_candle", () -> new ItemBlockColored(AWStructureBlocks.ALTAR_CANDLE.get()));
    public static final RegistryObject<ItemBlockBase> ALTAR_LECTERN = item("altar_lectern", () -> new ItemBlockBase(AWStructureBlocks.ALTAR_LECTERN.get()));
    public static final RegistryObject<ItemBlockBase> ALTAR_SUN = item("altar_sun", () -> new ItemBlockBase(AWStructureBlocks.ALTAR_SUN.get()));
    public static final RegistryObject<ItemBlockFlag> PROTECTION_FLAG = item("protection_flag", () -> new ItemBlockFlag(AWStructureBlocks.PROTECTION_FLAG.get()));
    public static final RegistryObject<ItemBlockFlag> DECORATIVE_FLAG = item("decorative_flag", () -> new ItemBlockFlag(AWStructureBlocks.DECORATIVE_FLAG.get()));
    public static final RegistryObject<ItemBlockBase> GOLDEN_IDOL = item("golden_idol", () -> new ItemBlockBase(AWStructureBlocks.GOLDEN_IDOL.get()));
    public static final RegistryObject<ItemBlockBase> ORC_TOTEM_1 = item("orc_totem_1", () -> new ItemBlockBase(AWStructureBlocks.ORC_TOTEM_1.get()));
    public static final RegistryObject<ItemBlockBase> ORC_TOTEM_2 = item("orc_totem_2", () -> new ItemBlockBase(AWStructureBlocks.ORC_TOTEM_2.get()));
    public static final RegistryObject<ItemBlockBase> ORC_TOTEM_2_LIT = item("orc_totem_2_lit", () -> new ItemBlockBase(AWStructureBlocks.ORC_TOTEM_2_LIT.get()));
    public static final RegistryObject<ItemBlockBase> GOBLIN_TOTEM_1 = item("goblin_totem_1", () -> new ItemBlockBase(AWStructureBlocks.GOBLIN_TOTEM_1.get()));
    public static final RegistryObject<ItemBlockBase> GOBLIN_TOTEM_2 = item("goblin_totem_2", () -> new ItemBlockBase(AWStructureBlocks.GOBLIN_TOTEM_2.get()));
    public static final RegistryObject<ItemBlockBase> GOBLIN_TOTEM_2_LIT = item("goblin_totem_2_lit", () -> new ItemBlockBase(AWStructureBlocks.GOBLIN_TOTEM_2_LIT.get()));
    public static final RegistryObject<ItemBlockBase> LOOT_BASKET = item("loot_basket", () -> new ItemBlockBase(AWStructureBlocks.LOOT_BASKET.get()));
    public static final RegistryObject<ItemBlockWoodenCoffin> WOODEN_COFFIN = item("wooden_coffin", () -> new ItemBlockWoodenCoffin(AWStructureBlocks.WOODEN_COFFIN.get()));
    public static final RegistryObject<ItemBlockStoneCoffin> STONE_COFFIN = item("stone_coffin", () -> new ItemBlockStoneCoffin(AWStructureBlocks.STONE_COFFIN.get()));
    public static final RegistryObject<WoodItemBlock> STOOL = item("stool", () -> new WoodItemBlock(AWStructureBlocks.STOOL.get()));
    public static final RegistryObject<ItemBlockBase> URN = item("urn", () -> new ItemBlockBase(AWStructureBlocks.URN.get()));
    public static final RegistryObject<WoodItemBlock> TABLE = item("table", () -> new WoodItemBlock(AWStructureBlocks.TABLE.get()));
    public static final RegistryObject<ItemBlockChair> CHAIR = item("chair", () -> new ItemBlockChair(AWStructureBlocks.CHAIR.get()));
    public static final RegistryObject<ItemBlockBase> TRIBAL_STOOL = item("tribal_stool", () -> new ItemBlockBase(AWStructureBlocks.TRIBAL_STOOL.get()));
    public static final RegistryObject<ItemBlockBase> WOODEN_THRONE = item("wooden_throne", () -> new ItemBlockBase(AWStructureBlocks.WOODEN_THRONE.get()));
    public static final RegistryObject<ItemBlockBase> GOLDEN_THRONE = item("golden_throne", () -> new ItemBlockBase(AWStructureBlocks.GOLDEN_THRONE.get()));
    public static final RegistryObject<ItemBlockPosts> WOODEN_POST = item("wooden_post", () -> new ItemBlockPosts(AWStructureBlocks.WOODEN_POST.get()));
    public static final RegistryObject<ItemBlockPosts> IRON_CAGE = item("iron_cage", () -> new ItemBlockPosts(AWStructureBlocks.IRON_CAGE.get()));
    public static final RegistryObject<ItemBlockPosts> GIBBET = item("gibbet", () -> new ItemBlockPosts(AWStructureBlocks.GIBBET.get()));
    public static final RegistryObject<ItemBlockStretchingRack> STRETCHING_RACK = item("stretching_rack", () -> new ItemBlockStretchingRack(AWStructureBlocks.STRETCHING_RACK.get()));
    public static final RegistryObject<ItemMultiBlock> STAKE = item("stake", () -> new ItemMultiBlock(AWStructureBlocks.STAKE.get(), new Vec3i(0, 0, 0), new Vec3i(0, 2, 0)));
    public static final RegistryObject<WoodItemBlock> BENCH = item("bench", () -> new WoodItemBlock(AWStructureBlocks.BENCH.get()));
    public static final RegistryObject<ItemMultiBlock> TRIBAL_CHAIR = item("tribal_chair", () -> new ItemMultiBlock(AWStructureBlocks.TRIBAL_CHAIR.get(), new Vec3i(0, 0, 0), new Vec3i(0, 1, 0)));
    public static final RegistryObject<ItemMultiBlock> SCISSOR_SEAT = item("scissor_seat", () -> new ItemMultiBlock(AWStructureBlocks.SCISSOR_SEAT.get(), new Vec3i(0, 0, 0), new Vec3i(0, 1, 0)));
    public static final RegistryObject<ItemBlockBase> STATUE = item("statue", () -> new ItemBlockBase(AWStructureBlocks.STATUE.get()));
    public static final RegistryObject<ItemBlockBase> COIN_STACK_COPPER = item("coin_stack_copper", () -> new ItemBlockBase(AWStructureBlocks.COIN_STACK_COPPER.get()));
    public static final RegistryObject<ItemBlockBase> COIN_STACK_SILVER = item("coin_stack_silver", () -> new ItemBlockBase(AWStructureBlocks.COIN_STACK_SILVER.get()));
    public static final RegistryObject<ItemBlockBase> COIN_STACK_GOLD = item("coin_stack_gold", () -> new ItemBlockBase(AWStructureBlocks.COIN_STACK_GOLD.get()));
    public static final RegistryObject<ItemBlockBase> COIN_STACK_ANCIENT = item("coin_stack_ancient", () -> new ItemBlockBase(AWStructureBlocks.COIN_STACK_ANCIENT.get()));
    public static final RegistryObject<ItemBlockGravestone> GRAVESTONE = item("gravestone", () -> new ItemBlockGravestone(AWStructureBlocks.GRAVESTONE.get()));

    private AWStructureItems() {}

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }

    public static ItemGateSpawner getGateSpawnerItem(int gateId) {
        RegistryObject<ItemGateSpawner> item = switch (gateId) {
            case 0 -> GATE_VERTICAL_WOODEN;
            case 1 -> GATE_VERTICAL_IRON;
            case 4 -> GATE_SINGLE_WOOD;
            case 5 -> GATE_SINGLE_IRON;
            case 8 -> GATE_DOUBLE_WOOD;
            case 9 -> GATE_DOUBLE_IRON;
            case 12 -> GATE_DRAWBRIDGE;
            default -> GATE_SPAWNER;
        };
        return item.get();
    }

    private static <T extends Item> RegistryObject<T> item(String id, Supplier<T> supplier) {
        return ITEMS.register(id, supplier);
    }
}
