package net.shadowmage.ancientwarfare.npc.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import net.shadowmage.ancientwarfare.core.owner.TeamViewerRegistry;
import net.shadowmage.ancientwarfare.npc.AncientWarfareNPC;
import net.shadowmage.ancientwarfare.npc.entity.NpcBase;
import net.shadowmage.ancientwarfare.npc.entity.NpcCombat;
import net.shadowmage.ancientwarfare.npc.entity.NpcPlayerOwned;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;

public class NpcAIFleeHostiles extends NpcAI<NpcPlayerOwned> {

    private static final int MAX_STAY_AWAY = 50;
    private static final int MAX_FLEE_RANGE = 16;
    private static final int HEIGHT_CHECK = 7;
    private static final int PURSUE_RANGE = 16 * 16;
    private static final double DISTANCE_FROM_ENTITY = 16;

    @SuppressWarnings({"java:S4738", "Guava"}) //need to use Guava here becuase vanilla code uses it
    private final Predicate<Mob> hostileOrFriendlyCombatNpcSelector;
    private final Comparator<Entity> sorter;
    private Vec3 fleeVector;
    private int stayOutOfSightTimer = 0;
    private int fearLevel = 0; // fear makes NPC's wait/flee for progressively longer periods
    private boolean homeCompromised = false;
    private final LinkedHashSet<NpcCombat> nearbySoldiers = new LinkedHashSet<>();

    private int ticker = 0;
    private static final int TICKER_MAX = 5; // scan for hostiles every 5 ticks

    public NpcAIFleeHostiles(final NpcPlayerOwned npc) {
        super(npc);
        hostileOrFriendlyCombatNpcSelector = selectedEntity -> {
            if (selectedEntity != null && selectedEntity.isAlive()) {
                if (selectedEntity instanceof NpcBase) {
                    return isFriendlySoldier(npc, (NpcBase) selectedEntity);
                } else {
                    return NpcAIFleeHostiles.this.npc.isHostileTowards(selectedEntity);
                }
            }
            return false;
        };

        sorter = Comparator.comparingDouble(npc::distanceToSqr);
        setMutexBits(MOVE);
    }

    private boolean isFriendlySoldier(NpcPlayerOwned npc, NpcBase selectedEntity) {
        return (selectedEntity.getNpcSubType().equals("soldier") && selectedEntity.hasCommandPermissions(npc.getOwner()))
                || selectedEntity.isHostileTowards(NpcAIFleeHostiles.this.npc);
    }

    @Override
    public boolean canUse() {
        if (!super.canUse()) {
            return false;
        }

        if (stayOutOfSightTimer > 0) {
            return true;
        }

        ticker++;
        if (ticker != TICKER_MAX) {
            return false;
        }
        ticker = 0;

        boolean flee = false;
        findNearbyRelevantEntities();
        if (!npc.nearbyHostiles.isEmpty()) {
            Entity nearestHostile = npc.nearbyHostiles.iterator().next();
            if (npc.getTownHallPosition().isPresent() || npc.hasRestriction()) {
                flee = true;
            } else {
                fleeVector = DefaultRandomPos.getPosAway(npc, MAX_FLEE_RANGE, HEIGHT_CHECK, new Vec3(nearestHostile.getX(), nearestHostile.getY(), nearestHostile.getZ()));
                flee = fleeVector != null && nearestHostile.distanceToSqr(fleeVector.x, fleeVector.y, fleeVector.z) >= nearestHostile.distanceToSqr(npc);
            }
            if (flee) {
                stayOutOfSightTimer = MAX_STAY_AWAY + fearLevel;
                if (nearestHostile instanceof LivingEntity) {
                    npc.setTarget((LivingEntity) nearestHostile);
                } else {
                    AncientWarfareNPC.LOG.error("Attempted to flee an entity that isn't Mob: '{}', ignoring! Please report this error.", ForgeRegistries.ENTITY_TYPES.getKey(nearestHostile.getType()));
                }
            }
        }
        return flee;
    }

