package net.shadowmage.ancientwarfare.npc.entity.faction;

import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.scores.Team;
import net.shadowmage.ancientwarfare.core.config.AWCoreStatics;
import net.shadowmage.ancientwarfare.core.util.NBTHelper;
import net.shadowmage.ancientwarfare.npc.ai.AIHelper;
import net.shadowmage.ancientwarfare.npc.ai.faction.NpcAIFactionFleeSun;
import net.shadowmage.ancientwarfare.npc.ai.faction.NpcAIFactionRestrictSun;
import net.shadowmage.ancientwarfare.npc.config.AWNPCStatics;
import net.shadowmage.ancientwarfare.npc.entity.NpcBase;
import net.shadowmage.ancientwarfare.npc.entity.NpcPlayerOwned;
import net.shadowmage.ancientwarfare.npc.entity.faction.attributes.AdditionalAttributes;
import net.shadowmage.ancientwarfare.npc.entity.faction.attributes.IAdditionalAttribute;
import net.shadowmage.ancientwarfare.npc.faction.FactionTracker;
import net.shadowmage.ancientwarfare.npc.init.AWNPCSounds;
import net.shadowmage.ancientwarfare.npc.item.ItemCommandBaton;
import net.shadowmage.ancientwarfare.npc.registry.*;
import net.shadowmage.ancientwarfare.structure.event.OneShotEntityDespawnListener;
import net.shadowmage.ancientwarfare.structure.util.RespawnData;
import org.apache.commons.lang3.Range;

import javax.annotation.Nullable;
import java.util.*;

@SuppressWarnings({"squid:MaximumInheritanceDepth", "squid:S2160"})
public abstract class NpcFaction extends NpcBase {
    private static final int DEATH_REVENGE_TICKS = 6000;
    private static final int HIT_REVENGE_TICKS = DEATH_REVENGE_TICKS / 20;
    private static final int REVENGE_LIST_VALIDATION_TICKS = DEATH_REVENGE_TICKS / 100;
    private static final double REVENGE_SET_RANGE = 50D;
    private static final String HEIGHT_TAG = "height";
    private static final EntityDataAccessor<String> SOUND = SynchedEntityData.defineId(NpcFaction.class, EntityDataSerializers.STRING);
    private float npcHeight = 1.8f;
    private float npcWidth = 0.6f;

    protected String factionName;
    private Map<String, Long> revengePlayers = new HashMap<>();
    public double dialogueSeed;

    public NpcFaction(Level world) {
        super(world);
        addAI();
    }

    public NpcFaction(Level world, String factionName) {
        super(world);
        setFactionNameAndDefaults(factionName);
        addAI();
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(SOUND, "none");
        // Get a random seeded number for dialogue.
        this.dialogueSeed = Math.random();
    }

    private final Map<IAdditionalAttribute<?>, Object> additionalAttributes = new HashMap<>();
    private boolean canDespawn = false;

    private void setDeathRevengePlayer(String playerName) {
        revengePlayers.put(playerName, level().getGameTime() + DEATH_REVENGE_TICKS);
    }

    @Override
    public FactionNpcDefault getNpcDefault() {
        return NpcDefaultsRegistry.getFactionNpcDefault(this);
    }

    @Override
    public void checkDespawn() {
        boolean removedBefore = isRemoved();
        Level currentLevel = level();

        // Snapshot before Mob#checkDespawn can call discard(). EntityLeaveLevelEvent
        // invalidates Forge capabilities synchronously, so reading the capability
        // after super.checkDespawn() loses the one-shot spawner data.
        RespawnData respawnSnapshot = removedBefore
                ? null
                : OneShotEntityDespawnListener.INSTANCE.snapshotRespawnData(this);

        super.checkDespawn();

        if (!removedBefore && isRemoved() && respawnSnapshot != null) {
            OneShotEntityDespawnListener.INSTANCE.queueRespawn(respawnSnapshot, currentLevel);
        }
    }

    public void setCanDespawn() {
        canDespawn = true;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return canDespawn;
    }

    private void addAI() {
        goalSelector.addGoal(2, new NpcAIFactionRestrictSun(this));
        goalSelector.addGoal(3, new NpcAIFactionFleeSun(this, 1.0D));
    }

