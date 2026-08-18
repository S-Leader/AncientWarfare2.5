package net.shadowmage.ancientwarfare.automation.init;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.shadowmage.ancientwarfare.automation.AncientWarfareAutomation;
import net.shadowmage.ancientwarfare.automation.block.*;
import net.shadowmage.ancientwarfare.automation.item.ItemBlockTorqueTile;
import net.shadowmage.ancientwarfare.automation.item.ItemBlockWarehouseStockLinker;
import net.shadowmage.ancientwarfare.automation.item.ItemBlockWorksiteStatic;
import net.shadowmage.ancientwarfare.automation.tile.TileChunkLoaderDeluxe;
import net.shadowmage.ancientwarfare.automation.tile.TileChunkLoaderSimple;
import net.shadowmage.ancientwarfare.automation.tile.TileMailbox;
import net.shadowmage.ancientwarfare.automation.tile.torque.*;
import net.shadowmage.ancientwarfare.automation.tile.torque.multiblock.TileFlywheelStorage;
import net.shadowmage.ancientwarfare.automation.tile.torque.multiblock.TileWindmillBlade;
import net.shadowmage.ancientwarfare.automation.tile.warehouse2.*;
import net.shadowmage.ancientwarfare.automation.tile.worksite.TileAutoCrafting;
import net.shadowmage.ancientwarfare.automation.tile.worksite.WorkSiteAnimalFarm;
import net.shadowmage.ancientwarfare.automation.tile.worksite.WorkSiteFishFarm;
import net.shadowmage.ancientwarfare.automation.tile.worksite.WorkSiteQuarry;
import net.shadowmage.ancientwarfare.automation.tile.worksite.cropfarm.WorkSiteCropFarm;
import net.shadowmage.ancientwarfare.automation.tile.worksite.fruitfarm.WorkSiteFruitFarm;
import net.shadowmage.ancientwarfare.automation.tile.worksite.treefarm.WorkSiteTreeFarm;
import net.shadowmage.ancientwarfare.core.item.ItemBlockOwnedRotatable;
import net.shadowmage.ancientwarfare.core.item.ItemBlockRotatableMetaTile;

import java.util.function.Supplier;