    private void findNearbyRelevantEntities() {
        npc.nearbyHostiles.clear();
        nearbySoldiers.clear();
        List<Mob> nearbyHostilesOrFriendlySoldiers = npc.level().getEntitiesOfClass(Mob.class,
                npc.getBoundingBox().inflate(DISTANCE_FROM_ENTITY, 3.0D, DISTANCE_FROM_ENTITY), hostileOrFriendlyCombatNpcSelector);
        if (nearbyHostilesOrFriendlySoldiers.isEmpty()) {
            return;
        }

        nearbyHostilesOrFriendlySoldiers.sort(sorter);
        for (Mob entity : nearbyHostilesOrFriendlySoldiers) {
            if (npc.hasLineOfSight(entity)) {
                if (entity instanceof NpcCombat) {
                    if (((NpcCombat) entity).getNpcSubType().equals("soldier") && ((NpcCombat) entity).hasCommandPermissions(npc.getOwner())) {
                        nearbySoldiers.add((NpcCombat) entity);
                    }
                } else if (entity instanceof OwnableEntity ownable && ownable.getOwner() != null) {
                    Entity owner = ownable.getOwner();
                    if (isFriendly(entity.level(), owner.getUUID(), owner.getName().getString())) {
                        return;
                    }
                } else {
                    npc.nearbyHostiles.add(entity);
                }
            }
        }
    }
    // check if  the owner is same or owned by a team member or owned by a friend

    public boolean isFriendly(Level world, @Nullable UUID ownerId, String ownerName) {
        return TeamViewerRegistry.areFriendly(world, npc.getOwner().getUUID(), ownerId, npc.getOwner().getName(), ownerName);
    }

    @Override
    public void start() {
        npc.addAITask(TASK_GO_HOME);
        npc.addAITask(TASK_FLEE);
        stayOutOfSightTimer = MAX_STAY_AWAY + fearLevel;
        fearLevel += MAX_STAY_AWAY;
    }

    @Override
    public void tick() {
        if (fearLevel > 0) {
            fearLevel--;
        }

        if (npc.getTarget() == null || !npc.getTarget().isAlive()) {
            npc.setTarget(null);
            stayOutOfSightTimer = 0;
            return;
        }
        BlockPos pos = null;
        double distSq = 0;

        if (npc.hasRestriction()) {
            distSq = npc.getDistanceSqFromHome();
            pos = npc.getRestrictCenter();
            if (distSq < MIN_RANGE && !npc.nearbyHostiles.isEmpty()) {
                homeCompromised = true;
                npc.addAITask(TASK_ALARM);
            }
        }

        if (homeCompromised || !npc.hasRestriction()) {
            Optional<BlockPos> townHallPosition = npc.getTownHallPosition();
            if (townHallPosition.isPresent()) {
                pos = townHallPosition.get();
                distSq = npc.getDistanceSq(pos);
                if (distSq <= MIN_RANGE) {
                    npc.getNavigation().stop();
                    announceDistress();
                }
            }
        }

        if (pos == null) {
            if (fleeVector == null) {
                return;
            }
            //check distance to flee vector
            distSq = npc.distanceToSqr(fleeVector.x, fleeVector.y, fleeVector.z);
            if (distSq > MIN_RANGE) {
                moveToPosition(fleeVector.x, fleeVector.y, fleeVector.z, distSq);
            } else {
                if (npc.distanceToSqr(npc.getTarget()) < PURSUE_RANGE) {//entity still chasing, find a new flee vector
                    fleeVector = DefaultRandomPos.getPosAway(npc, MAX_FLEE_RANGE, HEIGHT_CHECK, new Vec3(npc.getTarget().getX(), npc.getTarget().getY(), npc.getTarget().getZ()));
                    if (fleeVector == null) {
                        npc.setTarget(null);//retry next tick..perhaps...
                    }
                } else // entity too far to care, stop running
                {
                    npc.setTarget(null);
                }
            }
        }

        // keep running home
        if (pos != null && distSq > MIN_RANGE) {
            moveToPosition(pos, distSq);
        }
        if (stayOutOfSightTimer > 0) {
            stayOutOfSightTimer--;
        } else {
            announceDistress();
        }
    }

    @Override
    public void stop() {
        fleeVector = null;
        npc.getNavigation().stop();
        npc.setTarget(null);
        npc.removeAITask(TASK_GO_HOME + TASK_FLEE + TASK_ALARM);
        homeCompromised = false;
    }

    private void announceDistress() {
        for (NpcCombat soldier : nearbySoldiers) {
            soldier.respondToDistress(npc);
        }
    }
}