    public void setAdditionalAttribute(IAdditionalAttribute<?> attribute, Object value) {
        additionalAttributes.put(attribute, value);
    }

    public <T> Optional<T> getAdditionalAttributeValue(IAdditionalAttribute<T> attribute) {
        return Optional.ofNullable(attribute.getValueClass().cast(additionalAttributes.get(attribute)));
    }

    public boolean burnsInSun() {
        return getAdditionalAttributeValue(AdditionalAttributes.BURNS_IN_SUN).orElse(false);
    }

    public boolean isUndead() {
        return getAdditionalAttributeValue(AdditionalAttributes.UNDEAD).orElse(false);
    }

    @Override
    public void aiStep() {
        doSunBurn();
        super.aiStep();
    }

    private void doSunBurn() {
        if (burnsInSun() && level().isDay() && !level().isClientSide) {
            float brightness = getLightLevelDependentMagicValue();
            BlockPos blockpos = getVehicle() instanceof Boat ? blockPosition().above() : blockPosition();

            if (brightness > 0.5F && getRandom().nextFloat() * 30.0F < (brightness - 0.4F) * 2.0F && level().canSeeSky(blockpos)) {
                ItemStack helmet = getItemBySlot(EquipmentSlot.HEAD);
                if (helmet.isEmpty()) {
                    setSecondsOnFire(8);
                } else {
                    damageHelmet(helmet);
                }
            }
        }
    }

    private void damageHelmet(ItemStack helmet) {
        if (helmet.isDamageableItem()) {
            helmet.setDamageValue(helmet.getDamageValue() + getRandom().nextInt(2));

            if (helmet.getDamageValue() >= helmet.getMaxDamage()) {
                broadcastBreakEvent(EquipmentSlot.HEAD);
                setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
            }
        }
    }

    @Override
    public MobType getMobType() {
        if (isUndead()) {
            return MobType.UNDEAD;
        } else {
            return MobType.UNDEFINED;
        }
    }

    @Override
    public boolean canBeAffected(MobEffectInstance effect) { // makes lizardmen and coven immune to poison
        if (effect.getEffect() == MobEffects.POISON && (getFaction().equals("lizardman") || getFaction().equals("coven"))) {
            return false;
        }
        return super.canBeAffected(effect);
    }

    public void setFactionNameAndDefaults(String factionName) {
        this.factionName = factionName;
        FactionNpcDefault npcDefault = NpcDefaultsRegistry.getFactionNpcDefault(this);
        applyFactionNpcSettings(npcDefault);
        // do not apply the default equipment if the hasCustomEquipment tag was set to true
        if (!getCustomEquipmentOverride()) {
            npcDefault.applyEquipment(this);
        }

        if (AWNPCStatics.vanillaEquipmentDropRate) {
            // necessary to reset this to the default values as many old structures had NPCs scanned with 1.0f drop rates
            Arrays.fill(armorDropChances, 0.085f);
            Arrays.fill(handDropChances, 0.085f);
        } else {
            // makes faction NPCs drop all their items otherwise use the default vanilla drop rate
            Arrays.fill(armorDropChances, 1.f);
            Arrays.fill(handDropChances, 1.f);
        }

        Range<Float> heightRange = npcDefault.getHeightRange();
        float newHeight = heightRange.getMinimum() + getRandom().nextFloat() * (heightRange.getMaximum() - heightRange.getMinimum());
        float newWidth = (newHeight / 1.8f) * 0.6f * npcDefault.getThinness();
        setNpcSize(newWidth, newHeight);
    }

    protected void setNpcSize(float width, float height) {
        npcWidth = Mth.clamp(width, 0.1f, 10f);
        npcHeight = Mth.clamp(height, 0.1f, 40f);
        refreshDimensions();
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return isSleeping() ? super.getDimensions(pose) : EntityDimensions.scalable(npcWidth, npcHeight);
    }

    private void applyFactionNpcSettings(FactionNpcDefault npcDefault) {
        npcDefault.applyAttributes(this);
        npcDefault.applyAdditionalAttributes(this);
        xpReward = npcDefault.getExperienceDrop();
        npcDefault.applyPathSettings((GroundPathNavigation) getNavigation());
        String sound = getAdditionalAttributeValue(AdditionalAttributes.ENTITY_SOUND).isPresent() ? getAdditionalAttributeValue(AdditionalAttributes.ENTITY_SOUND).get() : "none";
        setSound(sound);
    }

