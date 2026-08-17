package net.shadowmage.ancientwarfare.core.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.shadowmage.ancientwarfare.core.AncientWarfareCore;
import net.shadowmage.ancientwarfare.core.compat.jei.PacketTransferRecipe;
import net.shadowmage.ancientwarfare.core.container.ContainerBase;
import net.shadowmage.ancientwarfare.core.init.AWMenuTypes;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Forge 1.20.1 packet channel and native-menu registration bridge.
 */
public final class NetworkHandler {
    public static final String CHANNELNAME = "main";
    private static final String PROTOCOL_VERSION = "3";
    public static final NetworkHandler INSTANCE = new NetworkHandler();

    private static final int PACKET_GUI = 1;
    public static final int PACKET_STRUCTURE = 2;
    private static final int PACKET_ITEM_KEY_INTERFACE = 3;
    private static final int PACKET_ENTITY = 5;
    private static final int PACKET_RESEARCH_INIT = 6;
    private static final int PACKET_RESEARCH_ADD = 7;
    private static final int PACKET_RESEARCH_START = 8;
    public static final int PACKET_STRUCTURE_REMOVE = 11;
    public static final int PACKET_NPC_COMMAND = 12;
    public static final int PACKET_FACTION_UPDATE = 13;
    private static final int PACKET_BLOCK_EVENT = 14;
    public static final int PACKET_AIM_UPDATE = 15;
    public static final int PACKET_AMMO_SELECT = 16;
    public static final int PACKET_AMMO_UPDATE = 17;
    public static final int PACKET_FIRE_UPDATE = 18;
    public static final int PACKET_PACK_COMMAND = 19;
    public static final int PACKET_SINGLE_AMMO_UPDATE = 20;
    public static final int PACKET_TURRET_ANGLES_UPDATE = 21;
    public static final int PACKET_UPGRADE_UPDATE = 22;
    public static final int PACKET_VEHICLE_INPUT = 23;
    public static final int PACKET_VEHICLE_MOVE = 24;
    private static final int PACKET_JEI_TRANSFER_RECIPE = 25;
    private static final int PACKET_MANUAL_RELOAD = 26;
    public static final int PACKET_EXTENDED_REACH_ATTACK = 27;
    public static final int PACKET_STRUCTURE_MAP = 28;
    public static final int PACKET_STRUCTURE_ENTRY = 29;
    public static final int PACKET_SOUND_BLOCK_PLAYER_SPEC_VALUES = 30;
    public static final int PACKET_TEAM_MEMBERSHIP_UPDATE = 31;
    public static final int PACKET_TEAM_STANDINGS_UPDATE = 32;
    public static final int PACKET_TEAM_STANDING_UPDATE = 33;
    public static final int PACKET_HIGHLIGHT_BLOCK = 34;
    public static final int PACKET_SHOW_BBS = 35;
    public static final int PACKET_ITEM_MOUSE_SCROLL = 36;

    private static ResourceLocation menuId(String path) {
        return new ResourceLocation(AncientWarfareCore.MOD_ID, path);
    }

