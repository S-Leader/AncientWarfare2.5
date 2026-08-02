package net.shadowmage.ancientwarfare.npc.entity.faction;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.level.Level;
import net.shadowmage.ancientwarfare.npc.AncientWarfareNPC;
import net.shadowmage.ancientwarfare.npc.ai.faction.NpcAIFactionRideHorse;
import net.shadowmage.ancientwarfare.npc.entity.faction.attributes.AdditionalAttributes;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public abstract class NpcFactionMounted extends NpcFaction implements IHorseMountedNpc {
    private boolean horseLives = true;
    private NpcAIFactionRideHorse horseAI = new NpcAIFactionRideHorse<>(this);

    @Override
    public boolean isHorseAlive() {
        return horseLives;
    }

    @Override
    public void setHorseKilled() {
        horseLives = false;
    }

    public NpcFactionMounted(Level world) {
        super(world);
        tasks.addTask(0, horseAI);
    }

    public NpcFactionMounted(Level world, String factionName) {
        super(world, factionName);
        tasks.addTask(0, horseAI);
    }

    @Override
    public void checkDespawn() {
        super.checkDespawn();
        if (isRemoved() && getVehicle() instanceof Horse) {
            getVehicle().discard();
            stopRiding();
        }
    }

    @Override
    protected void onRepack() {
        if (getVehicle() instanceof Horse) {
            getVehicle().discard();
            stopRiding();
        }
    }

    @Override
    public boolean worksInRain() {
        return true;
    }

    @Override
    public boolean isPassive() {
        return false;
    }

    @Override
    public boolean shouldSleep() {
        return false;
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        horseLives = tag.getBoolean("horseLives");
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("horseLives", horseLives);
    }

    @Override
    public Mob instantiateMountedEntity() {
        //noinspection unchecked
        Class<Mob> clazz = (Class<Mob>) getAdditionalAttributeValue(AdditionalAttributes.HORSE_ENTITY).orElse(Horse.class);
        try {
            Constructor<Mob> ctr = clazz.getConstructor(Level.class);
            return ctr.newInstance(world);
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException |
                 InvocationTargetException e) {
            AncientWarfareNPC.LOG.error("Error instantiating horse entity for class: " + clazz.toString(), e);
        }
        return new Horse(EntityType.HORSE, world);
    }

    @Override
    public void die(DamageSource damageSource) {
        horseAI.onKilled();
        super.die(damageSource);
    }
}
