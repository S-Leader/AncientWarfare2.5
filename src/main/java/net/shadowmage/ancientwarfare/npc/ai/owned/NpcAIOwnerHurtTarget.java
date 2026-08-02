package net.shadowmage.ancientwarfare.npc.ai.owned;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.shadowmage.ancientwarfare.npc.ai.AIHelper;
import net.shadowmage.ancientwarfare.npc.entity.NpcBase;
import net.shadowmage.ancientwarfare.npc.entity.NpcPlayerOwned;

import javax.annotation.Nullable;
import java.util.EnumSet;

public class NpcAIOwnerHurtTarget extends TargetGoal {
    private final NpcPlayerOwned npc;
    private LivingEntity attacker;
    private int timestamp;

    public NpcAIOwnerHurtTarget(NpcPlayerOwned npc) {
        super(npc, false);
        this.npc = npc;
        setFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        LivingEntity entitylivingbase = npc.level().getPlayerByUUID(npc.getOwner().getUUID());

        if (entitylivingbase == null) {
            return false;
        } else {
            attacker = entitylivingbase.getLastHurtMob();
            int i = entitylivingbase.getLastHurtMobTimestamp();
            return i != timestamp && isSuitableTarget(attacker) && AIHelper.isWithinFollowRange(npc, attacker);
        }
    }

    @Override
    public void start() {
        mob.setTarget(attacker);
        LivingEntity entitylivingbase = npc.level().getPlayerByUUID(npc.getOwner().getUUID());

        if (entitylivingbase != null) {
            timestamp = entitylivingbase.getLastHurtMobTimestamp();
        }

        super.start();
    }

    private boolean isSuitableTarget(@Nullable LivingEntity target) {
        return AIHelper.isTarget((NpcBase) mob, target, mustSee);
    }
}
