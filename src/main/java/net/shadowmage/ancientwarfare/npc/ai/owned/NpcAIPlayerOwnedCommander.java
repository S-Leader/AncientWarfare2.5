package net.shadowmage.ancientwarfare.npc.ai.owned;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.AABB;
import net.shadowmage.ancientwarfare.npc.ai.NpcAI;
import net.shadowmage.ancientwarfare.npc.entity.NpcBase;

import java.util.List;

public class NpcAIPlayerOwnedCommander extends NpcAI<NpcBase> {
    private int lastExecuted = -1;
    protected MobEffectInstance effect = new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20);//apply 1 second strength potion, times 1.3 damage

    public NpcAIPlayerOwnedCommander(NpcBase npc) {
        super(npc);
    }

    protected boolean canBeCommanded(NpcBase npc) {
        return npc.hasCommandPermissions(npc.getOwner());
    }

    @Override
    public boolean canUse() {
        if (!super.canUse() || isNotCommander(npc)) {
            return false;
        }
        return (lastExecuted == -1 || npc.tickCount - lastExecuted > 40);//Wait 2 seconds
    }

    protected boolean isNotCommander(NpcBase npc) {
        return !npc.getNpcSubType().equals("commander");
    }

    @Override
    public void start() {
        lastExecuted = npc.tickCount;
        double dist = npc.getAttribute(Attributes.FOLLOW_RANGE).getValue();
        AABB bb = npc.getBoundingBox().expandTowards(dist, dist / 2, dist);
        List<NpcBase> potentialTargets = npc.level().getEntitiesOfClass(NpcBase.class, bb,
                n -> n != null && canBeCommanded(n) && isNotCommander(n) && !n.hasEffect(effect.getEffect()));
        for (NpcBase npcBase : potentialTargets) {
            npcBase.addEffect(new MobEffectInstance(effect));
        }
    }

    @Override
    public void tick() {
        //noop
    }

    @Override
    public void stop() {
        //noop
    }

}
