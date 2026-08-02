package net.shadowmage.ancientwarfare.npc.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.shadowmage.ancientwarfare.npc.entity.NpcBase;
import net.shadowmage.ancientwarfare.npc.entity.NpcPlayerOwned;

import javax.annotation.Nullable;

public class NpcAIHurt extends HurtByTargetGoal {
    public NpcAIHurt(NpcBase npc) {
        super(npc);
        setAlertOthers();
    }

    @Override
    protected boolean canAttack(@Nullable LivingEntity target, TargetingConditions conditions) {
        return !(target instanceof Player || target instanceof NpcPlayerOwned)
                && AIHelper.isTarget((NpcBase) mob, target, mustSee);
    }
}
