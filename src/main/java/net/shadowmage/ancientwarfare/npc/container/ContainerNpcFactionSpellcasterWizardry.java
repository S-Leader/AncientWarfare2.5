package net.shadowmage.ancientwarfare.npc.container;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.shadowmage.ancientwarfare.npc.compat.ebwizardry.EBWizardryCompat;
import net.shadowmage.ancientwarfare.npc.entity.faction.NpcFactionSpellcasterWizardry;
import net.shadowmage.ancientwarfare.npc.registry.NpcDefaultsRegistry;
import net.shadowmage.ancientwarfare.npc.skin.NpcSkinSettings;

import java.util.List;

public class ContainerNpcFactionSpellcasterWizardry extends ContainerNpcBase<NpcFactionSpellcasterWizardry> implements ISkinSettingsContainer {
    private static final String SKIN_SETTINGS_TAG = "skinSettings";
    private final List<ResourceLocation> allSpells = new java.util.ArrayList<>();

    private List<ResourceLocation> assignedSpells;
    private int maxHealth;
    private NpcSkinSettings skinSettings;
    private boolean hasChanged; //if set to true, will set all flags to entity on container close

    public ContainerNpcFactionSpellcasterWizardry(Player player, int x, int y, int z) {
        super(player, x);
        skinSettings = entity.getSkinSettings();
        maxHealth = entity.getMaxHealthOverride();
        assignedSpells = entity.getSpells();
        allSpells.addAll(assignedSpells);
    }

    public void sendChangesToServer() {
        sendDataToServer(serializeContainerData());
    }

    private CompoundTag serializeContainerData() {
        CompoundTag tag = new CompoundTag();
        tag.put(SKIN_SETTINGS_TAG, skinSettings.serializeNBT());
        tag.putInt("maxHealth", maxHealth);
        ListTag spells = new ListTag();
        for (ResourceLocation spell : assignedSpells) spells.add(StringTag.valueOf(spell.toString()));
        tag.put("assignedSpells", spells);
        return tag;
    }

    @Override
    public void sendInitData() {
        sendDataToClient(serializeContainerData());
    }

    @Override
    public void handlePacketData(CompoundTag nbt) {
        assignedSpells = new java.util.ArrayList<>();
        for (Tag value : nbt.getList("assignedSpells", Tag.TAG_STRING)) {
            ResourceLocation id = ResourceLocation.tryParse(value.getAsString());
            if (id != null) assignedSpells.add(id);
        }
        maxHealth = nbt.getInt("maxHealth");
        skinSettings = NpcSkinSettings.deserializeNBT(nbt.getCompound(SKIN_SETTINGS_TAG));
        entity.setSkinSettings(skinSettings);
        hasChanged = true;
        refreshGui();
    }

    @Override
    public void removed(Player par1EntityPlayer) {
        if (hasChanged && !player.level().isClientSide) {
            hasChanged = false;
            entity.setSkinSettings(skinSettings.minimizeData());
            entity.setSpells(assignedSpells);
            entity.setMaxHealthOverride(maxHealth);
        }
        super.removed(par1EntityPlayer);
    }

    public List<ResourceLocation> getAssignedSpells() {
        return assignedSpells;
    }

    public void addSpell(ResourceLocation spell) {
        if (spell != null && !assignedSpells.contains(spell)) assignedSpells.add(spell);
        if (spell != null && !allSpells.contains(spell)) allSpells.add(spell);
    }

    public void removeSpell(ResourceLocation spell) {
        assignedSpells.remove(spell);
    }

    public List<ResourceLocation> getAllSpells() {
        return allSpells;
    }

    public void setNameAndPresetDefaults(String presetSubtypeName) {
        String faction = entity.getFaction();
        String nameTag = "entity.ancientwarfarenpc." + faction + "." + presetSubtypeName + "." + "name";
        entity.setCustomName(Component.translatable(nameTag));
        // add each spell to entity
        String[] spells = EBWizardryCompat.getDefaultSpells(NpcDefaultsRegistry.getFactionNpcDefault(entity.getFaction(), presetSubtypeName)).split(",");
        for (String spell : spells) {
            addSpell(ResourceLocation.tryParse(spell.trim()));
        }

        // set health
        maxHealth = (int) NpcDefaultsRegistry.getFactionNpcDefault(entity.getFaction(), presetSubtypeName).getBaseHealth();

        // set skin
        skinSettings.setSkinType(NpcSkinSettings.SkinType.NPC_TYPE);
        skinSettings.setRandom(true);
        skinSettings.setNpcTypeName(faction + "." + presetSubtypeName);
    }

    @Override
    public void handleNpcSkinUpdate() {
        sendDataToServer(SKIN_SETTINGS_TAG, skinSettings.serializeNBT());
    }

    @Override
    public NpcSkinSettings getSkinSettings() {
        return skinSettings;
    }

    @Override
    public void setSkinSettings(NpcSkinSettings skinSettings) {
        this.skinSettings = skinSettings;
    }
}


