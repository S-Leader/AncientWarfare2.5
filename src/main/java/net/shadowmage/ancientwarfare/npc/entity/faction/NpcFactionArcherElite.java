package net.shadowmage.ancientwarfare.npc.entity.faction;

import net.minecraft.world.level.Level;

public class NpcFactionArcherElite extends NpcFactionArcher {
    @SuppressWarnings("unused")
    public NpcFactionArcherElite(Level world) {
        super(world);
    }

    @SuppressWarnings("unused")
    public NpcFactionArcherElite(Level world, String factionName) {
        super(world, factionName);
    }

    @Override
    public String getNpcType() {
        return super.getNpcType() + ".elite";
    }
}
