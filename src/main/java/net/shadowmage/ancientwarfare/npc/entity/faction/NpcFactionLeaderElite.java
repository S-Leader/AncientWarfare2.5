package net.shadowmage.ancientwarfare.npc.entity.faction;

import net.minecraft.world.level.Level;

public class NpcFactionLeaderElite extends NpcFactionLeader {
    @SuppressWarnings("unused")
    public NpcFactionLeaderElite(Level world) {
        super(world);
    }

    @SuppressWarnings("unused")
    public NpcFactionLeaderElite(Level world, String factionName) {
        super(world, factionName);
    }

    @Override
    public String getNpcType() {
        return super.getNpcType() + ".elite";
    }
}
