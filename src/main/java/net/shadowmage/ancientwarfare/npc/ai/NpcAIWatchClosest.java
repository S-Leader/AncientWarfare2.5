package net.shadowmage.ancientwarfare.npc.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.shadowmage.ancientwarfare.npc.entity.NpcBase;

public class NpcAIWatchClosest extends LookAtPlayerGoal {
    public NpcAIWatchClosest(NpcBase npc, Class<? extends LivingEntity> watchTargetClass, float maxDistance) {
        super(npc, watchTargetClass, maxDistance);
    }

    @Override
    public boolean canUse() {
        //just a minor modification from vanilla that will always prefer to watch attacked target
        lookAt = mob.getTarget();
        return lookAt != null || super.canUse();
    }
}
