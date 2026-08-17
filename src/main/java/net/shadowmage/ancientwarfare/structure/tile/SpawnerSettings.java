package net.shadowmage.ancientwarfare.structure.tile;

import com.google.common.collect.ImmutableSet;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.ForgeRegistries;
import net.shadowmage.ancientwarfare.core.config.AWCoreStatics;
import net.shadowmage.ancientwarfare.core.datafixes.ComponentItemFixer;
import net.shadowmage.ancientwarfare.core.util.Constants;
import net.shadowmage.ancientwarfare.core.util.EntityTools;
import net.shadowmage.ancientwarfare.npc.entity.faction.*;
import net.shadowmage.ancientwarfare.npc.faction.FactionTracker;
import net.shadowmage.ancientwarfare.structure.AncientWarfareStructure;
import net.shadowmage.ancientwarfare.structure.config.AWStructureStatics;
import net.shadowmage.ancientwarfare.structure.init.AWStructureBlocks;
import net.shadowmage.ancientwarfare.structure.util.CapabilityRespawnData;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;

import static net.shadowmage.ancientwarfare.npc.event.EventHandler.NO_SPAWN_PREVENTION_TAG;

@SuppressWarnings("SpellCheckingInspection")
public class SpawnerSettings {
    private static final String RESPOND_TO_REDSTONE_TAG = "respondToRedstone";
    private static final String REDSTONE_MODE_TAG = "redstoneMode";
    private static final String PREV_REDSTONE_STATE_TAG = "prevRedstoneState";
    private static final String MIN_DELAY_TAG = "minDelay";
    private static final String MAX_DELAY_TAG = "maxDelay";
    private static final String SPAWN_DELAY_TAG = "spawnDelay";
    private static final String PLAYER_RANGE_TAG = "playerRange";
    private static final String MOB_RANGE_TAG = "mobRange";
    private static final String SPAWN_RANGE_TAG = "spawnRange";
    private static final String MAX_NEARBY_MONSTERS_TAG = "maxNearbyMonsters";
    private static final String XP_TO_DROP_TAG = "xpToDrop";
    private static final String LIGHT_SENSITIVE_TAG = "lightSensitive";
    private static final String TRANSPARENT_TAG = "transparent";
    private static final String DEBUG_MODE_TAG = "debugMode";
    private static final String SPAWN_GROUPS_TAG = "spawnGroups";
    private static final String INVENTORY_TAG = "inventory";
    private static final String HOSTILE_TAG = "hostile";
    private static final String FACTION_NAME_TAG = "factionName";
    private static final String SPAWN_Y_OFFSET_TAG = "spawnYOffset";
    private List<EntitySpawnGroup> spawnGroups = new ArrayList<>();

    private ItemStackHandler inventory = new ItemStackHandler(9);

    private boolean debugMode;
    private boolean transparent;
    private boolean respondToRedstone;//should this spawner respond to redstone impulses
    private boolean redstoneMode;//false==toggle, true==pulse/tick to spawn
    private boolean prevRedstoneState;//used to cache the powered status from last tick, to compare to this tick

    private int playerRange;
    private int mobRange;
    private int range = 4;

    private int maxDelay = 20 * 20;
    private int minDelay = 20 * 10;

    private int spawnDelay = maxDelay;

    private int maxNearbyMonsters;

    private boolean lightSensitive;

    private int xpToDrop;

    private int spawnYOffset = 0;

    private boolean isOneShotSpawner;
    private String factionName = "";

    float blockHardness = 2.f;

    /*
     * fields for a 'fake' tile-entity...set from the real tile-entity when it has its
     * world set (which is before first updateEntity() is called)
     */
    private Level world;
    private BlockPos pos;

    public boolean hasWorld() {
        return world != null;
    }

    public static SpawnerSettings getDefaultSettings() {
        SpawnerSettings settings = new SpawnerSettings();
        settings.playerRange = 16;
        settings.mobRange = 4;
        settings.maxNearbyMonsters = 8;

        EntitySpawnGroup group = new EntitySpawnGroup(settings);
        group.addSpawnSetting(new EntitySpawnSettings(group));
        settings.addSpawnGroup(group);

        return settings;
    }

    void setWorld(Level world, BlockPos pos) {
        this.world = world;
        this.pos = pos;
    }

    void onUpdate() {
        if (!respondToRedstone) {
            updateNormalMode();
        } else if (redstoneMode) {
            updateRedstoneModePulse();
        } else {
            updateRedstoneModeToggle();
        }
        if (spawnGroups.isEmpty()) {
            world.removeBlock(pos, false);
        }
    }

    private void updateRedstoneModeToggle() {
        prevRedstoneState = world.getBestNeighborSignal(pos) > 0 || world.getDirectSignalTo(pos) > 0;
        if (respondToRedstone && !redstoneMode && !prevRedstoneState) {
            //noop
            return;
        }
        updateNormalMode();
    }

