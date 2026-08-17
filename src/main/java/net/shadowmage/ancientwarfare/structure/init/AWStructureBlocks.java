package net.shadowmage.ancientwarfare.structure.init;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.shadowmage.ancientwarfare.npc.item.ItemCoin;
import net.shadowmage.ancientwarfare.structure.AncientWarfareStructure;
import net.shadowmage.ancientwarfare.structure.block.*;
import net.shadowmage.ancientwarfare.structure.block.altar.*;
import net.shadowmage.ancientwarfare.structure.item.ItemBlockGravestone;
import net.shadowmage.ancientwarfare.structure.item.ItemBlockStoneCoffin;
import net.shadowmage.ancientwarfare.structure.item.ItemBlockWoodenCoffin;
import net.shadowmage.ancientwarfare.structure.item.ItemLootChestPlacer;
import net.shadowmage.ancientwarfare.structure.item.ItemLootChestPlacer.LootContainerInfo;
import net.shadowmage.ancientwarfare.structure.tile.*;

import java.util.Arrays;
import java.util.function.Supplier;

import static net.shadowmage.ancientwarfare.structure.AncientWarfareStructure.MOD_ID;

/** Native Forge block and block-entity registration for the structure module. */
public final class AWStructureBlocks {
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MOD_ID);

    public static final RegistryObject<BlockAdvancedSpawner> ADVANCED_SPAWNER = block("advanced_spawner",
            () -> new BlockAdvancedSpawner().setBlockEntityType(AWStructureBlocks.ADVANCED_SPAWNER_TILE));
    public static final RegistryObject<BlockGateProxy> GATE_PROXY = block("gate_proxy",
            () -> new BlockGateProxy().setBlockEntityType(AWStructureBlocks.GATE_PROXY_TILE));
    public static final RegistryObject<BlockDraftingStation> DRAFTING_STATION = block("drafting_station",
            () -> new BlockDraftingStation().setBlockEntityType(AWStructureBlocks.DRAFTING_STATION_TILE));
    public static final RegistryObject<BlockStructureBuilder> STRUCTURE_BUILDER_TICKED = block("structure_builder_ticked",
            () -> new BlockStructureBuilder().setBlockEntityType(AWStructureBlocks.STRUCTURE_BUILDER_TICKED_TILE));
    public static final RegistryObject<BlockSoundBlock> SOUND_BLOCK = block("sound_block",
            () -> new BlockSoundBlock().setBlockEntityType(AWStructureBlocks.SOUND_BLOCK_TILE));
    public static final RegistryObject<BlockStructureScanner> STRUCTURE_SCANNER_BLOCK = block("structure_scanner_block",
            () -> new BlockStructureScanner().setBlockEntityType(AWStructureBlocks.STRUCTURE_SCANNER_BLOCK_TILE));
    public static final RegistryObject<BlockAdvancedLootChest> ADVANCED_LOOT_CHEST = block("advanced_loot_chest", BlockAdvancedLootChest::new);
    public static final RegistryObject<BlockTotemPart> TOTEM_PART = block("totem_part",
            () -> new BlockTotemPart().setBlockEntityType(AWStructureBlocks.TOTEM_PART_TILE));
    public static final RegistryObject<BlockFirePit> FIRE_PIT = block("fire_pit", BlockFirePit::new);
    public static final RegistryObject<BlockBrazierFlame> BRAZIER_FLAME = block("brazier_flame", BlockBrazierFlame::new);
    public static final RegistryObject<BlockBrazierEmber> BRAZIER_EMBER = block("brazier_ember", BlockBrazierEmber::new);
    public static final RegistryObject<BlockAltarShortCloth> ALTAR_SHORT_CLOTH = block("altar_short_cloth",
            () -> new BlockAltarShortCloth().setBlockEntityType(AWStructureBlocks.COLORED_TILE));
    public static final RegistryObject<BlockAltarLongCloth> ALTAR_LONG_CLOTH = block("altar_long_cloth",
            () -> new BlockAltarLongCloth().setBlockEntityType(AWStructureBlocks.COLORED_TILE));
    public static final RegistryObject<BlockAltarCandle> ALTAR_CANDLE = block("altar_candle",
            () -> new BlockAltarCandle().setBlockEntityType(AWStructureBlocks.ALTAR_CANDLE_TILE));
    public static final RegistryObject<BlockAltarLectern> ALTAR_LECTERN = block("altar_lectern", BlockAltarLectern::new);
    public static final RegistryObject<BlockAltarSun> ALTAR_SUN = block("altar_sun", BlockAltarSun::new);
    public static final RegistryObject<BlockProtectionFlag> PROTECTION_FLAG = block("protection_flag",
            () -> new BlockProtectionFlag().setBlockEntityType(AWStructureBlocks.PROTECTION_FLAG_TILE));
    public static final RegistryObject<BlockDecorativeFlag> DECORATIVE_FLAG = block("decorative_flag",
            () -> new BlockDecorativeFlag().setBlockEntityType(AWStructureBlocks.DECORATIVE_FLAG_TILE));
    public static final RegistryObject<BlockGoldenIdol> GOLDEN_IDOL = block("golden_idol", BlockGoldenIdol::new);
    public static final RegistryObject<BlockTotemCube> GOBLIN_TOTEM_1 = block("goblin_totem_1", () -> new BlockTotemCube("goblin_totem_1"));
    public static final RegistryObject<BlockTotemCube> GOBLIN_TOTEM_2 = block("goblin_totem_2", () -> new BlockTotemCube("goblin_totem_2"));
    public static final RegistryObject<BlockTotemCube> GOBLIN_TOTEM_2_LIT = block("goblin_totem_2_lit", () -> new BlockTotemCube("goblin_totem_2_lit", true));
    public static final RegistryObject<BlockTotemCube> ORC_TOTEM_1 = block("orc_totem_1", () -> new BlockTotemCube("orc_totem_1"));
    public static final RegistryObject<BlockTotemCube> ORC_TOTEM_2 = block("orc_totem_2", () -> new BlockTotemCube("orc_totem_2"));
    public static final RegistryObject<BlockTotemCube> ORC_TOTEM_2_LIT = block("orc_totem_2_lit", () -> new BlockTotemCube("orc_totem_2_lit", true));
    public static final RegistryObject<BlockLootBasket> LOOT_BASKET = block("loot_basket",
            () -> new BlockLootBasket().setBlockEntityType(AWStructureBlocks.LOOT_BASKET_TILE));
    public static final RegistryObject<BlockWoodenCoffin> WOODEN_COFFIN = block("wooden_coffin",
            () -> new BlockWoodenCoffin().setBlockEntityType(AWStructureBlocks.WOODEN_COFFIN_TILE));
    public static final RegistryObject<BlockStoneCoffin> STONE_COFFIN = block("stone_coffin",
            () -> new BlockStoneCoffin().setBlockEntityType(AWStructureBlocks.STONE_COFFIN_TILE));
    public static final RegistryObject<BlockStool> STOOL = block("stool", BlockStool::new);
    public static final RegistryObject<BlockUrn> URN = block("urn", () -> new BlockUrn().setBlockEntityType(AWStructureBlocks.URN_TILE));
    public static final RegistryObject<BlockTable> TABLE = block("table", BlockTable::new);
    public static final RegistryObject<BlockChair> CHAIR = block("chair", () -> new BlockChair().setBlockEntityType(AWStructureBlocks.CHAIR_TILE));
    public static final RegistryObject<BlockTribalStool> TRIBAL_STOOL = block("tribal_stool", BlockTribalStool::new);
    public static final RegistryObject<BlockWoodenThrone> WOODEN_THRONE = block("wooden_throne", BlockWoodenThrone::new);
    public static final RegistryObject<BlockGoldenThrone> GOLDEN_THRONE = block("golden_throne", BlockGoldenThrone::new);
    public static final RegistryObject<BlockWoodenPost> WOODEN_POST = block("wooden_post", BlockWoodenPost::new);
    public static final RegistryObject<BlockIronCage> IRON_CAGE = block("iron_cage", BlockIronCage::new);
    public static final RegistryObject<BlockGibbet> GIBBET = block("gibbet", BlockGibbet::new);
    public static final RegistryObject<BlockStretchingRack> STRETCHING_RACK = block("stretching_rack", BlockStretchingRack::new);
    public static final RegistryObject<BlockStake> STAKE = block("stake", () -> new BlockStake().setBlockEntityType(AWStructureBlocks.STAKE_TILE));
    public static final RegistryObject<BlockBench> BENCH = block("bench", BlockBench::new);
    public static final RegistryObject<BlockTribalChair> TRIBAL_CHAIR = block("tribal_chair", BlockTribalChair::new);
    public static final RegistryObject<BlockScissorSeat> SCISSOR_SEAT = block("scissor_seat", BlockScissorSeat::new);
    public static final RegistryObject<BlockStatue> STATUE = block("statue", () -> new BlockStatue().setBlockEntityType(AWStructureBlocks.STATUE_TILE));
    public static final RegistryObject<BlockCoinStack> COIN_STACK_COPPER = block("coin_stack_copper",
            () -> new BlockCoinStack("coin_stack_copper", ItemCoin.CoinMetal.COPPER));
    public static final RegistryObject<BlockCoinStack> COIN_STACK_SILVER = block("coin_stack_silver",
            () -> new BlockCoinStack("coin_stack_silver", ItemCoin.CoinMetal.SILVER));
    public static final RegistryObject<BlockCoinStack> COIN_STACK_GOLD = block("coin_stack_gold",
            () -> new BlockCoinStack("coin_stack_gold", ItemCoin.CoinMetal.GOLD));
    public static final RegistryObject<BlockCoinStack> COIN_STACK_ANCIENT = block("coin_stack_ancient",
            () -> new BlockCoinStack("coin_stack_ancient", ItemCoin.CoinMetal.ANCIENT));
    public static final RegistryObject<BlockGravestone> GRAVESTONE = block("gravestone",
            () -> new BlockGravestone().setBlockEntityType(AWStructureBlocks.GRAVESTONE_TILE));

    public static final RegistryObject<BlockEntityType<TileAdvancedSpawner>> ADVANCED_SPAWNER_TILE = tile(
            "advanced_spawner_tile", (type, pos, state) -> new TileAdvancedSpawner(type, pos, state), AWStructureBlocks.ADVANCED_SPAWNER);
    public static final RegistryObject<BlockEntityType<TEGateProxy>> GATE_PROXY_TILE = tile(
            "gate_proxy_tile", (type, pos, state) -> new TEGateProxy(type, pos, state), AWStructureBlocks.GATE_PROXY);
    public static final RegistryObject<BlockEntityType<TileDraftingStation>> DRAFTING_STATION_TILE = tile(
            "drafting_station_tile", (type, pos, state) -> new TileDraftingStation(type, pos, state), AWStructureBlocks.DRAFTING_STATION);
    public static final RegistryObject<BlockEntityType<TileStructureBuilder>> STRUCTURE_BUILDER_TICKED_TILE = tile(
            "structure_builder_ticked_tile", (type, pos, state) -> new TileStructureBuilder(type, pos, state), AWStructureBlocks.STRUCTURE_BUILDER_TICKED);
    public static final RegistryObject<BlockEntityType<TileSoundBlock>> SOUND_BLOCK_TILE = tile(
            "sound_block_tile", (type, pos, state) -> new TileSoundBlock(type, pos, state), AWStructureBlocks.SOUND_BLOCK);
    public static final RegistryObject<BlockEntityType<TileStructureScanner>> STRUCTURE_SCANNER_BLOCK_TILE = tile(
            "structure_scanner_block_tile", (type, pos, state) -> new TileStructureScanner(type, pos, state), AWStructureBlocks.STRUCTURE_SCANNER_BLOCK);
    public static final RegistryObject<BlockEntityType<TileTotemPart>> TOTEM_PART_TILE = tile(
            "totem_part_tile", (type, pos, state) -> new TileTotemPart(type, pos, state), AWStructureBlocks.TOTEM_PART);
    public static final RegistryObject<BlockEntityType<TileColored>> COLORED_TILE = tile(
            "colored_tile", (type, pos, state) -> new TileColored(type, pos, state), AWStructureBlocks.ALTAR_SHORT_CLOTH, AWStructureBlocks.ALTAR_LONG_CLOTH);
    public static final RegistryObject<BlockEntityType<TileAltarCandle>> ALTAR_CANDLE_TILE = tile(
            "altar_candle_tile", (type, pos, state) -> new TileAltarCandle(type, pos, state), AWStructureBlocks.ALTAR_CANDLE);
    public static final RegistryObject<BlockEntityType<TileProtectionFlag>> PROTECTION_FLAG_TILE = tile(
            "protection_flag_tile", (type, pos, state) -> new TileProtectionFlag(type, pos, state), AWStructureBlocks.PROTECTION_FLAG);
    public static final RegistryObject<BlockEntityType<TileDecorativeFlag>> DECORATIVE_FLAG_TILE = tile(
            "decorative_flag_tile", (type, pos, state) -> new TileDecorativeFlag(type, pos, state), AWStructureBlocks.DECORATIVE_FLAG);
    public static final RegistryObject<BlockEntityType<TileWoodenCoffin>> WOODEN_COFFIN_TILE = tile(
            "wooden_coffin", (type, pos, state) -> new TileWoodenCoffin(type, pos, state), AWStructureBlocks.WOODEN_COFFIN);
    public static final RegistryObject<BlockEntityType<TileStoneCoffin>> STONE_COFFIN_TILE = tile(
            "stone_coffin", (type, pos, state) -> new TileStoneCoffin(type, pos, state), AWStructureBlocks.STONE_COFFIN);
    public static final RegistryObject<BlockEntityType<TileUrn>> URN_TILE = tile(
            "urn_tile", (type, pos, state) -> new TileUrn(type, pos, state), AWStructureBlocks.URN);
    public static final RegistryObject<BlockEntityType<TileChair>> CHAIR_TILE = tile(
            "chair_tile", (type, pos, state) -> new TileChair(type, pos, state), AWStructureBlocks.CHAIR);
    public static final RegistryObject<BlockEntityType<TileStake>> STAKE_TILE = tile(
            "stake_tile", (type, pos, state) -> new TileStake(type, pos, state), AWStructureBlocks.STAKE);
    public static final RegistryObject<BlockEntityType<TileStatue>> STATUE_TILE = tile(
            "statue_tile", (type, pos, state) -> new TileStatue(type, pos, state), AWStructureBlocks.STATUE);
    public static final RegistryObject<BlockEntityType<TileGravestone>> GRAVESTONE_TILE = tile(
            "gravestone_tile", (type, pos, state) -> new TileGravestone(type, pos, state), AWStructureBlocks.GRAVESTONE);
    public static final RegistryObject<BlockEntityType<TileAdvancedLootChest>> ADVANCED_LOOT_CHEST_TILE = tile(
            "advanced_loot_chest_tile", (type, pos, state) -> new TileAdvancedLootChest(type, pos, state), AWStructureBlocks.ADVANCED_LOOT_CHEST);
    public static final RegistryObject<BlockEntityType<TileLootBasket>> LOOT_BASKET_TILE = tile(
            "loot_basket_tile", (type, pos, state) -> new TileLootBasket(type, pos, state), AWStructureBlocks.LOOT_BASKET);

    private AWStructureBlocks() {}

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
        modBus.addListener(AWStructureBlocks::commonSetup);
    }

    private static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(AWStructureBlocks::registerLootContainers);
    }

    @SuppressWarnings("ConstantConditions")
    private static void registerLootContainers() {
        ItemLootChestPlacer.registerLootContainer(ForgeRegistries.BLOCKS.getKey(AWStructureBlocks.ADVANCED_LOOT_CHEST.get()).toString(),
                new ItemStack(AWStructureBlocks.ADVANCED_LOOT_CHEST.get()), LootContainerInfo.SINGLE_BLOCK_PLACEMENT_CHECKER);
        ItemLootChestPlacer.registerLootContainer(ForgeRegistries.BLOCKS.getKey(AWStructureBlocks.LOOT_BASKET.get()).toString(),
                new ItemStack(AWStructureBlocks.LOOT_BASKET.get()), LootContainerInfo.SINGLE_BLOCK_PLACEMENT_CHECKER);
        NonNullList<ItemStack> subBlocks = NonNullList.create();
        AWStructureBlocks.WOODEN_COFFIN.get().getSubBlocks(AncientWarfareStructure.TAB.get(), subBlocks);
        subBlocks.forEach(subBlock -> ItemLootChestPlacer.registerLootContainer(
                ForgeRegistries.ITEMS.getKey(subBlock.getItem()).toString() + "_" + ItemBlockWoodenCoffin.getVariant(subBlock), subBlock,
                (block, world, pos, sidePlacedOn, placer) -> ItemBlockWoodenCoffin.canPlace(world, pos, sidePlacedOn, placer)));
        subBlocks = NonNullList.create();
        AWStructureBlocks.STONE_COFFIN.get().getSubBlocks(AncientWarfareStructure.TAB.get(), subBlocks);
        subBlocks.forEach(subBlock -> ItemLootChestPlacer.registerLootContainer(
                ForgeRegistries.ITEMS.getKey(subBlock.getItem()).toString() + "_" + ItemBlockStoneCoffin.getVariant(subBlock), subBlock,
                (block, world, pos, sidePlacedOn, placer) -> ItemBlockStoneCoffin.canPlace(world, pos, sidePlacedOn, placer)));
        subBlocks = NonNullList.create();
        AWStructureBlocks.GRAVESTONE.get().getSubBlocks(AncientWarfareStructure.TAB.get(), subBlocks);
        subBlocks.forEach(subBlock -> ItemLootChestPlacer.registerLootContainer(
                ForgeRegistries.ITEMS.getKey(subBlock.getItem()).toString() + "_" + ItemBlockGravestone.getVariant(subBlock), subBlock,
                LootContainerInfo.SINGLE_BLOCK_PLACEMENT_CHECKER));
        ItemLootChestPlacer.registerLootContainer(ForgeRegistries.BLOCKS.getKey(AWStructureBlocks.URN.get()).toString(),
                new ItemStack(AWStructureBlocks.URN.get()), LootContainerInfo.SINGLE_BLOCK_PLACEMENT_CHECKER);
    }

    private static <T extends Block> RegistryObject<T> block(String id, Supplier<T> supplier) {
        return BLOCKS.register(id, supplier);
    }

    @SafeVarargs
    private static <T extends BlockEntity> RegistryObject<BlockEntityType<T>> tile(
            String id, TileFactory<T> factory, RegistryObject<? extends Block>... blocks) {
        return BLOCK_ENTITIES.register(id, () -> {
            Block[] validBlocks = Arrays.stream(blocks).map(RegistryObject::get).toArray(Block[]::new);
            final BlockEntityType<T>[] holder = new BlockEntityType[1];
            BlockEntityType<T> type = BlockEntityType.Builder.of(
                    (pos, state) -> factory.create(holder[0], pos, state), validBlocks).build(null);
            holder[0] = type;
            return type;
        });
    }

    @FunctionalInterface
    private interface TileFactory<T extends BlockEntity> {
        T create(BlockEntityType<T> type, net.minecraft.core.BlockPos pos, net.minecraft.world.level.block.state.BlockState state);
    }
}
