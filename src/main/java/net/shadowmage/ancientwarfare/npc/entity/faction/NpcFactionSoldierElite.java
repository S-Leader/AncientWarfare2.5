package net.shadowmage.ancientwarfare.npc.entity.faction;

import net.minecraft.world.level.Level;

public class NpcFactionSoldierElite extends NpcFactionSoldier {
    @SuppressWarnings("unused")
    public NpcFactionSoldierElite(Level world) {
        super(world);
    }

    @SuppressWarnings("unused")
    public NpcFactionSoldierElite(Level world, String factionName) {
        super(world, factionName);
    }

    @Override
    public String getNpcType() {
        return super.getNpcType() + ".elite";
    }
}
