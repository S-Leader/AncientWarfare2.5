package net.shadowmage.ancientwarfare.structure;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.RegistryObject;
import net.shadowmage.ancientwarfare.core.command.LegacyCommandRegistrar;
import net.shadowmage.ancientwarfare.core.network.NetworkHandler;
import net.shadowmage.ancientwarfare.core.network.PacketBase;
import net.shadowmage.ancientwarfare.core.registry.RegistryLoader;
import net.shadowmage.ancientwarfare.structure.command.CommandStructure;
import net.shadowmage.ancientwarfare.structure.config.AWStructureStatics;
import net.shadowmage.ancientwarfare.structure.container.*;
import net.shadowmage.ancientwarfare.structure.datafixes.LootSettingsPotionRegistryNameFixer;
import net.shadowmage.ancientwarfare.structure.datafixes.TileLootFixer;
import net.shadowmage.ancientwarfare.structure.datafixes.WoodenCoffinFixer;
import net.shadowmage.ancientwarfare.structure.event.OneShotEntityDespawnListener;
import net.shadowmage.ancientwarfare.structure.init.AWStructureEntities;
import net.shadowmage.ancientwarfare.structure.init.AWStructureSounds;
import net.shadowmage.ancientwarfare.structure.network.*;
import net.shadowmage.ancientwarfare.structure.proxy.ClientProxyStructure;
import net.shadowmage.ancientwarfare.structure.proxy.CommonProxyStructure;
import net.shadowmage.ancientwarfare.structure.registry.BiomeGroupRegistry;
import net.shadowmage.ancientwarfare.structure.registry.EntitySpawnNBTRegistry;
import net.shadowmage.ancientwarfare.structure.registry.StructureBlockRegistry;
import net.shadowmage.ancientwarfare.structure.registry.TerritorySettingRegistry;
import net.shadowmage.ancientwarfare.structure.template.StructurePluginManager;
import net.shadowmage.ancientwarfare.structure.template.StructureTemplateManager;
import net.shadowmage.ancientwarfare.structure.template.WorldGenStructureManager;
import net.shadowmage.ancientwarfare.structure.template.datafixes.DataFixManager;
import net.shadowmage.ancientwarfare.structure.template.datafixes.fixers.*;
import net.shadowmage.ancientwarfare.structure.template.datafixes.fixers.json.JsonSimplificationFixer;
import net.shadowmage.ancientwarfare.structure.template.load.TemplateLoader;
import net.shadowmage.ancientwarfare.structure.util.CapabilityRespawnData;
import net.shadowmage.ancientwarfare.structure.worldgen.CapabilityTerritoryData;
import net.shadowmage.ancientwarfare.structure.worldgen.WorldGenTickHandler;
import net.shadowmage.ancientwarfare.structure.worldgen.WorldGenerationEventHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Forge 1.20.1 entry point for the structures module.
 */
@Mod(AncientWarfareStructure.MOD_ID)
public final class AncientWarfareStructure {
    public static final String MOD_ID = "ancientwarfarestructure";
    public static final RegistryObject<CreativeModeTab> TAB = AWStructureTab.TAB;
    public static final Logger LOG = LogManager.getLogger(MOD_ID);

    public static AncientWarfareStructure instance;
    public static CommonProxyStructure proxy;
    public static AWStructureStatics statics;

    public AncientWarfareStructure() {
        instance = this;
        statics = new AWStructureStatics("AncientWarfareStructures");
        proxy = DistExecutor.unsafeRunForDist(() -> ClientProxyStructure::new, () -> CommonProxyStructure::new);

        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        AWStructureTab.register(modBus);
        AWStructureEntities.register(modBus);
        AWStructureSounds.register(modBus);
        modBus.addListener(this::commonSetup);
        modBus.addListener(this::loadComplete);
        modBus.addListener(this::registerCapabilities);

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(net.shadowmage.ancientwarfare.structure.event.EventHandler.INSTANCE);
        MinecraftForge.EVENT_BUS.register(OneShotEntityDespawnListener.INSTANCE);
        if (AWStructureStatics.enableWorldGen) {
            MinecraftForge.EVENT_BUS.register(WorldGenTickHandler.INSTANCE);
            MinecraftForge.EVENT_BUS.register(WorldGenerationEventHandler.INSTANCE);
        }

        registerPackets();
        registerLegacyScreens();
        registerDataParsers();
        proxy.preInit();
        TemplateLoader.INSTANCE.initializeAndExportDefaults();
    }

    private void registerPackets() {
        PacketBase.registerPacketType(NetworkHandler.PACKET_STRUCTURE, PacketStructure.class, PacketStructure::new);
        PacketBase.registerPacketType(NetworkHandler.PACKET_STRUCTURE_REMOVE, PacketStructureRemove.class, PacketStructureRemove::new);
        PacketBase.registerPacketType(NetworkHandler.PACKET_SOUND_BLOCK_PLAYER_SPEC_VALUES, PacketSoundBlockPlayerSpecValues.class, PacketSoundBlockPlayerSpecValues::new);
        PacketBase.registerPacketType(NetworkHandler.PACKET_HIGHLIGHT_BLOCK, PacketHighlightBlock.class, PacketHighlightBlock::new);
        PacketBase.registerPacketType(NetworkHandler.PACKET_SHOW_BBS, PacketShowBoundingBoxes.class, PacketShowBoundingBoxes::new);
    }

