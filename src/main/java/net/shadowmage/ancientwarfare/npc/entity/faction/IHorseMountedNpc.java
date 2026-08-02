package net.shadowmage.ancientwarfare.npc.entity.faction;

import net.minecraft.world.entity.Mob;

public interface IHorseMountedNpc {
    boolean isHorseAlive();

    void setHorseKilled();

    Mob instantiateMountedEntity();
}
