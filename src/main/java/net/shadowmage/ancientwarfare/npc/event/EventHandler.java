package net.shadowmage.ancientwarfare.npc.event;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.shadowmage.ancientwarfare.core.config.AWCoreStatics;
import net.shadowmage.ancientwarfare.core.gamedata.AWGameData;
import net.shadowmage.ancientwarfare.core.util.WorldTools;
import net.shadowmage.ancientwarfare.npc.entity.NpcBase;
import net.shadowmage.ancientwarfare.npc.entity.NpcPlayerOwned;
import net.shadowmage.ancientwarfare.npc.entity.faction.NpcFaction;
import net.shadowmage.ancientwarfare.npc.registry.FactionRegistry;
import net.shadowmage.ancientwarfare.npc.registry.NpcDefaultsRegistry;
import net.shadowmage.ancientwarfare.structure.gamedata.StructureMap;
import net.shadowmage.ancientwarfare.structure.gamedata.TownMap;
import net.shadowmage.ancientwarfare.structure.init.AWStructureBlocks;
import net.shadowmage.ancientwarfare.structure.tile.ISpecialLootContainer;
import net.shadowmage.ancientwarfare.structure.tile.TileProtectionFlag;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

public class EventHandler {
    public static final String NO_SPAWN_PREVENTION_TAG = "noSpawnPrevention";
    private Set<Predicate<Goal>> additionalHostileAIChecks = new HashSet<>();
    public static final EventHandler INSTANCE = new EventHandler();

    private EventHandler() {
    }

    public void registerAdditionalHostileAICheck(Predicate<Goal> aiCheck) {
        additionalHostileAIChecks.add(aiCheck);
    }

    private int getHostileAIPriority(PathfinderMob entity) {
        for (WrappedGoal wrappedGoal : entity.targetSelector.getAvailableGoals()) {
            Goal goal = wrappedGoal.getGoal();
            if ((goal instanceof NearestAttackableTargetGoal<?> || anyAdditionalHostileAIChecksMatch(goal))
                    && wrappedGoal.getPriority() != -1) {
                return wrappedGoal.getPriority();
            }
        }
        return -1;
    }

    private boolean anyAdditionalHostileAIChecksMatch(Goal goal) {
        for (Predicate<Goal> additionalCheck : additionalHostileAIChecks) {
            if (additionalCheck.test(goal)) {
                return true;
            }
        }
        return false;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onEntityJoinWorld(EntityJoinLevelEvent event) {
        injectAi(event);
        preventHostileSpawnsInStructures(event);
    }

    private void preventHostileSpawnsInStructures(EntityJoinLevelEvent event) {
        Entity entity = event.getEntity();
        if (entity.level().isClientSide || !(entity instanceof Enemy) || isMarkedWithNoPreventionTag(entity)) {
            return;
        }

        Level world = event.getLevel();

        BlockPos pos = event.getEntity().blockPosition();

        if (AWGameData.INSTANCE.getPerWorldData(world, StructureMap.class).shouldPreventSpawnAtPos(world, pos)
                || AWGameData.INSTANCE.getPerWorldData(world, TownMap.class).shouldPreventSpawnAtPos(world, pos)) {
            event.setCanceled(true);
        }
    }

    private boolean isMarkedWithNoPreventionTag(Entity entity) {
        return entity.getTags().contains(NO_SPAWN_PREVENTION_TAG);
    }

    private void injectAi(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof NpcBase) {
            return;
        }
        if (!(event.getEntity() instanceof PathfinderMob)) {
            return;
        }
        // Use new "auto injection"
        PathfinderMob entity = (PathfinderMob) event.getEntity();

        // all mobs that can attack somebody should have targetTasks
        int targetTaskPriority = getHostileAIPriority(entity);
        if (targetTaskPriority != -1) {
            entity.targetSelector.addGoal(targetTaskPriority, new NearestAttackableTargetGoal<>(entity, NpcBase.class, 0, true, false,
                    e -> entityShouldTarget(entity, e instanceof NpcBase ? (NpcBase) e : null)
            ));
        }
    }

    private boolean entityShouldTarget(PathfinderMob entity, @Nullable NpcBase e) {
        if (e == null) {
            return false;
        }

        if ((e instanceof NpcFaction) && !e.isPassive() && FactionRegistry.getFaction(((NpcFaction) e).getFaction()).isTarget(entity)) {
            return true;
        }

        return (e instanceof NpcPlayerOwned) && NpcDefaultsRegistry.getOwnedNpcDefault((NpcPlayerOwned) e).isTarget(entity);

    }