    @Override
    public int getMaxFallDistance() {
        int i = super.getMaxFallDistance();
        if (i > 4) {
            i += level().getDifficulty().getId() * getMaxHealth() / 5;
        }
        if (i >= getHealth()) {
            return (int) getHealth();
        }
        return i;
    }

    @Override
    protected boolean tryCommand(Player player) {
        return player.isCreative() && super.tryCommand(player);
    }

    @Override
    public boolean hasCommandPermissions(UUID playerUuid, String playerName) {
        return false;
    }

    @Override
    @SuppressWarnings("squid:CallToDeprecatedMethod") //need to use I18n call that's available server side for this
    public String getNpcName() {
        //noinspection deprecation
        String name = I18n.get("entity.ancientwarfarenpc." + getNpcFullType() + ".name");
        if (hasCustomName()) {
            name = getCustomName().getString();
        }
        return name;
    }

    @Override
    protected float getLitBlockWeight(BlockPos pos) {
        return burnsInSun() ? 1F - level().getLightLevelDependentMagicValue(pos) : super.getLitBlockWeight(pos);
    }

    @Override
    public String getNpcFullType() {
        return factionName + "." + super.getNpcFullType();
    }

    @Override
    public boolean isHostileTowards(Entity e) {
        if (e instanceof Player || e instanceof NpcPlayerOwned) {
            String playerName = e instanceof Player ? e.getName().getString() : ((NpcBase) e).getOwner().getName();
            UUID playerUUID = e instanceof Player ? e.getUUID() : ((NpcBase) e).getOwner().getUUID();
            return revengePlayers.containsKey(playerName) || FactionTracker.INSTANCE.isHostileToPlayer(level(), playerUUID, playerName, getFaction());
        } else if (e instanceof NpcFaction) {
            NpcFaction npc = (NpcFaction) e;
            return !npc.getFaction().equals(factionName) && FactionRegistry.getFaction(getFaction()).isHostileTowards(npc.getFaction());
        } else {
            return FactionRegistry.getFaction(factionName).isTarget(e) || AIHelper.isAdditionalEntityToTarget(e);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (level().getDayTime() % REVENGE_LIST_VALIDATION_TICKS == 0) {
            Iterator<Map.Entry<String, Long>> it = revengePlayers.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, Long> playerRevengeTime = it.next();
                if (level().getGameTime() > playerRevengeTime.getValue()) {
                    it.remove();
                    setTarget(null);
                    setLastHurtByMob(null);
                }
            }
        }

    }

    private String getSound() {
        return entityData.get(SOUND);
    }

    public void setSound(String sound) {
        entityData.set(SOUND, sound);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        SoundEvent sound = "none".equals(getSound()) ? null : AWNPCSounds.getSoundEventFromString(getSound() + "_hurt");
        return sound == null ? SoundEvents.PLAYER_HURT : sound;
    }

    @Override
    protected SoundEvent getDeathSound() {
        SoundEvent sound = "none".equals(getSound()) ? null : AWNPCSounds.getSoundEventFromString(getSound() + "_death");
        return sound == null ? SoundEvents.PLAYER_DEATH : sound;
    }

    private void playAttackSound() {
        if ("none".equals(getSound())) {
            return;
        }
        SoundEvent sound = AWNPCSounds.getSoundEventFromString(getSound() + "_attack");
        if (sound != null) {
            playSound(sound, 1.0F, 1.2F / (getRandom().nextFloat() * 0.2F + 0.9F));
        }
    }

    @Override
    protected void actuallyHurt(DamageSource damageSrc, float damageAmount) {
        super.actuallyHurt(damageSrc, damageAmount);
        Entity source = damageSrc.getEntity();
        if (source instanceof Player) {
            revengePlayers.put(source.getName().getString(), level().getGameTime() + HIT_REVENGE_TICKS);
        } else if (source instanceof NpcBase npc) {
            revengePlayers.put(npc.getOwner().getName(), level().getGameTime() + HIT_REVENGE_TICKS);
        }
    }

