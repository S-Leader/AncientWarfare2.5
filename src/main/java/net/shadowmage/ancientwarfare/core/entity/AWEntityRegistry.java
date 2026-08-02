package net.shadowmage.ancientwarfare.core.entity;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegisterEvent;
import net.shadowmage.ancientwarfare.core.AncientWarfareCore;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Dynamic entity declaration registry used by all five AW modules.
 * Numeric entity ids and EntityRegistry were removed after 1.12; declarations
 * are now materialized as EntityType entries during Forge's RegisterEvent.
 */
public final class AWEntityRegistry {
    private AWEntityRegistry() {
    }

    public static final String NPC_WORKER = "aw_npc_worker";
    public static final String NPC_COMBAT = "aw_npc_combat";
    public static final String NPC_COURIER = "aw_npc_courier";
    public static final String NPC_TRADER = "aw_npc_trader";
    public static final String NPC_PRIEST = "aw_npc_priest";
    public static final String NPC_BARD = "aw_npc_bard";
    public static final String NPC_SIEGE_ENGINEER = "aw_npc_siege_engineer";
    public static final String NPC_FACTION_ARCHER = "faction.archer";
    public static final String NPC_FACTION_SOLDIER = "faction.soldier";
    public static final String NPC_FACTION_PRIEST = "faction.priest";
    public static final String NPC_FACTION_TRADER = "faction.trader";
    public static final String NPC_FACTION_COMMANDER = "faction.leader";
    public static final String NPC_FACTION_CAVALRY = "faction.cavalry";
    public static final String NPC_FACTION_MOUNTED_ARCHER = "faction.mounted_archer";
    public static final String NPC_FACTION_CIVILIAN_MALE = "faction.civilian.male";
    public static final String NPC_FACTION_ARCHER_ELITE = "faction.archer.elite";
    public static final String NPC_FACTION_SOLDIER_ELITE = "faction.soldier.elite";
    public static final String NPC_FACTION_LEADER_ELITE = "faction.leader.elite";
    public static final String NPC_FACTION_CIVILIAN_FEMALE = "faction.civilian.female";
    public static final String NPC_FACTION_BARD = "faction.bard";
    public static final String NPC_FACTION_SIEGE_ENGINEER = "faction.siege_engineer";
    public static final String NPC_FACTION_SPELLCASTER = "faction.spellcaster";
    public static final String VEHICLE = "vehicle";
    public static final String MISSILE = "missile";
    public static final String AW_GATES = "aw_gate";
    public static final String SEAT = "seat";

    private static final Map<String, List<EntityDeclaration>> DECLARATIONS = new LinkedHashMap<>();
    private static final Map<ResourceLocation, EntityType<?>> REGISTERED_TYPES = new LinkedHashMap<>();
    private static final ThreadLocal<EntityType<?>> CONSTRUCTION_TYPE = new ThreadLocal<>();

    /**
     * Returns the real registered type while a legacy Level-only constructor is running.
     */
    public static EntityType<?> currentConstructionType() {
        EntityType<?> type = CONSTRUCTION_TYPE.get();
        if (type == null) {
            throw new IllegalStateException("Ancient Warfare entity constructed outside its EntityType factory");
        }
        return type;
    }

    /**
     * Runs a factory that ends up in a legacy Level-only constructor with the construction type set.
     */
    public static <T> T constructWithType(EntityType<?> type, java.util.function.Supplier<T> factory) {
        EntityType<?> previous = CONSTRUCTION_TYPE.get();
        CONSTRUCTION_TYPE.set(type);
        try {
            return factory.get();
        } finally {
            restoreConstructionType(previous);
        }
    }

    private static void restoreConstructionType(@Nullable EntityType<?> previous) {
        if (previous == null) {
            CONSTRUCTION_TYPE.remove();
        } else {
            CONSTRUCTION_TYPE.set(previous);
        }
    }

    public static synchronized void registerEntity(EntityDeclaration declaration) {
        DECLARATIONS.computeIfAbsent(declaration.modID, ignored -> new ArrayList<>()).add(declaration);
    }

    public static void attachRegisterListener(IEventBus modBus, String modId) {
        modBus.addListener((RegisterEvent event) -> register(event, modId));
        modBus.addListener((net.minecraftforge.event.entity.EntityAttributeCreationEvent event) -> registerAttributes(event, modId));
    }

