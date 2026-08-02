package net.shadowmage.ancientwarfare.npc.ai;

import net.shadowmage.ancientwarfare.npc.entity.NpcBase;
import net.shadowmage.ancientwarfare.npc.entity.NpcCombat;

public class NpcAIDistressResponse extends NpcAI<NpcBase> {
    private NpcBase target = null;
    private static final double FOLLOW_STOP_DISTANCE = 2.d * 2.d;

    public NpcAIDistressResponse(NpcBase npc) {
        super(npc);
        setMutexBits(MOVE + ATTACK);
    }

    @Override
    public boolean canUse() {
        if (!super.canUse()) {
            return false;
        }
        if (npc instanceof NpcCombat) {
            target = ((NpcCombat) npc).getDistressedTarget();
            if (target != null) {
                if (target.isAlive()) {
                    return true;
                } else {
                    ((NpcCombat) npc).clearDistress();
                }
            }
        }
        return false;
    }

    @Override
    public void start() {
        moveRetryDelay = 0;
        npc.addAITask(TASK_FOLLOW + TASK_ATTACK);
    }

    @Override
    public void stop() {
        target = null;
        moveRetryDelay = 0;
        npc.removeAITask(TASK_FOLLOW + TASK_MOVE + TASK_ATTACK);
        if (npc instanceof NpcCombat) {
            ((NpcCombat) npc).clearDistress();
        }
    }

    @Override
    public void tick() {
        if (!target.isAlive()) {
            // mission failed!
            stop();
            return;
        }
        npc.getLookControl().setLookAt(target, 10.0F, (float) npc.getMaxHeadXRot());
        double distance = npc.distanceToSqr(target);
        if (distance > FOLLOW_STOP_DISTANCE) {
            npc.addAITask(TASK_MOVE + TASK_ATTACK);
            moveToEntity(target, distance);
        } else {
            // we've reached the distressed NPC, no need to follow them anymore
            stop();
        }
    }

}
