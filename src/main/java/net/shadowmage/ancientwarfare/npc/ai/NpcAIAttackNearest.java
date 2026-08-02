package net.shadowmage.ancientwarfare.npc.ai;

import com.google.common.base.Predicate;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.phys.AABB;
import net.shadowmage.ancientwarfare.npc.entity.NpcBase;

import javax.annotation.Nullable;

public class NpcAIAttackNearest extends NearestAttackableTargetGoal<LivingEntity> {
    @SuppressWarnings({"java:S4738", "Guava"})
    public NpcAIAttackNearest(NpcBase npc, @Nullable final Predicate<Entity> targetSelector) {
        super(npc, LivingEntity.class, 0, true, false, targetSelector == null ? null : targetSelector::apply);
        targetConditions = TargetingConditions.forCombat().range(getFollowDistance()).ignoreLineOfSight().ignoreInvisibilityTesting()
                .selector(target -> AIHelper.isTarget(npc, target, mustSee) && (targetSelector == null || targetSelector.apply(target)));
    }

    @Override
    public boolean canUse() {
        boolean ret = super.canUse();
        if (!ret && mob.getTarget() != null && !mob.getTarget().isAlive()) {
            mob.setTarget(null);
        }
        return ret;
    }

    @Override
    protected AABB getTargetSearchArea(double targetDistance) {
        return mob.getBoundingBox().inflate(targetDistance, targetDistance, targetDistance);
    }
}