    private void updateRedstoneModePulse() {
        boolean powered = world.getBestNeighborSignal(pos) > 0 || world.getDirectSignalTo(pos) > 0;
        if (!prevRedstoneState && powered) {
            spawnEntities();
        }
        prevRedstoneState = powered;
    }

    private void updateNormalMode() {
        if (spawnDelay > 0) {
            spawnDelay--;
        }
        if (spawnDelay <= 0) {
            int delayRange = maxDelay - minDelay;
            spawnDelay = minDelay + (delayRange <= 0 ? 0 : world.getRandom().nextInt(delayRange));
            spawnEntities();
        }
    }

    private void spawnEntities() {
        if (checkSpawnConditions()) {
            return;
        }

        int totalWeight = 0;
        for (EntitySpawnGroup group : this.spawnGroups)//count total weights
        {
            totalWeight += group.groupWeight;
        }
        int rand = totalWeight == 0 ? 0 : world.getRandom().nextInt(totalWeight);//select an object
        int check = 0;
        EntitySpawnGroup toSpawn = null;
        int index = 0;
        for (EntitySpawnGroup group : this.spawnGroups)//iterate to find selected object
        {
            check += group.groupWeight;
            if (rand < check)//object found, break
            {
                toSpawn = group;
                break;
            }
            index++;
        }

        if (toSpawn != null) {
            toSpawn.spawnEntities(world, pos, index, spawnYOffset, range);
            if (toSpawn.shouldRemove()) {
                spawnGroups.remove(toSpawn);
            }
        }
    }

    private boolean checkSpawnConditions() {
        if (checkLight()) {
            return true;
        }
        if (!checkPlayerConditions()) {
            return true;
        }

        return checkNearbyMobs();
    }

    private boolean checkLight() {
        if (lightSensitive) {
            int light = world.getBlockState(pos).getLightEmission(world, pos);

            return light >= 8;
        }
        return false;
    }

    private boolean checkNearbyMobs() {
        if (maxNearbyMonsters > 0 && mobRange > 0) {
            int nearbyCount = world.getEntitiesOfClass(LivingEntity.class, new AABB(pos, pos.offset(1, 1, 1)).inflate(mobRange, mobRange, mobRange)).size();
            if (nearbyCount >= maxNearbyMonsters) {
                AncientWarfareStructure.LOG.debug("skipping spawning because of too many nearby entities");
                return true;
            }
        }
        return false;
    }

