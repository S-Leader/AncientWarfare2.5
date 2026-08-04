package net.shadowmage.ancientwarfare.npc.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.shadowmage.ancientwarfare.npc.entity.NpcBase;

import java.util.Optional;

public abstract class NpcAIAttack<T extends NpcBase> extends NpcAI<T> {
    private LivingEntity target;
    private int attackDelay = 0;

    public NpcAIAttack(T npc) {
        super(npc);
    }

    @Override
    public boolean canUse() {
        return super.canUse() && hasTargetInRange();
    }

    private boolean hasTargetInRange() {
        if (target == null) {
            return getAttackTarget().isPresent() && getAttackTarget().map(LivingEntity::isAlive).orElse(false) && isTargetInRange();
        }
        return target.isAlive() && getAttackTarget().map(t -> t.equals(target)).orElse(false) && isTargetInRange();
    }

    private boolean isTargetInRange() {
        return getAttackTarget().map(t -> npc.distanceTo(t) < getAdjustedTargetDistance()).orElse(false);
    }

    private Optional<LivingEntity> getAttackTarget() {
        return npc.getTarget() != null ? Optional.of(npc.getTarget()) : Optional.ofNullable(npc.getLastHurtByMob());
    }

    @Override
    public final void start() {
        target = getAttackTarget().orElse(null);
        moveRetryDelay = 0;
        npc.addAITask(TASK_ATTACK);
        // Combat uses LivingEntity's normal one-shot swing animation when an
        // attack is actually performed. Keeping SWINGING_ARMS enabled for the
        // whole goal adds ModelNpc's synthetic work cycle on top of that swing,
        // making every melee hit appear to swing twice.
        npc.setSwingingArms(false);
        attackDelay = 0;
    }

    @Override
    public final void stop() {
        target = null;
        moveRetryDelay = 0;
        npc.removeAITask(TASK_MOVE + TASK_ATTACK);
        npc.setSwingingArms(false);
    }

    protected int getCoolDown() {
        return attackDelay;
    }

    @Override
    public final void tick() {
        npc.getLookControl().setLookAt(target, 30.f, 30.f);
        double distanceToEntitySq = npc.distanceToSqr(target.getX(), target.getBoundingBox().minY, target.getZ());
        if (shouldCloseOnTarget(distanceToEntitySq)) {
            npc.addAITask(TASK_MOVE);
            moveToEntity(target, distanceToEntitySq);
        } else {
            npc.getNavigation().stop();
            attackDelay--;
            doAttack(distanceToEntitySq);
        }
    }

    private double getAdjustedTargetDistance() {
        if (npc.getFollowingEntity() != null) {
            return Math.max(getTargetDistance() / getDistanceToFollowingEntity(), 3D);
        }
        return Math.max(getTargetDistance() / (getHomeDistance() / 20), 3D);
    }

    private double getDistanceToFollowingEntity() {
        return Math.sqrt(npc.getDistanceSq(npc.getFollowingEntity().blockPosition()));
    }

    private double getHomeDistance() {
        return Math.sqrt(npc.getDistanceSq(npc.getRestrictCenter()));
    }

    private double getTargetDistance() {
        AttributeInstance iattributeinstance = npc.getAttribute(Attributes.FOLLOW_RANGE);
        //noinspection ConstantConditions
        return iattributeinstance == null ? 35.0D : iattributeinstance.getValue();
    }

    protected abstract boolean shouldCloseOnTarget(double distanceToEntity);

    protected abstract void doAttack(double distanceToEntity);

    public final LivingEntity getTarget() {
        return target;
    }

    public final void setAttackDelay(int value) {
        attackDelay = value;
    }

    public final int getAttackDelay() {
        return attackDelay;
    }
}
