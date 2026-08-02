package net.shadowmage.ancientwarfare.npc.ai;

import com.google.common.base.Predicate;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.shadowmage.ancientwarfare.npc.entity.NpcBase;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class NpcAIMedicBase<T extends NpcBase> extends NpcAI<T> {
    private static final int HEAL_DELAY_MAX = 20;
    private static final int INJURED_RECHECK_DELAY_MAX = 20;
    private static final float AMOUNT_TO_HEAL_EACH_TRY = 0.5f;

    private int injuredRecheckDelay = 0;
    private int healDelay = 0;

    private LivingEntity targetToHeal = null;

    private final Comparator<LivingEntity> sorter;
    @SuppressWarnings({"java:S4738", "Guava"}) //need to use this because it's what vanilla uses
    private final Predicate<LivingEntity> selector;

    public NpcAIMedicBase(T npc) {
        super(npc);
        sorter = Comparator.comparingDouble(npc::distanceToSqr);
        selector = entity -> entity != null && entity.isAlive() && entity.getHealth() < entity.getMaxHealth() && !NpcAIMedicBase.this.npc.isHostileTowards(entity);
        setMutexBits(MOVE | ATTACK);
    }

    @Override
    public boolean canUse() {
        if (!super.canUse()) {
            return false;
        }

        if (!isProperSubtype()) {
            return false;
        }

        if (validateTarget()) {
            return true;
        }

        if (injuredRecheckDelay-- > 0) {
            return false;
        }
        injuredRecheckDelay = INJURED_RECHECK_DELAY_MAX;
        double dist = npc.getAttribute(Attributes.FOLLOW_RANGE).getValue();
        AABB bb = npc.getBoundingBox().expandTowards(dist, dist / 2, dist);
        List<LivingEntity> potentialTargets = npc.level().getEntitiesOfClass(LivingEntity.class, bb, selector);
        if (potentialTargets.isEmpty()) {
            return false;
        }
        potentialTargets.sort(sorter);
        List<LivingEntity> sub = potentialTargets.stream().filter(input -> input instanceof NpcBase || input instanceof Player).collect(Collectors.toList());
        for (LivingEntity base : sub) {
            targetToHeal = base;
            if (validateTarget()) {
                return true;
            }
        }
        targetToHeal = potentialTargets.get(0);
        if (!validateTarget()) {
            targetToHeal = null;
            return false;
        }
        return true;
    }

    private boolean validateTarget() {
        return targetToHeal != null && targetToHeal.isAlive() && targetToHeal.getHealth() < targetToHeal.getMaxHealth();
    }

    protected boolean isProperSubtype() {
        return "medic".equals(npc.getNpcSubType());
    }

    @Override
    public void start() {
        npc.addAITask(TASK_GUARD);
    }

    @Override
    public void tick() {
        double dist = npc.distanceToSqr(targetToHeal);
        double attackDistance = (npc.getBbWidth() * npc.getBbWidth() * 2.0F * 2.0F) + (targetToHeal.getBbWidth() * targetToHeal.getBbWidth() * 2.0F * 2.0F);
        if (dist > attackDistance) {
            npc.addAITask(TASK_MOVE);
            moveToEntity(targetToHeal, dist);
            healDelay = HEAL_DELAY_MAX;
        } else {
            npc.removeAITask(TASK_MOVE);
            healDelay--;
            if (healDelay < 0) {
                healDelay = HEAL_DELAY_MAX;
                npc.swing(InteractionHand.MAIN_HAND);
                targetToHeal.heal(getAmountToHealEachTry());
            }
        }
    }

    protected float getAmountToHealEachTry() {
        return AMOUNT_TO_HEAL_EACH_TRY;
    }

    @Override
    public void stop() {
        npc.removeAITask(TASK_MOVE + TASK_GUARD);
        targetToHeal = null;
    }

}
