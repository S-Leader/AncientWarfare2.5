package net.shadowmage.ancientwarfare.npc.ai;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.shadowmage.ancientwarfare.core.config.AWCoreStatics;
import net.shadowmage.ancientwarfare.npc.config.AWNPCStatics;
import net.shadowmage.ancientwarfare.npc.entity.NpcBase;
import net.shadowmage.ancientwarfare.npc.item.IExtendedReachWeapon;

public class NpcAIAttackMeleeLongRange extends NpcAIAttack<NpcBase> {
    private float attackReach = 2.5F + AWCoreStatics.meleeReachModifier;

    public NpcAIAttackMeleeLongRange(NpcBase npc) {
        super(npc);
        setMutexBits(ATTACK + MOVE);
    }

    public void setAttackReachFromWeapon(ItemStack weapon) {
        float reach = 2.5F + AWCoreStatics.meleeReachModifier;
        if (weapon.getItem() instanceof IExtendedReachWeapon) {
            reach = ((IExtendedReachWeapon) weapon.getItem()).getReach() - 1F + AWCoreStatics.meleeReachModifier;
        }
        attackReach = reach;
    }

    @Override
    protected boolean shouldCloseOnTarget(double distanceToEntity) {
        double attackDistance = (npc.getBbWidth() / 2D) + (getTarget().getBbWidth() / 2D) + attackReach;
        return (distanceToEntity > (attackDistance * attackDistance)) || !npc.getSensing().hasLineOfSight(getTarget());
    }

    @Override
    protected void doAttack(double distanceToEntity) {
        npc.removeAITask(TASK_MOVE);
        if (getAttackDelay() <= 0) {
            npc.swing(InteractionHand.MAIN_HAND);
            npc.doHurtTarget(getTarget());
            setAttackDelay(getCoolDown());
            npc.addExperience(AWNPCStatics.npcXpFromAttack);
        }
    }

    @Override
    protected int getCoolDown() {
        int entityAttackCooldown = (int) (1.0D / npc.getAttribute(Attributes.ATTACK_SPEED).getValue() * 20.0D);

        return entityAttackCooldown > 0 ? entityAttackCooldown : 20;
    }
}