    public static final ResourceLocation GUI_CRAFTING = menuId("crafting");
    public static final ResourceLocation GUI_SCANNER = menuId("scanner");
    public static final ResourceLocation GUI_BUILDER = menuId("builder");
    public static final ResourceLocation GUI_NPC_INVENTORY = menuId("npc_inventory");
    public static final ResourceLocation GUI_WORKSITE_INVENTORY_SIDE_ADJUST = menuId("worksite_inventory_side_adjust");
    public static final ResourceLocation GUI_NPC_TRADE_ORDER = menuId("npc_trade_order");
    public static final ResourceLocation GUI_SPAWNER_ADVANCED = menuId("spawner_advanced");
    public static final ResourceLocation GUI_SPAWNER_ADVANCED_BLOCK = menuId("spawner_advanced_block");
    public static final ResourceLocation GUI_SPAWNER_ADVANCED_INVENTORY = menuId("spawner_advanced_inventory");
    public static final ResourceLocation GUI_SPAWNER_ADVANCED_BLOCK_INVENTORY = menuId("spawner_advanced_block_inventory");
    public static final ResourceLocation GUI_GATE_CONTROL = menuId("gate_control");
    public static final ResourceLocation GUI_RESEARCH_STATION = menuId("research_station");
    public static final ResourceLocation GUI_DRAFTING_STATION = menuId("drafting_station");
    public static final ResourceLocation GUI_WORKSITE_ANIMAL_CONTROL = menuId("worksite_animal_control");
    public static final ResourceLocation GUI_WORKSITE_AUTO_CRAFT = menuId("worksite_auto_craft");
    public static final ResourceLocation GUI_WORKSITE_FISH_CONTROL = menuId("worksite_fish_control");
    public static final ResourceLocation GUI_MAILBOX_INVENTORY = menuId("mailbox_inventory");
    public static final ResourceLocation GUI_WAREHOUSE_CONTROL = menuId("warehouse_control");
    public static final ResourceLocation GUI_WAREHOUSE_STORAGE = menuId("warehouse_storage");
    public static final ResourceLocation GUI_WAREHOUSE_STOCK = menuId("warehouse_stock");
    public static final ResourceLocation GUI_WAREHOUSE_OUTPUT = menuId("warehouse_output");
    public static final ResourceLocation GUI_WAREHOUSE_CRAFTING = menuId("warehouse_crafting");
    public static final ResourceLocation GUI_CHUNK_LOADER_DELUXE = menuId("chunk_loader_deluxe");
    public static final ResourceLocation GUI_WORKSITE_QUARRY = menuId("worksite_quarry");
    public static final ResourceLocation GUI_WORKSITE_TREE_FARM = menuId("worksite_tree_farm");
    public static final ResourceLocation GUI_WORKSITE_ANIMAL_FARM = menuId("worksite_animal_farm");
    public static final ResourceLocation GUI_WORKSITE_CROP_FARM = menuId("worksite_crop_farm");
    public static final ResourceLocation GUI_WORKSITE_FISH_FARM = menuId("worksite_fish_farm");
    public static final ResourceLocation GUI_WORKSITE_QUARRY_BOUNDS = menuId("worksite_quarry_bounds");
    public static final ResourceLocation GUI_STIRLING_GENERATOR = menuId("stirling_generator");
    public static final ResourceLocation GUI_WAREHOUSE_STOCK_LINKER = menuId("warehouse_stock_linker");
    public static final ResourceLocation GUI_NPC_WORK_ORDER = menuId("npc_work_order");
    public static final ResourceLocation GUI_NPC_UPKEEP_ORDER = menuId("npc_upkeep_order");
    public static final ResourceLocation GUI_NPC_COMBAT_ORDER = menuId("npc_combat_order");
    public static final ResourceLocation GUI_NPC_ROUTING_ORDER = menuId("npc_routing_order");
    public static final ResourceLocation GUI_NPC_FACTION_TRADE_SETUP = menuId("npc_faction_trade_setup");
    public static final ResourceLocation GUI_BACKPACK = menuId("backpack");
    public static final ResourceLocation GUI_NPC_TOWN_HALL = menuId("npc_town_hall");
    public static final ResourceLocation GUI_NPC_FACTION_TRADE_VIEW = menuId("npc_faction_trade_view");
    public static final ResourceLocation GUI_NPC_BARD = menuId("npc_bard");
    public static final ResourceLocation GUI_NPC_CREATIVE = menuId("npc_creative");
    public static final ResourceLocation GUI_RESEARCH_BOOK = menuId("research_book");
    public static final ResourceLocation GUI_WORKSITE_BOUNDS = menuId("worksite_bounds");
    public static final ResourceLocation GUI_NPC_PLAYER_OWNED_TRADE = menuId("npc_player_owned_trade");
    public static final ResourceLocation GUI_SOUND_BLOCK = menuId("sound_block");
    public static final ResourceLocation GUI_NPC_FACTION_BARD = menuId("npc_faction_bard");
    public static final ResourceLocation GUI_VEHICLE_AMMO_SELECTION = menuId("vehicle_ammo_selection");
    public static final ResourceLocation GUI_VEHICLE_INVENTORY = menuId("vehicle_inventory");
    public static final ResourceLocation GUI_VEHICLE_STATS = menuId("vehicle_stats");
    public static final ResourceLocation GUI_WORKSITE_FRUIT_FARM = menuId("worksite_fruit_farm");
    public static final ResourceLocation GUI_TOWN_BUILDER = menuId("town_builder");
    public static final ResourceLocation GUI_LOOT_CHEST_PLACER = menuId("loot_chest_placer");
    public static final ResourceLocation GUI_MANUAL = menuId("manual");
    public static final ResourceLocation GUI_INFO_TOOL = menuId("info_tool");
    public static final ResourceLocation GUI_GATE_CONTROL_CREATIVE = menuId("gate_control_creative");
    public static final ResourceLocation GUI_LOOT_BASKET = menuId("loot_basket");
    public static final ResourceLocation GUI_STAKE = menuId("stake");
    public static final ResourceLocation GUI_STATUE = menuId("statue");
    public static final ResourceLocation GUI_NPC_FACTION_SPELLCASTER_WIZARDRY = menuId("npc_faction_spellcaster_wizardry");

