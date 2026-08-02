package net.shadowmage.ancientwarfare.npc.init;

import com.google.common.collect.ImmutableList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import net.shadowmage.ancientwarfare.core.config.AWCoreStatics;
import net.shadowmage.ancientwarfare.core.entity.AWEntityRegistry;
import net.shadowmage.ancientwarfare.core.entity.AWEntityRegistry.EntityDeclaration;
import net.shadowmage.ancientwarfare.core.init.AWCoreItems;
import net.shadowmage.ancientwarfare.npc.AncientWarfareNPC;
import net.shadowmage.ancientwarfare.npc.entity.*;
import net.shadowmage.ancientwarfare.npc.entity.faction.*;
import net.shadowmage.ancientwarfare.npc.entity.vehicle.NpcSiegeEngineer;

import java.util.*;
import java.util.function.Supplier;

public class AWNPCEntities {
    private AWNPCEntities() {
    }

    private static final String COMBAT_TYPE = "combat";
    private static final String SOLDIER_SUBTYPE = "soldier";
    private static final String COMMANDER_SUBTYPE = "commander";
    private static final String ARCHER_SUBTYPE = "archer";
    private static final String WORKER_TYPE = "worker";
    private static final String MINER_SUBTYPE = "miner";
    private static final String SPELLCASTER_SUBTYPE = "spellcaster";
    private static int nextID = 0;
    private static NpcFactionDeclaration wizreg;

    /*
     * Npc base type -> NpcDeclaration<br>
     * Used to retrieve declaration for creating entities<br>
     * NpcDeclaration also stores information pertaining to npc-sub-type basic setup
     */
    private static final HashMap<String, NpcDeclaration> npcMap = new HashMap<>();

    private static boolean registrationsPrepared;

    public static synchronized void register(IEventBus modBus) {
        if (!registrationsPrepared) {
            addPlayerOwnedNpcs();
            addFaction();
            registrationsPrepared = true;
        }
        AWEntityRegistry.attachRegisterListener(modBus, AncientWarfareNPC.MOD_ID);
    }

    private static void addPlayerOwnedNpcs() {
        NpcDeclaration reg = new NpcDeclaration(NpcCombat.class, AWEntityRegistry.NPC_COMBAT, COMBAT_TYPE, SOLDIER_SUBTYPE);
        reg.addSubTypes(COMMANDER_SUBTYPE, SOLDIER_SUBTYPE, ARCHER_SUBTYPE, "medic", "engineer");
        addNpcRegistration(reg);

        reg = new NpcDeclaration(NpcWorker.class, AWEntityRegistry.NPC_WORKER, WORKER_TYPE, MINER_SUBTYPE);
        reg.addSubTypes(MINER_SUBTYPE, "farmer", "lumberjack", "researcher", "craftsman");
        addNpcRegistration(reg);

        reg = new NpcDeclaration(NpcCourier.class, AWEntityRegistry.NPC_COURIER, "courier");
        addNpcRegistration(reg);

        reg = new NpcDeclaration(NpcTrader.class, AWEntityRegistry.NPC_TRADER, "trader");
        addNpcRegistration(reg);

        reg = new NpcDeclaration(NpcPriest.class, AWEntityRegistry.NPC_PRIEST, "priest");
        addNpcRegistration(reg);

        reg = new NpcDeclaration(NpcBard.class, AWEntityRegistry.NPC_BARD, "bard");
        addNpcRegistration(reg);

        reg = new NpcDeclaration(NpcSiegeEngineer.class, AWEntityRegistry.NPC_SIEGE_ENGINEER, "siege_engineer");
        addNpcRegistration(reg);
    }

