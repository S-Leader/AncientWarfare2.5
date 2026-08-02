package net.shadowmage.ancientwarfare.npc.ai.owned;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.shadowmage.ancientwarfare.npc.ai.AIHelper;
import net.shadowmage.ancientwarfare.npc.entity.NpcPlayerOwned;

import javax.annotation.Nullable;
import java.util.EnumSet;

public class NpcAIOwnerHurtByTarget extends TargetGoal {
    private final NpcPlayerOwned npc;
    private LivingEntity attacker;
    private int timestamp;

    public NpcAIOwnerHurtByTarget(NpcPlayerOwned npc) {
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
            attacker = entitylivingbase.getLastHurtByMob();
            int i = entitylivingbase.getLastHurtByMobTimestamp();
            return i != timestamp && isSuitableTarget(attacker) && AIHelper.isWithinFollowRange(npc, attacker);
        }
    }

    @Override
    public void start() {
        mob.setTarget(attacker);
        LivingEntity entitylivingbase = npc.level().getPlayerByUUID(npc.getOwner().getUUID());

        if (entitylivingbase != null) {
            timestamp = entitylivingbase.getLastHurtByMobTimestamp();
        }

        super.start();
    }

    private boolean isSuitableTarget(@Nullable LivingEntity target) {
        return AIHelper.isTarget(npc, target, mustSee);
    }
}
