package net.shadowmage.ancientwarfare.npc.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;

import javax.annotation.Nullable;

/**
 * Ground navigation with AW2's door policy and mounted-NPC forwarding.
 */
public class NpcNavigator extends GroundPathNavigation {
    private final Mob entity;

    public NpcNavigator(Mob living) {
        super(living, living.level());
        entity = living;
    }

    @Override
    protected PathFinder createPathFinder(int maxVisitedNodes) {
        this.nodeEvaluator = new NpcWalkNodeProcessor();
        return new PathFinder(this.nodeEvaluator, maxVisitedNodes);
    }

    public void setCanSwim(boolean value) {
        setCanFloat(value);
    }

    public void setEnterDoors(boolean value) {
        nodeEvaluator.setCanOpenDoors(value);
        nodeEvaluator.setCanPassDoors(value);
    }

    public boolean getEnterDoors() {
        return nodeEvaluator.canPassDoors();
    }

    public void onWorldChange() {
        // PathNavigation reads the entity's current level directly in modern versions.
    }

    @Nullable
    public Path getPathToPos(BlockPos pos) {
        return hasMount() ? mount().getNavigation().createPath(pos, 0) : createPath(pos, 0);
    }

    @Nullable
    public Path getPathToEntityLiving(Entity target) {
        return hasMount() ? mount().getNavigation().createPath(target, 0) : createPath(target, 0);
    }

    @Override
    public boolean moveTo(@Nullable Path path, double speed) {
        if (hasMount()) {
            mount().getNavigation().moveTo(path, speed);
        }
        return super.moveTo(path, speed);
    }

    @Override
    public void stop() {
        if (hasMount()) {
            mount().getNavigation().stop();
        }
        super.stop();
    }

    @Override
    public void tick() {
        super.tick();
        if (!isDone() && hasMount()) {
            mount().getNavigation().tick();
        }
    }

    private boolean hasMount() {
        return entity.getVehicle() instanceof Mob;
    }

    private Mob mount() {
        return (Mob) entity.getVehicle();
    }

    @Override
    protected boolean canUpdatePath() {
        return super.canUpdatePath() || hasMount();
    }

    @Override
    public String toString() {
        return "NpcNavigator{done=" + isDone() + ", path=" + getPath()
                + (hasMount() ? ", mountPath=" + mount().getNavigation().getPath() : "") + '}';
    }
}