    @Override
    public boolean canTarget(Entity e) {
        // Stealth code, takes into account invisibility, line of sight, sneaking, etc
        // If invis, range multiplier is 0.1x
        // If LOS is blocked, range multiplier is 0.75x
        // If sneaking, range multiplier is 0.5x
        // (These values are configurable.)
        // These can all stack if they are enabled. Base follow range uses the attribute.
        double adjustedTargettingRange = this.getAttributeValue(Attributes.FOLLOW_RANGE);
        if (e.isInvisible()) {
            adjustedTargettingRange *= AWCoreStatics.invisibilityFollowRangePenalty;
        }
        if (e.isCrouching()) {
            adjustedTargettingRange *= AWCoreStatics.sneakingFollowRangePenalty;
        }
        if (e instanceof LivingEntity living && !this.getSensing().hasLineOfSight(living)) {
            adjustedTargettingRange *= AWCoreStatics.obscuredFollowRangePenalty;
        }
        if (this.distanceTo(e) > adjustedTargettingRange) {
            // Cannot detect the target entity.
            return false;
        }

        if (e instanceof NpcFaction) {
            return !((NpcFaction) e).getFaction().equals(getFaction());
        }
        return e instanceof LivingEntity;
    }

    @Override
    public boolean canBeAttackedBy(Entity e) {
        //can only be attacked by other factions, not your own...disable friendly fire
        return !(e instanceof NpcFaction) || !getFaction().equals(((NpcFaction) e).getFaction());
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if ((Math.random() < 0.2)) {
            playAttackSound();
        }
        return super.doHurtTarget(target);
    }

    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);
        Player player;
        String playerName;
        Entity source = damageSource.getEntity();

        if (source instanceof Player || source instanceof NpcPlayerOwned) {
            playerName = source instanceof Player ? source.getName().getString() :
                    ((NpcBase) source).getOwner().getName();
            FactionTracker.INSTANCE.adjustStandingFor(level(), playerName, getFaction(), FactionRegistry.getFaction(getFaction()).getStandingSettings().getStandingChange(StandingChanges.KILL));
            setDeathRevengePlayer(playerName);
            level().getEntitiesOfClass(NpcFaction.class, getBoundingBox().inflate(REVENGE_SET_RANGE))
                    .forEach(factionNpc -> {
                        if (factionNpc.getFaction().equals(getFaction())) {
                            factionNpc.setDeathRevengePlayer(playerName);
                        }
                    });
            // If the Nemesis factions mechanic is enabled:
            if (AWCoreStatics.nemesisFactions) {
                try {
                    player = source instanceof Player ? (Player) source :
                            level().getPlayerByUUID(((NpcBase) source).getOwner().getUUID());
                } catch (ClassCastException e) {
                    System.out.println("AW2t: Nemesis system could not find player who killed NPC.");
                    return;
                }
                if (AWCoreStatics.nemesisFactionsMap.containsKey(getFaction())) {
                    String nemesisFaction = ((String) AWCoreStatics.nemesisFactionsMap.get(getFaction())).toLowerCase();
                    // getFaction returns a unique empty faction if the faction name is not valid
                    if (FactionRegistry.getFaction(nemesisFaction).equals(FactionRegistry.EMPTY_FACTION)) {
                        System.out.println("AW2t: Invalid faction found in nemesis list! " + nemesisFaction);
                    } else {
                        // Both faction names were valid, do nemesis standings change
                        String prettyNemesisFactionName = Component.translatable("gui.ancientwarfarenpc.faction_name." + nemesisFaction).getString();
                        if (AWCoreStatics.DEBUG)
                            System.out.println("AW2t: Nemesis system adjusted standings for " + nemesisFaction);
                        if (AWCoreStatics.showLargeNemesisRepChanges) {
                            int currentStanding = FactionTracker.INSTANCE.getStandingFor(level(), playerName, nemesisFaction);
                            // If the faction is hostile to the player, but will become neutral/friendly due to this nemesis rep, play the big message
                            if (currentStanding < 0 && currentStanding + AWCoreStatics.nemesisRepChange >= 0) {
                                String message = "The " + prettyNemesisFactionName + " are pleased with the devastation you have wrought upon their enemies. They will welcome you with open arms.";
                                player.sendSystemMessage(Component.literal(ChatFormatting.DARK_AQUA + message));
                            }
                        }
                        if (AWCoreStatics.showSmallNemesisRepChanges) {
                            String message;
                            if (AWCoreStatics.nemesisRepChange > 0) {
                                message = "+" + AWCoreStatics.nemesisRepChange + " rep with the " + prettyNemesisFactionName;
                            } else {
                                message = AWCoreStatics.nemesisRepChange + " rep with the " + prettyNemesisFactionName;
                            }
                            player.sendSystemMessage(Component.literal(ChatFormatting.DARK_AQUA + message));
                        }
                        FactionTracker.INSTANCE.adjustStandingFor(level(), playerName, nemesisFaction, AWCoreStatics.nemesisRepChange);
                    }
                } else {
                    System.out.println("AW2t: Nemesis system did not find faction " + getFaction() + " in list.");
                    if (AWCoreStatics.DEBUG)
                        System.out.println("AW2t: Full list: " + AWCoreStatics.nemesisFactionsMap.toString());
                }
            } else {
                if (AWCoreStatics.DEBUG) System.out.println("AW2t: Nemesis system disabled in config; skipping");
            }
        }
    }

    @Nullable
    @Override
    protected ResourceLocation getDefaultLootTable() {
        return NpcDefaultsRegistry.getFactionNpcDefault(this).getLootTable();
    }

    @Override
    public String getNpcSubType() {
        return "";
    }

    public String getFaction() {
        return factionName;
    }

    @Override
    public Team getTeam() {
        return null;
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        super.writeSpawnData(buffer);
        buffer.writeUtf(factionName);
        buffer.writeFloat(npcHeight);
        buffer.writeFloat(npcWidth);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf buffer) {
        super.readSpawnData(buffer);
        factionName = buffer.readUtf(20);
        npcHeight = buffer.readFloat();
        npcWidth = buffer.readFloat();
        refreshDimensions();
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        factionName = tag.getString("factionName");
        applyFactionNpcSettings(NpcDefaultsRegistry.getFactionNpcDefault(this));
        canDespawn = tag.getBoolean("canDespawn");
        revengePlayers = NBTHelper.getMap(tag.getList("revengePlayers", Tag.TAG_COMPOUND),
                t -> t.getString("playerName"), t -> t.getLong("time"));
        if (tag.contains(HEIGHT_TAG)) {
            setNpcSize(tag.getFloat("width"), tag.getFloat(HEIGHT_TAG));
        } else {
            setFactionNameAndDefaults(factionName);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (factionName != null) {
            tag.putString("factionName", factionName);
        }
        tag.putBoolean("canDespawn", canDespawn);
        tag.put("revengePlayers", NBTHelper.mapToCompoundList(revengePlayers,
                (t, playerName) -> t.putString("playerName", playerName), (t, time) -> t.putLong("time", time)));
        tag.putFloat(HEIGHT_TAG, npcHeight);
        tag.putFloat("width", npcWidth);
    }

    public float getRenderSizeModifier() {
        return npcHeight / 1.8f;
    }

    public float getWidthModifier() {
        return npcWidth / 0.6f;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        // Allow creative mode players to still access GUIs for NPC setup.
        if (player.isCreative()) {
            return super.mobInteract(player, hand);
        }
        boolean playerIsHoldingItem = !player.getItemInHand(hand).isEmpty();
        // If the NPC is hostile to the player, and the player is holding an item, skip the dialogue.
        // This is necessary so that dialogue does not take priority over the right-click action of weapons or shields.
        // A player right-clicking a shield to a hostile NPC probably wants to block, not talk.
        if (this.isHostileTowards(player) && playerIsHoldingItem) {
            return super.mobInteract(player, hand);
        }
        boolean baton = playerIsHoldingItem && player.getItemInHand(hand).getItem() instanceof ItemCommandBaton;
        if (AWCoreStatics.npcDialogue && !baton && isAlive()) {
            if (!player.level().isClientSide) {
                // Say something to the player who right-clicked, pass in the NPC object containing important data
                NPCDialogue.speakToPlayer(player, this);
            }
            return InteractionResult.sidedSuccess(level().isClientSide);
        }
        return InteractionResult.PASS;
    }
}
