package net.shadowmage.ancientwarfare.npc.ai;

import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;
import net.shadowmage.ancientwarfare.npc.entity.NpcBase;

public class NpcAIWander extends NpcAI<NpcBase> {

    private Vec3 vec3;

    public NpcAIWander(NpcBase npc) {
        this(npc, 0.625);
    }

    public NpcAIWander(NpcBase npc, double par2) {
        super(npc);
        moveSpeed = par2;
        setMutexBits(MOVE);
    }

    @Override
    public boolean canUse() {
        if (!super.canUse() || npc.shouldBeAtHome() || npc.getRandom().nextInt(120) != 0 || npc.getNavigation().isInProgress()) {
            return false;
        } else {
            vec3 = DefaultRandomPos.getPos(npc, MIN_RANGE, 7);
            return vec3 != null;
        }
    }

    @Override
    public void start() {
        npc.addAITask(NpcAI.TASK_WANDER + NpcAI.TASK_MOVE);
        setPath(vec3.x, vec3.y, vec3.z);
    }

    @Override
    public void stop() {
        npc.removeAITask(NpcAI.TASK_WANDER + NpcAI.TASK_MOVE);
    }

}
