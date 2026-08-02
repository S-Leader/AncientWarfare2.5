package net.shadowmage.ancientwarfare.npc.compat.ebwizardry.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

/**
 * Movement/targeting portion of the legacy Wizardry casting goal.
 */
public class EntityAIAttackSpellImproved<T extends Mob> extends Goal {
    private final T attacker;
    private final double speed;
    private final double maxDistanceSquared;

    public EntityAIAttackSpellImproved(T attacker, double speed, float maxDistance, int baseCooldown, int continuousSpellDuration) {
        this.attacker = attacker;
        this.speed = speed;
        this.maxDistanceSquared = maxDistance * maxDistance;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return attacker.getTarget() != null;
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity target = attacker.getTarget();
        return target != null && target.isAlive();
    }

    @Override
    public void tick() {
        LivingEntity target = attacker.getTarget();
        if (target == null) return;
        attacker.getLookControl().setLookAt(target, 30.0F, 30.0F);
        if (attacker.distanceToSqr(target) > maxDistanceSquared || !attacker.getSensing().hasLineOfSight(target)) {
            attacker.getNavigation().moveTo(target, speed);
        } else {
            attacker.getNavigation().stop();
        }
    }
}
