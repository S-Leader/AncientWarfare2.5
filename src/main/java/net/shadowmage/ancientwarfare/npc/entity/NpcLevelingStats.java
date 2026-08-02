package net.shadowmage.ancientwarfare.npc.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.shadowmage.ancientwarfare.core.util.Constants;
import net.shadowmage.ancientwarfare.npc.config.AWNPCStatics;

import java.util.HashMap;

public class NpcLevelingStats {

    private final HashMap<String, ExperienceEntry> experienceMap = new HashMap<>();
    private int xp;//'generic' xp, always incremented for all xp-types
    private int level;
    private final NpcBase npc;

    public NpcLevelingStats(NpcBase npc) {
        this.npc = npc;
    }

    public int getExperience() {
        String type = npc.getNpcFullType();
        if (experienceMap.containsKey(type)) {
            return experienceMap.get(type).xp;
        }
        return 0;
    }

    public int getLevel() {
        String type = npc.getNpcFullType();
        if (experienceMap.containsKey(type)) {
            return experienceMap.get(type).level;
        }
        return 0;
    }

    public int getBaseExperience() {
        return xp;
    }

    public int getBaseLevel() {
        return level;
    }

    public void addExperience(int xpGained) {
        if (npc.level().isClientSide) {
            return;
        }
        String type = npc.getNpcFullType();
        if (!experienceMap.containsKey(type)) {
            experienceMap.put(type, new ExperienceEntry());
        }
        ExperienceEntry entry = experienceMap.get(type);
        entry.xp += xpGained;
        while (entry.level < (npc instanceof NpcCombat ? AWNPCStatics.maxNpcCombatLevel : AWNPCStatics.maxNpcWorkerLevel) && entry.xp >= getXPToLevel(entry.level + 1)) {
            entry.xp -= getXPToLevel(entry.level + 1);
            entry.level++;
            onSubLevelGained(entry.level);
        }
        this.xp += xpGained;
        while (level < (npc instanceof NpcCombat ? AWNPCStatics.maxNpcCombatLevel : AWNPCStatics.maxNpcWorkerLevel) && this.xp >= getXPToLevel(level)) {
            this.xp -= getXPToLevel(level);
            onBaseLevelGained(level + 1);
        }
    }

    private void onBaseLevelGained(int newLevel) {
        level = newLevel;
        if (newLevel <= (npc instanceof NpcCombat ? AWNPCStatics.maxNpcCombatLevel : AWNPCStatics.maxNpcWorkerLevel)) {
            if (npc.getMaxHealthOverride() <= 0) {
                double health = npc.getNpcDefault().getBaseHealth() + newLevel;
                npc.getAttribute(Attributes.MAX_HEALTH).setBaseValue(health);
            }
            npc.updateDamageFromLevel();
        }
    }

    private void onSubLevelGained(int newLevel) {
        npc.updateDamageFromLevel();
    }

    private int getXPToLevel(int level) {
        return (level + 3) * (level + 3);
    }

    public CompoundTag writeToNBT(CompoundTag tag) {
        tag.putInt("xp", xp);
        tag.putInt("level", level);
        ListTag entryList = new ListTag();
        CompoundTag xpTag;
        for (String key : this.experienceMap.keySet()) {
            xpTag = new CompoundTag();
            xpTag.putString("type", key);
            xpTag.putInt("xp", experienceMap.get(key).xp);
            xpTag.putInt("level", experienceMap.get(key).level);
            entryList.add(xpTag);
        }
        tag.put("entryList", entryList);
        return tag;
    }

    public void readFromNBT(CompoundTag tag) {
        experienceMap.clear();
        xp = tag.getInt("xp");
        level = tag.getInt("level");
        ListTag entryList = tag.getList("entryList", Constants.NBT.TAG_COMPOUND);
        CompoundTag xpTag;
        ExperienceEntry entry;
        for (int i = 0; i < entryList.size(); i++) {
            xpTag = entryList.getCompound(i);
            entry = new ExperienceEntry();
            entry.xp = xpTag.getInt("xp");
            entry.level = xpTag.getInt("level");
            experienceMap.put(xpTag.getString("type"), entry);
        }
    }

    private class ExperienceEntry {
        int xp;
        int level;
    }

}
