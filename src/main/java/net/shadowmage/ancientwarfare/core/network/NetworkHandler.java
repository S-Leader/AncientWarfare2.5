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

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Forge 1.20.1 packet channel and native-menu registration bridge.
 */
public final class NetworkHandler {
    public static final String CHANNELNAME = "main";
    private static final String PROTOCOL_VERSION = "2";
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

    public static final int GUI_CRAFTING = 0;
    public static final int GUI_SCANNER = 1;
    public static final int GUI_BUILDER = 2;
    public static final int GUI_NPC_INVENTORY = 4;
    public static final int GUI_WORKSITE_INVENTORY_SIDE_ADJUST = 5;
    public static final int GUI_NPC_TRADE_ORDER = 6;
    public static final int GUI_SPAWNER_ADVANCED = 7;
    public static final int GUI_SPAWNER_ADVANCED_BLOCK = 8;
    public static final int GUI_SPAWNER_ADVANCED_INVENTORY = 9;
    public static final int GUI_SPAWNER_ADVANCED_BLOCK_INVENTORY = 10;
    public static final int GUI_GATE_CONTROL = 11;
    public static final int GUI_RESEARCH_STATION = 12;
    public static final int GUI_DRAFTING_STATION = 13;
    public static final int GUI_WORKSITE_ANIMAL_CONTROL = 14;
    public static final int GUI_WORKSITE_AUTO_CRAFT = 15;
    public static final int GUI_WORKSITE_FISH_CONTROL = 16;
    public static final int GUI_MAILBOX_INVENTORY = 17;
    public static final int GUI_WAREHOUSE_CONTROL = 18;
    public static final int GUI_WAREHOUSE_STORAGE = 19;
    public static final int GUI_WAREHOUSE_STOCK = 20;
    public static final int GUI_WAREHOUSE_OUTPUT = 21;
    public static final int GUI_WAREHOUSE_CRAFTING = 22;
    public static final int GUI_CHUNK_LOADER_DELUXE = 23;
    public static final int GUI_WORKSITE_QUARRY = 24;
    public static final int GUI_WORKSITE_TREE_FARM = 25;
    public static final int GUI_WORKSITE_ANIMAL_FARM = 26;
    public static final int GUI_WORKSITE_CROP_FARM = 27;
    public static final int GUI_WORKSITE_FISH_FARM = 29;
    public static final int GUI_WORKSITE_QUARRY_BOUNDS = 30;
    public static final int GUI_STIRLING_GENERATOR = 31;
    public static final int GUI_WAREHOUSE_STOCK_LINKER = 32;
    public static final int GUI_NPC_WORK_ORDER = 34;
    public static final int GUI_NPC_UPKEEP_ORDER = 35;
    public static final int GUI_NPC_COMBAT_ORDER = 36;
    public static final int GUI_NPC_ROUTING_ORDER = 37;
    public static final int GUI_NPC_FACTION_TRADE_SETUP = 39;
    public static final int GUI_BACKPACK = 40;
    public static final int GUI_NPC_TOWN_HALL = 41;
    public static final int GUI_NPC_FACTION_TRADE_VIEW = 42;
    public static final int GUI_NPC_BARD = 43;
    public static final int GUI_NPC_CREATIVE = 44;
    public static final int GUI_RESEARCH_BOOK = 45;
    public static final int GUI_WORKSITE_BOUNDS = 46;
    public static final int GUI_NPC_PLAYER_OWNED_TRADE = 47;
    public static final int GUI_SOUND_BLOCK = 48;
    public static final int GUI_NPC_FACTION_BARD = 49;
    public static final int GUI_VEHICLE_AMMO_SELECTION = 50;
    public static final int GUI_VEHICLE_INVENTORY = 51;
    public static final int GUI_VEHICLE_STATS = 52;
    public static final int GUI_WORKSITE_FRUIT_FARM = 53;
    public static final int GUI_TOWN_BUILDER = 54;
    public static final int GUI_LOOT_CHEST_PLACER = 55;
    public static final int GUI_MANUAL = 56;
    public static final int GUI_INFO_TOOL = 57;
    public static final int GUI_GATE_CONTROL_CREATIVE = 58;
    public static final int GUI_LOOT_BASKET = 59;
    public static final int GUI_STAKE = 60;
    public static final int GUI_STATUE = 61;
    public static final int GUI_NPC_FACTION_SPELLCASTER_WIZARDRY = 62;

    private final Map<Integer, Class<?>> guiClasses = new HashMap<>();
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

    public static void registerContainer(int id, Class<? extends ContainerBase> containerClass) {
        AWMenuTypes.registerLegacy(id, containerClass);
    }

    public static void registerGui(int id, Class<?> guiClass) {
        INSTANCE.guiClasses.put(id, guiClass);
    }

    @Nullable
    Class<?> getGuiClass(int id) {
        return guiClasses.get(id);
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

    int validateClientGuiRegistrations() {
        List<String> errors = new ArrayList<>();
        for (AWMenuTypes.MenuRegistration registration : AWMenuTypes.registrations()) {
            int id = registration.legacyGuiId();
            Class<?> guiClass = guiClasses.get(id);
            if (guiClass == null) {
                errors.add("GUI id " + id + " has no client screen");
                continue;
            }
            try {
                guiClass.getConstructor(ContainerBase.class);
            } catch (NoSuchMethodException exception) {
                errors.add(guiClass.getName() + " lacks (ContainerBase) constructor");
            }
        }
        if (!errors.isEmpty()) {
            throw new IllegalStateException("Invalid Ancient Warfare GUI registrations: " + String.join("; ", errors));
        }
        return AWMenuTypes.registrations().size();
    }

}
