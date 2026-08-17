package net.shadowmage.ancientwarfare.npc;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.RegistryObject;
import net.shadowmage.ancientwarfare.core.command.LegacyCommandRegistrar;
import net.shadowmage.ancientwarfare.core.compat.CompatLoader;
import net.shadowmage.ancientwarfare.core.gamedata.AWGameData;
import net.shadowmage.ancientwarfare.core.gamedata.WorldData;
import net.shadowmage.ancientwarfare.core.network.NetworkHandler;
import net.shadowmage.ancientwarfare.core.network.PacketBase;
import net.shadowmage.ancientwarfare.core.registry.RegistryLoader;
import net.shadowmage.ancientwarfare.npc.command.CommandDebugAI;
import net.shadowmage.ancientwarfare.npc.command.CommandFaction;
import net.shadowmage.ancientwarfare.npc.command.CommandTeams;
import net.shadowmage.ancientwarfare.npc.compat.EpicSiegeCompat;
import net.shadowmage.ancientwarfare.npc.compat.TwilightForestCompat;
import net.shadowmage.ancientwarfare.npc.compat.ebwizardry.EBWizardryCompat;
import net.shadowmage.ancientwarfare.npc.config.AWNPCStatics;
import net.shadowmage.ancientwarfare.npc.crafting.AWNpcCrafting;
import net.shadowmage.ancientwarfare.npc.container.*;
import net.shadowmage.ancientwarfare.npc.faction.FactionTracker;
import net.shadowmage.ancientwarfare.npc.init.AWNPCEntities;
import net.shadowmage.ancientwarfare.npc.init.AWNPCBlocks;
import net.shadowmage.ancientwarfare.npc.init.AWNPCItems;
import net.shadowmage.ancientwarfare.npc.init.AWNPCSounds;
import net.shadowmage.ancientwarfare.npc.network.*;
import net.shadowmage.ancientwarfare.npc.proxy.NpcClientProxy;
import net.shadowmage.ancientwarfare.npc.proxy.NpcCommonProxy;
import net.shadowmage.ancientwarfare.npc.registry.*;
import net.shadowmage.ancientwarfare.structure.network.PacketStructureEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Forge 1.20.1 entry point for the NPC module.
 */
@Mod(AncientWarfareNPC.MOD_ID)
public final class AncientWarfareNPC {
    public static final String MOD_ID = "ancientwarfarenpc";
    public static final String MOD_PREFIX = MOD_ID + ":";
    public static final RegistryObject<CreativeModeTab> TAB = AWNPCTab.TAB;
    public static final Logger LOG = LogManager.getLogger(MOD_ID);

    public static AncientWarfareNPC instance;
    public static NpcCommonProxy proxy;
    public static AWNPCStatics statics;

    public AncientWarfareNPC() {
        instance = this;
        statics = new AWNPCStatics("AncientWarfareNpc");
        proxy = DistExecutor.unsafeRunForDist(() -> NpcClientProxy::new, () -> NpcCommonProxy::new);

        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        AWNPCTab.register(modBus);
        AWNPCBlocks.register(modBus);
        AWNPCItems.register(modBus);
        AWNPCEntities.register(modBus);
        AWNpcCrafting.register(modBus);
        AWNPCSounds.register(modBus);
        modBus.addListener(this::commonSetup);
        modBus.addListener(this::loadComplete);

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(net.shadowmage.ancientwarfare.npc.event.EventHandler.INSTANCE);

        registerNativeMenus();
        registerPackets();
        registerCompatibility();
        registerDataParsers();
        proxy.preInit();
    }

