package net.shadowmage.ancientwarfare.npc.ai.owned;

import com.google.common.primitives.Floats;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.shadowmage.ancientwarfare.npc.ai.NpcAIAttack;
import net.shadowmage.ancientwarfare.npc.ai.NpcRangedWeaponAttackController;
import net.shadowmage.ancientwarfare.npc.config.AWNPCStatics;
import net.shadowmage.ancientwarfare.npc.entity.NpcBase;

public class NpcAIPlayerOwnedAttackRanged extends NpcAIAttack<NpcBase> {

    private final RangedAttackMob rangedAttacker;
    private static final double ATTACK_DISTANCE = AWNPCStatics.archerRange * AWNPCStatics.archerRange;
    private final NpcRangedWeaponAttackController specialWeaponController = new NpcRangedWeaponAttackController();

    public NpcAIPlayerOwnedAttackRanged(NpcBase npc) {
        super(npc);
        rangedAttacker = (RangedAttackMob) npc;//will classcastexception if improperly used..
        moveSpeed = 1.d;
        setMutexBits(ATTACK + MOVE);
    }

    @Override
    protected boolean shouldCloseOnTarget(double dist) {
        if (npc.doNotPursue) {
            return (dist < 0);
        }
        if (specialWeaponController.isTridentMeleeMode(npc, dist)) {
            return specialWeaponController.shouldCloseForTridentMelee(npc, getTarget(), dist);
        }
        return (dist > ATTACK_DISTANCE || !npc.getSensing().hasLineOfSight(getTarget()));
    }

    @Override
    protected void doAttack(double dist) {
        npc.removeAITask(TASK_MOVE);
        npc.getNavigation().stop();
        if (specialWeaponController.tickTridentMelee(npc, getTarget(), dist,
                getAttackDelay(), this::setAttackDelay)) {
            return;
        }
        float pwr = (float) (ATTACK_DISTANCE / dist);
        //noinspection UnstableApiUsage
        pwr = Floats.constrainToRange(pwr, 0.1f, 1f);
        if (specialWeaponController.tickSpecial(npc, rangedAttacker, getTarget(), pwr,
                getAttackDelay(), 35, this::setAttackDelay)) {
            return;
        }
        if (getAttackDelay() <= 0) {
            rangedAttacker.performRangedAttack(getTarget(), pwr);
            setAttackDelay(35);
        }
    }

    @Override
    protected void onAttackGoalStopped() {
        specialWeaponController.reset(npc);
    }
}