    private boolean checkPlayerConditions() {
        if (playerRange > 0) {
            List<Player> nearbyPlayers = getPlayersWithinAABB();
            if (nearbyPlayers.isEmpty()) {
                return false;
            }

            for (Player player : nearbyPlayers) {
                if ((debugMode || (!player.isCreative() && !player.isSpectator())) && !isContinuousSpawnerOfFriendlyFaction(player)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isContinuousSpawnerOfFriendlyFaction(Player player) {
        return !isOneShotSpawner && !factionName.isEmpty() && !FactionTracker.INSTANCE.isHostileToPlayer(world, player.getUUID(), player.getName().getString(), factionName);
    }

    private List<Player> getPlayersWithinAABB() {
        List<Player> players = new ArrayList<>();

        for (Player player : world.players()) {
            if (player.getBoundingBox().intersects(new AABB(pos, pos.offset(1, 1, 1)).inflate(playerRange, playerRange, playerRange))) {
//				if(AWCoreStatics.spawnersRequireLineOfSight) {
//					// LOS check to player
//					if(player.level().rayTraceBlocks(new Vec3(player.getX(), player.getY() + (double)player.getEyeHeight(), player.getZ()), new Vec3(pos.getX(), pos.getY()+1, pos.getZ()), false, true, false) == null) {
//						players.add(player);
//					}
//				}
//				else {
                players.add(player);
//				}
            }
        }
        return players;
    }

    public CompoundTag writeToNBT(CompoundTag tag) {
        tag.putBoolean(RESPOND_TO_REDSTONE_TAG, respondToRedstone);
        if (respondToRedstone) {
            tag.putBoolean(REDSTONE_MODE_TAG, redstoneMode);
            tag.putBoolean(PREV_REDSTONE_STATE_TAG, prevRedstoneState);
        }
        tag.putInt(MIN_DELAY_TAG, minDelay);
        tag.putInt(MAX_DELAY_TAG, maxDelay);
        tag.putInt(SPAWN_DELAY_TAG, spawnDelay);
        tag.putInt(PLAYER_RANGE_TAG, playerRange);
        tag.putInt(MOB_RANGE_TAG, mobRange);
        tag.putInt(SPAWN_RANGE_TAG, range);
        tag.putInt(MAX_NEARBY_MONSTERS_TAG, maxNearbyMonsters);
        tag.putInt(XP_TO_DROP_TAG, xpToDrop);
        tag.putBoolean(LIGHT_SENSITIVE_TAG, lightSensitive);
        tag.putBoolean(TRANSPARENT_TAG, transparent);
        tag.putBoolean(DEBUG_MODE_TAG, debugMode);
        ListTag groupList = new ListTag();
        CompoundTag groupTag;
        for (EntitySpawnGroup group : this.spawnGroups) {
            groupTag = new CompoundTag();
            group.writeToNBT(groupTag);
            groupList.add(groupTag);
        }
        tag.put(SPAWN_GROUPS_TAG, groupList);

        tag.put(INVENTORY_TAG, inventory.serializeNBT());
        tag.putInt(SPAWN_Y_OFFSET_TAG, spawnYOffset);

        return tag;
    }

    public void readFromNBT(CompoundTag sourceTag) {
        CompoundTag tag = unwrapSettingsTag(sourceTag);
        spawnGroups.clear();

        if (tag.contains(RESPOND_TO_REDSTONE_TAG)) respondToRedstone = tag.getBoolean(RESPOND_TO_REDSTONE_TAG);
        if (respondToRedstone) {
            if (tag.contains(REDSTONE_MODE_TAG)) redstoneMode = tag.getBoolean(REDSTONE_MODE_TAG);
            if (tag.contains(PREV_REDSTONE_STATE_TAG)) prevRedstoneState = tag.getBoolean(PREV_REDSTONE_STATE_TAG);
        }
        if (tag.contains(MIN_DELAY_TAG)) minDelay = Math.max(tag.getInt(MIN_DELAY_TAG), 10);
        if (tag.contains(MAX_DELAY_TAG)) maxDelay = Math.max(tag.getInt(MAX_DELAY_TAG), minDelay);
        if (tag.contains(SPAWN_DELAY_TAG)) spawnDelay = tag.getInt(SPAWN_DELAY_TAG);
        if (tag.contains(PLAYER_RANGE_TAG)) playerRange = tag.getInt(PLAYER_RANGE_TAG);
        if (tag.contains(MOB_RANGE_TAG)) mobRange = tag.getInt(MOB_RANGE_TAG);
        if (tag.contains(SPAWN_RANGE_TAG)) range = tag.getInt(SPAWN_RANGE_TAG);
        if (tag.contains(MAX_NEARBY_MONSTERS_TAG)) maxNearbyMonsters = tag.getInt(MAX_NEARBY_MONSTERS_TAG);
        if (tag.contains(XP_TO_DROP_TAG)) xpToDrop = tag.getInt(XP_TO_DROP_TAG);
        if (tag.contains(LIGHT_SENSITIVE_TAG)) lightSensitive = tag.getBoolean(LIGHT_SENSITIVE_TAG);
        if (tag.contains(TRANSPARENT_TAG)) transparent = tag.getBoolean(TRANSPARENT_TAG);
        if (tag.contains(DEBUG_MODE_TAG)) debugMode = tag.getBoolean(DEBUG_MODE_TAG);

        ListTag groupList = tag.getList(SPAWN_GROUPS_TAG, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < groupList.size(); i++) {
            EntitySpawnGroup group = new EntitySpawnGroup(this);
            group.readFromNBT(groupList.getCompound(i));
            if (!group.getEntitiesToSpawn().isEmpty()) {
                spawnGroups.add(group);
            }
        }

        // 1.12 vanilla spawner NBT used SpawnData/SpawnPotentials instead of AW's
        // spawnGroups tree. Convert it before the empty spawner removes itself.
        if (spawnGroups.isEmpty() && !tag.contains(SPAWN_GROUPS_TAG)) {
            readLegacySpawnerData(tag);
        }

        if (tag.contains(INVENTORY_TAG)) inventory.deserializeNBT(tag.getCompound(INVENTORY_TAG));
        if (tag.contains(SPAWN_Y_OFFSET_TAG)) spawnYOffset = tag.getInt(SPAWN_Y_OFFSET_TAG);

        // Vanilla/1.12 field names. Read only when the AW field was absent so a
        // mixed tag cannot overwrite an explicitly configured modern value.
        if (!tag.contains(MIN_DELAY_TAG) && tag.contains("MinSpawnDelay")) minDelay = Math.max(10, tag.getInt("MinSpawnDelay"));
        if (!tag.contains(MAX_DELAY_TAG) && tag.contains("MaxSpawnDelay")) maxDelay = Math.max(minDelay, tag.getInt("MaxSpawnDelay"));
        if (!tag.contains(SPAWN_DELAY_TAG) && tag.contains("Delay")) spawnDelay = tag.getInt("Delay");
        if (!tag.contains(PLAYER_RANGE_TAG) && tag.contains("RequiredPlayerRange")) playerRange = tag.getInt("RequiredPlayerRange");
        if (!tag.contains(SPAWN_RANGE_TAG) && tag.contains("SpawnRange")) range = tag.getInt("SpawnRange");
        if (!tag.contains(MAX_NEARBY_MONSTERS_TAG) && tag.contains("MaxNearbyEntities")) maxNearbyMonsters = tag.getInt("MaxNearbyEntities");

        updateSpawnProperties();
    }

    /** Accept item tags, BlockEntityTag wrappers, tile NBT, and direct settings NBT. */
    public static CompoundTag unwrapSettingsTag(CompoundTag source) {
        if (source == null) return new CompoundTag();
        if (source.contains("spawnerSettings", Constants.NBT.TAG_COMPOUND)) {
            return source.getCompound("spawnerSettings");
        }
        if (source.contains("BlockEntityTag", Constants.NBT.TAG_COMPOUND)) {
            CompoundTag blockEntityTag = source.getCompound("BlockEntityTag");
            if (blockEntityTag.contains("spawnerSettings", Constants.NBT.TAG_COMPOUND)) {
                return blockEntityTag.getCompound("spawnerSettings");
            }
            return blockEntityTag;
        }
        return source;
    }

    public static boolean containsSpawnerConfiguration(CompoundTag source) {
        CompoundTag tag = unwrapSettingsTag(source);
        return tag.contains(SPAWN_GROUPS_TAG) || tag.contains("SpawnData") || tag.contains("SpawnPotentials")
                || tag.contains("EntityId") || tag.contains("entityId") || tag.contains("mobID");
    }

    private void readLegacySpawnerData(CompoundTag tag) {
        ListTag potentials = tag.getList("SpawnPotentials", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < potentials.size(); i++) {
            CompoundTag potential = potentials.getCompound(i);
            CompoundTag entityData = extractLegacyEntityData(potential);
            ResourceLocation entityId = extractLegacyEntityId(entityData);
            if (entityId == null) continue;
            EntitySpawnGroup group = createLegacyGroup(entityId, entityData, getLegacySpawnCount(tag));
            int weight = potential.contains("Weight") ? potential.getInt("Weight") : potential.getInt("weight");
            group.setWeight(weight <= 0 ? 1 : weight);
            spawnGroups.add(group);
        }

        if (spawnGroups.isEmpty()) {
            CompoundTag entityData = tag.contains("SpawnData", Constants.NBT.TAG_COMPOUND)
                    ? extractLegacyEntityData(tag.getCompound("SpawnData")) : tag;
            ResourceLocation entityId = extractLegacyEntityId(entityData);
            if (entityId == null) {
                // Malformed old spawners must remain usable instead of becoming an
                // invisible empty block that deletes itself on the next tick.
                entityId = new ResourceLocation("minecraft", "zombie");
                entityData = new CompoundTag();
            }
            spawnGroups.add(createLegacyGroup(entityId, entityData, getLegacySpawnCount(tag)));
        }
    }

    private EntitySpawnGroup createLegacyGroup(ResourceLocation entityId, CompoundTag entityData, int spawnCount) {
        EntitySpawnGroup group = new EntitySpawnGroup(this);
        EntitySpawnSettings setting = new EntitySpawnSettings(group);
        setting.setEntityToSpawn(entityId);
        CompoundTag custom = entityData.copy();
        custom.remove("id");
        custom.remove("EntityId");
        custom.remove("entityId");
        custom.remove("mobID");
        if (!custom.isEmpty()) setting.setCustomSpawnTag(custom);
        setting.setSpawnCountMin(spawnCount);
        setting.setSpawnCountMax(spawnCount);
        setting.setSpawnLimitTotal(-1);
        group.addSpawnSetting(setting);
        return group;
    }

    private static int getLegacySpawnCount(CompoundTag tag) {
        return tag.contains("SpawnCount") ? Math.max(1, tag.getInt("SpawnCount")) : 1;
    }

    private static CompoundTag extractLegacyEntityData(CompoundTag tag) {
        if (tag.contains("data", Constants.NBT.TAG_COMPOUND)) return extractLegacyEntityData(tag.getCompound("data"));
        if (tag.contains("entity", Constants.NBT.TAG_COMPOUND)) return tag.getCompound("entity");
        if (tag.contains("Entity", Constants.NBT.TAG_COMPOUND)) return tag.getCompound("Entity");
        return tag;
    }

    @Nullable
    private static ResourceLocation extractLegacyEntityId(CompoundTag tag) {
        String id = "";
        if (tag.contains("entityId")) id = tag.getString("entityId");
        else if (tag.contains("EntityId")) id = tag.getString("EntityId");
        else if (tag.contains("mobID")) id = tag.getString("mobID");
        else if (tag.contains("id")) id = tag.getString("id");
        if (id == null || id.isBlank() || id.endsWith("advanced_spawner_tile")) return null;
        if (id.indexOf(':') < 0) id = "minecraft:" + id.toLowerCase(Locale.ROOT);
        return ResourceLocation.tryParse(id);
    }

    void updateSpawnProperties() {
        if (world == null || world.isClientSide) {
            return;
        }

        isOneShotSpawner = false;
        factionName = "";
        if (spawnGroups.size() == 1 && spawnGroups.get(0).entitiesToSpawn.size() == 1) {
            EntitySpawnSettings entitySettings = spawnGroups.get(0).entitiesToSpawn.get(0);
            if (entitySettings.maxToSpawn == 1 && entitySettings.minToSpawn == 1 && entitySettings.remainingSpawnCount == 1) {
                isOneShotSpawner = true;
            }
            EntityType<?> entityType = ForgeRegistries.ENTITY_TYPES.getValue(entitySettings.entityId);
            Entity entity = entityType == null ? null : entityType.create(world);
            factionName = entity instanceof NpcFaction && entitySettings.customTag != null
                    ? entitySettings.customTag.getString(FACTION_NAME_TAG) : "";
        }
    }

    public void addSpawnGroup(EntitySpawnGroup group) {
        spawnGroups.add(group);
    }

    public List<EntitySpawnGroup> getSpawnGroups() {
        return spawnGroups;
    }

    public final boolean isLightSensitive() {
        return lightSensitive;
    }

    public final void toggleLightSensitive() {
        this.lightSensitive = !lightSensitive;
    }

    public final boolean isRespondToRedstone() {
        return respondToRedstone;
    }

    public final void toggleRespondToRedstone() {
        this.respondToRedstone = !respondToRedstone;
    }

    public final boolean getRedstoneMode() {
        return redstoneMode;
    }

    public final void toggleRedstoneMode() {
        this.redstoneMode = !redstoneMode;
    }

    public final int getPlayerRange() {
        return playerRange;
    }

    public final void setPlayerRange(int playerRange) {
        this.playerRange = playerRange;
    }

    public final int getMobRange() {
        return mobRange;
    }

    public final void setMobRange(int mobRange) {
        this.mobRange = mobRange;
    }

    public final int getSpawnRange() {
        return this.range;
    }

    public final void setSpawnRange(int range) {
        this.range = range;
    }

    public final int getMaxDelay() {
        return maxDelay;
    }

    public final void setMaxDelay(int maxDelay) {
        if (minDelay > maxDelay)
            minDelay = maxDelay;
        this.maxDelay = maxDelay;
    }

    public final int getMinDelay() {
        return minDelay;
    }

    public final void setMinDelay(int minDelay) {
        if (minDelay > maxDelay)
            maxDelay = minDelay;
        this.minDelay = minDelay;
    }

    public final int getSpawnDelay() {
        return spawnDelay;
    }

    public final void setSpawnDelay(int spawnDelay) {
        if (spawnDelay > maxDelay)
            maxDelay = spawnDelay;
        if (spawnDelay < minDelay)
            minDelay = spawnDelay;
        this.spawnDelay = spawnDelay;
    }

    public final int getMaxNearbyMonsters() {
        return maxNearbyMonsters;
    }

    public final void setMaxNearbyMonsters(int maxNearbyMonsters) {
        this.maxNearbyMonsters = maxNearbyMonsters;
    }

    public final void setXpToDrop(int xp) {
        this.xpToDrop = xp;
    }

    public final void setBlockHardness(float hardness) {
        this.blockHardness = hardness;
    }

    public final int getXpToDrop() {
        return xpToDrop;
    }

    public final float getBlockHardness() {
        return blockHardness;
    }

    public final IItemHandler getInventory() {
        return inventory;
    }

    public final boolean isDebugMode() {
        return debugMode;
    }

    public final void toggleDebugMode() {
        debugMode = !debugMode;
    }

    public final boolean isTransparent() {
        return transparent;
    }

    public final void toggleTransparent() {
        this.transparent = !transparent;
    }

    public void setPos(BlockPos posIn) {
        this.pos = posIn;
    }

    public int getSpawnYOffset() {
        return spawnYOffset;
    }

    public void setSpawnYOffset(int spawnYOffset) {
        this.spawnYOffset = spawnYOffset;
    }

    public static boolean spawnsHostileNpcs(SpawnerSettings spawnerSettings) {
        return spawnerSettings.spawnsEntity(spawnerSettings::isHostileNpc);
    }

    private static final Set<Class<? extends Entity>> HOSTILE_NPC_CLASS_TYPES = ImmutableSet.of(
            NpcFactionLeader.class, NpcFactionPriest.class, NpcFactionArcher.class, NpcFactionSiegeEngineer.class, NpcFactionMounted.class, NpcFactionSoldier.class
    );

    private boolean isHostileNpc(Class<? extends Entity> entityClass) {
        return HOSTILE_NPC_CLASS_TYPES.stream().anyMatch(entityClass::isAssignableFrom);
    }

    private boolean spawnsEntity(Predicate<Class<? extends Entity>> isEntityOfType) {
        if (world == null) {
            return false;
        }

        // A spawner may contain multiple weighted groups and multiple entity entries
        // per group. Checking only [0][0] lets a hostile faction NPC hidden in a later
        // entry be ignored by protection-flag conquest checks.
        for (EntitySpawnGroup group : getSpawnGroups()) {
            for (EntitySpawnSettings spawnSetting : group.getEntitiesToSpawn()) {
                if (spawnSetting.getSpawnTotal() == 0) {
                    continue;
                }
                EntityType<?> entityType = ForgeRegistries.ENTITY_TYPES.getValue(spawnSetting.getEntityId());
                Entity testEntity = entityType == null ? null : entityType.create(world);
                if (testEntity != null && isEntityOfType.test(testEntity.getClass())) {
                    testEntity.discard();
                    return true;
                }
                if (testEntity != null) {
                    testEntity.discard();
                }
            }
        }
        return false;
    }

    public static final class EntitySpawnGroup {
        private int groupWeight = 1;
        private List<EntitySpawnSettings> entitiesToSpawn = new ArrayList<>();
        private SpawnerSettings settings;

        public EntitySpawnGroup(SpawnerSettings settings) {
            this.settings = settings;
        }

        public SpawnerSettings getParentSettings() {
            return settings;
        }

        public void setWeight(int weight) {
            this.groupWeight = weight <= 0 ? 1 : weight;
        }

        public void addSpawnSetting(EntitySpawnSettings setting) {
            entitiesToSpawn.add(setting);
        }

        private void spawnEntities(Level world, BlockPos spawnPos, int grpIndex, int yOffset, int range) {
            spawnPos = spawnPos.offset(0, yOffset, 0);
            Iterator<EntitySpawnSettings> it = entitiesToSpawn.iterator();
            int index = 0;
            EntitySpawnSettings entitySpawnSettings;
            while (it.hasNext() && (entitySpawnSettings = it.next()) != null) {
                entitySpawnSettings.spawnEntities(world, spawnPos, range);
                if (entitySpawnSettings.shouldRemove()) {
                    it.remove();
                }

                int a1 = 0;
                int b2 = entitySpawnSettings.remainingSpawnCount;
                int a = (a1 << 16) | (grpIndex & 0x0000ffff);
                int b = (index << 16) | (b2 & 0x0000ffff);
                world.blockEvent(spawnPos, AWStructureBlocks.ADVANCED_SPAWNER.get(), a, b);
                index++;
            }
        }

        private boolean shouldRemove() {
            return entitiesToSpawn.isEmpty();
        }

        public List<EntitySpawnSettings> getEntitiesToSpawn() {
            return entitiesToSpawn;
        }

        public int getWeight() {
            return groupWeight;
        }

        public void writeToNBT(CompoundTag tag) {
            tag.putInt("groupWeight", groupWeight);
            ListTag settingsList = new ListTag();

            CompoundTag settingTag;
            for (EntitySpawnSettings setting : this.entitiesToSpawn) {
                settingTag = new CompoundTag();
                setting.writeToNBT(settingTag);
                settingsList.add(settingTag);
            }
            tag.put("settingsList", settingsList);
        }

        public void readFromNBT(CompoundTag tag) {
            setWeight(tag.contains("groupWeight") ? tag.getInt("groupWeight") : 1);
            ListTag settingsList = tag.getList("settingsList", Constants.NBT.TAG_COMPOUND);
            EntitySpawnSettings setting;
            for (int i = 0; i < settingsList.size(); i++) {
                setting = new EntitySpawnSettings(this);
                setting.readFromNBT(settingsList.getCompound(i));
                if (!setting.shouldRemove()) {
                    this.entitiesToSpawn.add(setting);
                }
            }
        }
    }

    public static final class EntitySpawnSettings {
        private static final String ENTITY_ID_TAG = "entityId";
        private static final String CUSTOM_TAG = "customTag";
        private static final String MIN_TO_SPAWN_TAG = "minToSpawn";
        private static final String MAX_TO_SPAWN_TAG = "maxToSpawn";
        private static final String REMAINING_SPAWN_COUNT_TAG = "remainingSpawnCount";
        private static final String CUSTOM_NAME_TAG = "CustomName";
        private ResourceLocation entityId = new ResourceLocation("pig");
        private CompoundTag customTag;
        private int minToSpawn = 2;
        private int maxToSpawn = 4;
        int remainingSpawnCount = -1;
        private boolean hostile = true;
        private EntitySpawnGroup group;

        public EntitySpawnSettings(EntitySpawnGroup group) {
            this.group = group;
        }

        private EntitySpawnGroup getParentSettings() {
            return group;
        }

        public final void writeToNBT(CompoundTag tag) {
            tag.putBoolean(HOSTILE_TAG, hostile);
            tag.putString(ENTITY_ID_TAG, entityId.toString());
            if (customTag != null) {
                tag.put(CUSTOM_TAG, customTag);
            }
            tag.putInt(MIN_TO_SPAWN_TAG, minToSpawn);
            tag.putInt(MAX_TO_SPAWN_TAG, maxToSpawn);
            tag.putInt(REMAINING_SPAWN_COUNT_TAG, remainingSpawnCount);
        }

        public final void readFromNBT(CompoundTag tag) {
            hostile = !tag.contains(HOSTILE_TAG) || tag.getBoolean(HOSTILE_TAG);
            remainingSpawnCount = tag.contains(REMAINING_SPAWN_COUNT_TAG) ? tag.getInt(REMAINING_SPAWN_COUNT_TAG) : -1;
            ResourceLocation parsed = extractLegacyEntityId(tag);
            setEntityToSpawn(parsed == null ? new ResourceLocation("minecraft", "zombie") : parsed);
            if (tag.contains(CUSTOM_TAG, Constants.NBT.TAG_COMPOUND)) {
                customTag = tag.getCompound(CUSTOM_TAG);
            } else {
                CompoundTag legacyEntity = extractLegacyEntityData(tag);
                CompoundTag copied = legacyEntity.copy();
                copied.remove("id");
                copied.remove(ENTITY_ID_TAG);
                copied.remove("EntityId");
                copied.remove("mobID");
                if (!copied.isEmpty()) customTag = copied;
            }
            minToSpawn = tag.contains(MIN_TO_SPAWN_TAG) ? Math.max(1, tag.getInt(MIN_TO_SPAWN_TAG)) : 1;
            maxToSpawn = tag.contains(MAX_TO_SPAWN_TAG) ? Math.max(minToSpawn, tag.getInt(MAX_TO_SPAWN_TAG)) : minToSpawn;
        }

        public final void setEntityToSpawn(Entity entity) {
            hostile = !(entity instanceof AgeableMob);
            ResourceLocation registryName = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
            if (registryName != null) {
                setEntityToSpawn(registryName);
            }
        }

        public final void setEntityToSpawn(ResourceLocation entityId) {
            this.entityId = entityId;
            if (!ForgeRegistries.ENTITY_TYPES.containsKey(this.entityId) || AWStructureStatics.excludedSpawnerEntities.contains(this.entityId.toString())) {
                AncientWarfareStructure.LOG.debug("{} is not a valid entityId, or has been set as invalid for spawners. Searching for replacement.", entityId);
                if (AWCoreStatics.mobReplacementMap.containsKey(this.entityId.toString())) {
                    String replacementId = (String) AWCoreStatics.mobReplacementMap.get(this.entityId.toString());
                    AncientWarfareStructure.LOG.debug("Found replacement entity for {}: {}", entityId, replacementId);
                    ResourceLocation replacementEntity = new ResourceLocation(replacementId);
                    if (!ForgeRegistries.ENTITY_TYPES.containsKey(replacementEntity)) {
                        if (hostile) {
                            AncientWarfareStructure.LOG.debug("Replacement found for entity {}, but it was invalid; spawning zombie", entityId);
                            this.entityId = new ResourceLocation("zombie");
                        } else {
                            remainingSpawnCount = 0;
                        }
                    } else {
                        this.entityId = replacementEntity;
                    }
                } else if (hostile) {
                    AncientWarfareStructure.LOG.debug("No replacement found for entity {}; spawning zombie", entityId);
                    this.entityId = new ResourceLocation("zombie");
                } else {
                    remainingSpawnCount = 0;
                }
            }
        }

        public final void setCustomSpawnTag(@Nullable CompoundTag tag) {
            this.customTag = tag;
        }

        public final void setSpawnCountMin(int min) {
            this.minToSpawn = min;
        }

        public final void setSpawnCountMax(int max) {
            this.maxToSpawn = Math.max(minToSpawn, max);
        }

        public final void setSpawnLimitTotal(int total) {
            this.remainingSpawnCount = total;
        }

        private boolean shouldRemove() {
            return remainingSpawnCount == 0;
        }

        public final ResourceLocation getEntityId() {
            return entityId;
        }

        public final String getEntityName() {
            if (customTag != null && customTag.contains(FACTION_NAME_TAG)) {
                return EntityTools.getUnlocName(entityId).replace("faction", getCustomTag().getString(FACTION_NAME_TAG));
            }
            return EntityTools.getUnlocName(entityId);
        }

        public final Optional<String> getCustomName() {
            if (customTag != null && customTag.contains(CUSTOM_NAME_TAG)) {
                return Optional.of(customTag.getString(CUSTOM_NAME_TAG));
            }
            return Optional.empty();
        }

        public final int getSpawnMin() {
            return minToSpawn;
        }

        public final int getSpawnMax() {
            return maxToSpawn;
        }

        public final int getSpawnTotal() {
            return remainingSpawnCount;
        }

        public final CompoundTag getCustomTag() {
            return customTag;
        }

        private int getNumToSpawn(RandomSource rand) {
            int randRange = maxToSpawn - minToSpawn;
            int toSpawn;
            if (randRange <= 0) {
                toSpawn = minToSpawn;
            } else {
                toSpawn = minToSpawn + rand.nextInt(randRange);
            }
            if (remainingSpawnCount >= 0 && toSpawn > remainingSpawnCount) {
                toSpawn = remainingSpawnCount;
            }
            return toSpawn;
        }

        private void spawnEntities(Level world, BlockPos spawnPos, int range) {
            int toSpawn = getNumToSpawn(world.getRandom());

            for (int i = 0; i < toSpawn; i++) {
                EntityType<?> entityType = ForgeRegistries.ENTITY_TYPES.getValue(entityId);
                Entity e = entityType == null ? null : entityType.create(world);
                if (e == null) {
                    return;
                }
                boolean doSpawn = findAndSetSpawnLocation(world, spawnPos, range, e);
                if (doSpawn) {
                    spawnEntityAt(e, world);
                    if (remainingSpawnCount > 0) {
                        remainingSpawnCount--;
                    }
                }
            }
        }

        private boolean findAndSetSpawnLocation(Level world, BlockPos spawnPos, int range, Entity e) {
            int spawnTry = 0;
            while (spawnTry < range + 5) {
                int x = spawnPos.getX() - range + world.getRandom().nextInt(range * 2 + 1);
                int z = spawnPos.getZ() - range + world.getRandom().nextInt(range * 2 + 1);
                for (int y = spawnPos.getY() - range; y <= spawnPos.getY() + range; y++) {
                    e.moveTo(x + 0.5D, y, z + 0.5D, world.getRandom().nextFloat() * 360.0F, 0.0F);
                    if (range == 0 || checkEntityIsNotColliding(e)) {
                        return true;
                    }
                }
                spawnTry++;
            }
            return false;
        }

        private boolean checkEntityIsNotColliding(Entity entity) {
            return entity.level().noCollision(entity);
        }

        private boolean canDespawn(Entity entity) {
            return !(entity instanceof Mob mob) || !mob.isPersistenceRequired();
        }

        private void spawnEntityAt(Entity e, Level world) {
            if (e instanceof Mob mob && world instanceof ServerLevel serverLevel) {
                mob.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(e.blockPosition()), MobSpawnType.SPAWNER, null, null);
                mob.spawnAnim();
            }
            setDataFromTag(e); //some data needs to be set before spawning entity in the world (like factionName)
            e.getTags().add(NO_SPAWN_PREVENTION_TAG);
            world.addFreshEntity(e);
            setDataFromTag(e); //and some data needs to be set after onInitialSpawn fires for entity]
            if (e instanceof NpcFaction) {
                ((NpcFaction) e).setCanDespawn();
            }
            if (getParentSettings().getParentSettings().isOneShotSpawner && canDespawn(e)) {
                setRespawnData(e);
            }
        }

        private void setRespawnData(Entity e) {
            CapabilityRespawnData.get(e).ifPresent(respawnData -> {
                SpawnerSettings owner = getParentSettings().getParentSettings();

                // Every entity produced by one one-shot spawner must point back to
                // the spawner block, not to its randomized entity spawn location.
                // Using e.blockPosition() gave every mob a different key, so a group
                // of four or five mobs could recreate four or five stacked spawners.
                BlockPos sourcePos = owner.pos == null ? e.blockPosition() : owner.pos;
                respawnData.setRespawnPos(sourcePos.immutable());
                respawnData.setSpawnerSettings(owner.writeToNBT(new CompoundTag()));
                respawnData.setSpawnTime(e.level().getGameTime());
            });
        }

        private void setDataFromTag(Entity e) {
            if (customTag != null) {
                CompoundTag temp = new CompoundTag();
                e.saveWithoutId(temp);
                Set<String> keys = customTag.getAllKeys();
                for (String key : keys) {
                    if (customTag.get(key) != null) {
                        temp.put(key, customTag.get(key).copy());
                    }
                }
                ComponentItemFixer.fixRecursively(temp);
                e.load(temp);
                if (e instanceof NpcFaction && customTag.contains(FACTION_NAME_TAG)) {
                    ((NpcFaction) e).setFactionNameAndDefaults(customTag.getString(FACTION_NAME_TAG));
                }
            }
        }
    }
}