    /**
     * 1.12 registered attributes per-instance in applyEntityAttributes; 1.20 requires
     * a per-EntityType supplier or living entities crash on construction. Values are
     * baseline only — NpcDefault applies the configured per-type stats at spawn.
     */
    @SuppressWarnings("unchecked")
    private static void registerAttributes(net.minecraftforge.event.entity.EntityAttributeCreationEvent event, String modId) {
        for (EntityDeclaration declaration : DECLARATIONS.getOrDefault(modId, List.of())) {
            if (declaration.registeredType != null
                    && net.minecraft.world.entity.LivingEntity.class.isAssignableFrom(declaration.entityClass)) {
                event.put((EntityType<? extends net.minecraft.world.entity.LivingEntity>) declaration.registeredType,
                        Mob.createMobAttributes()
                                .add(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE)
                                .add(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_SPEED)
                                .build());
            }
        }
    }

    private static void register(RegisterEvent event, String modId) {
        event.register(Registries.ENTITY_TYPE, helper -> {
            for (EntityDeclaration declaration : DECLARATIONS.getOrDefault(modId, List.of())) {
                ResourceLocation id = new ResourceLocation(modId, declaration.entityName);
                EntityType<Entity> type = EntityType.Builder
                        .<Entity>of((entityType, level) -> declaration.createEntity(entityType, level), declaration.category())
                        .sized(declaration.width(), declaration.height())
                        .clientTrackingRange(declaration.trackingRange())
                        .updateInterval(declaration.updateFrequency())
                        .setShouldReceiveVelocityUpdates(declaration.sendsVelocityUpdates())
                        .build(id.toString());
                helper.register(id, type);
                declaration.registeredType = type;
                REGISTERED_TYPES.put(id, type);
            }
        });
    }

    @Nullable
    public static EntityType<?> getType(ResourceLocation id) {
        return REGISTERED_TYPES.get(id);
    }

    /**
     * Creates one of AW's dynamically registered entities through its real
     * EntityType. Legacy gameplay code must use this instead of invoking the
     * old Level-only constructor directly.
     */
    @Nullable
    public static <T extends Entity> T createEntity(ResourceLocation id, Level level, Class<T> expectedClass) {
        EntityType<?> type = getType(id);
        if (type == null) {
            AncientWarfareCore.LOG.error("No Ancient Warfare entity type is registered for {}", id);
            return null;
        }
        Entity entity = type.create(level);
        if (!expectedClass.isInstance(entity)) {
            AncientWarfareCore.LOG.error("Entity type {} created {} instead of {}", id,
                    entity == null ? "null" : entity.getClass().getName(), expectedClass.getName());
            return null;
        }
        return expectedClass.cast(entity);
    }

    public abstract static class EntityDeclaration {
        private final Class<? extends Entity> entityClass;
        private final String entityName;
        @SuppressWarnings("unused")
        private final int legacyNumericId;
        private final String modID;
        private EntityType<?> registeredType;

        protected EntityDeclaration(Class<? extends Entity> entityClass, String entityName, int legacyNumericId, String modID) {
            this.entityClass = entityClass;
            this.entityName = entityName;
            this.legacyNumericId = legacyNumericId;
            this.modID = modID;
        }

        @Nullable
        protected Entity createEntity(EntityType<?> type, Level level) {
            try {
                Constructor<? extends Entity> modern = entityClass.getConstructor(EntityType.class, Level.class);
                return modern.newInstance(type, level);
            } catch (NoSuchMethodException ignored) {
                try {
                    Constructor<? extends Entity> legacy = entityClass.getConstructor(Level.class);
                    EntityType<?> previous = CONSTRUCTION_TYPE.get();
                    CONSTRUCTION_TYPE.set(type);
                    try {
                        return legacy.newInstance(level);
                    } finally {
                        restoreConstructionType(previous);
                    }
                } catch (ReflectiveOperationException legacyFailure) {
                    logConstructionFailure(legacyFailure);
                    return null;
                }
            } catch (ReflectiveOperationException modernFailure) {
                logConstructionFailure(modernFailure);
                return null;
            }
        }

        private void logConstructionFailure(ReflectiveOperationException failure) {
            Throwable cause = failure instanceof InvocationTargetException invocation && invocation.getCause() != null
                    ? invocation.getCause()
                    : failure;
            AncientWarfareCore.LOG.error("Couldn't create entity {}", entityClass.getName(), cause);
        }

        public String name() {
            return entityName;
        }

        public abstract Object mod();

        public abstract int trackingRange();

        public abstract int updateFrequency();

        public abstract boolean sendsVelocityUpdates();

        public MobCategory category() {
            return Mob.class.isAssignableFrom(entityClass) ? MobCategory.CREATURE : MobCategory.MISC;
        }

        public float width() {
            return Mob.class.isAssignableFrom(entityClass) ? 0.6F : 1.0F;
        }

        public float height() {
            return Mob.class.isAssignableFrom(entityClass) ? 1.95F : 1.0F;
        }

        public Class<? extends Entity> getEntityClass() {
            return entityClass;
        }

        @Nullable
        public EntityType<?> getRegisteredType() {
            return registeredType;
        }
    }
}
