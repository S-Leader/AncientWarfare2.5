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
import net.shadowmage.ancientwarfare.automation.item.ItemBlockTorqueTile;
import net.shadowmage.ancientwarfare.automation.item.ItemBlockWarehouseStockLinker;
import net.shadowmage.ancientwarfare.automation.item.ItemBlockWarehouseStorage;
import net.shadowmage.ancientwarfare.automation.item.ItemBlockWorksiteStatic;
import net.shadowmage.ancientwarfare.automation.tile.warehouse2.TileWarehouse;
import net.shadowmage.ancientwarfare.automation.tile.worksite.WorkSiteAnimalFarm;
import net.shadowmage.ancientwarfare.automation.tile.worksite.WorkSiteFishFarm;
import net.shadowmage.ancientwarfare.automation.tile.worksite.WorkSiteQuarry;
import net.shadowmage.ancientwarfare.automation.tile.worksite.cropfarm.WorkSiteCropFarm;
import net.shadowmage.ancientwarfare.automation.tile.worksite.fruitfarm.WorkSiteFruitFarm;
import net.shadowmage.ancientwarfare.automation.tile.worksite.treefarm.WorkSiteTreeFarm;
import net.shadowmage.ancientwarfare.core.block.BlockBase;
import net.shadowmage.ancientwarfare.core.item.ItemBlockMeta;
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
    public static BlockWarehouseStorage WAREHOUSE_STORAGE;
    public static BlockWarehouseInterface WAREHOUSE_INTERFACE;
    public static BlockWarehouseCraftingStation WAREHOUSE_CRAFTING;
    public static BlockWarehouseStockViewer WAREHOUSE_STOCK_VIEWER;
    public static BlockWarehouseStockLinker WAREHOUSE_STOCK_LINKER;
    public static BlockMailbox MAILBOX;

    public static BlockFlywheelController FLYWHEEL_CONTROLLER;
    public static BlockFlywheelStorage FLYWHEEL_STORAGE;
    public static BlockTorqueTransport TORQUE_JUNCTION;
    public static BlockTorqueTransportShaft TORQUE_SHAFT;
    public static BlockTorqueDistributor TORQUE_DISTRIBUTOR;
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
            WAREHOUSE_INTERFACE = block(helper, "warehouse_interface", new BlockWarehouseInterface("warehouse_interface"));
            WAREHOUSE_CRAFTING = block(helper, "warehouse_crafting", new BlockWarehouseCraftingStation("warehouse_crafting"));
            WAREHOUSE_STOCK_VIEWER = block(helper, "warehouse_stock_viewer", new BlockWarehouseStockViewer("warehouse_stock_viewer"));
            WAREHOUSE_STOCK_LINKER = block(helper, "warehouse_stock_linker", new BlockWarehouseStockLinker("warehouse_stock_linker"));
            AUTO_CRAFTING = block(helper, "auto_crafting", new BlockAutoCrafting("auto_crafting"));
            MAILBOX = block(helper, "mailbox", new BlockMailbox("mailbox"));
            FLYWHEEL_CONTROLLER = block(helper, "flywheel_controller", new BlockFlywheelController("flywheel_controller"));
            FLYWHEEL_STORAGE = block(helper, "flywheel_storage", new BlockFlywheelStorage("flywheel_storage"));
            TORQUE_JUNCTION = block(helper, "torque_junction", new BlockTorqueJunction("torque_junction"));
            TORQUE_SHAFT = block(helper, "torque_shaft", new BlockTorqueTransportShaft("torque_shaft"));
            TORQUE_DISTRIBUTOR = block(helper, "torque_distributor", new BlockTorqueDistributor("torque_distributor"));
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
            item(helper, "warehouse_storage", new ItemBlockWarehouseStorage(WAREHOUSE_STORAGE));
            item(helper, "warehouse_interface", new BlockItem(WAREHOUSE_INTERFACE, new Item.Properties()));
            item(helper, "warehouse_crafting", new BlockItem(WAREHOUSE_CRAFTING, new Item.Properties()));
            item(helper, "warehouse_stock_viewer", new ItemBlockOwnedRotatable(WAREHOUSE_STOCK_VIEWER));
            item(helper, "warehouse_stock_linker", new ItemBlockWarehouseStockLinker(WAREHOUSE_STOCK_LINKER));
            item(helper, "auto_crafting", new ItemBlockRotatableMetaTile(AUTO_CRAFTING));
            item(helper, "mailbox", new ItemBlockOwnedRotatable(MAILBOX));
            item(helper, "flywheel_controller", new ItemBlockTorqueTile(FLYWHEEL_CONTROLLER));
            item(helper, "flywheel_storage", new ItemBlockMeta(FLYWHEEL_STORAGE));
            item(helper, "torque_junction", new ItemBlockTorqueTile(TORQUE_JUNCTION));
            item(helper, "torque_shaft", new ItemBlockTorqueTile(TORQUE_SHAFT));
            item(helper, "torque_distributor", new ItemBlockTorqueTile(TORQUE_DISTRIBUTOR));
            item(helper, "stirling_generator", new ItemBlockTorqueTile(STIRLING_GENERATOR));
            item(helper, "waterwheel_generator", new ItemBlockTorqueTile(WATERWHEEL_GENERATOR));
            item(helper, "hand_cranked_generator", new ItemBlockTorqueTile(HAND_CRANKED_GENERATOR));
            item(helper, "windmill_blade", new BlockItem(WINDMILL_BLADE, new Item.Properties()));
            item(helper, "windmill_generator", new ItemBlockTorqueTile(WINDMILL_GENERATOR));
            item(helper, "chunk_loader_simple", new BlockItem(CHUNK_LOADER_SIMPLE, new Item.Properties()));
            item(helper, "chunk_loader_deluxe", new BlockItem(CHUNK_LOADER_DELUXE, new Item.Properties()));
        });

        event.register(ForgeRegistries.Keys.BLOCK_ENTITY_TYPES, helper -> {
            registerTile(helper, "quarry_tile", QUARRY);
            registerTile(helper, "tree_farm_tile", TREE_FARM);
            registerTile(helper, "crop_farm_tile", CROP_FARM);
            registerTile(helper, "fruit_farm_tile", FRUIT_FARM);
            registerTile(helper, "animal_farm_tile", ANIMAL_FARM);
            registerTile(helper, "fish_farm_tile", FISH_FARM);
            registerTile(helper, "warehouse_control_tile", WAREHOUSE_CONTROL);
            registerTile(helper, "warehouse_storage_tile", WAREHOUSE_STORAGE);
            registerTile(helper, "warehouse_interface_tile", WAREHOUSE_INTERFACE);
            registerTile(helper, "warehouse_crafting_tile", WAREHOUSE_CRAFTING);
            registerTile(helper, "warehouse_stock_viewer_tile", WAREHOUSE_STOCK_VIEWER);
            registerTile(helper, "warehouse_stock_linker_tile", WAREHOUSE_STOCK_LINKER);
            registerTile(helper, "auto_crafting_tile", AUTO_CRAFTING);
            registerTile(helper, "mailbox_tile", MAILBOX);
            registerTile(helper, "flywheel_controller_tile", FLYWHEEL_CONTROLLER);
            registerTile(helper, "flywheel_storage_tile", FLYWHEEL_STORAGE);
            registerTile(helper, "torque_junction_tile", TORQUE_JUNCTION);
            registerTile(helper, "torque_shaft_tile", TORQUE_SHAFT);
            registerTile(helper, "torque_distributor_tile", TORQUE_DISTRIBUTOR);
            registerTile(helper, "stirling_generator_tile", STIRLING_GENERATOR);
            registerTile(helper, "waterwheel_generator_tile", WATERWHEEL_GENERATOR);
            registerTile(helper, "hand_cranked_generator_tile", HAND_CRANKED_GENERATOR);
            registerTile(helper, "windmill_blade_tile", WINDMILL_BLADE);
            registerTile(helper, "windmill_generator_tile", WINDMILL_GENERATOR);
            registerTile(helper, "chunk_loader_simple_tile", CHUNK_LOADER_SIMPLE);
            registerTile(helper, "chunk_loader_deluxe_tile", CHUNK_LOADER_DELUXE);
        });
    }

    @SuppressWarnings("unchecked")
    private static <T extends BlockEntity> BlockEntityType<T> registerTile(
            RegisterEvent.RegisterHelper<BlockEntityType<?>> helper, String name, BlockBase block) {
        return LegacyBlockEntityRegistry.registerStateAware(helper, id(name),
                state -> (T) block.createTileEntity(null, state), block);
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