    private static void addFaction() {
        NpcFactionDeclaration reg;

        reg = new NpcFactionDeclaration(NpcFactionArcher.class, AWEntityRegistry.NPC_FACTION_ARCHER, ARCHER_SUBTYPE);
        addNpcRegistration(reg);

        reg = new NpcFactionDeclaration(NpcFactionSoldier.class, AWEntityRegistry.NPC_FACTION_SOLDIER, SOLDIER_SUBTYPE);
        addNpcRegistration(reg);

        reg = new NpcFactionDeclaration(NpcFactionLeader.class, AWEntityRegistry.NPC_FACTION_COMMANDER, COMMANDER_SUBTYPE);
        addNpcRegistration(reg);

        reg = new NpcFactionDeclaration(NpcFactionPriest.class, AWEntityRegistry.NPC_FACTION_PRIEST, "priest");
        addNpcRegistration(reg);

        reg = new NpcFactionDeclaration(NpcFactionTrader.class, AWEntityRegistry.NPC_FACTION_TRADER, "trader");
        addNpcRegistration(reg);

        reg = new NpcFactionDeclaration(NpcFactionMountedSoldier.class, AWEntityRegistry.NPC_FACTION_CAVALRY, SOLDIER_SUBTYPE);
        addNpcRegistration(reg);

        reg = new NpcFactionDeclaration(NpcFactionMountedArcher.class, AWEntityRegistry.NPC_FACTION_MOUNTED_ARCHER, ARCHER_SUBTYPE);
        addNpcRegistration(reg);

        reg = new NpcFactionDeclaration(NpcFactionCivilianMale.class, AWEntityRegistry.NPC_FACTION_CIVILIAN_MALE, "civilian_male");
        addNpcRegistration(reg);

        reg = new NpcFactionDeclaration(NpcFactionCivilianFemale.class, AWEntityRegistry.NPC_FACTION_CIVILIAN_FEMALE, "civilian_female");
        addNpcRegistration(reg);

        reg = new NpcFactionDeclaration(NpcFactionArcherElite.class, AWEntityRegistry.NPC_FACTION_ARCHER_ELITE, ARCHER_SUBTYPE);
        addNpcRegistration(reg);

        reg = new NpcFactionDeclaration(NpcFactionSoldierElite.class, AWEntityRegistry.NPC_FACTION_SOLDIER_ELITE, SOLDIER_SUBTYPE);
        addNpcRegistration(reg);

        reg = new NpcFactionDeclaration(NpcFactionLeaderElite.class, AWEntityRegistry.NPC_FACTION_LEADER_ELITE, COMMANDER_SUBTYPE);
        addNpcRegistration(reg);

        reg = new NpcFactionDeclaration(NpcFactionBard.class, AWEntityRegistry.NPC_FACTION_BARD, "bard");
        addNpcRegistration(reg);

        reg = new NpcFactionDeclaration(NpcFactionSiegeEngineer.class, AWEntityRegistry.NPC_FACTION_SIEGE_ENGINEER, "siege_engineer");
        addNpcRegistration(reg);

        registerSpellcasterFactionNpc();
    }

    private static void registerSpellcasterFactionNpc() {
        NpcFactionDeclaration reg;

        /* optional dependency for EBWizardry spell casters
         * References to the EBWizardry specific class can only be here, to avoid class loading if the mod is no present.
         * Any reference outside of the lambdas will crash the game if EBWizardry is not present */
        Supplier<Runnable> registerWizardrySpellcaster = () -> () -> {
            wizreg = new NpcFactionDeclaration(NpcFactionSpellcasterWizardry.class, AWEntityRegistry.NPC_FACTION_SPELLCASTER, SPELLCASTER_SUBTYPE);
            addNpcRegistration(wizreg);
        };

        if (ModList.get().isLoaded("ebwizardry")) {
            registerWizardrySpellcaster.get().run();
        } else {
            reg = new NpcFactionDeclaration(NpcFactionSpellcaster.class, AWEntityRegistry.NPC_FACTION_SPELLCASTER, SPELLCASTER_SUBTYPE);
            addNpcRegistration(reg);
        }
    }

    /*
     * has to be called during post-init so that all items/etc are fully initialized
     */
    public static void loadNpcSubtypeEquipment() {
        addNpcSubtypeEquipment(WORKER_TYPE, "farmer", new ItemStack(Items.IRON_HOE));
        addNpcSubtypeEquipment(WORKER_TYPE, MINER_SUBTYPE, new ItemStack(Items.IRON_PICKAXE));
        addNpcSubtypeEquipment(WORKER_TYPE, "lumberjack", new ItemStack(Items.IRON_AXE));
        addNpcSubtypeEquipment(WORKER_TYPE, "researcher", new ItemStack(AWCoreItems.IRON_QUILL));
        addNpcSubtypeEquipment(WORKER_TYPE, "craftsman", new ItemStack(AWCoreItems.IRON_HAMMER));

        addNpcSubtypeEquipment(COMBAT_TYPE, COMMANDER_SUBTYPE, new ItemStack(AWNPCItems.IRON_COMMAND_BATON));
        addNpcSubtypeEquipment(COMBAT_TYPE, SOLDIER_SUBTYPE, new ItemStack(Items.IRON_SWORD));
        addNpcSubtypeEquipment(COMBAT_TYPE, ARCHER_SUBTYPE, new ItemStack(Items.BOW));
        addNpcSubtypeEquipment(COMBAT_TYPE, "engineer", new ItemStack(AWCoreItems.IRON_HAMMER));
        Item medicItem = ForgeRegistries.ITEMS.getValue(AWCoreStatics.medicItems.get(0));
        addNpcSubtypeEquipment(COMBAT_TYPE, "medic", new ItemStack(medicItem == null ? Items.GOLDEN_APPLE : medicItem));
    }

