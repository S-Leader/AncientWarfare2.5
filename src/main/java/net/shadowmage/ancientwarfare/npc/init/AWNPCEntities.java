package net.shadowmage.ancientwarfare.npc.init;

import com.google.common.collect.ImmutableList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.shadowmage.ancientwarfare.core.config.AWCoreStatics;
import net.shadowmage.ancientwarfare.core.init.AWCoreItems;
import net.shadowmage.ancientwarfare.npc.AncientWarfareNPC;
import net.shadowmage.ancientwarfare.npc.entity.*;
import net.shadowmage.ancientwarfare.npc.entity.faction.*;
import net.shadowmage.ancientwarfare.npc.entity.vehicle.NpcSiegeEngineer;

import javax.annotation.Nullable;
import java.util.*;

/** Modern Forge entity registration for all Ancient Warfare NPCs. */
public final class AWNPCEntities {
    private AWNPCEntities() {
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

    private static final String COMBAT_TYPE = "combat";
    private static final String SOLDIER_SUBTYPE = "soldier";
    private static final String COMMANDER_SUBTYPE = "commander";
    private static final String ARCHER_SUBTYPE = "archer";
    private static final String WORKER_TYPE = "worker";
    private static final String MINER_SUBTYPE = "miner";
    private static final String SPELLCASTER_SUBTYPE = "spellcaster";

    private static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, AncientWarfareNPC.MOD_ID);
    private static final Map<String, NpcDeclaration<?>> NPC_MAP = new LinkedHashMap<>();
    private static final Map<String, RegistryObject<? extends EntityType<? extends NpcBase>>> TYPES_BY_REGISTRY_NAME = new LinkedHashMap<>();
    private static boolean prepared;

    public static synchronized void register(IEventBus modBus) {
        if (!prepared) {
            addPlayerOwnedNpcs();
            addFaction();
            prepared = true;
        }
        ENTITIES.register(modBus);
        modBus.addListener(AWNPCEntities::registerAttributes);
    }

    private static void registerAttributes(EntityAttributeCreationEvent event) {
        for (NpcDeclaration<?> declaration : NPC_MAP.values()) {
            event.put(declaration.getEntityType(), Mob.createMobAttributes()
                    .add(Attributes.ATTACK_DAMAGE)
                    .add(Attributes.ATTACK_SPEED)
                    .build());
        }
    }

    private static void addPlayerOwnedNpcs() {
        NpcDeclaration<NpcCombat> reg = new NpcDeclaration<>(NpcCombat.class, NPC_COMBAT, NpcCombat::new, COMBAT_TYPE, SOLDIER_SUBTYPE);
        reg.addSubTypes(COMMANDER_SUBTYPE, SOLDIER_SUBTYPE, ARCHER_SUBTYPE, "medic", "engineer");
        addNpcRegistration(reg);

        NpcDeclaration<NpcWorker> worker = new NpcDeclaration<>(NpcWorker.class, NPC_WORKER, NpcWorker::new, WORKER_TYPE, MINER_SUBTYPE);
        worker.addSubTypes(MINER_SUBTYPE, "farmer", "lumberjack", "researcher", "craftsman");
        addNpcRegistration(worker);

        addNpcRegistration(new NpcDeclaration<>(NpcCourier.class, NPC_COURIER, NpcCourier::new, "courier"));
        addNpcRegistration(new NpcDeclaration<>(NpcTrader.class, NPC_TRADER, NpcTrader::new, "trader"));
        addNpcRegistration(new NpcDeclaration<>(NpcPriest.class, NPC_PRIEST, NpcPriest::new, "priest"));
        addNpcRegistration(new NpcDeclaration<>(NpcBard.class, NPC_BARD, NpcBard::new, "bard"));
        addNpcRegistration(new NpcDeclaration<>(NpcSiegeEngineer.class, NPC_SIEGE_ENGINEER, NpcSiegeEngineer::new, "siege_engineer"));
    }