    @SubscribeEvent
    public void onChestClicked(PlayerInteractEvent.RightClickBlock evt) {
        if (!AWCoreStatics.chestProtection)
            return; // No need to check for locked chests when this config option is false
        Level world = evt.getLevel();
        BlockPos pos = evt.getPos();
        Player player = evt.getEntity();
        if (!player.getAbilities().instabuild && isLockedContainer(world, pos)) {
            AWGameData.INSTANCE.getPerWorldData(world, StructureMap.class).getStructureAt(world, pos).ifPresent(structure -> {
                Optional<TileProtectionFlag> tile = WorldTools.getTile(world, structure.getProtectionFlagPos(), TileProtectionFlag.class);
                if (AWCoreStatics.allowStealing) {
//					System.out.println("Allow stealing attempt!");
                    // Skip the flag protection. Always let the player loot chests if no one is around to see it
                    for (NpcFaction factionNpc : world.getEntitiesOfClass(NpcFaction.class, structure.getBB().getAABB())) {
                        // If one of the entities can see the player, prevent them from opening the chest.
                        if (factionNpc.canTarget(player) && player.hasLineOfSight(factionNpc)) {
                            evt.setCanceled(true);
                            evt.setCancellationResult(InteractionResult.FAIL);
                            factionNpc.addEffect(new MobEffectInstance(MobEffects.GLOWING, AWCoreStatics.glowDuration));
                            if (world.isClientSide) {
                                player.displayClientMessage(Component.translatable("gui.ancientwarfarenpc.no_chest_stealing",
                                        StringUtils.capitalize(factionNpc.getFaction())), true);
                            }
                            return;
                        }
                    }
                } else { // Old code for full loot chest protection. Player must clear enemies and claim the flag to open chests.
//					System.out.println("Old-school chest protection!");
                    if (tile.isPresent() && tile.get().shouldProtectAgainst(player)) {
                        evt.setCanceled(true);
                        evt.setCancellationResult(InteractionResult.FAIL);
                        if (world.isClientSide) {
                            player.displayClientMessage(Component.translatable("gui.ancientwarfarenpc.no_chest_access_flag_not_claimed"), true);
                        }
                    } else {
                        for (NpcFaction factionNpc : world.getEntitiesOfClass(NpcFaction.class, structure.getBB().getAABB())) {
                            if (!factionNpc.isPassive()) {
                                evt.setCanceled(true);
                                evt.setCancellationResult(InteractionResult.FAIL);
                                factionNpc.addEffect(new MobEffectInstance(MobEffects.GLOWING, AWCoreStatics.glowDuration));
                                if (world.isClientSide) {
                                    player.displayClientMessage(Component.translatable("gui.ancientwarfarenpc.no_chest_access",
                                            StringUtils.capitalize(factionNpc.getFaction())), true);
                                }
                                return;
                            }
                        }
                    }
                }
            });
        }
    }

    @SubscribeEvent
    public void playerDigSpeed(PlayerEvent.BreakSpeed evt) {
        Player player = evt.getEntity();
        if (player.isCreative() || player.isSpectator()) {
            return;
        }

        Optional<BlockPos> position = evt.getPosition();
        if (!position.isPresent()) {
            return;
        }
        BlockPos pos = position.get();

        Level world = player.level();
        AWGameData.INSTANCE.getPerWorldData(world, StructureMap.class).getStructureAt(world, pos)
                .flatMap(structureEntry -> WorldTools.getTile(world, structureEntry.getProtectionFlagPos(), TileProtectionFlag.class)).ifPresent(tile -> {
                    if (tile.shouldProtectAgainst(player) && shouldBlockSlowDownDigging(world, pos)) {
                        evt.setNewSpeed(evt.getOriginalSpeed() / AWCoreStatics.blockProtectionMulti);
                    }
                });
    }

    private boolean shouldBlockSlowDownDigging(Level world, BlockPos pos) {
        if (!AWCoreStatics.blockProtection) return false;
        BlockState state = world.getBlockState(pos);
        if (!state.isSolidRender(world, pos)) {
            return isLockedContainer(world, pos);
        }

        Block block = state.getBlock();
        return !(block == Blocks.SPAWNER || block == AWStructureBlocks.ADVANCED_SPAWNER.get());
    }

    public static final String GENERATED_INVENTORY_TAG = "generatedInventory";

    private boolean isLockedContainer(Level world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        if (!state.hasBlockEntity()) {
            return false;
        }

        return WorldTools.getTile(world, pos).map(te -> te.getPersistentData().getBoolean(GENERATED_INVENTORY_TAG) || te instanceof ISpecialLootContainer).orElse(false);
    }

    @SubscribeEvent
    public void onLivingAttack(LivingAttackEvent evt) {
        // When demons are going to take damage, if it is fire/lava/heat damage, ignore it.
        if (AWCoreStatics.demonsImmuneToFire && evt.getSource().is(DamageTypeTags.IS_FIRE)) {
            // Only active if the demons_immune_to_fire option is enabled in the config.
            if (evt.getEntity() instanceof NpcFaction && ((NpcFaction) evt.getEntity()).getFaction().equals("demon")) {
                evt.setCanceled(true);
            }
        }
    }

}
