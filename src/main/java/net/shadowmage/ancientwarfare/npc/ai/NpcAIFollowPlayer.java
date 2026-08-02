package net.shadowmage.ancientwarfare.npc.ai;

import net.minecraft.world.entity.Entity;
import net.shadowmage.ancientwarfare.npc.entity.NpcBase;

public class NpcAIFollowPlayer extends NpcAI<NpcBase> {
    private static final double ATTACK_IGNORE_DISTANCE = 4.d * 4.d;
    private static final double FOLLOW_STOP_DISTANCE = 4.d * 4.d;

    private Entity target;

    public NpcAIFollowPlayer(NpcBase npc) {
        super(npc);
        setMutexBits(MOVE);
    }

    @Override
    public boolean canUse() {
        if (!super.canUse()) {
            return false;
        }
        target = npc.getFollowingEntity();
        if (target == null) {
            return false;
        }
        if (npc.getTarget() != null) {
            return npc.distanceToSqr(target) >= ATTACK_IGNORE_DISTANCE || npc.distanceToSqr(npc.getTarget()) >= ATTACK_IGNORE_DISTANCE;
        }
        return true;
    }

    @Override
    public void start() {
        moveRetryDelay = 0;
        npc.addAITask(TASK_FOLLOW);
    }

    @Override
    public void stop() {
        target = null;
        moveRetryDelay = 0;
        npc.removeAITask(TASK_FOLLOW + TASK_MOVE);
    }

    @Override
    public void tick() {
        npc.getLookControl().setLookAt(target, 10.0F, (float) npc.getMaxHeadXRot());
        double distance = npc.distanceToSqr(target);
        if (distance > FOLLOW_STOP_DISTANCE) {
            npc.addAITask(TASK_MOVE);
            moveToEntity(target, distance);
        } else {
            npc.removeAITask(TASK_MOVE);
            npc.getNavigation().stop();
        }
    }
}
