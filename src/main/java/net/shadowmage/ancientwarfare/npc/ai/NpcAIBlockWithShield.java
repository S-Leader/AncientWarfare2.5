package net.shadowmage.ancientwarfare.npc.ai;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.ToolActions;
import net.shadowmage.ancientwarfare.npc.config.AWNPCStatics;
import net.shadowmage.ancientwarfare.npc.entity.NpcBase;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Predicate;

public class NpcAIBlockWithShield extends NpcAI<NpcBase> {
    private LivingEntity target;
    private static final float SAFE_MELEE_DISTANCE = 4.5F;
    private int reactionDelayTicks = 0;
    private static final int SHIELD_WITHDRAW_DELAY = 40;
    private int shieldWithdrawTicks = 0;

    public NpcAIBlockWithShield(NpcBase npc) {
        super(npc);
    }

    @Override
    public boolean canUse() {
        return super.canUse() && canExecute(e -> e.isAlive() && shouldDefendFrom(e));
    }

    private boolean canExecute(Predicate<LivingEntity> defendFrom) {
        if (target == null) {
            return hasShieldInOffhand() && npc.getShieldDisabledTick() <= 0 && (shieldWithdrawTicks > 0 || getAttackTarget().map(defendFrom::test).orElse(false));
        }
        return hasShieldInOffhand() && npc.getShieldDisabledTick() <= 0 && target.isAlive() && getAttackTarget().isPresent() && (shieldWithdrawTicks > 0 || getAttackTarget().map(t -> t.equals(target) && shouldDefendFrom(target)).orElse(false));
    }

    private boolean shouldDefendFrom(@Nullable LivingEntity e) {
        return e != null && isPlayerOrTargetsThisNpc(e) && ((e.swinging && npc.distanceTo(e) < 4) || isAimingWithBow(e));
    }

    private boolean isPlayerOrTargetsThisNpc(LivingEntity e) {
        if (e instanceof Player) {
            return true;
        }
        if (e instanceof Mob) {
            Mob living = (Mob) e;
            return living.getTarget() != null && living.getTarget().equals(npc);
        }
        return false;
    }

    @Override
    public void start() {
        if (!npc.isUsingItem()) {
            init();
        }
    }

    @SuppressWarnings("java:S2259")
    private void init() {
        target = getAttackTarget().orElse(null);
        int maxReactionDelay = target != null && isAimingWithBow(target) ? AWNPCStatics.shieldBowReactionDelay : AWNPCStatics.shieldReactionDelay;
        reactionDelayTicks = npc.getRandom().nextInt(Math.max(1, maxReactionDelay));
        shieldWithdrawTicks = SHIELD_WITHDRAW_DELAY;
    }

    @Override
    public final void stop() {
        target = null;
        reactionDelayTicks = 0;
        npc.startAIControlFlag(ATTACK);
    }

    @Override
    public final void tick() {
        if (reactionDelayTicks > 0) {
            reactionDelayTicks--;
            return;
        }
        if (!shouldDefendFrom(target)) {
            shieldWithdrawTicks--;
            return;
        }
        npc.stopAIControlFlag(ATTACK);
        npc.startUsingItem(InteractionHand.OFF_HAND);
        npc.setUseItemRemainingTicks(SHIELD_WITHDRAW_DELAY);
        npc.getNavigation().stop();

        if (target != null) {
            npc.getLookControl().setLookAt(target, 30.f, 30.f);
            double distanceToEntity = npc.distanceToSqr(target.getX(), target.getBoundingBox().minY, target.getZ());

            if (!shouldCloseOnTarget(distanceToEntity) || isAimingWithBow(target)) {
                startBlocking();
                npc.getNavigation().moveTo(target.getX(), target.getY(), target.getZ(), npc.getDefaultMoveSpeed() * 0.5f);
            }
        }
    }

    private void startBlocking() {
        npc.startUsingItem(InteractionHand.OFF_HAND);
    }

    private Optional<LivingEntity> getAttackTarget() {
        return npc.getTarget() != null ? Optional.of(npc.getTarget()) : Optional.ofNullable(npc.getLastHurtByMob());
    }

    protected boolean shouldCloseOnTarget(double distanceToEntity) {
        double attackDistance = (npc.getBbWidth() / 2D) + (getTarget().getBbWidth() / 2D) + SAFE_MELEE_DISTANCE;
        return (distanceToEntity > (attackDistance * attackDistance)) || !npc.getSensing().hasLineOfSight(getTarget());
    }

    public final LivingEntity getTarget() {
        return target;
    }

    private boolean hasShieldInOffhand() {
        return npc.getOffhandItem().canPerformAction(ToolActions.SHIELD_BLOCK);
    }

    private boolean isAimingWithBow(LivingEntity entity) {
        return (npc.isRangedWeapon(entity.getMainHandItem().getItem()) && entity.isUsingItem() && entity.getUsedItemHand() == InteractionHand.MAIN_HAND) ||
                (npc.isRangedWeapon(entity.getOffhandItem().getItem()) && entity.isUsingItem() && entity.getUsedItemHand() == InteractionHand.OFF_HAND);
    }

    public void onPreDamage(DamageSource source, float damage) {
        if (damage > 0 && !source.is(DamageTypeTags.BYPASSES_SHIELD) && canExecute(LivingEntity::isAlive)) {
            init();
            tick();
        }
    }
}
