package net.shadowmage.ancientwarfare.npc.ai.faction;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.shadowmage.ancientwarfare.npc.ai.NpcAIAttack;
import net.shadowmage.ancientwarfare.npc.config.AWNPCStatics;
import net.shadowmage.ancientwarfare.npc.entity.NpcBase;

public class NpcAIFactionRangedAttack extends NpcAIAttack<NpcBase> {
    private final RangedAttackMob rangedAttacker;

    private int attackDistanceSq;
    private final int rangedAttackDelay;

    public <T extends NpcBase & RangedAttackMob> NpcAIFactionRangedAttack(T npc, double moveSpeed, int attackDistanceSq, int attackDelay) {
        super(npc);
        rangedAttacker = npc;
        this.moveSpeed = moveSpeed;
        this.attackDistanceSq = attackDistanceSq;
        this.rangedAttackDelay = attackDelay;
    }

    public <T extends NpcBase & RangedAttackMob> NpcAIFactionRangedAttack(T npc) {
        this(npc, 1, (int) Math.pow(AWNPCStatics.archerRange, 2), 45);
    }

    @Override
    protected boolean shouldCloseOnTarget(double dist) {
        return (dist > getAttackDistanceSq() || !npc.getSensing().hasLineOfSight(getTarget()));
    }

    private double getAttackDistanceSq() {
        if (attackDistanceSq == -1) {
            double distance = npc.getAttribute(Attributes.FOLLOW_RANGE).getValue();
            attackDistanceSq = (int) (distance * distance);
        }
        return attackDistanceSq;
    }

    @Override
    protected void doAttack(double dist) {
        if (!npc.isUsingItem()) {
            npc.startUsingItem(InteractionHand.MAIN_HAND);
        } else if (getAttackDelay() <= 0) {
            npc.stopUsingItem();
            float pwr = (float) (getAttackDistanceSq() / dist);
            pwr = Math.min(Math.max(pwr, 0.1F), 1F);
            rangedAttacker.performRangedAttack(getTarget(), pwr);
            setAttackDelay(rangedAttackDelay);
        }
    }
}