public final class AWAutomationBlocks {
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, AncientWarfareAutomation.MOD_ID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, AncientWarfareAutomation.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, AncientWarfareAutomation.MOD_ID);

    public static final RegistryObject<BlockWorksiteBase> QUARRY = block("quarry", () -> new BlockWorksiteBase("quarry").setBlockEntityType(AWAutomationBlocks.QUARRY_TILE));
    public static final RegistryObject<BlockWorksiteBase> TREE_FARM = block("tree_farm", () -> new BlockWorksiteBase("tree_farm").setBlockEntityType(AWAutomationBlocks.TREE_FARM_TILE));
    public static final RegistryObject<BlockWorksiteBase> CROP_FARM = block("crop_farm", () -> new BlockWorksiteBase("crop_farm").setBlockEntityType(AWAutomationBlocks.CROP_FARM_TILE));
    public static final RegistryObject<BlockWorksiteBase> FRUIT_FARM = block("fruit_farm", () -> new BlockWorksiteBase("fruit_farm").setBlockEntityType(AWAutomationBlocks.FRUIT_FARM_TILE));
    public static final RegistryObject<BlockWorksiteBase> ANIMAL_FARM = block("animal_farm", () -> new BlockWorksiteBase("animal_farm").setBlockEntityType(AWAutomationBlocks.ANIMAL_FARM_TILE));
    public static final RegistryObject<BlockWorksiteBase> FISH_FARM = block("fish_farm", () -> new BlockWorksiteBase("fish_farm").setBlockEntityType(AWAutomationBlocks.FISH_FARM_TILE));
    public static final RegistryObject<BlockWorksiteBase> WAREHOUSE_CONTROL = block("warehouse_control", () -> new BlockWorksiteBase("warehouse_control").setBlockEntityType(AWAutomationBlocks.WAREHOUSE_CONTROL_TILE));

    public static final RegistryObject<BlockWarehouseStorage> WAREHOUSE_STORAGE_SMALL = block("warehouse_storage_small", () -> new BlockWarehouseStorage("warehouse_storage_small", BlockWarehouseStorage.Size.SMALL).setBlockEntityType(AWAutomationBlocks.WAREHOUSE_STORAGE_TILE));
    public static final RegistryObject<BlockWarehouseStorage> WAREHOUSE_STORAGE_MEDIUM = block("warehouse_storage_medium", () -> new BlockWarehouseStorage("warehouse_storage_medium", BlockWarehouseStorage.Size.MEDIUM).setBlockEntityType(AWAutomationBlocks.WAREHOUSE_STORAGE_TILE));
    public static final RegistryObject<BlockWarehouseStorage> WAREHOUSE_STORAGE_LARGE = block("warehouse_storage_large", () -> new BlockWarehouseStorage("warehouse_storage_large", BlockWarehouseStorage.Size.LARGE).setBlockEntityType(AWAutomationBlocks.WAREHOUSE_STORAGE_TILE));

    public static final RegistryObject<BlockWarehouseInterface> WAREHOUSE_INTERFACE = block("warehouse_interface", () -> new BlockWarehouseInterface("warehouse_interface").setBlockEntityType(AWAutomationBlocks.WAREHOUSE_INTERFACE_TILE));
    public static final RegistryObject<BlockWarehouseCraftingStation> WAREHOUSE_CRAFTING = block("warehouse_crafting", () -> new BlockWarehouseCraftingStation("warehouse_crafting").setBlockEntityType(AWAutomationBlocks.WAREHOUSE_CRAFTING_TILE));
    public static final RegistryObject<BlockWarehouseStockViewer> WAREHOUSE_STOCK_VIEWER = block("warehouse_stock_viewer", () -> new BlockWarehouseStockViewer("warehouse_stock_viewer").setBlockEntityType(AWAutomationBlocks.WAREHOUSE_STOCK_VIEWER_TILE));
    public static final RegistryObject<BlockWarehouseStockLinker> WAREHOUSE_STOCK_LINKER = block("warehouse_stock_linker", () -> new BlockWarehouseStockLinker("warehouse_stock_linker").setBlockEntityType(AWAutomationBlocks.WAREHOUSE_STOCK_LINKER_TILE));
    public static final RegistryObject<BlockAutoCrafting> AUTO_CRAFTING = block("auto_crafting", () -> new BlockAutoCrafting("auto_crafting").setBlockEntityType(AWAutomationBlocks.AUTO_CRAFTING_TILE));
    public static final RegistryObject<BlockMailbox> MAILBOX = block("mailbox", () -> new BlockMailbox("mailbox").setBlockEntityType(AWAutomationBlocks.MAILBOX_TILE));

    public static final RegistryObject<BlockFlywheelController> FLYWHEEL_CONTROLLER_LIGHT = block("flywheel_controller_light", () -> new BlockFlywheelController("flywheel_controller_light", TorqueTier.LIGHT).setBlockEntityType(AWAutomationBlocks.FLYWHEEL_CONTROLLER_TILE));
    public static final RegistryObject<BlockFlywheelController> FLYWHEEL_CONTROLLER_MEDIUM = block("flywheel_controller_medium", () -> new BlockFlywheelController("flywheel_controller_medium", TorqueTier.MEDIUM).setBlockEntityType(AWAutomationBlocks.FLYWHEEL_CONTROLLER_TILE));
    public static final RegistryObject<BlockFlywheelController> FLYWHEEL_CONTROLLER_HEAVY = block("flywheel_controller_heavy", () -> new BlockFlywheelController("flywheel_controller_heavy", TorqueTier.HEAVY).setBlockEntityType(AWAutomationBlocks.FLYWHEEL_CONTROLLER_TILE));

    public static final RegistryObject<BlockFlywheelStorage> FLYWHEEL_STORAGE_LIGHT = block("flywheel_storage_light", () -> new BlockFlywheelStorage("flywheel_storage_light", TorqueTier.LIGHT).setBlockEntityType(AWAutomationBlocks.FLYWHEEL_STORAGE_TILE));
    public static final RegistryObject<BlockFlywheelStorage> FLYWHEEL_STORAGE_MEDIUM = block("flywheel_storage_medium", () -> new BlockFlywheelStorage("flywheel_storage_medium", TorqueTier.MEDIUM).setBlockEntityType(AWAutomationBlocks.FLYWHEEL_STORAGE_TILE));
    public static final RegistryObject<BlockFlywheelStorage> FLYWHEEL_STORAGE_HEAVY = block("flywheel_storage_heavy", () -> new BlockFlywheelStorage("flywheel_storage_heavy", TorqueTier.HEAVY).setBlockEntityType(AWAutomationBlocks.FLYWHEEL_STORAGE_TILE));

    public static final RegistryObject<BlockTorqueJunction> TORQUE_JUNCTION_LIGHT = block("torque_junction_light", () -> new BlockTorqueJunction("torque_junction_light", TorqueTier.LIGHT).setBlockEntityType(AWAutomationBlocks.TORQUE_JUNCTION_TILE));
    public static final RegistryObject<BlockTorqueJunction> TORQUE_JUNCTION_MEDIUM = block("torque_junction_medium", () -> new BlockTorqueJunction("torque_junction_medium", TorqueTier.MEDIUM).setBlockEntityType(AWAutomationBlocks.TORQUE_JUNCTION_TILE));
    public static final RegistryObject<BlockTorqueJunction> TORQUE_JUNCTION_HEAVY = block("torque_junction_heavy", () -> new BlockTorqueJunction("torque_junction_heavy", TorqueTier.HEAVY).setBlockEntityType(AWAutomationBlocks.TORQUE_JUNCTION_TILE));

    public static final RegistryObject<BlockTorqueTransportShaft> TORQUE_SHAFT_LIGHT = block("torque_shaft_light", () -> new BlockTorqueTransportShaft("torque_shaft_light", TorqueTier.LIGHT).setBlockEntityType(AWAutomationBlocks.TORQUE_SHAFT_TILE));
    public static final RegistryObject<BlockTorqueTransportShaft> TORQUE_SHAFT_MEDIUM = block("torque_shaft_medium", () -> new BlockTorqueTransportShaft("torque_shaft_medium", TorqueTier.MEDIUM).setBlockEntityType(AWAutomationBlocks.TORQUE_SHAFT_TILE));
    public static final RegistryObject<BlockTorqueTransportShaft> TORQUE_SHAFT_HEAVY = block("torque_shaft_heavy", () -> new BlockTorqueTransportShaft("torque_shaft_heavy", TorqueTier.HEAVY).setBlockEntityType(AWAutomationBlocks.TORQUE_SHAFT_TILE));

    public static final RegistryObject<BlockTorqueDistributor> TORQUE_DISTRIBUTOR_LIGHT = block("torque_distributor_light", () -> new BlockTorqueDistributor("torque_distributor_light", TorqueTier.LIGHT).setBlockEntityType(AWAutomationBlocks.TORQUE_DISTRIBUTOR_TILE));
    public static final RegistryObject<BlockTorqueDistributor> TORQUE_DISTRIBUTOR_MEDIUM = block("torque_distributor_medium", () -> new BlockTorqueDistributor("torque_distributor_medium", TorqueTier.MEDIUM).setBlockEntityType(AWAutomationBlocks.TORQUE_DISTRIBUTOR_TILE));
    public static final RegistryObject<BlockTorqueDistributor> TORQUE_DISTRIBUTOR_HEAVY = block("torque_distributor_heavy", () -> new BlockTorqueDistributor("torque_distributor_heavy", TorqueTier.HEAVY).setBlockEntityType(AWAutomationBlocks.TORQUE_DISTRIBUTOR_TILE));

    public static final RegistryObject<BlockTorqueGenerator> STIRLING_GENERATOR = block("stirling_generator", () -> new BlockStirlingGenerator("stirling_generator").setBlockEntityType(AWAutomationBlocks.STIRLING_GENERATOR_TILE));
    public static final RegistryObject<BlockTorqueGenerator> WATERWHEEL_GENERATOR = block("waterwheel_generator", () -> new BlockWaterwheelGenerator("waterwheel_generator").setBlockEntityType(AWAutomationBlocks.WATERWHEEL_GENERATOR_TILE));
    public static final RegistryObject<BlockHandCrankedGenerator> HAND_CRANKED_GENERATOR = block("hand_cranked_generator", () -> new BlockHandCrankedGenerator("hand_cranked_generator").setBlockEntityType(AWAutomationBlocks.HAND_CRANKED_GENERATOR_TILE));
    public static final RegistryObject<BlockWindmillBlade> WINDMILL_BLADE = block("windmill_blade", () -> new BlockWindmillBlade("windmill_blade").setBlockEntityType(AWAutomationBlocks.WINDMILL_BLADE_TILE));
    public static final RegistryObject<BlockWindmillGenerator> WINDMILL_GENERATOR = block("windmill_generator", () -> new BlockWindmillGenerator("windmill_generator").setBlockEntityType(AWAutomationBlocks.WINDMILL_GENERATOR_TILE));
    public static final RegistryObject<BlockChunkLoaderSimple> CHUNK_LOADER_SIMPLE = block("chunk_loader_simple", () -> new BlockChunkLoaderSimple("chunk_loader_simple").setBlockEntityType(AWAutomationBlocks.CHUNK_LOADER_SIMPLE_TILE));
    public static final RegistryObject<BlockChunkLoaderDeluxe> CHUNK_LOADER_DELUXE = block("chunk_loader_deluxe", () -> new BlockChunkLoaderDeluxe("chunk_loader_deluxe").setBlockEntityType(AWAutomationBlocks.CHUNK_LOADER_DELUXE_TILE));

    // Block items use the same stable registry path as their block. No manual registry-event ordering is involved.
    static {
        item("quarry", () -> new ItemBlockWorksiteStatic(AWAutomationBlocks.QUARRY.get()));
        item("tree_farm", () -> new ItemBlockWorksiteStatic(AWAutomationBlocks.TREE_FARM.get()));
        item("crop_farm", () -> new ItemBlockWorksiteStatic(AWAutomationBlocks.CROP_FARM.get()));
        item("fruit_farm", () -> new ItemBlockWorksiteStatic(AWAutomationBlocks.FRUIT_FARM.get()));
        item("animal_farm", () -> new ItemBlockWorksiteStatic(AWAutomationBlocks.ANIMAL_FARM.get()));
        item("fish_farm", () -> new ItemBlockWorksiteStatic(AWAutomationBlocks.FISH_FARM.get()));
        item("warehouse_control", () -> new ItemBlockWorksiteStatic(AWAutomationBlocks.WAREHOUSE_CONTROL.get()));
        item("warehouse_storage_small", () -> new BlockItem(AWAutomationBlocks.WAREHOUSE_STORAGE_SMALL.get(), new Item.Properties()));
        item("warehouse_storage_medium", () -> new BlockItem(AWAutomationBlocks.WAREHOUSE_STORAGE_MEDIUM.get(), new Item.Properties()));
        item("warehouse_storage_large", () -> new BlockItem(AWAutomationBlocks.WAREHOUSE_STORAGE_LARGE.get(), new Item.Properties()));
        item("warehouse_interface", () -> new BlockItem(AWAutomationBlocks.WAREHOUSE_INTERFACE.get(), new Item.Properties()));
        item("warehouse_crafting", () -> new BlockItem(AWAutomationBlocks.WAREHOUSE_CRAFTING.get(), new Item.Properties()));
        item("warehouse_stock_viewer", () -> new ItemBlockOwnedRotatable(AWAutomationBlocks.WAREHOUSE_STOCK_VIEWER.get()));
        item("warehouse_stock_linker", () -> new ItemBlockWarehouseStockLinker(AWAutomationBlocks.WAREHOUSE_STOCK_LINKER.get()));
        item("auto_crafting", () -> new ItemBlockRotatableMetaTile(AWAutomationBlocks.AUTO_CRAFTING.get()));
        item("mailbox", () -> new ItemBlockOwnedRotatable(AWAutomationBlocks.MAILBOX.get()));
        item("flywheel_controller_light", () -> new ItemBlockTorqueTile(AWAutomationBlocks.FLYWHEEL_CONTROLLER_LIGHT.get(), false));
        item("flywheel_controller_medium", () -> new ItemBlockTorqueTile(AWAutomationBlocks.FLYWHEEL_CONTROLLER_MEDIUM.get(), false));
        item("flywheel_controller_heavy", () -> new ItemBlockTorqueTile(AWAutomationBlocks.FLYWHEEL_CONTROLLER_HEAVY.get(), false));
        item("flywheel_storage_light", () -> new BlockItem(AWAutomationBlocks.FLYWHEEL_STORAGE_LIGHT.get(), new Item.Properties()));
        item("flywheel_storage_medium", () -> new BlockItem(AWAutomationBlocks.FLYWHEEL_STORAGE_MEDIUM.get(), new Item.Properties()));
        item("flywheel_storage_heavy", () -> new BlockItem(AWAutomationBlocks.FLYWHEEL_STORAGE_HEAVY.get(), new Item.Properties()));
        item("torque_junction_light", () -> new ItemBlockTorqueTile(AWAutomationBlocks.TORQUE_JUNCTION_LIGHT.get(), false));
        item("torque_junction_medium", () -> new ItemBlockTorqueTile(AWAutomationBlocks.TORQUE_JUNCTION_MEDIUM.get(), false));
        item("torque_junction_heavy", () -> new ItemBlockTorqueTile(AWAutomationBlocks.TORQUE_JUNCTION_HEAVY.get(), false));
        item("torque_shaft_light", () -> new ItemBlockTorqueTile(AWAutomationBlocks.TORQUE_SHAFT_LIGHT.get(), false));
        item("torque_shaft_medium", () -> new ItemBlockTorqueTile(AWAutomationBlocks.TORQUE_SHAFT_MEDIUM.get(), false));
        item("torque_shaft_heavy", () -> new ItemBlockTorqueTile(AWAutomationBlocks.TORQUE_SHAFT_HEAVY.get(), false));
        item("torque_distributor_light", () -> new ItemBlockTorqueTile(AWAutomationBlocks.TORQUE_DISTRIBUTOR_LIGHT.get(), false));
        item("torque_distributor_medium", () -> new ItemBlockTorqueTile(AWAutomationBlocks.TORQUE_DISTRIBUTOR_MEDIUM.get(), false));
        item("torque_distributor_heavy", () -> new ItemBlockTorqueTile(AWAutomationBlocks.TORQUE_DISTRIBUTOR_HEAVY.get(), false));
        item("stirling_generator", () -> new ItemBlockTorqueTile(AWAutomationBlocks.STIRLING_GENERATOR.get()));
        item("waterwheel_generator", () -> new ItemBlockTorqueTile(AWAutomationBlocks.WATERWHEEL_GENERATOR.get()));
        item("hand_cranked_generator", () -> new ItemBlockTorqueTile(AWAutomationBlocks.HAND_CRANKED_GENERATOR.get()));
        item("windmill_blade", () -> new BlockItem(AWAutomationBlocks.WINDMILL_BLADE.get(), new Item.Properties()));
        item("windmill_generator", () -> new ItemBlockTorqueTile(AWAutomationBlocks.WINDMILL_GENERATOR.get()));
        item("chunk_loader_simple", () -> new BlockItem(AWAutomationBlocks.CHUNK_LOADER_SIMPLE.get(), new Item.Properties()));
        item("chunk_loader_deluxe", () -> new BlockItem(AWAutomationBlocks.CHUNK_LOADER_DELUXE.get(), new Item.Properties()));
    }

    public static final RegistryObject<BlockEntityType<WorkSiteQuarry>> QUARRY_TILE = tile("quarry_tile", () ->
            BlockEntityType.Builder.of((pos, state) -> new WorkSiteQuarry(AWAutomationBlocks.QUARRY_TILE.get(), pos, state), AWAutomationBlocks.QUARRY.get()).build(null));
    public static final RegistryObject<BlockEntityType<WorkSiteTreeFarm>> TREE_FARM_TILE = tile("tree_farm_tile", () ->
            BlockEntityType.Builder.of((pos, state) -> new WorkSiteTreeFarm(AWAutomationBlocks.TREE_FARM_TILE.get(), pos, state), AWAutomationBlocks.TREE_FARM.get()).build(null));
    public static final RegistryObject<BlockEntityType<WorkSiteCropFarm>> CROP_FARM_TILE = tile("crop_farm_tile", () ->
            BlockEntityType.Builder.of((pos, state) -> new WorkSiteCropFarm(AWAutomationBlocks.CROP_FARM_TILE.get(), pos, state), AWAutomationBlocks.CROP_FARM.get()).build(null));
    public static final RegistryObject<BlockEntityType<WorkSiteFruitFarm>> FRUIT_FARM_TILE = tile("fruit_farm_tile", () ->
            BlockEntityType.Builder.of((pos, state) -> new WorkSiteFruitFarm(AWAutomationBlocks.FRUIT_FARM_TILE.get(), pos, state), AWAutomationBlocks.FRUIT_FARM.get()).build(null));
    public static final RegistryObject<BlockEntityType<WorkSiteAnimalFarm>> ANIMAL_FARM_TILE = tile("animal_farm_tile", () ->
            BlockEntityType.Builder.of((pos, state) -> new WorkSiteAnimalFarm(AWAutomationBlocks.ANIMAL_FARM_TILE.get(), pos, state), AWAutomationBlocks.ANIMAL_FARM.get()).build(null));
    public static final RegistryObject<BlockEntityType<WorkSiteFishFarm>> FISH_FARM_TILE = tile("fish_farm_tile", () ->
            BlockEntityType.Builder.of((pos, state) -> new WorkSiteFishFarm(AWAutomationBlocks.FISH_FARM_TILE.get(), pos, state), AWAutomationBlocks.FISH_FARM.get()).build(null));
    public static final RegistryObject<BlockEntityType<TileWarehouse>> WAREHOUSE_CONTROL_TILE = tile("warehouse_control_tile", () ->
            BlockEntityType.Builder.of((pos, state) -> new TileWarehouse(AWAutomationBlocks.WAREHOUSE_CONTROL_TILE.get(), pos, state), AWAutomationBlocks.WAREHOUSE_CONTROL.get()).build(null));
    public static final RegistryObject<BlockEntityType<TileWarehouseStorage>> WAREHOUSE_STORAGE_TILE = tile("warehouse_storage_tile", () ->
            BlockEntityType.Builder.of((pos, state) -> {
                BlockWarehouseStorage block = (BlockWarehouseStorage) state.getBlock();
                BlockWarehouseStorage.Size size = block.getFixedSize() == null ? state.getValue(BlockWarehouseStorage.sizeProperty()) : block.getFixedSize();
                return switch (size) {
                    case MEDIUM ->
                            new TileWarehouseStorageMedium(AWAutomationBlocks.WAREHOUSE_STORAGE_TILE.get(), pos, state);
                    case LARGE ->
                            new TileWarehouseStorageLarge(AWAutomationBlocks.WAREHOUSE_STORAGE_TILE.get(), pos, state);
                    default -> new TileWarehouseStorage(AWAutomationBlocks.WAREHOUSE_STORAGE_TILE.get(), pos, state);
                };
            }, AWAutomationBlocks.WAREHOUSE_STORAGE_SMALL.get(), AWAutomationBlocks.WAREHOUSE_STORAGE_MEDIUM.get(), AWAutomationBlocks.WAREHOUSE_STORAGE_LARGE.get()).build(null));
    public static final RegistryObject<BlockEntityType<TileWarehouseInterface>> WAREHOUSE_INTERFACE_TILE = tile("warehouse_interface_tile", () ->
            BlockEntityType.Builder.of((pos, state) -> new TileWarehouseInterface(AWAutomationBlocks.WAREHOUSE_INTERFACE_TILE.get(), pos, state), AWAutomationBlocks.WAREHOUSE_INTERFACE.get()).build(null));
    public static final RegistryObject<BlockEntityType<TileWarehouseCraftingStation>> WAREHOUSE_CRAFTING_TILE = tile("warehouse_crafting_tile", () ->
            BlockEntityType.Builder.of((pos, state) -> new TileWarehouseCraftingStation(AWAutomationBlocks.WAREHOUSE_CRAFTING_TILE.get(), pos, state), AWAutomationBlocks.WAREHOUSE_CRAFTING.get()).build(null));
    public static final RegistryObject<BlockEntityType<TileWarehouseStockViewer>> WAREHOUSE_STOCK_VIEWER_TILE = tile("warehouse_stock_viewer_tile", () ->
            BlockEntityType.Builder.of((pos, state) -> new TileWarehouseStockViewer(AWAutomationBlocks.WAREHOUSE_STOCK_VIEWER_TILE.get(), pos, state), AWAutomationBlocks.WAREHOUSE_STOCK_VIEWER.get()).build(null));
    public static final RegistryObject<BlockEntityType<TileWarehouseStockLinker>> WAREHOUSE_STOCK_LINKER_TILE = tile("warehouse_stock_linker_tile", () ->
            BlockEntityType.Builder.of((pos, state) -> new TileWarehouseStockLinker(AWAutomationBlocks.WAREHOUSE_STOCK_LINKER_TILE.get(), pos, state), AWAutomationBlocks.WAREHOUSE_STOCK_LINKER.get()).build(null));
    public static final RegistryObject<BlockEntityType<TileAutoCrafting>> AUTO_CRAFTING_TILE = tile("auto_crafting_tile", () ->
            BlockEntityType.Builder.of((pos, state) -> new TileAutoCrafting(AWAutomationBlocks.AUTO_CRAFTING_TILE.get(), pos, state), AWAutomationBlocks.AUTO_CRAFTING.get()).build(null));
    public static final RegistryObject<BlockEntityType<TileMailbox>> MAILBOX_TILE = tile("mailbox_tile", () ->
            BlockEntityType.Builder.of((pos, state) -> new TileMailbox(AWAutomationBlocks.MAILBOX_TILE.get(), pos, state), AWAutomationBlocks.MAILBOX.get()).build(null));

    public static final RegistryObject<BlockEntityType<TileFlywheelController>> FLYWHEEL_CONTROLLER_TILE = tile("flywheel_controller_tile", () ->
            BlockEntityType.Builder.of((pos, state) -> switch (((BlockFlywheelController) state.getBlock()).getFixedTier()) {
                case MEDIUM ->
                        new TileFlywheelControllerMedium(AWAutomationBlocks.FLYWHEEL_CONTROLLER_TILE.get(), pos, state);
                case HEAVY ->
                        new TileFlywheelControllerHeavy(AWAutomationBlocks.FLYWHEEL_CONTROLLER_TILE.get(), pos, state);
                default ->
                        new TileFlywheelControllerLight(AWAutomationBlocks.FLYWHEEL_CONTROLLER_TILE.get(), pos, state);
            }, AWAutomationBlocks.FLYWHEEL_CONTROLLER_LIGHT.get(), AWAutomationBlocks.FLYWHEEL_CONTROLLER_MEDIUM.get(), AWAutomationBlocks.FLYWHEEL_CONTROLLER_HEAVY.get()).build(null));
    public static final RegistryObject<BlockEntityType<TileFlywheelStorage>> FLYWHEEL_STORAGE_TILE = tile("flywheel_storage_tile", () ->
            BlockEntityType.Builder.of((pos, state) -> new TileFlywheelStorage(AWAutomationBlocks.FLYWHEEL_STORAGE_TILE.get(), pos, state),
                    AWAutomationBlocks.FLYWHEEL_STORAGE_LIGHT.get(), AWAutomationBlocks.FLYWHEEL_STORAGE_MEDIUM.get(), AWAutomationBlocks.FLYWHEEL_STORAGE_HEAVY.get()).build(null));
    public static final RegistryObject<BlockEntityType<TileTorqueSidedCell>> TORQUE_JUNCTION_TILE = tile("torque_junction_tile", () ->
            BlockEntityType.Builder.of((pos, state) -> switch (((BlockTorqueJunction) state.getBlock()).getTier(state)) {
                case MEDIUM -> new TileConduitMedium(AWAutomationBlocks.TORQUE_JUNCTION_TILE.get(), pos, state);
                case HEAVY -> new TileConduitHeavy(AWAutomationBlocks.TORQUE_JUNCTION_TILE.get(), pos, state);
                default -> new TileConduitLight(AWAutomationBlocks.TORQUE_JUNCTION_TILE.get(), pos, state);
            }, AWAutomationBlocks.TORQUE_JUNCTION_LIGHT.get(), AWAutomationBlocks.TORQUE_JUNCTION_MEDIUM.get(), AWAutomationBlocks.TORQUE_JUNCTION_HEAVY.get()).build(null));
    public static final RegistryObject<BlockEntityType<TileTorqueShaft>> TORQUE_SHAFT_TILE = tile("torque_shaft_tile", () ->
            BlockEntityType.Builder.of((pos, state) -> switch (((BlockTorqueTransportShaft) state.getBlock()).getTier(state)) {
                case MEDIUM -> new TileTorqueShaftMedium(AWAutomationBlocks.TORQUE_SHAFT_TILE.get(), pos, state);
                case HEAVY -> new TileTorqueShaftHeavy(AWAutomationBlocks.TORQUE_SHAFT_TILE.get(), pos, state);
                default -> new TileTorqueShaftLight(AWAutomationBlocks.TORQUE_SHAFT_TILE.get(), pos, state);
            }, AWAutomationBlocks.TORQUE_SHAFT_LIGHT.get(), AWAutomationBlocks.TORQUE_SHAFT_MEDIUM.get(), AWAutomationBlocks.TORQUE_SHAFT_HEAVY.get()).build(null));
    public static final RegistryObject<BlockEntityType<TileDistributor>> TORQUE_DISTRIBUTOR_TILE = tile("torque_distributor_tile", () ->
            BlockEntityType.Builder.of((pos, state) -> switch (((BlockTorqueDistributor) state.getBlock()).getTier(state)) {
                case MEDIUM -> new TileDistributorMedium(AWAutomationBlocks.TORQUE_DISTRIBUTOR_TILE.get(), pos, state);
                case HEAVY -> new TileDistributorHeavy(AWAutomationBlocks.TORQUE_DISTRIBUTOR_TILE.get(), pos, state);
                default -> new TileDistributorLight(AWAutomationBlocks.TORQUE_DISTRIBUTOR_TILE.get(), pos, state);
            }, AWAutomationBlocks.TORQUE_DISTRIBUTOR_LIGHT.get(), AWAutomationBlocks.TORQUE_DISTRIBUTOR_MEDIUM.get(), AWAutomationBlocks.TORQUE_DISTRIBUTOR_HEAVY.get()).build(null));
    public static final RegistryObject<BlockEntityType<TileStirlingGenerator>> STIRLING_GENERATOR_TILE = tile("stirling_generator_tile", () ->
            BlockEntityType.Builder.of((pos, state) -> new TileStirlingGenerator(AWAutomationBlocks.STIRLING_GENERATOR_TILE.get(), pos, state), AWAutomationBlocks.STIRLING_GENERATOR.get()).build(null));
    public static final RegistryObject<BlockEntityType<TileWaterwheelGenerator>> WATERWHEEL_GENERATOR_TILE = tile("waterwheel_generator_tile", () ->
            BlockEntityType.Builder.of((pos, state) -> new TileWaterwheelGenerator(AWAutomationBlocks.WATERWHEEL_GENERATOR_TILE.get(), pos, state), AWAutomationBlocks.WATERWHEEL_GENERATOR.get()).build(null));
    public static final RegistryObject<BlockEntityType<TileHandCrankedGenerator>> HAND_CRANKED_GENERATOR_TILE = tile("hand_cranked_generator_tile", () ->
            BlockEntityType.Builder.of((pos, state) -> new TileHandCrankedGenerator(AWAutomationBlocks.HAND_CRANKED_GENERATOR_TILE.get(), pos, state), AWAutomationBlocks.HAND_CRANKED_GENERATOR.get()).build(null));
    public static final RegistryObject<BlockEntityType<TileWindmillBlade>> WINDMILL_BLADE_TILE = tile("windmill_blade_tile", () ->
            BlockEntityType.Builder.of((pos, state) -> new TileWindmillBlade(AWAutomationBlocks.WINDMILL_BLADE_TILE.get(), pos, state), AWAutomationBlocks.WINDMILL_BLADE.get()).build(null));
    public static final RegistryObject<BlockEntityType<TileWindmillController>> WINDMILL_GENERATOR_TILE = tile("windmill_generator_tile", () ->
            BlockEntityType.Builder.of((pos, state) -> new TileWindmillController(AWAutomationBlocks.WINDMILL_GENERATOR_TILE.get(), pos, state), AWAutomationBlocks.WINDMILL_GENERATOR.get()).build(null));
    public static final RegistryObject<BlockEntityType<TileChunkLoaderSimple>> CHUNK_LOADER_SIMPLE_TILE = tile("chunk_loader_simple_tile", () ->
            BlockEntityType.Builder.of((pos, state) -> new TileChunkLoaderSimple(AWAutomationBlocks.CHUNK_LOADER_SIMPLE_TILE.get(), pos, state), AWAutomationBlocks.CHUNK_LOADER_SIMPLE.get()).build(null));
    public static final RegistryObject<BlockEntityType<TileChunkLoaderDeluxe>> CHUNK_LOADER_DELUXE_TILE = tile("chunk_loader_deluxe_tile", () ->
            BlockEntityType.Builder.of((pos, state) -> new TileChunkLoaderDeluxe(AWAutomationBlocks.CHUNK_LOADER_DELUXE_TILE.get(), pos, state), AWAutomationBlocks.CHUNK_LOADER_DELUXE.get()).build(null));

    private AWAutomationBlocks() {
    }

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
    }

    public static Item getWarehouseStorageItem(BlockWarehouseStorage.Size size) {
        return switch (size) {
            case SMALL -> AWAutomationBlocks.WAREHOUSE_STORAGE_SMALL.get().asItem();
            case MEDIUM -> AWAutomationBlocks.WAREHOUSE_STORAGE_MEDIUM.get().asItem();
            case LARGE -> AWAutomationBlocks.WAREHOUSE_STORAGE_LARGE.get().asItem();
        };
    }

    public static Item getFlywheelControllerItem(TorqueTier tier) {
        return switch (tier) {
            case LIGHT -> AWAutomationBlocks.FLYWHEEL_CONTROLLER_LIGHT.get().asItem();
            case MEDIUM -> AWAutomationBlocks.FLYWHEEL_CONTROLLER_MEDIUM.get().asItem();
            case HEAVY -> AWAutomationBlocks.FLYWHEEL_CONTROLLER_HEAVY.get().asItem();
        };
    }

    public static Item getFlywheelStorageItem(TorqueTier tier) {
        return switch (tier) {
            case LIGHT -> AWAutomationBlocks.FLYWHEEL_STORAGE_LIGHT.get().asItem();
            case MEDIUM -> AWAutomationBlocks.FLYWHEEL_STORAGE_MEDIUM.get().asItem();
            case HEAVY -> AWAutomationBlocks.FLYWHEEL_STORAGE_HEAVY.get().asItem();
        };
    }

    public static Item getTorqueJunctionItem(TorqueTier tier) {
        return switch (tier) {
            case LIGHT -> AWAutomationBlocks.TORQUE_JUNCTION_LIGHT.get().asItem();
            case MEDIUM -> AWAutomationBlocks.TORQUE_JUNCTION_MEDIUM.get().asItem();
            case HEAVY -> AWAutomationBlocks.TORQUE_JUNCTION_HEAVY.get().asItem();
        };
    }

    public static Item getTorqueShaftItem(TorqueTier tier) {
        return switch (tier) {
            case LIGHT -> AWAutomationBlocks.TORQUE_SHAFT_LIGHT.get().asItem();
            case MEDIUM -> AWAutomationBlocks.TORQUE_SHAFT_MEDIUM.get().asItem();
            case HEAVY -> AWAutomationBlocks.TORQUE_SHAFT_HEAVY.get().asItem();
        };
    }

    public static Item getTorqueDistributorItem(TorqueTier tier) {
        return switch (tier) {
            case LIGHT -> AWAutomationBlocks.TORQUE_DISTRIBUTOR_LIGHT.get().asItem();
            case MEDIUM -> AWAutomationBlocks.TORQUE_DISTRIBUTOR_MEDIUM.get().asItem();
            case HEAVY -> AWAutomationBlocks.TORQUE_DISTRIBUTOR_HEAVY.get().asItem();
        };
    }

    private static <T extends Block> RegistryObject<T> block(String name, Supplier<T> factory) {
        return BLOCKS.register(name, factory);
    }

    private static RegistryObject<Item> item(String name, Supplier<? extends Item> factory) {
        return ITEMS.register(name, factory);
    }

    private static <T extends net.minecraft.world.level.block.entity.BlockEntity> RegistryObject<BlockEntityType<T>> tile(String name, Supplier<BlockEntityType<T>> factory) {
        return BLOCK_ENTITIES.register(name, factory);
    }
}
