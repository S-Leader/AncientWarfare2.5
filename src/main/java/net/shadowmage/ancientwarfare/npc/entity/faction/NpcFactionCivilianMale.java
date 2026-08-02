package net.shadowmage.ancientwarfare.npc.entity.faction;

import net.minecraft.world.level.Level;

public class NpcFactionCivilianMale extends NpcFactionCivilian {
    @SuppressWarnings("unused")
    public NpcFactionCivilianMale(Level world) {
        super(world);
    }

    @SuppressWarnings("unused")
    public NpcFactionCivilianMale(Level world, String factionName) {
        super(world, factionName);
    }

    @Override
    public String getNpcType() {
        return "civilian.male";
    }
}
