package net.shadowmage.ancientwarfare.npc.ai.faction;

import net.minecraft.world.entity.ai.goal.RestrictSunGoal;
import net.shadowmage.ancientwarfare.npc.entity.faction.NpcFaction;

public class NpcAIFactionRestrictSun extends RestrictSunGoal {
    private NpcFaction npc;

    public NpcAIFactionRestrictSun(NpcFaction npc) {
        super(npc);
        this.npc = npc;
    }

    @Override
    public boolean canUse() {
        return npc.burnsInSun() && super.canUse();
    }
}