    private void registerNativeMenus() {
        NetworkHandler.registerContainer(NetworkHandler.GUI_NPC_INVENTORY, ContainerNpcInventory.class);
        NetworkHandler.registerContainer(NetworkHandler.GUI_NPC_WORK_ORDER, ContainerWorkOrder.class);
        NetworkHandler.registerContainer(NetworkHandler.GUI_NPC_UPKEEP_ORDER, ContainerUpkeepOrder.class);
        NetworkHandler.registerContainer(NetworkHandler.GUI_NPC_COMBAT_ORDER, ContainerCombatOrder.class);
        NetworkHandler.registerContainer(NetworkHandler.GUI_NPC_FACTION_TRADE_SETUP, ContainerNpcFactionTradeSetup.class);
        NetworkHandler.registerContainer(NetworkHandler.GUI_NPC_FACTION_TRADE_VIEW, ContainerNpcFactionTradeView.class);
        NetworkHandler.registerContainer(NetworkHandler.GUI_NPC_ROUTING_ORDER, ContainerRoutingOrder.class);
        NetworkHandler.registerContainer(NetworkHandler.GUI_NPC_TOWN_HALL, ContainerTownHall.class);
        NetworkHandler.registerContainer(NetworkHandler.GUI_NPC_BARD, ContainerNpcBard.class);
        NetworkHandler.registerContainer(NetworkHandler.GUI_NPC_CREATIVE, ContainerNpcCreativeControls.class);
        NetworkHandler.registerContainer(NetworkHandler.GUI_NPC_TRADE_ORDER, ContainerTradeOrder.class);
        NetworkHandler.registerContainer(NetworkHandler.GUI_NPC_PLAYER_OWNED_TRADE, ContainerNpcPlayerOwnedTrade.class);
        NetworkHandler.registerContainer(NetworkHandler.GUI_NPC_FACTION_BARD, ContainerNpcFactionBard.class);
        if (ModList.get().isLoaded("ebwizardry")) {
            WizardryMenus.register();
        }
    }

    /** Keep optional Wizardry menu classes out of the base mod class-loading path. */
    private static final class WizardryMenus {
        private static void register() {
            NetworkHandler.registerContainer(
                    NetworkHandler.GUI_NPC_FACTION_SPELLCASTER_WIZARDRY,
                    ContainerNpcFactionSpellcasterWizardry.class);
        }
    }

    private void registerPackets() {
        PacketBase.registerPacketType(NetworkHandler.PACKET_NPC_COMMAND, PacketNpcCommand.class, PacketNpcCommand::new);
        PacketBase.registerPacketType(NetworkHandler.PACKET_FACTION_UPDATE, PacketFactionUpdate.class, PacketFactionUpdate::new);
        PacketBase.registerPacketType(NetworkHandler.PACKET_EXTENDED_REACH_ATTACK, PacketExtendedReachAttack.class, PacketExtendedReachAttack::new);
        PacketBase.registerPacketType(NetworkHandler.PACKET_STRUCTURE_ENTRY, PacketStructureEntry.class, PacketStructureEntry::new);
        PacketBase.registerPacketType(NetworkHandler.PACKET_TEAM_MEMBERSHIP_UPDATE, PacketTeamMembershipUpdate.class, PacketTeamMembershipUpdate::new);
        PacketBase.registerPacketType(NetworkHandler.PACKET_TEAM_STANDINGS_UPDATE, PacketTeamStandingsUpdate.class, PacketTeamStandingsUpdate::new);
        PacketBase.registerPacketType(NetworkHandler.PACKET_TEAM_STANDING_UPDATE, PacketTeamStandingUpdate.class, PacketTeamStandingUpdate::new);
    }

    private void registerCompatibility() {
        CompatLoader.registerCompat(new EpicSiegeCompat());
        CompatLoader.registerCompat(new TwilightForestCompat());
        CompatLoader.registerCompat(new EBWizardryCompat());
    }

    private void registerDataParsers() {
        RegistryLoader.registerParser(new FactionRegistry.FactionParser());
        RegistryLoader.registerParser(new TargetRegistry.TargetListParser());
        RegistryLoader.registerParser(new NpcDefaultsRegistry.FactionNpcDefaultsParser());
        RegistryLoader.registerParser(new NpcDefaultsRegistry.OwnedNpcDefaultsParser());
        RegistryLoader.registerParser(new FactionTradeListRegistry.Parser());
        RegistryLoader.registerParser(new NPCDialogue.Parser());
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(proxy::init);
    }

    private void loadComplete(FMLLoadCompleteEvent event) {
        proxy.loadSkins();
        AWNPCEntities.loadNpcSubtypeEquipment();
        MinecraftForge.EVENT_BUS.register(FactionTracker.INSTANCE);
        AWNPCItems.addFactionBlocks();
        statics.save();
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        LegacyCommandRegistrar.register(event.getDispatcher(), new CommandFaction());
        LegacyCommandRegistrar.register(event.getDispatcher(), new CommandTeams());
        LegacyCommandRegistrar.register(event.getDispatcher(), new CommandDebugAI());
    }

    @SubscribeEvent
    public void worldLoaded(LevelEvent.Load event) {
        if (!event.getLevel().isClientSide() && event.getLevel() instanceof Level level) {
            WorldData data = AWGameData.INSTANCE.getPerWorldData(level, WorldData.class);
            if (data != null) {
                AWNPCStatics.npcAIDebugMode = data.get("NpcAIDebugMode");
            }
        }
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        statics.save();
    }
}
