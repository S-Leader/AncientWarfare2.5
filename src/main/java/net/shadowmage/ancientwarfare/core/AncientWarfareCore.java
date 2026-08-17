package net.shadowmage.ancientwarfare.core;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.RegistryObject;
import net.shadowmage.ancientwarfare.core.command.CommandResearch;
import net.shadowmage.ancientwarfare.core.command.CommandUtils;
import net.shadowmage.ancientwarfare.core.command.LegacyCommandRegistrar;
import net.shadowmage.ancientwarfare.core.compat.CompatLoader;
import net.shadowmage.ancientwarfare.core.compat.ftb.FTBCompat;
import net.shadowmage.ancientwarfare.core.config.AWCoreStatics;
import net.shadowmage.ancientwarfare.core.container.*;
import net.shadowmage.ancientwarfare.core.crafting.AWCraftingManager;
import net.shadowmage.ancientwarfare.core.crafting.AWWorkbenchCrafting;
import net.shadowmage.ancientwarfare.core.datafixes.AWDataFixes;
import net.shadowmage.ancientwarfare.core.entity.AWFakePlayer;
import net.shadowmage.ancientwarfare.core.init.AWCoreBlocks;
import net.shadowmage.ancientwarfare.core.init.AWCoreItems;
import net.shadowmage.ancientwarfare.core.init.AWCoreLoot;
import net.shadowmage.ancientwarfare.core.init.AWMenuTypes;
import net.shadowmage.ancientwarfare.core.network.NetworkHandler;
import net.shadowmage.ancientwarfare.core.proxy.ClientProxy;
import net.shadowmage.ancientwarfare.core.proxy.CommonProxy;
import net.shadowmage.ancientwarfare.core.proxy.CommonProxyBase;
import net.shadowmage.ancientwarfare.core.registry.RegistryLoader;
import net.shadowmage.ancientwarfare.core.registry.ResearchRegistry;
import net.shadowmage.ancientwarfare.core.research.ResearchTracker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Forge 1.20.1 entry point for the shared Ancient Warfare module.
 */
@Mod(AncientWarfareCore.MOD_ID)
public final class AncientWarfareCore {
    public static final String MOD_ID = "ancientwarfare";
    public static final RegistryObject<CreativeModeTab> TAB = AWCoreTab.TAB;
    public static final Logger LOG = LogManager.getLogger(MOD_ID);

    public static AncientWarfareCore instance;
    public static CommonProxyBase proxy;
    public static AWCoreStatics statics;

    public AncientWarfareCore() {
        instance = this;
        statics = new AWCoreStatics("AncientWarfare");
        proxy = DistExecutor.unsafeRunForDist(() -> ClientProxy::new, () -> CommonProxy::new);

        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        AWCoreTab.register(modBus);
        AWCoreBlocks.register(modBus);
        AWCoreItems.register(modBus);
        AWWorkbenchCrafting.register(modBus);
        AWMenuTypes.register(modBus);
        modBus.addListener(this::commonSetup);
        modBus.addListener(this::loadComplete);

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(ResearchTracker.INSTANCE);

        registerNativeMenus();
        RegistryLoader.registerParser(new ResearchRegistry.ResearchParser());
        CompatLoader.registerCompat(new FTBCompat());
        AWCraftingManager.init();
        proxy.preInit();
    }

    private void registerNativeMenus() {
        NetworkHandler.registerContainer(NetworkHandler.GUI_CRAFTING, ContainerEngineeringStation.class);
        NetworkHandler.registerContainer(NetworkHandler.GUI_RESEARCH_STATION, ContainerResearchStation.class);
        NetworkHandler.registerContainer(NetworkHandler.GUI_BACKPACK, ContainerBackpack.class);
        NetworkHandler.registerContainer(NetworkHandler.GUI_RESEARCH_BOOK, ContainerResearchBook.class);
        NetworkHandler.registerContainer(NetworkHandler.GUI_MANUAL, ContainerManual.class);
        NetworkHandler.registerContainer(NetworkHandler.GUI_INFO_TOOL, ContainerInfoTool.class);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            NetworkHandler.INSTANCE.registerNetwork();
            proxy.init();
            AWCoreItems.load();
            AWCraftingManager.registerIngredients();
            RegistryLoader.load();
            AWCraftingManager.loadRecipes();
            CompatLoader.init();
            AWDataFixes.registerDataFixes();
            AWCoreLoot.init();
        });
    }

    private void loadComplete(FMLLoadCompleteEvent event) {
        proxy.postInit();
        statics.save();
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        LegacyCommandRegistrar.register(event.getDispatcher(), new CommandResearch());
        LegacyCommandRegistrar.register(event.getDispatcher(), new CommandUtils());
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        statics.save();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onDimensionUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel) {
            AWFakePlayer.onWorldUnload();
        }
    }
}
