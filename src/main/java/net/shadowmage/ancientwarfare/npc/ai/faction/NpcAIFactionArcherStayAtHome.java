package net.shadowmage.ancientwarfare.npc.ai.faction;

import net.shadowmage.ancientwarfare.npc.ai.NpcAI;
import net.shadowmage.ancientwarfare.npc.entity.NpcBase;

public class NpcAIFactionArcherStayAtHome extends NpcAI<NpcBase> {
    public NpcAIFactionArcherStayAtHome(NpcBase npc) {
        super(npc);
        setMutexBits(ATTACK + MOVE);
    }

    @Override
    public boolean canUse() {
        return super.canUse() && (npc.getTarget() == null || !npc.getTarget().isAlive() && npc.hasRestriction()) && !isAtHome();
    }

    private boolean isAtHome() {
        return npc.getDistanceSqFromHome() <= MIN_RANGE;
    }

    @Override
    public void start() {
        npc.addAITask(TASK_MOVE);
    }

    @Override
    public void tick() {
        moveToPosition(npc.getRestrictCenter(), npc.getDistanceSqFromHome());
    }

    @Override
    public void stop() {
        npc.removeAITask(TASK_MOVE);
    }
}
