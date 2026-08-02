package net.shadowmage.ancientwarfare.structure.tile;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.shadowmage.ancientwarfare.core.util.Constants;
import net.shadowmage.ancientwarfare.structure.util.PotionHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LootSettings {
    private boolean hasLoot = false;
    private ResourceLocation lootTableName = null;
    private int lootRolls = 1;
    private boolean splashPotion = false;
    private List<MobEffectInstance> effects = new ArrayList<>();
    private boolean spawnEntity = false;
    private ResourceLocation entity = null;
    private CompoundTag entityNBT = new CompoundTag();
    private boolean hasMessage = false;
    private String playerMessage = "";

    public CompoundTag serializeNBT() {
        CompoundTag ret = new CompoundTag();

        ret.putBoolean("hasLoot", hasLoot);
        if (lootTableName != null) {
            ret.putString("lootTableName", lootTableName.toString());
            ret.putInt("lootRolls", lootRolls);
        }
        ret.putBoolean("splashPotion", splashPotion);
        if (!effects.isEmpty()) {
            ListTag effectList = new ListTag();
            for (MobEffectInstance potioneffect : effects) {
                effectList.add(PotionHelper.writeCustomPotionEffectToNBT(potioneffect));
            }
            ret.put("effects", effectList);
        }
        ret.putBoolean("spawnEntity", spawnEntity);
        if (entity != null) {
            ret.putString("entity", entity.toString());
            if (!entityNBT.isEmpty()) {
                ret.put("entityNBT", entityNBT);
            }
        }
        ret.putBoolean("hasMessage", hasMessage);
        if (!playerMessage.isEmpty()) {
            ret.putString("playerMessage", playerMessage);
        }

        return ret;
    }

    public static LootSettings deserializeNBT(CompoundTag nbt) {
        LootSettings lootSettings = new LootSettings();
        lootSettings.hasLoot = nbt.getBoolean("hasLoot");
        lootSettings.lootTableName = new ResourceLocation(nbt.getString("lootTableName"));
        lootSettings.lootRolls = nbt.getInt("lootRolls");
        lootSettings.splashPotion = nbt.getBoolean("splashPotion");
        ListTag effectList = nbt.getList("effects", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < effectList.size(); i++) {
            PotionHelper.readCustomPotionEffectFromNBT(effectList.getCompound(i)).ifPresent(lootSettings.effects::add);
        }
        lootSettings.spawnEntity = nbt.getBoolean("spawnEntity");
        lootSettings.entity = new ResourceLocation(nbt.getString("entity"));
        lootSettings.entityNBT = nbt.getCompound("entityNBT");
        lootSettings.hasMessage = nbt.getBoolean("hasMessage");
        lootSettings.playerMessage = nbt.getString("playerMessage");

        return lootSettings;
    }

    public void transferToContainer(ISpecialLootContainer container) {
        LootSettings applicableSettings = new LootSettings();

        if (hasLoot()) {
            applicableSettings.setHasLoot(true);
            applicableSettings.setLootTableName(lootTableName);
            applicableSettings.setLootRolls(lootRolls);
        }
        if (splashPotion) {
            applicableSettings.setSplashPotion(true);
            applicableSettings.setEffects(effects);
        }
        if (spawnEntity) {
            applicableSettings.setSpawnEntity(true);
            applicableSettings.setEntity(entity);
            applicableSettings.setEntityNBT(entityNBT);
        }

        if (hasMessage) {
            applicableSettings.setHasMessage(true);
            applicableSettings.setPlayerMessage(playerMessage);
        }
        container.setLootSettings(applicableSettings);
    }

    public LootSettings transferFromContainer(ISpecialLootContainer te) {
        LootSettings lootSettings = te.getLootSettings();

        if (lootSettings.hasLoot) {
            hasLoot = true;
            lootTableName = lootSettings.lootTableName;
            lootRolls = lootSettings.lootRolls;
        } else {
            hasLoot = false;
        }

        if (lootSettings.splashPotion) {
            splashPotion = true;
            effects = lootSettings.effects;
        } else {
            splashPotion = false;
        }

        if (lootSettings.spawnEntity) {
            spawnEntity = true;
            entity = lootSettings.entity;
            entityNBT = lootSettings.entityNBT;
        } else {
            spawnEntity = false;
        }

        if (lootSettings.hasMessage) {
            hasMessage = true;
            playerMessage = lootSettings.playerMessage;
        }

        return this;
    }

    public boolean hasLoot() {
        return hasLoot;
    }

    public boolean hasMessage() {
        return hasMessage;
    }

    public Optional<ResourceLocation> getLootTableName() {
        return Optional.ofNullable(lootTableName);
    }

    public void removeLoot() {
        hasLoot = false;
        lootTableName = null;
        lootRolls = 1;
    }

    public int getLootRolls() {
        return lootRolls;
    }

    public void setLootTableName(ResourceLocation lootTableName) {
        this.lootTableName = lootTableName;
    }

    public void setLootRolls(int lootRolls) {
        this.lootRolls = lootRolls;
    }

    public void setHasLoot(boolean hasLoot) {
        this.hasLoot = hasLoot;
    }

    public boolean getHasLoot() {
        return hasLoot;
    }

    public void setSplashPotion(boolean splashPotion) {
        this.splashPotion = splashPotion;
    }

    public boolean getSplashPotion() {
        return splashPotion;
    }

    public void setSpawnEntity(boolean spawnEntity) {
        this.spawnEntity = spawnEntity;
    }

    public void setEffects(List<MobEffectInstance> effects) {
        this.effects = effects;
    }

    public void setEntity(ResourceLocation entity) {
        this.entity = entity;
    }

    public void setHasMessage(boolean hasMessage) {
        this.hasMessage = hasMessage;
    }

    public void setPlayerMessage(String playerMessage) {
        this.playerMessage = playerMessage;
    }

    public void setEntityNBT(CompoundTag entityNBT) {
        this.entityNBT = entityNBT;
    }

    public List<MobEffectInstance> getEffects() {
        return effects;
    }

    public boolean getSpawnEntity() {
        return spawnEntity;
    }

    public CompoundTag getEntityNBT() {
        return entityNBT;
    }

    public ResourceLocation getEntity() {
        return entity;
    }

    public String getPlayerMessage() {
        return playerMessage;
    }

    public boolean hasLootToSpawn() {
        return spawnEntity || splashPotion || hasLoot || hasMessage;
    }
}
