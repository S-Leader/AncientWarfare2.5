package net.shadowmage.ancientwarfare.npc.ai;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;

/**
 * 1.20.1 replacement for the removed EntityAIRestrictOpenDoor task.
 *
 * <p>The legacy task disabled opening wooden doors while the restriction was
 * active. Modern navigation exposes that setting directly, so this goal keeps
 * the same observable behaviour without depending on the deleted village-door
 * classes.</p>
 */
public final class NpcAIRestrictOpenDoor extends Goal {
    private final PathfinderMob mob;
    private boolean previousCanOpenDoors;

    public NpcAIRestrictOpenDoor(PathfinderMob mob) {
        this.mob = mob;
    }

    @Override
    public boolean canUse() {
        return mob.getNavigation() instanceof GroundPathNavigation && !mob.level().isDay();
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public void start() {
        if (mob.getNavigation() instanceof GroundPathNavigation navigation) {
            previousCanOpenDoors = navigation.canOpenDoors();
            navigation.setCanOpenDoors(false);
        }
    }

    @Override
    public void stop() {
        if (mob.getNavigation() instanceof GroundPathNavigation navigation) {
            navigation.setCanOpenDoors(previousCanOpenDoors);
        }
    }
}
