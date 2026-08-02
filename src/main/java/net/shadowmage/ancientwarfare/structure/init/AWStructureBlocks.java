package net.shadowmage.ancientwarfare.structure.init;

import net.minecraft.core.NonNullList;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import net.shadowmage.ancientwarfare.core.item.ItemBlockBase;
import net.shadowmage.ancientwarfare.core.tile.LegacyBlockEntityRegistry;
import net.shadowmage.ancientwarfare.core.util.LegacyBlockRegistryHelper;
import net.shadowmage.ancientwarfare.npc.item.ItemCoin;
import net.shadowmage.ancientwarfare.structure.AncientWarfareStructure;
import net.shadowmage.ancientwarfare.structure.block.*;
import net.shadowmage.ancientwarfare.structure.block.altar.*;
import net.shadowmage.ancientwarfare.structure.item.*;
import net.shadowmage.ancientwarfare.structure.item.ItemLootChestPlacer.LootContainerInfo;
import net.shadowmage.ancientwarfare.structure.tile.*;

import static net.shadowmage.ancientwarfare.structure.AncientWarfareStructure.MOD_ID;

@Mod.EventBusSubscriber(modid = AncientWarfareStructure.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class AWStructureBlocks {
    private AWStructureBlocks() {
    }

    public static Block ADVANCED_SPAWNER;
    public static Block GATE_PROXY;
    public static Block DRAFTING_STATION;
    public static Block STRUCTURE_BUILDER_TICKED;
    public static Block SOUND_BLOCK;
    public static Block STRUCTURE_SCANNER_BLOCK;
    public static Block ADVANCED_LOOT_CHEST;
    public static BlockEntityType<TileAdvancedLootChest> ADVANCED_LOOT_CHEST_TILE;
    public static Block FIRE_PIT;
    public static Block TOTEM_PART;
    public static Block ALTAR_SHORT_CLOTH;
    public static Block ALTAR_LONG_CLOTH;
    public static Block ALTAR_CANDLE;
    public static Block ALTAR_LECTERN;
    public static Block ALTAR_SUN;
    public static BlockProtectionFlag PROTECTION_FLAG;
    public static BlockDecorativeFlag DECORATIVE_FLAG;
    public static Block GOLDEN_IDOL;
    public static Block ORC_TOTEM_1;
    public static Block ORC_TOTEM_2;
    public static Block ORC_TOTEM_2_LIT;
    public static Block GOBLIN_TOTEM_1;
    public static Block GOBLIN_TOTEM_2;
    public static Block GOBLIN_TOTEM_2_LIT;
    public static Block LOOT_BASKET;
    public static BlockEntityType<TileLootBasket> LOOT_BASKET_TILE;
    public static Block BRAZIER_EMBER;
    public static Block BRAZIER_FLAME;
    public static Block WOODEN_COFFIN;
    public static Block STONE_COFFIN;
    public static Block STOOL;
    public static Block URN;
    public static Block TABLE;
    public static Block CHAIR;
    public static Block TRIBAL_STOOL;
    public static Block WOODEN_THRONE;
    public static Block GOLDEN_THRONE;
    public static Block WOODEN_POST;
    public static Block IRON_CAGE;
    public static Block GIBBET;
    public static Block STRETCHING_RACK;
    public static Block STAKE;
    public static Block BENCH;
    public static Block TRIBAL_CHAIR;
    public static Block SCISSOR_SEAT;
    public static Block STATUE;
    public static Block COIN_STACK_COPPER;
    public static Block COIN_STACK_SILVER;
    public static Block COIN_STACK_GOLD;
    public static Block COIN_STACK_ANCIENT;
    public static Block GRAVESTONE;

    @SubscribeEvent
    public static void registerItemBlocks(RegisterEvent event) {
        event.register(ForgeRegistries.Keys.ITEMS, helper -> {

            LegacyBlockRegistryHelper.registerItem(helper, new ItemBlockAdvancedSpawner(ADVANCED_SPAWNER));
            LegacyBlockRegistryHelper.registerItem(helper, new ItemBlockBase(GATE_PROXY));
            LegacyBlockRegistryHelper.registerItem(helper, new ItemBlockBase(DRAFTING_STATION));
            AWStructureItems.STRUCTURE_BUILDER_TICKED = LegacyBlockRegistryHelper.registerItem(helper, new ItemBlockStructureBuilder(STRUCTURE_BUILDER_TICKED));
            LegacyBlockRegistryHelper.registerItem(helper, new ItemBlockBase(SOUND_BLOCK));
            LegacyBlockRegistryHelper.registerItem(helper, new ItemBlockBase(STRUCTURE_SCANNER_BLOCK));
            LegacyBlockRegistryHelper.registerItem(helper, new ItemBlockBase(ADVANCED_LOOT_CHEST));
            LegacyBlockRegistryHelper.registerItem(helper, new ItemBlockFirePit(FIRE_PIT));
            AWStructureItems.TOTEM_PART = LegacyBlockRegistryHelper.registerItem(helper, new ItemBlockTotemPart(TOTEM_PART));
            LegacyBlockRegistryHelper.registerItem(helper, new ItemBlockBrazierEmber(BRAZIER_EMBER));
            LegacyBlockRegistryHelper.registerItem(helper, new ItemBlockBrazierFlame(BRAZIER_FLAME));

            AWStructureItems.ALTAR_SHORT_CLOTH = LegacyBlockRegistryHelper.registerItem(helper, new ItemBlockColored(ALTAR_SHORT_CLOTH));
            AWStructureItems.ALTAR_LONG_CLOTH = LegacyBlockRegistryHelper.registerItem(helper, new ItemBlockColored(ALTAR_LONG_CLOTH));
            AWStructureItems.ALTAR_CANDLE = LegacyBlockRegistryHelper.registerItem(helper, new ItemBlockColored(ALTAR_CANDLE));
            LegacyBlockRegistryHelper.registerItem(helper, new ItemBlockBase(ALTAR_LECTERN));
            LegacyBlockRegistryHelper.registerItem(helper, new ItemBlockBase(ALTAR_SUN));
            LegacyBlockRegistryHelper.registerItem(helper, new ItemBlockFlag(PROTECTION_FLAG));
            LegacyBlockRegistryHelper.registerItem(helper, new ItemBlockFlag(DECORATIVE_FLAG));
            LegacyBlockRegistryHelper.registerItem(helper, new ItemBlockBase(GOLDEN_IDOL));

            LegacyBlockRegistryHelper.registerItem(helper, new ItemBlockBase(ORC_TOTEM_1));
            LegacyBlockRegistryHelper.registerItem(helper, new ItemBlockBase(ORC_TOTEM_2));
            LegacyBlockRegistryHelper.registerItem(helper, new ItemBlockBase(ORC_TOTEM_2_LIT));
            LegacyBlockRegistryHelper.registerItem(helper, new ItemBlockBase(GOBLIN_TOTEM_1));
            LegacyBlockRegistryHelper.registerItem(helper, new ItemBlockBase(GOBLIN_TOTEM_2));
            LegacyBlockRegistryHelper.registerItem(helper, new ItemBlockBase(GOBLIN_TOTEM_2_LIT));

            LegacyBlockRegistryHelper.registerItem(helper, new ItemBlockBase(LOOT_BASKET));
            LegacyBlockRegistryHelper.registerItem(helper, new ItemBlockWoodenCoffin(WOODEN_COFFIN));
            LegacyBlockRegistryHelper.registerItem(helper, new ItemBlockStoneCoffin(STONE_COFFIN));
            LegacyBlockRegistryHelper.registerItem(helper, new WoodItemBlock(STOOL));
            LegacyBlockRegistryHelper.registerItem(helper, new ItemBlockBase(URN));
            LegacyBlockRegistryHelper.registerItem(helper, new WoodItemBlock(TABLE));
            LegacyBlockRegistryHelper.registerItem(helper, new ItemBlockChair(CHAIR));
            LegacyBlockRegistryHelper.registerItem(helper, new ItemBlockBase(TRIBAL_STOOL));
            LegacyBlockRegistryHelper.registerItem(helper, new ItemBlockBase(WOODEN_THRONE));
            LegacyBlockRegistryHelper.registerItem(helper, new ItemBlockBase(GOLDEN_THRONE));
            LegacyBlockRegistryHelper.registerItem(helper, new ItemBlockPosts(WOODEN_POST));
            LegacyBlockRegistryHelper.registerItem(helper, new ItemBlockPosts(IRON_CAGE));
            LegacyBlockRegistryHelper.registerItem(helper, new ItemBlockPosts(GIBBET));
            LegacyBlockRegistryHelper.registerItem(helper, new ItemBlockStretchingRack(STRETCHING_RACK));
            LegacyBlockRegistryHelper.registerItem(helper, new ItemMultiBlock(STAKE, new Vec3i(0, 0, 0), new Vec3i(0, 2, 0)));
            LegacyBlockRegistryHelper.registerItem(helper, new WoodItemBlock(BENCH));
            LegacyBlockRegistryHelper.registerItem(helper, new ItemMultiBlock(TRIBAL_CHAIR, new Vec3i(0, 0, 0), new Vec3i(0, 1, 0)));
            LegacyBlockRegistryHelper.registerItem(helper, new ItemMultiBlock(SCISSOR_SEAT, new Vec3i(0, 0, 0), new Vec3i(0, 1, 0)));
            LegacyBlockRegistryHelper.registerItem(helper, new ItemBlockBase(STATUE));
            LegacyBlockRegistryHelper.registerItem(helper, new ItemBlockBase(COIN_STACK_COPPER));
            LegacyBlockRegistryHelper.registerItem(helper, new ItemBlockBase(COIN_STACK_SILVER));
            LegacyBlockRegistryHelper.registerItem(helper, new ItemBlockBase(COIN_STACK_GOLD));
            LegacyBlockRegistryHelper.registerItem(helper, new ItemBlockBase(COIN_STACK_ANCIENT));
            LegacyBlockRegistryHelper.registerItem(helper, new ItemBlockGravestone(GRAVESTONE));
        });
    }

    @SubscribeEvent
    public static void commonSetup(net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent event) {
        //Deferred out of the item RegisterEvent: builds ItemStacks and reads the creative-tab
        //RegistryObject, neither of which is legal while registries are still being populated.
        event.enqueueWork(AWStructureBlocks::registerLootContainers);
    }

    @SuppressWarnings("ConstantConditions")
    private static void registerLootContainers() {
        ItemLootChestPlacer.registerLootContainer(ForgeRegistries.BLOCKS.getKey(ADVANCED_LOOT_CHEST).toString(), new ItemStack(ADVANCED_LOOT_CHEST), LootContainerInfo.SINGLE_BLOCK_PLACEMENT_CHECKER);
        ItemLootChestPlacer.registerLootContainer(ForgeRegistries.BLOCKS.getKey(LOOT_BASKET).toString(), new ItemStack(LOOT_BASKET), LootContainerInfo.SINGLE_BLOCK_PLACEMENT_CHECKER);
        NonNullList<ItemStack> subBlocks = NonNullList.create();
        ((BlockWoodenCoffin) WOODEN_COFFIN).getSubBlocks(AncientWarfareStructure.TAB.get(), subBlocks);
        subBlocks.forEach(subBlock -> ItemLootChestPlacer.registerLootContainer(
                ForgeRegistries.ITEMS.getKey(subBlock.getItem()).toString() + "_" + ItemBlockWoodenCoffin.getVariant(subBlock), subBlock,
                (block, world, pos, sidePlacedOn, placer) -> ItemBlockWoodenCoffin.canPlace(world, pos, sidePlacedOn, placer)));
        subBlocks = NonNullList.create();
        ((BlockStoneCoffin) STONE_COFFIN).getSubBlocks(AncientWarfareStructure.TAB.get(), subBlocks);
        subBlocks.forEach(subBlock -> ItemLootChestPlacer.registerLootContainer(
                ForgeRegistries.ITEMS.getKey(subBlock.getItem()).toString() + "_" + ItemBlockStoneCoffin.getVariant(subBlock), subBlock,
                (block, world, pos, sidePlacedOn, placer) -> ItemBlockStoneCoffin.canPlace(world, pos, sidePlacedOn, placer)));
        subBlocks = NonNullList.create();
        ((BlockGravestone) GRAVESTONE).getSubBlocks(AncientWarfareStructure.TAB.get(), subBlocks);
        subBlocks.forEach(subBlock -> ItemLootChestPlacer.registerLootContainer(
                ForgeRegistries.ITEMS.getKey(subBlock.getItem()).toString() + "_" + ItemBlockGravestone.getVariant(subBlock), subBlock,
                LootContainerInfo.SINGLE_BLOCK_PLACEMENT_CHECKER));
        ItemLootChestPlacer.registerLootContainer(ForgeRegistries.BLOCKS.getKey(URN).toString(), new ItemStack(URN), LootContainerInfo.SINGLE_BLOCK_PLACEMENT_CHECKER);
    }

    @SubscribeEvent
    public static void registerBlocks(RegisterEvent event) {
        event.register(ForgeRegistries.Keys.BLOCKS, helper -> {
            ADVANCED_SPAWNER = LegacyBlockRegistryHelper.register(helper, new BlockAdvancedSpawner());
            GATE_PROXY = LegacyBlockRegistryHelper.register(helper, new BlockGateProxy());
            DRAFTING_STATION = LegacyBlockRegistryHelper.register(helper, new BlockDraftingStation());
            STRUCTURE_BUILDER_TICKED = LegacyBlockRegistryHelper.register(helper, new BlockStructureBuilder());
            SOUND_BLOCK = LegacyBlockRegistryHelper.register(helper, new BlockSoundBlock());
            STRUCTURE_SCANNER_BLOCK = LegacyBlockRegistryHelper.register(helper, new BlockStructureScanner());
            ADVANCED_LOOT_CHEST = LegacyBlockRegistryHelper.register(helper, new BlockAdvancedLootChest());
            TOTEM_PART = LegacyBlockRegistryHelper.register(helper, new BlockTotemPart());
            FIRE_PIT = LegacyBlockRegistryHelper.register(helper, new BlockFirePit());
            BRAZIER_FLAME = LegacyBlockRegistryHelper.register(helper, new BlockBrazierFlame());
            BRAZIER_EMBER = LegacyBlockRegistryHelper.register(helper, new BlockBrazierEmber());
            ALTAR_SHORT_CLOTH = LegacyBlockRegistryHelper.register(helper, new BlockAltarShortCloth());
            ALTAR_LONG_CLOTH = LegacyBlockRegistryHelper.register(helper, new BlockAltarLongCloth());
            ALTAR_CANDLE = LegacyBlockRegistryHelper.register(helper, new BlockAltarCandle());
            ALTAR_LECTERN = LegacyBlockRegistryHelper.register(helper, new BlockAltarLectern());
            ALTAR_SUN = LegacyBlockRegistryHelper.register(helper, new BlockAltarSun());
            PROTECTION_FLAG = LegacyBlockRegistryHelper.register(helper, new BlockProtectionFlag());
            DECORATIVE_FLAG = LegacyBlockRegistryHelper.register(helper, new BlockDecorativeFlag());
            GOLDEN_IDOL = LegacyBlockRegistryHelper.register(helper, new BlockGoldenIdol());
            GOBLIN_TOTEM_1 = LegacyBlockRegistryHelper.register(helper, new BlockTotemCube("goblin_totem_1"));
            GOBLIN_TOTEM_2 = LegacyBlockRegistryHelper.register(helper, new BlockTotemCube("goblin_totem_2"));
            GOBLIN_TOTEM_2_LIT = LegacyBlockRegistryHelper.register(helper, new BlockTotemCube("goblin_totem_2_lit", true));
            ORC_TOTEM_1 = LegacyBlockRegistryHelper.register(helper, new BlockTotemCube("orc_totem_1"));
            ORC_TOTEM_2 = LegacyBlockRegistryHelper.register(helper, new BlockTotemCube("orc_totem_2"));
            ORC_TOTEM_2_LIT = LegacyBlockRegistryHelper.register(helper, new BlockTotemCube("orc_totem_2_lit", true));
            LOOT_BASKET = LegacyBlockRegistryHelper.register(helper, new BlockLootBasket());
            WOODEN_COFFIN = LegacyBlockRegistryHelper.register(helper, new BlockWoodenCoffin());
            STONE_COFFIN = LegacyBlockRegistryHelper.register(helper, new BlockStoneCoffin());
            STOOL = LegacyBlockRegistryHelper.register(helper, new BlockStool());
            URN = LegacyBlockRegistryHelper.register(helper, new BlockUrn());
            TABLE = LegacyBlockRegistryHelper.register(helper, new BlockTable());
            CHAIR = LegacyBlockRegistryHelper.register(helper, new BlockChair());
            TRIBAL_STOOL = LegacyBlockRegistryHelper.register(helper, new BlockTribalStool());
            WOODEN_THRONE = LegacyBlockRegistryHelper.register(helper, new BlockWoodenThrone());
            GOLDEN_THRONE = LegacyBlockRegistryHelper.register(helper, new BlockGoldenThrone());
            WOODEN_POST = LegacyBlockRegistryHelper.register(helper, new BlockWoodenPost());
            IRON_CAGE = LegacyBlockRegistryHelper.register(helper, new BlockIronCage());
            GIBBET = LegacyBlockRegistryHelper.register(helper, new BlockGibbet());
            STRETCHING_RACK = LegacyBlockRegistryHelper.register(helper, new BlockStretchingRack());
            STAKE = LegacyBlockRegistryHelper.register(helper, new BlockStake());
            BENCH = LegacyBlockRegistryHelper.register(helper, new BlockBench());
            TRIBAL_CHAIR = LegacyBlockRegistryHelper.register(helper, new BlockTribalChair());
            SCISSOR_SEAT = LegacyBlockRegistryHelper.register(helper, new BlockScissorSeat());
            STATUE = LegacyBlockRegistryHelper.register(helper, new BlockStatue());
            COIN_STACK_COPPER = LegacyBlockRegistryHelper.register(helper, new BlockCoinStack("coin_stack_copper", ItemCoin.CoinMetal.COPPER));
            COIN_STACK_SILVER = LegacyBlockRegistryHelper.register(helper, new BlockCoinStack("coin_stack_silver", ItemCoin.CoinMetal.SILVER));
            COIN_STACK_GOLD = LegacyBlockRegistryHelper.register(helper, new BlockCoinStack("coin_stack_gold", ItemCoin.CoinMetal.GOLD));
            COIN_STACK_ANCIENT = LegacyBlockRegistryHelper.register(helper, new BlockCoinStack("coin_stack_ancient", ItemCoin.CoinMetal.ANCIENT));
            GRAVESTONE = LegacyBlockRegistryHelper.register(helper, new BlockGravestone());
        });

        event.register(ForgeRegistries.Keys.BLOCK_ENTITY_TYPES, helper -> {
            registerTile(helper, "advanced_spawner_tile", TileAdvancedSpawner::new, ADVANCED_SPAWNER);
            registerTile(helper, "gate_proxy_tile", TEGateProxy::new, GATE_PROXY);
            registerTile(helper, "drafting_station_tile", TileDraftingStation::new, DRAFTING_STATION);
            registerTile(helper, "structure_builder_ticked_tile", TileStructureBuilder::new, STRUCTURE_BUILDER_TICKED);
            registerTile(helper, "sound_block_tile", TileSoundBlock::new, SOUND_BLOCK);
            registerTile(helper, "structure_scanner_block_tile", TileStructureScanner::new, STRUCTURE_SCANNER_BLOCK);
            registerTile(helper, "totem_part_tile", TileTotemPart::new, TOTEM_PART);
            registerTile(helper, "colored_tile", TileColored::new, ALTAR_SHORT_CLOTH, ALTAR_LONG_CLOTH);
            registerTile(helper, "altar_candle_tile", TileAltarCandle::new, ALTAR_CANDLE);
            registerTile(helper, "protection_flag_tile", TileProtectionFlag::new, PROTECTION_FLAG);
            registerTile(helper, "decorative_flag_tile", TileDecorativeFlag::new, DECORATIVE_FLAG);
            registerTile(helper, "wooden_coffin", TileWoodenCoffin::new, WOODEN_COFFIN);
            registerTile(helper, "stone_coffin", TileStoneCoffin::new, STONE_COFFIN);
            registerTile(helper, "urn_tile", TileUrn::new, URN);
            registerTile(helper, "chair_tile", TileChair::new, CHAIR);
            registerTile(helper, "stake_tile", TileStake::new, STAKE);
            registerTile(helper, "statue_tile", TileStatue::new, STATUE);
            registerTile(helper, "gravestone_tile", TileGravestone::new, GRAVESTONE);
            ADVANCED_LOOT_CHEST_TILE = LegacyBlockEntityRegistry.register(helper,
                    new ResourceLocation(MOD_ID, "advanced_loot_chest_tile"),
                    TileAdvancedLootChest::new, ADVANCED_LOOT_CHEST);
            LOOT_BASKET_TILE = LegacyBlockEntityRegistry.register(helper,
                    new ResourceLocation(MOD_ID, "loot_basket_tile"),
                    TileLootBasket::new, LOOT_BASKET);
        });
    }

    private static <T extends net.minecraft.world.level.block.entity.BlockEntity> void registerTile(
            RegisterEvent.RegisterHelper<net.minecraft.world.level.block.entity.BlockEntityType<?>> helper,
            String id, java.util.function.Supplier<T> factory, Block... blocks) {
        LegacyBlockEntityRegistry.register(helper, new ResourceLocation(MOD_ID, id), factory, blocks);
    }
}
