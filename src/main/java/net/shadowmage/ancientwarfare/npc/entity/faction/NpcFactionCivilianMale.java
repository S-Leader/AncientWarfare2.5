package net.shadowmage.ancientwarfare.npc.entity.faction;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class NpcFactionCivilianMale extends NpcFactionCivilian {
    @SuppressWarnings("unused")
    public NpcFactionCivilianMale(EntityType<? extends PathfinderMob> type, Level world) {
        super(type, world);
    }

    @SuppressWarnings("unused")


    @Override
    public String getNpcType() {
        return "civilian.male";
    }
}
