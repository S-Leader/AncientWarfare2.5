package net.shadowmage.ancientwarfare.npc.ai.faction;

import com.google.common.base.Predicate;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.shadowmage.ancientwarfare.npc.entity.faction.NpcFaction;

import javax.annotation.Nullable;

public class NpcAIFactionHurt extends HurtByTargetGoal {

    private Predicate<Entity> targetSelector;

    public NpcAIFactionHurt(NpcFaction npc, @Nullable final Predicate<Entity> targetSelector) {
        super(npc);
        setAlertOthers();
        this.targetSelector = targetSelector;
    }

    @Override
    protected boolean canAttack(@Nullable LivingEntity target, TargetingConditions conditions) {
        return targetSelector.apply(target);
    }

}