    private final Map<Integer, ConcurrentLinkedQueue<CompoundTag>> pendingClientGuiPackets = new ConcurrentHashMap<>();
    private SimpleChannel channel;

    private NetworkHandler() {
    }

    public synchronized void registerNetwork() {
        if (channel != null) {
            return;
        }
        channel = NetworkRegistry.newSimpleChannel(
                new ResourceLocation(AncientWarfareCore.MOD_ID, CHANNELNAME),
                () -> PROTOCOL_VERSION,
                PROTOCOL_VERSION::equals,
                PROTOCOL_VERSION::equals);

        PacketBase.registerPacketType(PACKET_GUI, PacketGui.class, PacketGui::new);
        PacketBase.registerPacketType(PACKET_ITEM_KEY_INTERFACE, PacketItemInteraction.class, PacketItemInteraction::new);
        PacketBase.registerPacketType(PACKET_ENTITY, PacketEntity.class, PacketEntity::new);
        PacketBase.registerPacketType(PACKET_RESEARCH_INIT, PacketResearchInit.class, PacketResearchInit::new);
        PacketBase.registerPacketType(PACKET_RESEARCH_ADD, PacketResearchUpdate.class, PacketResearchUpdate::new);
        PacketBase.registerPacketType(PACKET_RESEARCH_START, PacketResearchStart.class, PacketResearchStart::new);
        PacketBase.registerPacketType(PACKET_BLOCK_EVENT, PacketBlockEvent.class, PacketBlockEvent::new);
        PacketBase.registerPacketType(PACKET_MANUAL_RELOAD, PacketManualReload.class, PacketManualReload::new);
        PacketBase.registerPacketType(PACKET_ITEM_MOUSE_SCROLL, PacketItemMouseScroll.class, PacketItemMouseScroll::new);
        if (ModList.get().isLoaded("jei")) {
            PacketBase.registerPacketType(PACKET_JEI_TRANSFER_RECIPE, PacketTransferRecipe.class, PacketTransferRecipe::new);
        }
        PacketBase.bindChannel(channel);
    }

    public static void sendToServer(PacketBase packet) {
        INSTANCE.requireChannel().sendToServer(packet);
    }

    public static void sendToPlayer(ServerPlayer player, PacketBase packet) {
        INSTANCE.requireChannel().send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    public static void sendToAllPlayers(PacketBase packet) {
        INSTANCE.requireChannel().send(PacketDistributor.ALL.noArg(), packet);
    }

    public static void sendToAllTracking(Entity entity, PacketBase packet) {
        INSTANCE.requireChannel().send(PacketDistributor.TRACKING_ENTITY.with(() -> entity), packet);
    }

    public static void sendToAllTrackingChunk(Level world, int chunkX, int chunkZ, PacketBase packet) {
        LevelChunk chunk = world.getChunk(chunkX, chunkZ);
        INSTANCE.requireChannel().send(PacketDistributor.TRACKING_CHUNK.with(() -> chunk), packet);
    }

    private SimpleChannel requireChannel() {
        if (channel == null) {
            throw new IllegalStateException("Ancient Warfare network channel has not been initialized");
        }
        return channel;
    }

    public static void registerContainer(ResourceLocation id, Class<? extends ContainerBase> containerClass) {
        AWMenuTypes.registerMenu(id, containerClass);
    }

    /**
     * Forge's container-open message and AW's legacy initialization message use
     * different channels. Keep early initialization data until the screen has
     * installed its menu, rather than dropping it against InventoryMenu.
     */
    void queuePendingGuiPacket(int menuId, CompoundTag data) {
        ConcurrentLinkedQueue<CompoundTag> packets =
                pendingClientGuiPackets.computeIfAbsent(menuId, ignored -> new ConcurrentLinkedQueue<>());
        if (packets.size() >= 32) {
            packets.poll();
        }
        packets.offer(data.copy());
    }

    void flushPendingGuiPackets(ContainerBase menu) {
        ConcurrentLinkedQueue<CompoundTag> packets = pendingClientGuiPackets.remove(menu.containerId);
        if (packets == null) {
            return;
        }
        CompoundTag data;
        while ((data = packets.poll()) != null) {
            menu.onPacketData(data);
        }
    }

}

