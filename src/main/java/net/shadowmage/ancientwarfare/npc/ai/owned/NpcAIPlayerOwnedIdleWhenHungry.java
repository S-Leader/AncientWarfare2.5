package net.shadowmage.ancientwarfare.npc.ai.owned;

import net.shadowmage.ancientwarfare.npc.ai.NpcAI;
import net.shadowmage.ancientwarfare.npc.entity.NpcBase;

public class NpcAIPlayerOwnedIdleWhenHungry extends NpcAI<NpcBase> {

    int moveTimer = 0;

    public NpcAIPlayerOwnedIdleWhenHungry(NpcBase npc) {
        super(npc);
        setMutexBits(MOVE + ATTACK + HUNGRY);
    }

    @Override
    public boolean canUse() {
        return super.canUse() && npc.getTarget() == null && npc.requiresUpkeep() && npc.getFoodRemaining() == 0;
    }

    @Override
    public void start() {
        npc.addAITask(TASK_IDLE_HUNGRY);
        moveTimer = 0;
        if (npc.hasRestriction()) {
            returnHome();
        }
    }

    @Override
    public void stop() {
        npc.removeAITask(TASK_IDLE_HUNGRY);
    }

    @Override
    public void tick() {
        if (npc.hasRestriction()) {
            moveTimer--;
            if (moveTimer <= 0) {
                returnHome();
                moveTimer = 10;
            }
        }
    }

}
