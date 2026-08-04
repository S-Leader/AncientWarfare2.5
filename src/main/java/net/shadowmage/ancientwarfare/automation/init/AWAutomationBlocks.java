package net.shadowmage.ancientwarfare.automation.init;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import net.shadowmage.ancientwarfare.automation.AncientWarfareAutomation;
import net.shadowmage.ancientwarfare.automation.block.*;
import net.shadowmage.ancientwarfare.automation.item.ItemBlockLegacyTorqueTile;
import net.shadowmage.ancientwarfare.automation.item.ItemBlockLegacyVariant;
import net.shadowmage.ancientwarfare.automation.item.ItemBlockTorqueTile;
import net.shadowmage.ancientwarfare.automation.item.ItemBlockWarehouseStockLinker;
import net.shadowmage.ancientwarfare.automation.item.ItemBlockWorksiteStatic;
import net.shadowmage.ancientwarfare.automation.tile.warehouse2.TileWarehouse;
import net.shadowmage.ancientwarfare.automation.tile.worksite.WorkSiteAnimalFarm;
import net.shadowmage.ancientwarfare.automation.tile.worksite.WorkSiteFishFarm;
import net.shadowmage.ancientwarfare.automation.tile.worksite.WorkSiteQuarry;
import net.shadowmage.ancientwarfare.automation.tile.worksite.cropfarm.WorkSiteCropFarm;
import net.shadowmage.ancientwarfare.automation.tile.worksite.fruitfarm.WorkSiteFruitFarm;
import net.shadowmage.ancientwarfare.automation.tile.worksite.treefarm.WorkSiteTreeFarm;
import net.shadowmage.ancientwarfare.core.block.BlockBase;
import net.shadowmage.ancientwarfare.core.item.ItemBlockOwnedRotatable;
import net.shadowmage.ancientwarfare.core.item.ItemBlockRotatableMetaTile;
import net.shadowmage.ancientwarfare.core.tile.LegacyBlockEntityRegistry;

