package net.shadowmage.ancientwarfare.npc.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import net.shadowmage.ancientwarfare.npc.entity.NpcBase;

import javax.annotation.Nullable;
import java.util.EnumSet;

/*
 * AI template class with utility methods and member access for a non-specific NPC type
 *
 * @author Shadowmage
 */
public abstract class NpcAI<T extends NpcBase> extends Goal {
    /*
     * used during npc-ai task-rendering to determine how many bits to loop through
     * of the task bitfield -- needs to be increased if more task types / bits are added
     */
    public static final int NUMBER_OF_TASKS = 14;
    public static final int TASK_ATTACK = 1;
    public static final int TASK_UPKEEP = 2;
    public static final int TASK_IDLE_HUNGRY = 4;
    public static final int TASK_GO_HOME = 8;
    public static final int TASK_WORK = 16;
    public static final int TASK_PATROL = 32;
    public static final int TASK_GUARD = 64;
    public static final int TASK_FOLLOW = 128;
    public static final int TASK_WANDER = 256;
    public static final int TASK_MOVE = 512;
    public static final int TASK_ALARM = 1024;
    public static final int TASK_FLEE = 2048;
    public static final int TASK_SLEEP = 4096;
    public static final int TASK_RAIN = 8192;

    /*
     * internal flag used to determine exclusion types
     */
    public static final int MOVE = 1;
    public static final int ATTACK = 2;
    public static final int SWIM = 4;
    public static final int HUNGRY = 8;

    public static final int MIN_RANGE = 9;

    protected int moveRetryDelay;
    protected double moveSpeed = 1.d;
    private final double maxPFDist;
    private final double maxPFDistSq;
    private int legacyMutexBits;

    protected T npc;

    public NpcAI(T npc) {
        this.npc = npc;
        maxPFDist = npc.getAttributeValue(Attributes.FOLLOW_RANGE) * 0.90d;

        maxPFDistSq = maxPFDist * maxPFDist;
    }

    /**
     * Retains Ancient Warfare's four internal mutex bits while mapping the
     * movement/attack/swim portions to the vanilla 1.20.1 Goal flags.
     */
    protected final void setMutexBits(int bits) {
        this.legacyMutexBits = bits;
        EnumSet<Flag> flags = EnumSet.noneOf(Flag.class);
        if ((bits & MOVE) != 0) flags.add(Flag.MOVE);
        if ((bits & ATTACK) != 0) flags.add(Flag.LOOK);
        if ((bits & SWIM) != 0) flags.add(Flag.JUMP);
        setFlags(flags);
    }

    protected final int getMutexBits() {
        return legacyMutexBits;
    }

    protected final void moveToEntity(Entity target, double sqDist) {
        moveToPosition(target.getX(), target.getBoundingBox().minY, target.getZ(), sqDist);
    }

    protected final void moveToPosition(int x, int y, int z, double sqDist) {
        moveToPosition(x + 0.5d, y, z + 0.5d, sqDist);
    }

    protected final void moveToPosition(BlockPos pos, double sqDist) {
        moveToPosition(pos.getX(), pos.getY(), pos.getZ(), sqDist);
    }

    protected final void forceMoveToPosition(BlockPos pos, double sqDist) {
        moveRetryDelay = 0;
        moveToPosition(pos, sqDist);
    }

    protected final void moveToPosition(double x, double y, double z, double sqDist) {
        moveRetryDelay -= 10;
        if (moveRetryDelay <= 0) {
            if (sqDist > maxPFDistSq) {
                moveLongDistance(x, y, z);
                moveRetryDelay = 30;
            } else {
                setPath(x, y, z);
                moveRetryDelay = 5;
                if (sqDist > 256) {
                    moveRetryDelay += 5;
                }
                if (sqDist > 1024) {
                    moveRetryDelay += 10;
                }
            }
        }
    }

    @Override
    public boolean canUse() {
        return npc.getIsAIEnabled() && !npc.isAIFlagStopped(getMutexBits());
    }

    @Override
    public final boolean canContinueToUse() {
        return super.canContinueToUse();
    }

    protected final void moveLongDistance(double x, double y, double z) {
        Vec3 vec = new Vec3(x - npc.getX(), y - npc.getY(), z - npc.getZ());

        //normalize vector to a -1 <-> 1 value range
        double w = Math.sqrt(vec.x * vec.x + vec.y * vec.y + vec.z * vec.z);
        if (w != 0) {
            vec = vec.scale(1d / w);
        }

        //then mult by PF distance to find the proper vector for our PF length
        vec = vec.scale(maxPFDist);

        //finally re-offset by npc position to get an actual target position
        vec = vec.add(npc.getX(), npc.getY(), npc.getZ());

        //move npc towards the calculated partial target
        setPath(vec.x, vec.y, vec.z);
    }

    protected final void returnHome() {
        setPath(npc.getRestrictCenter());
    }

    protected final void setPath(double x, double y, double z) {
        Path path = trimPath(npc.getNavigation().createPath(x, y, z, 0));
        npc.getNavigation().moveTo(path, moveSpeed);
    }

    protected final void setPath(BlockPos pos) {
        Path path = trimPath(npc.getNavigation().createPath(pos, 0));
        npc.getNavigation().moveTo(path, moveSpeed);
    }

    @Nullable
    protected Path trimPath(@Nullable Path path) {
        if (path != null) {
            int index = path.getNextNodeIndex();
            Node pathpoint = path.getNode(index);
            if (npc.getWalkTargetValue(new BlockPos(pathpoint.x, pathpoint.y, pathpoint.z)) >= 0) {

                for (int i = index + 1; i < path.getNodeCount(); i++) {
                    pathpoint = path.getNode(i);
                    if (npc.getWalkTargetValue(new BlockPos(pathpoint.x, pathpoint.y, pathpoint.z)) < 0) {
                        path.truncateNodes(i);
                        break;
                    }
                }
            } else {
                Vec3 vec = DefaultRandomPos.getPosAway(npc, MIN_RANGE, MIN_RANGE, new Vec3(npc.getX(), npc.getY(), npc.getZ()));
                if (vec != null) {
                    return npc.getNavigation().createPath(vec.x, vec.y, vec.z, 0);
                }
            }
        }
        return path;
    }
}
