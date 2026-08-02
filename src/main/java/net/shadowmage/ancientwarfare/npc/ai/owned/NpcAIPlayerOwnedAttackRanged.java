package net.shadowmage.ancientwarfare.npc.ai.owned;

import com.google.common.primitives.Floats;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.shadowmage.ancientwarfare.npc.ai.NpcAIAttack;
import net.shadowmage.ancientwarfare.npc.config.AWNPCStatics;
import net.shadowmage.ancientwarfare.npc.entity.NpcBase;

public class NpcAIPlayerOwnedAttackRanged extends NpcAIAttack<NpcBase> {

    private final RangedAttackMob rangedAttacker;
    private static final double ATTACK_DISTANCE = AWNPCStatics.archerRange * AWNPCStatics.archerRange;

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
        return (dist > ATTACK_DISTANCE || !npc.getSensing().hasLineOfSight(getTarget()));
    }

    @Override
    protected void doAttack(double dist) {
        npc.removeAITask(TASK_MOVE);
        npc.getNavigation().stop();
        if (getAttackDelay() <= 0) {
            float pwr = (float) (ATTACK_DISTANCE / dist);
            //noinspection UnstableApiUsage
            pwr = Floats.constrainToRange(pwr, 0.1f, 1f);
            rangedAttacker.performRangedAttack(getTarget(), pwr);
            setAttackDelay(35);
        }
    }
}