@Mod.EventBusSubscriber(modid = AncientWarfareAutomation.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class AWAutomationBlocks {
    private AWAutomationBlocks() {
    }

    public static BlockWorksiteBase QUARRY;
    public static BlockWorksiteBase CROP_FARM;
    public static BlockWorksiteBase FRUIT_FARM;
    public static BlockWorksiteBase TREE_FARM;
    public static BlockWorksiteBase ANIMAL_FARM;
    public static BlockWorksiteBase FISH_FARM;
    public static BlockAutoCrafting AUTO_CRAFTING;
    public static BlockWorksiteBase WAREHOUSE_CONTROL;

    /** Legacy metadata block retained only for old worlds/items. */
    public static BlockWarehouseStorage WAREHOUSE_STORAGE;
    public static BlockWarehouseStorage WAREHOUSE_STORAGE_SMALL;
    public static BlockWarehouseStorage WAREHOUSE_STORAGE_MEDIUM;
    public static BlockWarehouseStorage WAREHOUSE_STORAGE_LARGE;

    public static BlockWarehouseInterface WAREHOUSE_INTERFACE;
    public static BlockWarehouseCraftingStation WAREHOUSE_CRAFTING;
    public static BlockWarehouseStockViewer WAREHOUSE_STOCK_VIEWER;
    public static BlockWarehouseStockLinker WAREHOUSE_STOCK_LINKER;
    public static BlockMailbox MAILBOX;

    /** Legacy metadata blocks retained only for old worlds/items. */
    public static BlockFlywheelController FLYWHEEL_CONTROLLER;
    public static BlockFlywheelStorage FLYWHEEL_STORAGE;
    public static BlockTorqueJunction TORQUE_JUNCTION;
    public static BlockTorqueTransportShaft TORQUE_SHAFT;
    public static BlockTorqueDistributor TORQUE_DISTRIBUTOR;

    public static BlockFlywheelController FLYWHEEL_CONTROLLER_LIGHT;
    public static BlockFlywheelController FLYWHEEL_CONTROLLER_MEDIUM;
    public static BlockFlywheelController FLYWHEEL_CONTROLLER_HEAVY;
    public static BlockFlywheelStorage FLYWHEEL_STORAGE_LIGHT;
    public static BlockFlywheelStorage FLYWHEEL_STORAGE_MEDIUM;
    public static BlockFlywheelStorage FLYWHEEL_STORAGE_HEAVY;
    public static BlockTorqueJunction TORQUE_JUNCTION_LIGHT;
    public static BlockTorqueJunction TORQUE_JUNCTION_MEDIUM;
    public static BlockTorqueJunction TORQUE_JUNCTION_HEAVY;
    public static BlockTorqueTransportShaft TORQUE_SHAFT_LIGHT;
    public static BlockTorqueTransportShaft TORQUE_SHAFT_MEDIUM;
    public static BlockTorqueTransportShaft TORQUE_SHAFT_HEAVY;
    public static BlockTorqueDistributor TORQUE_DISTRIBUTOR_LIGHT;
    public static BlockTorqueDistributor TORQUE_DISTRIBUTOR_MEDIUM;
    public static BlockTorqueDistributor TORQUE_DISTRIBUTOR_HEAVY;

    public static BlockHandCrankedGenerator HAND_CRANKED_GENERATOR;
    public static BlockTorqueGenerator STIRLING_GENERATOR;
    public static BlockTorqueGenerator WATERWHEEL_GENERATOR;
    public static BlockWindmillBlade WINDMILL_BLADE;
    public static BlockWindmillGenerator WINDMILL_GENERATOR;
    public static BlockChunkLoaderSimple CHUNK_LOADER_SIMPLE;
    public static BlockChunkLoaderDeluxe CHUNK_LOADER_DELUXE;

    @SubscribeEvent
    public static void register(RegisterEvent event) {
        event.register(ForgeRegistries.Keys.BLOCKS, helper -> {
            QUARRY = block(helper, "quarry", new BlockWorksiteBase("quarry").setTileFactory(WorkSiteQuarry::new));
            TREE_FARM = block(helper, "tree_farm", new BlockWorksiteBase("tree_farm").setTileFactory(WorkSiteTreeFarm::new));
            CROP_FARM = block(helper, "crop_farm", new BlockWorksiteBase("crop_farm").setTileFactory(WorkSiteCropFarm::new));
            FRUIT_FARM = block(helper, "fruit_farm", new BlockWorksiteBase("fruit_farm").setTileFactory(WorkSiteFruitFarm::new));
            ANIMAL_FARM = block(helper, "animal_farm", new BlockWorksiteBase("animal_farm").setTileFactory(WorkSiteAnimalFarm::new));
            FISH_FARM = block(helper, "fish_farm", new BlockWorksiteBase("fish_farm").setTileFactory(WorkSiteFishFarm::new));
            WAREHOUSE_CONTROL = block(helper, "warehouse_control", new BlockWorksiteBase("warehouse_control").setTileFactory(TileWarehouse::new));

            WAREHOUSE_STORAGE = block(helper, "warehouse_storage", new BlockWarehouseStorage("warehouse_storage"));
            WAREHOUSE_STORAGE_SMALL = block(helper, "warehouse_storage_small", new BlockWarehouseStorage("warehouse_storage_small", BlockWarehouseStorage.Size.SMALL));
            WAREHOUSE_STORAGE_MEDIUM = block(helper, "warehouse_storage_medium", new BlockWarehouseStorage("warehouse_storage_medium", BlockWarehouseStorage.Size.MEDIUM));
            WAREHOUSE_STORAGE_LARGE = block(helper, "warehouse_storage_large", new BlockWarehouseStorage("warehouse_storage_large", BlockWarehouseStorage.Size.LARGE));

            WAREHOUSE_INTERFACE = block(helper, "warehouse_interface", new BlockWarehouseInterface("warehouse_interface"));
            WAREHOUSE_CRAFTING = block(helper, "warehouse_crafting", new BlockWarehouseCraftingStation("warehouse_crafting"));
            WAREHOUSE_STOCK_VIEWER = block(helper, "warehouse_stock_viewer", new BlockWarehouseStockViewer("warehouse_stock_viewer"));
            WAREHOUSE_STOCK_LINKER = block(helper, "warehouse_stock_linker", new BlockWarehouseStockLinker("warehouse_stock_linker"));
            AUTO_CRAFTING = block(helper, "auto_crafting", new BlockAutoCrafting("auto_crafting"));
            MAILBOX = block(helper, "mailbox", new BlockMailbox("mailbox"));

            FLYWHEEL_CONTROLLER = block(helper, "flywheel_controller", new BlockFlywheelController("flywheel_controller"));
            FLYWHEEL_CONTROLLER_LIGHT = block(helper, "flywheel_controller_light", new BlockFlywheelController("flywheel_controller_light", TorqueTier.LIGHT));
            FLYWHEEL_CONTROLLER_MEDIUM = block(helper, "flywheel_controller_medium", new BlockFlywheelController("flywheel_controller_medium", TorqueTier.MEDIUM));
            FLYWHEEL_CONTROLLER_HEAVY = block(helper, "flywheel_controller_heavy", new BlockFlywheelController("flywheel_controller_heavy", TorqueTier.HEAVY));

            FLYWHEEL_STORAGE = block(helper, "flywheel_storage", new BlockFlywheelStorage("flywheel_storage"));
            FLYWHEEL_STORAGE_LIGHT = block(helper, "flywheel_storage_light", new BlockFlywheelStorage("flywheel_storage_light", TorqueTier.LIGHT));
            FLYWHEEL_STORAGE_MEDIUM = block(helper, "flywheel_storage_medium", new BlockFlywheelStorage("flywheel_storage_medium", TorqueTier.MEDIUM));
            FLYWHEEL_STORAGE_HEAVY = block(helper, "flywheel_storage_heavy", new BlockFlywheelStorage("flywheel_storage_heavy", TorqueTier.HEAVY));

            TORQUE_JUNCTION = block(helper, "torque_junction", new BlockTorqueJunction("torque_junction"));
            TORQUE_JUNCTION_LIGHT = block(helper, "torque_junction_light", new BlockTorqueJunction("torque_junction_light", TorqueTier.LIGHT));
            TORQUE_JUNCTION_MEDIUM = block(helper, "torque_junction_medium", new BlockTorqueJunction("torque_junction_medium", TorqueTier.MEDIUM));
            TORQUE_JUNCTION_HEAVY = block(helper, "torque_junction_heavy", new BlockTorqueJunction("torque_junction_heavy", TorqueTier.HEAVY));

            TORQUE_SHAFT = block(helper, "torque_shaft", new BlockTorqueTransportShaft("torque_shaft"));
            TORQUE_SHAFT_LIGHT = block(helper, "torque_shaft_light", new BlockTorqueTransportShaft("torque_shaft_light", TorqueTier.LIGHT));
            TORQUE_SHAFT_MEDIUM = block(helper, "torque_shaft_medium", new BlockTorqueTransportShaft("torque_shaft_medium", TorqueTier.MEDIUM));
            TORQUE_SHAFT_HEAVY = block(helper, "torque_shaft_heavy", new BlockTorqueTransportShaft("torque_shaft_heavy", TorqueTier.HEAVY));

            TORQUE_DISTRIBUTOR = block(helper, "torque_distributor", new BlockTorqueDistributor("torque_distributor"));
            TORQUE_DISTRIBUTOR_LIGHT = block(helper, "torque_distributor_light", new BlockTorqueDistributor("torque_distributor_light", TorqueTier.LIGHT));
            TORQUE_DISTRIBUTOR_MEDIUM = block(helper, "torque_distributor_medium", new BlockTorqueDistributor("torque_distributor_medium", TorqueTier.MEDIUM));
            TORQUE_DISTRIBUTOR_HEAVY = block(helper, "torque_distributor_heavy", new BlockTorqueDistributor("torque_distributor_heavy", TorqueTier.HEAVY));

            STIRLING_GENERATOR = block(helper, "stirling_generator", new BlockStirlingGenerator("stirling_generator"));
            WATERWHEEL_GENERATOR = block(helper, "waterwheel_generator", new BlockWaterwheelGenerator("waterwheel_generator"));
            HAND_CRANKED_GENERATOR = block(helper, "hand_cranked_generator", new BlockHandCrankedGenerator("hand_cranked_generator"));
            WINDMILL_BLADE = block(helper, "windmill_blade", new BlockWindmillBlade("windmill_blade"));
            WINDMILL_GENERATOR = block(helper, "windmill_generator", new BlockWindmillGenerator("windmill_generator"));
            CHUNK_LOADER_SIMPLE = block(helper, "chunk_loader_simple", new BlockChunkLoaderSimple("chunk_loader_simple"));
            CHUNK_LOADER_DELUXE = block(helper, "chunk_loader_deluxe", new BlockChunkLoaderDeluxe("chunk_loader_deluxe"));
        });

        event.register(ForgeRegistries.Keys.ITEMS, helper -> {
            item(helper, "quarry", new ItemBlockWorksiteStatic(QUARRY));
            item(helper, "tree_farm", new ItemBlockWorksiteStatic(TREE_FARM));
            item(helper, "crop_farm", new ItemBlockWorksiteStatic(CROP_FARM));
            item(helper, "fruit_farm", new ItemBlockWorksiteStatic(FRUIT_FARM));
            item(helper, "animal_farm", new ItemBlockWorksiteStatic(ANIMAL_FARM));
            item(helper, "fish_farm", new ItemBlockWorksiteStatic(FISH_FARM));
            item(helper, "warehouse_control", new ItemBlockWorksiteStatic(WAREHOUSE_CONTROL));

            item(helper, "warehouse_storage", new ItemBlockLegacyVariant(WAREHOUSE_STORAGE,
                    meta -> getWarehouseStorageItem(BlockWarehouseStorage.Size.byMetadata(meta))));
            item(helper, "warehouse_storage_small", new BlockItem(WAREHOUSE_STORAGE_SMALL, new Item.Properties()));
            item(helper, "warehouse_storage_medium", new BlockItem(WAREHOUSE_STORAGE_MEDIUM, new Item.Properties()));
            item(helper, "warehouse_storage_large", new BlockItem(WAREHOUSE_STORAGE_LARGE, new Item.Properties()));

            item(helper, "warehouse_interface", new BlockItem(WAREHOUSE_INTERFACE, new Item.Properties()));
            item(helper, "warehouse_crafting", new BlockItem(WAREHOUSE_CRAFTING, new Item.Properties()));
            item(helper, "warehouse_stock_viewer", new ItemBlockOwnedRotatable(WAREHOUSE_STOCK_VIEWER));
            item(helper, "warehouse_stock_linker", new ItemBlockWarehouseStockLinker(WAREHOUSE_STOCK_LINKER));
            item(helper, "auto_crafting", new ItemBlockRotatableMetaTile(AUTO_CRAFTING));
            item(helper, "mailbox", new ItemBlockOwnedRotatable(MAILBOX));

            item(helper, "flywheel_controller", new ItemBlockLegacyTorqueTile(FLYWHEEL_CONTROLLER,
                    meta -> getFlywheelControllerItem(TorqueTier.byMetadata(meta))));
            item(helper, "flywheel_controller_light", new ItemBlockTorqueTile(FLYWHEEL_CONTROLLER_LIGHT, false));
            item(helper, "flywheel_controller_medium", new ItemBlockTorqueTile(FLYWHEEL_CONTROLLER_MEDIUM, false));
            item(helper, "flywheel_controller_heavy", new ItemBlockTorqueTile(FLYWHEEL_CONTROLLER_HEAVY, false));

            item(helper, "flywheel_storage", new ItemBlockLegacyVariant(FLYWHEEL_STORAGE,
                    meta -> getFlywheelStorageItem(TorqueTier.byMetadata(meta))));
            item(helper, "flywheel_storage_light", new BlockItem(FLYWHEEL_STORAGE_LIGHT, new Item.Properties()));
            item(helper, "flywheel_storage_medium", new BlockItem(FLYWHEEL_STORAGE_MEDIUM, new Item.Properties()));
            item(helper, "flywheel_storage_heavy", new BlockItem(FLYWHEEL_STORAGE_HEAVY, new Item.Properties()));

            item(helper, "torque_junction", new ItemBlockLegacyTorqueTile(TORQUE_JUNCTION,
                    meta -> getTorqueJunctionItem(TorqueTier.byMetadata(meta))));
            item(helper, "torque_junction_light", new ItemBlockTorqueTile(TORQUE_JUNCTION_LIGHT, false));
            item(helper, "torque_junction_medium", new ItemBlockTorqueTile(TORQUE_JUNCTION_MEDIUM, false));
            item(helper, "torque_junction_heavy", new ItemBlockTorqueTile(TORQUE_JUNCTION_HEAVY, false));

            item(helper, "torque_shaft", new ItemBlockLegacyTorqueTile(TORQUE_SHAFT,
                    meta -> getTorqueShaftItem(TorqueTier.byMetadata(meta))));
            item(helper, "torque_shaft_light", new ItemBlockTorqueTile(TORQUE_SHAFT_LIGHT, false));
            item(helper, "torque_shaft_medium", new ItemBlockTorqueTile(TORQUE_SHAFT_MEDIUM, false));
            item(helper, "torque_shaft_heavy", new ItemBlockTorqueTile(TORQUE_SHAFT_HEAVY, false));

            item(helper, "torque_distributor", new ItemBlockLegacyTorqueTile(TORQUE_DISTRIBUTOR,
                    meta -> getTorqueDistributorItem(TorqueTier.byMetadata(meta))));
            item(helper, "torque_distributor_light", new ItemBlockTorqueTile(TORQUE_DISTRIBUTOR_LIGHT, false));
            item(helper, "torque_distributor_medium", new ItemBlockTorqueTile(TORQUE_DISTRIBUTOR_MEDIUM, false));
            item(helper, "torque_distributor_heavy", new ItemBlockTorqueTile(TORQUE_DISTRIBUTOR_HEAVY, false));

            item(helper, "stirling_generator", new ItemBlockTorqueTile(STIRLING_GENERATOR));
            item(helper, "waterwheel_generator", new ItemBlockTorqueTile(WATERWHEEL_GENERATOR));
            item(helper, "hand_cranked_generator", new ItemBlockTorqueTile(HAND_CRANKED_GENERATOR));
            item(helper, "windmill_blade", new BlockItem(WINDMILL_BLADE, new Item.Properties()));
            item(helper, "windmill_generator", new ItemBlockTorqueTile(WINDMILL_GENERATOR));
            item(helper, "chunk_loader_simple", new BlockItem(CHUNK_LOADER_SIMPLE, new Item.Properties()));
            item(helper, "chunk_loader_deluxe", new BlockItem(CHUNK_LOADER_DELUXE, new Item.Properties()));
        });

        event.register(ForgeRegistries.Keys.BLOCK_ENTITY_TYPES, helper -> {
            registerTiles(helper, "quarry_tile", QUARRY);
            registerTiles(helper, "tree_farm_tile", TREE_FARM);
            registerTiles(helper, "crop_farm_tile", CROP_FARM);
            registerTiles(helper, "fruit_farm_tile", FRUIT_FARM);
            registerTiles(helper, "animal_farm_tile", ANIMAL_FARM);
            registerTiles(helper, "fish_farm_tile", FISH_FARM);
            registerTiles(helper, "warehouse_control_tile", WAREHOUSE_CONTROL);
            registerTiles(helper, "warehouse_storage_tile", WAREHOUSE_STORAGE,
                    WAREHOUSE_STORAGE_SMALL, WAREHOUSE_STORAGE_MEDIUM, WAREHOUSE_STORAGE_LARGE);
            registerTiles(helper, "warehouse_interface_tile", WAREHOUSE_INTERFACE);
            registerTiles(helper, "warehouse_crafting_tile", WAREHOUSE_CRAFTING);
            registerTiles(helper, "warehouse_stock_viewer_tile", WAREHOUSE_STOCK_VIEWER);
            registerTiles(helper, "warehouse_stock_linker_tile", WAREHOUSE_STOCK_LINKER);
            registerTiles(helper, "auto_crafting_tile", AUTO_CRAFTING);
            registerTiles(helper, "mailbox_tile", MAILBOX);
            registerTiles(helper, "flywheel_controller_tile", FLYWHEEL_CONTROLLER,
                    FLYWHEEL_CONTROLLER_LIGHT, FLYWHEEL_CONTROLLER_MEDIUM, FLYWHEEL_CONTROLLER_HEAVY);
            registerTiles(helper, "flywheel_storage_tile", FLYWHEEL_STORAGE,
                    FLYWHEEL_STORAGE_LIGHT, FLYWHEEL_STORAGE_MEDIUM, FLYWHEEL_STORAGE_HEAVY);
            registerTiles(helper, "torque_junction_tile", TORQUE_JUNCTION,
                    TORQUE_JUNCTION_LIGHT, TORQUE_JUNCTION_MEDIUM, TORQUE_JUNCTION_HEAVY);
            registerTiles(helper, "torque_shaft_tile", TORQUE_SHAFT,
                    TORQUE_SHAFT_LIGHT, TORQUE_SHAFT_MEDIUM, TORQUE_SHAFT_HEAVY);
            registerTiles(helper, "torque_distributor_tile", TORQUE_DISTRIBUTOR,
                    TORQUE_DISTRIBUTOR_LIGHT, TORQUE_DISTRIBUTOR_MEDIUM, TORQUE_DISTRIBUTOR_HEAVY);
            registerTiles(helper, "stirling_generator_tile", STIRLING_GENERATOR);
            registerTiles(helper, "waterwheel_generator_tile", WATERWHEEL_GENERATOR);
            registerTiles(helper, "hand_cranked_generator_tile", HAND_CRANKED_GENERATOR);
            registerTiles(helper, "windmill_blade_tile", WINDMILL_BLADE);
            registerTiles(helper, "windmill_generator_tile", WINDMILL_GENERATOR);
            registerTiles(helper, "chunk_loader_simple_tile", CHUNK_LOADER_SIMPLE);
            registerTiles(helper, "chunk_loader_deluxe_tile", CHUNK_LOADER_DELUXE);
        });
    }

    public static Item getWarehouseStorageItem(BlockWarehouseStorage.Size size) {
        return switch (size) {
            case SMALL -> WAREHOUSE_STORAGE_SMALL.asItem();
            case MEDIUM -> WAREHOUSE_STORAGE_MEDIUM.asItem();
            case LARGE -> WAREHOUSE_STORAGE_LARGE.asItem();
        };
    }

    public static Item getFlywheelControllerItem(TorqueTier tier) {
        return switch (tier) {
            case LIGHT -> FLYWHEEL_CONTROLLER_LIGHT.asItem();
            case MEDIUM -> FLYWHEEL_CONTROLLER_MEDIUM.asItem();
            case HEAVY -> FLYWHEEL_CONTROLLER_HEAVY.asItem();
        };
    }

    public static Item getFlywheelStorageItem(TorqueTier tier) {
        return switch (tier) {
            case LIGHT -> FLYWHEEL_STORAGE_LIGHT.asItem();
            case MEDIUM -> FLYWHEEL_STORAGE_MEDIUM.asItem();
            case HEAVY -> FLYWHEEL_STORAGE_HEAVY.asItem();
        };
    }

    public static Item getTorqueJunctionItem(TorqueTier tier) {
        return switch (tier) {
            case LIGHT -> TORQUE_JUNCTION_LIGHT.asItem();
            case MEDIUM -> TORQUE_JUNCTION_MEDIUM.asItem();
            case HEAVY -> TORQUE_JUNCTION_HEAVY.asItem();
        };
    }

    public static Item getTorqueShaftItem(TorqueTier tier) {
        return switch (tier) {
            case LIGHT -> TORQUE_SHAFT_LIGHT.asItem();
            case MEDIUM -> TORQUE_SHAFT_MEDIUM.asItem();
            case HEAVY -> TORQUE_SHAFT_HEAVY.asItem();
        };
    }

    public static Item getTorqueDistributorItem(TorqueTier tier) {
        return switch (tier) {
            case LIGHT -> TORQUE_DISTRIBUTOR_LIGHT.asItem();
            case MEDIUM -> TORQUE_DISTRIBUTOR_MEDIUM.asItem();
            case HEAVY -> TORQUE_DISTRIBUTOR_HEAVY.asItem();
        };
    }

    public static boolean isLegacyVariantItem(Item item) {
        return item == WAREHOUSE_STORAGE.asItem()
                || item == FLYWHEEL_CONTROLLER.asItem()
                || item == FLYWHEEL_STORAGE.asItem()
                || item == TORQUE_JUNCTION.asItem()
                || item == TORQUE_SHAFT.asItem()
                || item == TORQUE_DISTRIBUTOR.asItem();
    }

    @SuppressWarnings("unchecked")
    private static <T extends BlockEntity> BlockEntityType<T> registerTiles(
            RegisterEvent.RegisterHelper<BlockEntityType<?>> helper, String name, BlockBase... blocks) {
        Block[] validBlocks = blocks;
        return LegacyBlockEntityRegistry.registerStateAware(helper, id(name), state -> {
            Block block = state.getBlock();
            if (!(block instanceof BlockBase legacyBlock)) {
                throw new IllegalStateException("No legacy block factory for " + block);
            }
            return (T) legacyBlock.createTileEntity(null, state);
        }, validBlocks);
    }

    private static <T extends Block> T block(RegisterEvent.RegisterHelper<Block> helper, String name, T block) {
        helper.register(id(name), block);
        return block;
    }

    private static void item(RegisterEvent.RegisterHelper<Item> helper, String name, Item item) {
        helper.register(id(name), item);
    }

    private static ResourceLocation id(String name) {
        return new ResourceLocation(AncientWarfareAutomation.MOD_ID, name);
    }
}
