package net.shadowmage.ancientwarfare.structure.template;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
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
    private static HashMap<String, StructureTemplate> loadedTemplates = new HashMap<>();

    //used on client side - only these get synced on log on and then get displayed in structure selection guis, subsequent calls to getTemplate will trigger syncing of the full template detail
    private static Set<String> allTemplateNames = new HashSet<>();
    private static Set<String> survivalTemplateNames = new HashSet<>();

    private static Set<ITemplateObserver> observers = new HashSet<>();

    public static void addTemplate(StructureTemplate template) {
        if (template.getValidationSettings() == null) {
            return;
        }
        if (template.getValidationSettings().isWorldGenEnabled()) {
            WorldGenStructureManager.INSTANCE.registerWorldGenStructure(template);
        }
        if (loadedTemplates.keySet().contains(template.name)) {
            AncientWarfareStructure.proxy.clearTemplatePreviewCache();
        }
        loadedTemplates.put(template.name, template);

        syncTemplateToClient(template);
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

    public static void removeAll() {
        //creating a new list because otherwise we run into concurrent modification exception as the collection is both queried and modified
        new ArrayList<>(loadedTemplates.keySet()).forEach(StructureTemplateManager::removeTemplate);
    }

    public static Optional<StructureTemplate> getTemplate(String name) {
        StructureTemplate template = loadedTemplates.get(name);
        if (template == null && FMLEnvironment.dist == Dist.CLIENT && allTemplateNames.contains(name)) {
            PacketStructure pkt = new PacketStructure();
            pkt.packetData.putString(SYNC_TEMPLATE_TAG, name);
            NetworkHandler.sendToServer(pkt);
        }
        return Optional.ofNullable(template);
    }

    public static Map<String, StructureTemplate> getSurvivalStructures() {
        return loadedTemplates.entrySet().stream().filter(e -> e.getValue().getValidationSettings().isSurvival()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public static void onTemplateData(CompoundTag tag) {
        if (tag.contains(SINGLE_STRUCTURE_TAG)) {
            StructureTemplate template = StructureTemplate.deserializeNBT(tag.getCompound(SINGLE_STRUCTURE_TAG));
            addTemplate(template);
            observers.forEach(observer -> observer.notifyTemplateChange(template));
        } else if (tag.contains(SYNC_TEMPLATE_TAG)) {
            getTemplate(tag.getString(SYNC_TEMPLATE_TAG)).ifPresent(StructureTemplateManager::syncTemplateToClient);
        } else if (tag.contains(STRUCTURE_LIST_TAG)) {
            loadedTemplates.clear();
            allTemplateNames = NBTHelper.getStringSet(tag.getList(STRUCTURE_LIST_TAG, Constants.NBT.TAG_STRING));
            survivalTemplateNames = NBTHelper.getStringSet(tag.getList("survivalStructures", Constants.NBT.TAG_STRING));
        }
    }

    public static Set<String> getTemplates() {
        return allTemplateNames;
    }

    public static Set<String> getSurvivalTemplates() {
        return survivalTemplateNames;
    }

    public static boolean templateExists(String name) {
        return loadedTemplates.keySet().contains(name);
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
