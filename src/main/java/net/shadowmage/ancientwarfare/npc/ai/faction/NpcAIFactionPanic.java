package net.shadowmage.ancientwarfare.npc.ai.faction;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;

public class NpcAIFactionPanic extends PanicGoal {

    public NpcAIFactionPanic(PathfinderMob creature, double speedIn) {
        super(creature, speedIn);
    }

    @Override
    protected boolean findRandomPosition() {

        Vec3 vec3d;
        if (mob.getLastHurtByMob() != null) {
            vec3d = DefaultRandomPos.getPosAway(mob, 10, 4, mob.getLastHurtByMob().position());
        } else {
            vec3d = DefaultRandomPos.getPos(mob, 5, 4);
        }

        if (vec3d == null) {
            return false;
        } else {
            posX = vec3d.x;
            posY = vec3d.y;
            posZ = vec3d.z;
            return true;
        }
    }

}
