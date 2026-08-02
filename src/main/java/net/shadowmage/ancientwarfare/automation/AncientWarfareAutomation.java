package net.shadowmage.ancientwarfare.automation;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.RegistryObject;
import net.shadowmage.ancientwarfare.automation.chunkloader.AWChunkLoader;
import net.shadowmage.ancientwarfare.automation.command.CommandWarehouse;
import net.shadowmage.ancientwarfare.automation.compat.agricraft.AgricraftCompat;
import net.shadowmage.ancientwarfare.automation.config.AWAutomationStatics;
import net.shadowmage.ancientwarfare.automation.container.*;
import net.shadowmage.ancientwarfare.automation.proxy.ClientProxyAutomation;
import net.shadowmage.ancientwarfare.automation.proxy.RFProxy;
import net.shadowmage.ancientwarfare.automation.registry.CropFarmRegistry;
import net.shadowmage.ancientwarfare.automation.registry.FruitFarmRegistry;
import net.shadowmage.ancientwarfare.automation.registry.TreeFarmRegistry;
import net.shadowmage.ancientwarfare.automation.tile.warehouse2.WarehouseDebugger;
import net.shadowmage.ancientwarfare.core.command.LegacyCommandRegistrar;
import net.shadowmage.ancientwarfare.core.compat.CompatLoader;
import net.shadowmage.ancientwarfare.core.network.NetworkHandler;
import net.shadowmage.ancientwarfare.core.proxy.CommonProxy;
import net.shadowmage.ancientwarfare.core.proxy.CommonProxyBase;
import net.shadowmage.ancientwarfare.core.registry.RegistryLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Forge 1.20.1 entry point for the automation module.
 */
@Mod(AncientWarfareAutomation.MOD_ID)
public final class AncientWarfareAutomation {
    public static final String MOD_ID = "ancientwarfareautomation";
    public static final RegistryObject<CreativeModeTab> TAB = AWAutomationTab.TAB;
    public static final Logger LOG = LogManager.getLogger(MOD_ID);

    public static AncientWarfareAutomation instance;
    public static CommonProxyBase proxy;
    public static AWAutomationStatics statics;

    public AncientWarfareAutomation() {
        instance = this;
        RFProxy.loadInstance();
        statics = new AWAutomationStatics("AncientWarfareAutomation");
        proxy = DistExecutor.unsafeRunForDist(() -> ClientProxyAutomation::new, () -> CommonProxy::new);

        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        AWAutomationTab.register(modBus);
        modBus.addListener(this::commonSetup);

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(AWChunkLoader.INSTANCE);
        MinecraftForge.EVENT_BUS.register(new WarehouseDebugger());

        registerLegacyScreens();
        registerDataParsers();
        CompatLoader.registerCompat(new AgricraftCompat());
        proxy.preInit();
    }

    private void registerLegacyScreens() {
        NetworkHandler.registerContainer(NetworkHandler.GUI_WORKSITE_INVENTORY_SIDE_ADJUST, ContainerWorksiteInventorySideSelection.class);
        NetworkHandler.registerContainer(NetworkHandler.GUI_WORKSITE_ANIMAL_CONTROL, ContainerWorksiteAnimalControl.class);
        NetworkHandler.registerContainer(NetworkHandler.GUI_WORKSITE_AUTO_CRAFT, ContainerWorksiteAutoCrafting.class);
        NetworkHandler.registerContainer(NetworkHandler.GUI_WORKSITE_FISH_CONTROL, ContainerWorksiteFishControl.class);
        NetworkHandler.registerContainer(NetworkHandler.GUI_MAILBOX_INVENTORY, ContainerMailbox.class);
        NetworkHandler.registerContainer(NetworkHandler.GUI_WAREHOUSE_CONTROL, ContainerWarehouseControl.class);
        NetworkHandler.registerContainer(NetworkHandler.GUI_WAREHOUSE_STORAGE, ContainerWarehouseStorage.class);
        NetworkHandler.registerContainer(NetworkHandler.GUI_WAREHOUSE_OUTPUT, ContainerWarehouseInterface.class);
        NetworkHandler.registerContainer(NetworkHandler.GUI_WAREHOUSE_CRAFTING, ContainerWarehouseCraftingStation.class);
        NetworkHandler.registerContainer(NetworkHandler.GUI_WORKSITE_QUARRY, ContainerWorksiteQuarry.class);
        NetworkHandler.registerContainer(NetworkHandler.GUI_WORKSITE_QUARRY_BOUNDS, ContainerWorksiteQuarryBounds.class);
        NetworkHandler.registerContainer(NetworkHandler.GUI_WORKSITE_TREE_FARM, ContainerWorksiteTreeFarm.class);
        NetworkHandler.registerContainer(NetworkHandler.GUI_WORKSITE_CROP_FARM, ContainerWorksiteCropFarm.class);
        NetworkHandler.registerContainer(NetworkHandler.GUI_WORKSITE_FRUIT_FARM, ContainerWorksiteFruitFarm.class);
        NetworkHandler.registerContainer(NetworkHandler.GUI_WORKSITE_ANIMAL_FARM, ContainerWorksiteAnimalFarm.class);
        NetworkHandler.registerContainer(NetworkHandler.GUI_WORKSITE_FISH_FARM, ContainerWorksiteFishFarm.class);
        NetworkHandler.registerContainer(NetworkHandler.GUI_STIRLING_GENERATOR, ContainerStirlingGenerator.class);
        NetworkHandler.registerContainer(NetworkHandler.GUI_CHUNK_LOADER_DELUXE, ContainerChunkLoaderDeluxe.class);
        NetworkHandler.registerContainer(NetworkHandler.GUI_WAREHOUSE_STOCK, ContainerWarehouseStockViewer.class);
        NetworkHandler.registerContainer(NetworkHandler.GUI_WAREHOUSE_STOCK_LINKER, ContainerWarehouseStockLinker.class);
        NetworkHandler.registerContainer(NetworkHandler.GUI_WORKSITE_BOUNDS, ContainerWorksiteBoundsAdjust.class);
    }

    private void registerDataParsers() {
        RegistryLoader.registerParser(new CropFarmRegistry.TillableParser());
        RegistryLoader.registerParser(new CropFarmRegistry.CropParser());
        RegistryLoader.registerParser(new CropFarmRegistry.SoilParser());
        RegistryLoader.registerParser(new FruitFarmRegistry.FruitParser());
        RegistryLoader.registerParser(new TreeFarmRegistry.PlantableParser());
        RegistryLoader.registerParser(new TreeFarmRegistry.SoilParser());
        RegistryLoader.registerParser(new TreeFarmRegistry.TreeScannerParser());
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            proxy.init();
            statics.save();
        });
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        LegacyCommandRegistrar.register(event.getDispatcher(), new CommandWarehouse());
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        statics.save();
    }
}
