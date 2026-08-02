package net.shadowmage.ancientwarfare.npc.entity.faction;

import net.minecraft.world.level.Level;

public class NpcFactionCivilianFemale extends NpcFactionCivilian {
    @SuppressWarnings("unused")
    public NpcFactionCivilianFemale(Level world) {
        super(world);
    }

    @SuppressWarnings("unused")
    public NpcFactionCivilianFemale(Level world, String factionName) {
        super(world, factionName);
    }

    @Override
    public String getNpcType() {
        return "civilian.female";
    }

    @Override
    public boolean isFemale() {
        return true;
    }
}