    private static void addNpcRegistration(NpcDeclaration reg) {
        AWEntityRegistry.registerEntity(reg);
        getNpcMap().put(reg.getNpcType(), reg);
    }

    public static NpcBase createNpc(Level world, String npcType, String npcSubtype, String faction) {
        if (!getNpcMap().containsKey(npcType)) {
            return null;
        }
        NpcDeclaration reg = getNpcMap().get(npcType);
        return reg.createEntity(world, npcSubtype, faction);
    }

    private static void addNpcSubtypeEquipment(String npcType, String npcSubtype, ItemStack equipment) {
        if (!getNpcMap().containsKey(npcType)) {
            throw new IllegalArgumentException("npc type must first be mapped");
        }
        NpcDeclaration reg = getNpcMap().get(npcType);
        reg.addSubtypeEquipment(npcSubtype, equipment);
    }

    public static Map<String, NpcDeclaration> getNpcMap() {
        return npcMap;
    }

    public static NpcDeclaration getNpcDeclaration(String npcType) {
        return npcMap.get(npcType);
    }

    public static class NpcDeclaration extends EntityDeclaration {

        private final String itemModelVariant;
        private boolean spawnBaseEntity = true;
        private final String npcType;
        private final HashMap<String, ItemStack> spawnEquipment = new HashMap<>();

        public NpcDeclaration(Class<? extends Entity> entityClass, String entityName, String npcType) {
            this(entityClass, entityName, npcType, npcType.replace(".", "_"));
        }

        public NpcDeclaration(Class<? extends Entity> entityClass, String entityName, String npcType, String itemModelVariant) {
            super(entityClass, entityName, nextID++, AncientWarfareNPC.MOD_ID);
            this.npcType = npcType;
            this.itemModelVariant = itemModelVariant;
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

        public NpcBase createEntity(Level world, String subType, String factionName) {
            NpcBase npc = (NpcBase) createEntity(getRegisteredType(), world);
            if (!subType.isEmpty()) {
                ItemStack stack = spawnEquipment.get(subType);
                if (!stack.isEmpty()) {
                    npc.setItemSlot(EquipmentSlot.MAINHAND, stack.copy());
                }
            }
            return npc;
        }

        @Override
        public final Object mod() {
            return AncientWarfareNPC.instance;
        }

        @Override
        public final int trackingRange() {
            return 120;
        }

        @Override
        public final int updateFrequency() {
            return 3;
        }

        @Override
        public final boolean sendsVelocityUpdates() {
            return true;
        }

        public List<String> getItemModelVariants() {
            return new ImmutableList.Builder<String>().addAll(spawnEquipment.keySet()).add(itemModelVariant).build();
        }

        public String getItemModelVariant(String npcSubType) {
            if (npcSubType.isEmpty()) {
                return itemModelVariant;
            }
            return npcSubType;
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
    }

    public static class NpcFactionDeclaration extends NpcDeclaration {
        private NpcFactionDeclaration(Class<? extends NpcFaction> entityClass, String entityName, String itemModelVariant) {
            super(entityClass, entityName, entityName, itemModelVariant);
        }

        @Override
        public NpcFaction createEntity(Level world, String subType, String factionName) {
            //The (Level, String) ctor chains into NpcBase(Level), which needs the construction type thread-local set.
            return AWEntityRegistry.constructWithType(getRegisteredType(), () -> {
                try {
                    return (NpcFaction) getEntityClass().getConstructor(Level.class, String.class).newInstance(world, factionName);
                } catch (Exception e) {
                    AncientWarfareNPC.LOG.error("Couldn't create entity:" + e.getMessage());
                }
                return null;
            });
        }
    }
}