    private static void addFaction() {
        addNpcRegistration(new NpcFactionDeclaration<>(NpcFactionArcher.class, NPC_FACTION_ARCHER, NpcFactionArcher::new, ARCHER_SUBTYPE));
        addNpcRegistration(new NpcFactionDeclaration<>(NpcFactionSoldier.class, NPC_FACTION_SOLDIER, NpcFactionSoldier::new, SOLDIER_SUBTYPE));
        addNpcRegistration(new NpcFactionDeclaration<>(NpcFactionLeader.class, NPC_FACTION_COMMANDER, NpcFactionLeader::new, COMMANDER_SUBTYPE));
        addNpcRegistration(new NpcFactionDeclaration<>(NpcFactionPriest.class, NPC_FACTION_PRIEST, NpcFactionPriest::new, "priest"));
        addNpcRegistration(new NpcFactionDeclaration<>(NpcFactionTrader.class, NPC_FACTION_TRADER, NpcFactionTrader::new, "trader"));
        addNpcRegistration(new NpcFactionDeclaration<>(NpcFactionMountedSoldier.class, NPC_FACTION_CAVALRY, NpcFactionMountedSoldier::new, SOLDIER_SUBTYPE));
        addNpcRegistration(new NpcFactionDeclaration<>(NpcFactionMountedArcher.class, NPC_FACTION_MOUNTED_ARCHER, NpcFactionMountedArcher::new, ARCHER_SUBTYPE));
        addNpcRegistration(new NpcFactionDeclaration<>(NpcFactionCivilianMale.class, NPC_FACTION_CIVILIAN_MALE, NpcFactionCivilianMale::new, "civilian_male"));
        addNpcRegistration(new NpcFactionDeclaration<>(NpcFactionCivilianFemale.class, NPC_FACTION_CIVILIAN_FEMALE, NpcFactionCivilianFemale::new, "civilian_female"));
        addNpcRegistration(new NpcFactionDeclaration<>(NpcFactionArcherElite.class, NPC_FACTION_ARCHER_ELITE, NpcFactionArcherElite::new, ARCHER_SUBTYPE));
        addNpcRegistration(new NpcFactionDeclaration<>(NpcFactionSoldierElite.class, NPC_FACTION_SOLDIER_ELITE, NpcFactionSoldierElite::new, SOLDIER_SUBTYPE));
        addNpcRegistration(new NpcFactionDeclaration<>(NpcFactionLeaderElite.class, NPC_FACTION_LEADER_ELITE, NpcFactionLeaderElite::new, COMMANDER_SUBTYPE));
        addNpcRegistration(new NpcFactionDeclaration<>(NpcFactionBard.class, NPC_FACTION_BARD, NpcFactionBard::new, "bard"));
        addNpcRegistration(new NpcFactionDeclaration<>(NpcFactionSiegeEngineer.class, NPC_FACTION_SIEGE_ENGINEER, NpcFactionSiegeEngineer::new, "siege_engineer"));
        registerSpellcasterFactionNpc();
    }

    private static void registerSpellcasterFactionNpc() {
        if (ModList.get().isLoaded("ebwizardry")) {
            addNpcRegistration(WizardryRegistration.create());
        } else {
            addNpcRegistration(new NpcFactionDeclaration<>(NpcFactionSpellcaster.class, NPC_FACTION_SPELLCASTER,
                    NpcFactionSpellcaster::new, SPELLCASTER_SUBTYPE));
        }
    }

    /** Isolates the optional Wizardry class from normal AWNPCEntities class loading. */
    private static final class WizardryRegistration {
        private WizardryRegistration() {}

        private static NpcFactionDeclaration<NpcFactionSpellcasterWizardry> create() {
            return new NpcFactionDeclaration<>(NpcFactionSpellcasterWizardry.class, NPC_FACTION_SPELLCASTER,
                    NpcFactionSpellcasterWizardry::new, SPELLCASTER_SUBTYPE);
        }
    }

    public static void loadNpcSubtypeEquipment() {
        addNpcSubtypeEquipment(WORKER_TYPE, "farmer", new ItemStack(Items.IRON_HOE));
        addNpcSubtypeEquipment(WORKER_TYPE, MINER_SUBTYPE, new ItemStack(Items.IRON_PICKAXE));
        addNpcSubtypeEquipment(WORKER_TYPE, "lumberjack", new ItemStack(Items.IRON_AXE));
        addNpcSubtypeEquipment(WORKER_TYPE, "researcher", new ItemStack(AWCoreItems.IRON_QUILL.get()));
        addNpcSubtypeEquipment(WORKER_TYPE, "craftsman", new ItemStack(AWCoreItems.IRON_HAMMER.get()));

        addNpcSubtypeEquipment(COMBAT_TYPE, COMMANDER_SUBTYPE, new ItemStack(AWNPCItems.IRON_COMMAND_BATON.get()));
        addNpcSubtypeEquipment(COMBAT_TYPE, SOLDIER_SUBTYPE, new ItemStack(Items.IRON_SWORD));
        addNpcSubtypeEquipment(COMBAT_TYPE, ARCHER_SUBTYPE, new ItemStack(Items.BOW));
        addNpcSubtypeEquipment(COMBAT_TYPE, "engineer", new ItemStack(AWCoreItems.IRON_HAMMER.get()));
        Item medicItem = ForgeRegistries.ITEMS.getValue(AWCoreStatics.medicItems.get(0));
        addNpcSubtypeEquipment(COMBAT_TYPE, "medic", new ItemStack(medicItem == null ? Items.GOLDEN_APPLE : medicItem));
    }

