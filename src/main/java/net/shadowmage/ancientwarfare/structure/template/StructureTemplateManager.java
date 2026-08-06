package net.shadowmage.ancientwarfare.structure.template;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.util.thread.EffectiveSide;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.shadowmage.ancientwarfare.core.network.NetworkHandler;
import net.shadowmage.ancientwarfare.core.util.Constants;
import net.shadowmage.ancientwarfare.core.util.NBTHelper;
import net.shadowmage.ancientwarfare.structure.AncientWarfareStructure;
import net.shadowmage.ancientwarfare.structure.network.PacketStructure;
import net.shadowmage.ancientwarfare.structure.network.PacketStructureRemove;

import java.util.*;
import java.util.stream.Collectors;

public class StructureTemplateManager {
    private StructureTemplateManager() {
    }

    private static final String SINGLE_STRUCTURE_TAG = "singleStructure";
    private static final String SYNC_TEMPLATE_TAG = "syncTemplate";
    private static final String STRUCTURE_LIST_TAG = "structureList";

    /**
     * Authoritative templates loaded from disk. This map is used by world generation and the logical server.
     * It must never be cleared by a client-bound template-list packet in an integrated server.
     */
    private static final Map<String, StructureTemplate> loadedTemplates = new HashMap<>();

    /**
     * Templates explicitly synchronized by the currently connected server for client GUIs/previews.
     * Keeping this separate is required because logical client and logical server share static fields in
     * singleplayer.
     */
    private static final Map<String, StructureTemplate> clientLoadedTemplates = new HashMap<>();

    // Used on the logical client. Only names are sent on login; details are requested lazily.
    private static Set<String> allTemplateNames = new HashSet<>();
    private static Set<String> survivalTemplateNames = new HashSet<>();
    private static boolean clientTemplateListReceived = false;

    private static final Set<ITemplateObserver> observers = new HashSet<>();

    public static void addTemplate(StructureTemplate template) {
        if (template.getValidationSettings() == null) {
            return;
        }
        if (template.getValidationSettings().isWorldGenEnabled()) {
            WorldGenStructureManager.INSTANCE.registerWorldGenStructure(template);
        }
        if (loadedTemplates.containsKey(template.name)) {
            AncientWarfareStructure.proxy.clearTemplatePreviewCache();
        }
        loadedTemplates.put(template.name, template);

        syncTemplateToClient(template);
    }

    private static void addClientTemplate(StructureTemplate template) {
        clientLoadedTemplates.put(template.name, template);
        observers.forEach(observer -> observer.notifyTemplateChange(template));
    }

    private static void syncTemplateToClient(StructureTemplate template) {
        if (ServerLifecycleHooks.getCurrentServer() != null) {
            PacketStructure pkt = new PacketStructure();
            pkt.packetData.put(SINGLE_STRUCTURE_TAG, template.serializeNBT());
            NetworkHandler.sendToAllPlayers(pkt);
        }
    }

    public static void onPlayerConnect(ServerPlayer player) {
        Set<String> survivalTemplates = loadedTemplates.entrySet().stream()
                .filter(e -> e.getValue().getValidationSettings().isSurvival())
                .map(Map.Entry::getKey).collect(Collectors.toSet());
        PacketStructure pkt = new PacketStructure();
        pkt.packetData.put(STRUCTURE_LIST_TAG, NBTHelper.getNBTStringList(loadedTemplates.keySet()));
        pkt.packetData.put("survivalStructures", NBTHelper.getNBTStringList(survivalTemplates));
        NetworkHandler.sendToPlayer(player, pkt);
    }

    public static boolean removeTemplate(String name) {
        if (loadedTemplates.containsKey(name)) {
            loadedTemplates.remove(name);
            if (ServerLifecycleHooks.getCurrentServer() != null) {
                NetworkHandler.sendToAllPlayers(new PacketStructureRemove(name));
            }
            return true;
        }
        return false;
    }

