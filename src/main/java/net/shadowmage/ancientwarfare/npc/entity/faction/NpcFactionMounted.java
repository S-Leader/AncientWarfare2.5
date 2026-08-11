package net.shadowmage.ancientwarfare.npc.entity.faction;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.PolarBear;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.animal.horse.SkeletonHorse;
import net.minecraft.world.entity.animal.horse.ZombieHorse;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.level.Level;
import net.shadowmage.ancientwarfare.npc.AncientWarfareNPC;
import net.shadowmage.ancientwarfare.npc.ai.faction.NpcAIFactionRideHorse;
import net.shadowmage.ancientwarfare.npc.entity.faction.attributes.AdditionalAttributes;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

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

    private static final Map<Class<? extends Mob>, EntityType<? extends Mob>> VANILLA_MOUNT_TYPES = Map.of(
            Horse.class, EntityType.HORSE,
            SkeletonHorse.class, EntityType.SKELETON_HORSE,
            ZombieHorse.class, EntityType.ZOMBIE_HORSE,
            Llama.class, EntityType.LLAMA,
            Chicken.class, EntityType.CHICKEN,
            Pig.class, EntityType.PIG,
            PolarBear.class, EntityType.POLAR_BEAR,
            Spider.class, EntityType.SPIDER
    );

    @Override
    public Mob instantiateMountedEntity() {
        //noinspection unchecked
        Class<? extends Mob> clazz = (Class<? extends Mob>) getAdditionalAttributeValue(AdditionalAttributes.HORSE_ENTITY)
                .orElse(Horse.class);

        /*
         * 1.20 entities are constructed by EntityType factories. Vanilla Horse no
         * longer has the old (Level) constructor, so reflecting that constructor
         * always throws NoSuchMethodException. The faction config's legacy mount
         * classes are mapped to their real modern EntityTypes first.
         */
        EntityType<? extends Mob> vanillaType = VANILLA_MOUNT_TYPES.get(clazz);
        if (vanillaType != null) {
            Entity created = vanillaType.create(world);
            if (clazz.isInstance(created)) {
                return clazz.cast(created);
            }
            AncientWarfareNPC.LOG.error("EntityType {} did not create configured mount class {}", vanillaType, clazz.getName());
        }

        /*
         * Retain compatibility with add-ons that still expose AW's legacy Level
         * constructor. Modern third-party mounts should be supplied through a
         * dedicated EntityType-aware integration rather than being constructed
         * with a fake vanilla EntityType.
         */
        try {
            Constructor<? extends Mob> ctr = clazz.getConstructor(Level.class);
            return ctr.newInstance(world);
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException |
                 InvocationTargetException e) {
            AncientWarfareNPC.LOG.error("Error instantiating configured mount entity for class: {}", clazz.getName(), e);
        }

        Mob fallback = EntityType.HORSE.create(world);
        return fallback != null ? fallback : new Horse(EntityType.HORSE, world);
    }

    @Override
    public void die(DamageSource damageSource) {
        horseAI.onKilled();
        super.die(damageSource);
    }
}
