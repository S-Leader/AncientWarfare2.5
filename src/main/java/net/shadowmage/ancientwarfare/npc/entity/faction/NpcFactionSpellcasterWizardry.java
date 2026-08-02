package net.shadowmage.ancientwarfare.npc.entity.faction;


import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.shadowmage.ancientwarfare.core.init.AWMenuTypes;
import net.shadowmage.ancientwarfare.core.network.NetworkHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Wizardry Redux-aware spellcaster shell.
 * <p>
 * Redux changed its public Java API completely on 1.20.1. Spell choices are
 * therefore stored as stable registry ids; the optional Redux adapter can
 * resolve those ids without leaking alpha API classes into the NPC module.
 */
public class NpcFactionSpellcasterWizardry extends NpcFactionSpellcaster {
    private final List<ResourceLocation> spells = new ArrayList<>();
    private int healCooldown = -1;

    public NpcFactionSpellcasterWizardry(Level level) {
        super(level);
    }

    public NpcFactionSpellcasterWizardry(Level level, String factionName) {
        super(level, factionName);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (level().isClientSide || !isAlive()) return;
        if (healCooldown > 0) {
            healCooldown--;
        } else if (healCooldown == 0 && getHealth() < getMaxHealth()) {
            heal(4.0F);
            healCooldown = -1;
        } else if (healCooldown < 0) {
            healCooldown = getHealth() < 25.0F ? 150 : 400;
        }
    }

    public List<ResourceLocation> getSpells() {
        return new ArrayList<>(spells);
    }

    public void setSpells(List<ResourceLocation> spellIds) {
        spells.clear();
        for (ResourceLocation id : spellIds) if (id != null) spells.add(id);
    }

    @Override
    public boolean hasAltGui() {
        return true;
    }

    @Override
    public void openAltGui(Player player) {
        AWMenuTypes.open(player, NetworkHandler.GUI_NPC_FACTION_SPELLCASTER_WIZARDRY, getId(), 0, 0);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        writeSpellData(tag);
        tag.putInt("wizardryHealCooldown", healCooldown);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        readSpellData(tag);
        healCooldown = tag.getInt("wizardryHealCooldown");
    }

    @Override
    public void writeAdditionalItemData(CompoundTag tag) {
        super.writeAdditionalItemData(tag);
        writeSpellData(tag);
    }

    @Override
    public void readAdditionalItemData(CompoundTag tag) {
        super.readAdditionalItemData(tag);
        readSpellData(tag);
    }

    private void writeSpellData(CompoundTag tag) {
        ListTag list = new ListTag();
        for (ResourceLocation spell : spells) list.add(StringTag.valueOf(spell.toString()));
        tag.put("spells", list);
    }

    private void readSpellData(CompoundTag tag) {
        spells.clear();
        ListTag list = tag.getList("spells", Tag.TAG_STRING);
        for (Tag value : list) {
            ResourceLocation id = ResourceLocation.tryParse(value.getAsString());
            if (id != null) spells.add(id);
        }
    }
}