    public static void removeClientTemplate(String name) {
        clientLoadedTemplates.remove(name);
        allTemplateNames.remove(name);
        survivalTemplateNames.remove(name);
    }

    public static void removeAll() {
        // creating a new list because otherwise we run into concurrent modification exception as the collection is both queried and modified
        new ArrayList<>(loadedTemplates.keySet()).forEach(StructureTemplateManager::removeTemplate);
    }

    public static Optional<StructureTemplate> getTemplate(String name) {
        if (isSynchronizedClientView()) {
            StructureTemplate template = clientLoadedTemplates.get(name);
            if (template == null && allTemplateNames.contains(name)) {
                PacketStructure pkt = new PacketStructure();
                pkt.packetData.putString(SYNC_TEMPLATE_TAG, name);
                NetworkHandler.sendToServer(pkt);
            }
            return Optional.ofNullable(template);
        }
        return Optional.ofNullable(loadedTemplates.get(name));
    }

    private static boolean isSynchronizedClientView() {
        return clientTemplateListReceived && EffectiveSide.get() == LogicalSide.CLIENT;
    }

    public static Map<String, StructureTemplate> getSurvivalStructures() {
        Map<String, StructureTemplate> templates = isSynchronizedClientView() ? clientLoadedTemplates : loadedTemplates;
        return templates.entrySet().stream()
                .filter(e -> e.getValue().getValidationSettings().isSurvival())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Handles a template packet on its actual logical reception side.
     *
     * @param tag packet payload
     * @param serverSide true for a client -> server request, false for a server -> client synchronization packet
     */
    public static void onTemplateData(CompoundTag tag, boolean serverSide) {
        if (tag.contains(SINGLE_STRUCTURE_TAG)) {
            StructureTemplate template = StructureTemplate.deserializeNBT(tag.getCompound(SINGLE_STRUCTURE_TAG));
            if (serverSide) {
                // Preserve the old packet semantics for any server-bound full-template packet.
                addTemplate(template);
            } else {
                addClientTemplate(template);
            }
        } else if (tag.contains(SYNC_TEMPLATE_TAG)) {
            if (serverSide) {
                StructureTemplate template = loadedTemplates.get(tag.getString(SYNC_TEMPLATE_TAG));
                if (template != null) {
                    syncTemplateToClient(template);
                }
            }
        } else if (tag.contains(STRUCTURE_LIST_TAG) && !serverSide) {
            // Never clear loadedTemplates here. In integrated singleplayer that is the same JVM/static state
            // used by the logical server and doing so removes every town building template from worldgen.
            clientLoadedTemplates.clear();
            allTemplateNames = NBTHelper.getStringSet(tag.getList(STRUCTURE_LIST_TAG, Constants.NBT.TAG_STRING));
            survivalTemplateNames = NBTHelper.getStringSet(tag.getList("survivalStructures", Constants.NBT.TAG_STRING));
            clientTemplateListReceived = true;
        }
    }

    public static Set<String> getTemplates() {
        if (isSynchronizedClientView()) {
            return Collections.unmodifiableSet(allTemplateNames);
        }
        return Collections.unmodifiableSet(loadedTemplates.keySet());
    }

    public static Set<String> getSurvivalTemplates() {
        if (isSynchronizedClientView()) {
            return Collections.unmodifiableSet(survivalTemplateNames);
        }
        return loadedTemplates.entrySet().stream()
                .filter(e -> e.getValue().getValidationSettings().isSurvival())
                .map(Map.Entry::getKey)
                .collect(Collectors.toUnmodifiableSet());
    }

    public static boolean templateExists(String name) {
        return isSynchronizedClientView() ? clientLoadedTemplates.containsKey(name) : loadedTemplates.containsKey(name);
    }

    public static void registerObserver(ITemplateObserver observer) {
        observers.add(observer);
    }

    public static void unregisterObserver(ITemplateObserver observer) {
        observers.remove(observer);
    }

    public interface ITemplateObserver {
        void notifyTemplateChange(StructureTemplate template);
    }
}