    private static <T extends NpcBase> void addNpcRegistration(NpcDeclaration<T> declaration) {
        NPC_MAP.put(declaration.getNpcType(), declaration);
        TYPES_BY_REGISTRY_NAME.put(declaration.getRegistryName(), declaration.getEntityTypeObject());
    }

    @Nullable
    public static NpcBase createNpc(Level world, String npcType, String npcSubtype, String faction) {
        NpcDeclaration<?> declaration = NPC_MAP.get(npcType);
        return declaration == null ? null : declaration.createEntity(world, npcSubtype, faction);
    }

    private static void addNpcSubtypeEquipment(String npcType, String npcSubtype, ItemStack equipment) {
        NpcDeclaration<?> declaration = NPC_MAP.get(npcType);
        if (declaration == null) {
            throw new IllegalArgumentException("npc type must first be mapped");
        }
        declaration.addSubtypeEquipment(npcSubtype, equipment);
    }

    public static Map<String, NpcDeclaration<?>> getNpcMap() {
        return Collections.unmodifiableMap(NPC_MAP);
    }

    @Nullable
    public static NpcDeclaration<?> getNpcDeclaration(String npcType) {
        return NPC_MAP.get(npcType);
    }

    @Nullable
    public static EntityType<? extends NpcBase> getEntityType(String registryName) {
        RegistryObject<? extends EntityType<? extends NpcBase>> object = TYPES_BY_REGISTRY_NAME.get(registryName);
        return object == null ? null : object.get();
    }

    public static class NpcDeclaration<T extends NpcBase> {
        private final Class<T> entityClass;
        private final String registryName;
        private final String itemModelVariant;
        private boolean spawnBaseEntity = true;
        private final String npcType;
        private final HashMap<String, ItemStack> spawnEquipment = new HashMap<>();
        private final RegistryObject<EntityType<T>> entityType;

        public NpcDeclaration(Class<T> entityClass, String registryName, EntityType.EntityFactory<T> factory, String npcType) {
            this(entityClass, registryName, factory, npcType, npcType.replace('.', '_'));
        }

        public NpcDeclaration(Class<T> entityClass, String registryName, EntityType.EntityFactory<T> factory,
                              String npcType, String itemModelVariant) {
            this.entityClass = entityClass;
            this.registryName = registryName;
            this.npcType = npcType;
            this.itemModelVariant = itemModelVariant;
            this.entityType = ENTITIES.register(registryName, () -> EntityType.Builder
                    .of(factory, MobCategory.CREATURE)
                    .sized(0.6F, 1.95F)
                    .clientTrackingRange(120)
                    .updateInterval(3)
                    .setShouldReceiveVelocityUpdates(true)
                    .build(AncientWarfareNPC.MOD_ID + ":" + registryName));
        }

        public String getFaction() {
            return "";
        }

        private void addSubTypes(String... subTypes) {
            Arrays.stream(subTypes).forEach(s -> addSubtypeEquipment(s, ItemStack.EMPTY));
        }

        private void addSubtypeEquipment(String type, ItemStack equipment) {
            spawnEquipment.put(type, equipment);
        }

        @Nullable
        public T createEntity(Level world, String subType, String factionName) {
            T npc = entityType.get().create(world);
            if (npc == null) {
                return null;
            }
            if (!subType.isEmpty()) {
                ItemStack stack = spawnEquipment.get(subType);
                if (stack != null && !stack.isEmpty()) {
                    npc.setItemSlot(EquipmentSlot.MAINHAND, stack.copy());
                }
            }
            return npc;
        }

        public List<String> getItemModelVariants() {
            return new ImmutableList.Builder<String>().addAll(spawnEquipment.keySet()).add(itemModelVariant).build();
        }

        public String getItemModelVariant(String npcSubType) {
            return npcSubType.isEmpty() ? itemModelVariant : npcSubType;
        }

        public Set<String> getSubTypes() {
            return spawnEquipment.keySet();
        }

        public boolean canSpawnBaseEntity() {
            return spawnBaseEntity;
        }

        public String getNpcType() {
            return npcType;
        }

        public String getRegistryName() {
            return registryName;
        }

        public Class<T> getEntityClass() {
            return entityClass;
        }

        public RegistryObject<EntityType<T>> getEntityTypeObject() {
            return entityType;
        }

        public EntityType<T> getEntityType() {
            return entityType.get();
        }
    }

    public static class NpcFactionDeclaration<T extends NpcFaction> extends NpcDeclaration<T> {
        private NpcFactionDeclaration(Class<T> entityClass, String registryName, EntityType.EntityFactory<T> factory,
                                      String itemModelVariant) {
            super(entityClass, registryName, factory, registryName, itemModelVariant);
        }

        @Override
        public T createEntity(Level world, String subType, String factionName) {
            T npc = super.createEntity(world, subType, factionName);
            if (npc != null) {
                npc.setFactionNameAndDefaults(factionName);
            }
            return npc;
        }
    }
}