    private void registerLegacyScreens() {
        NetworkHandler.registerContainer(NetworkHandler.GUI_SCANNER, ContainerStructureScanner.class);
        NetworkHandler.registerContainer(NetworkHandler.GUI_BUILDER, ContainerStructureSelection.class);
        NetworkHandler.registerContainer(NetworkHandler.GUI_TOWN_BUILDER, ContainerTownSelection.class);
        NetworkHandler.registerContainer(NetworkHandler.GUI_SPAWNER_ADVANCED, ContainerSpawnerAdvanced.class);
        NetworkHandler.registerContainer(NetworkHandler.GUI_SPAWNER_ADVANCED_BLOCK, ContainerSpawnerAdvancedBlock.class);
        NetworkHandler.registerContainer(NetworkHandler.GUI_SPAWNER_ADVANCED_INVENTORY, ContainerSpawnerAdvancedInventoryItem.class);
        NetworkHandler.registerContainer(NetworkHandler.GUI_SPAWNER_ADVANCED_BLOCK_INVENTORY, ContainerSpawnerAdvancedInventoryBlock.class);
        NetworkHandler.registerContainer(NetworkHandler.GUI_GATE_CONTROL, ContainerGateControl.class);
        NetworkHandler.registerContainer(NetworkHandler.GUI_GATE_CONTROL_CREATIVE, ContainerGateControl.class);
        NetworkHandler.registerContainer(NetworkHandler.GUI_DRAFTING_STATION, ContainerDraftingStation.class);
        NetworkHandler.registerContainer(NetworkHandler.GUI_SOUND_BLOCK, ContainerSoundBlock.class);
        NetworkHandler.registerContainer(NetworkHandler.GUI_LOOT_CHEST_PLACER, ContainerLootChestPlacer.class);
        NetworkHandler.registerContainer(NetworkHandler.GUI_LOOT_BASKET, ContainerLootBasket.class);
        NetworkHandler.registerContainer(NetworkHandler.GUI_STAKE, ContainerStake.class);
        NetworkHandler.registerContainer(NetworkHandler.GUI_STATUE, ContainerStatue.class);
    }

    private void registerDataParsers() {
        RegistryLoader.registerParser(new EntitySpawnNBTRegistry.Parser());
        RegistryLoader.registerParser(new BiomeGroupRegistry.Parser());
        RegistryLoader.registerParser(new StructureBlockRegistry.Parser());
        RegistryLoader.registerParser(new TerritorySettingRegistry.Parser());
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            proxy.init();
            registerDataFixes();
        });
    }

    private void registerDataFixes() {
        DataFixManager.registerRuleFixer(new FactionExpansionFixer());
        DataFixManager.registerRuleFixer(new JsonSimplificationFixer());
        DataFixManager.registerRuleFixer(new BlockMetaToBlockStateFixer());
        DataFixManager.registerRuleFixer(new EntityPositionToNBTFixer());
        DataFixManager.registerRuleFixer(new RuleNameConsolidationFixer());
        DataFixManager.registerRuleFixer(new EntityRuleNameFixer());
        DataFixManager.registerRuleFixer(new EntityEquipmentFixer());
        DataFixManager.registerRuleFixer(new TileLootFixer());
        DataFixManager.registerRuleFixer(new LootSettingsPotionRegistryNameFixer());
        DataFixManager.registerRuleFixer(new WoodenCoffinFixer());
    }

    private void loadComplete(FMLLoadCompleteEvent event) {
        StructurePluginManager.INSTANCE.loadPlugins();
        WorldGenStructureManager.INSTANCE.loadBiomeList();
        TemplateLoader.INSTANCE.loadTemplates();
        statics.save();
        AWStructureStatics.logSkippableBlocksCoveredByMaterial();
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        CapabilityRespawnData.register(event);
        CapabilityTerritoryData.register(event);
    }

    @SubscribeEvent
    public void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            StructureTemplateManager.onPlayerConnect(player);
        }
    }

    @SubscribeEvent
    public void onEntityCapabilityAttach(AttachCapabilitiesEvent<Entity> event) {
        CapabilityRespawnData.onAttach(event);
    }

    @SubscribeEvent
    public void onLevelCapabilityAttach(AttachCapabilitiesEvent<Level> event) {
        CapabilityTerritoryData.onAttach(event);
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        LegacyCommandRegistrar.register(event.getDispatcher(), new CommandStructure());
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        if (AWStructureStatics.enableWorldGen) {
            WorldGenTickHandler.INSTANCE.finalTick();
        }
        statics.save();
    }
}
