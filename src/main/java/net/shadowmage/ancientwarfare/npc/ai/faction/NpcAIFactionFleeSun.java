package net.shadowmage.ancientwarfare.npc.ai.faction;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.shadowmage.ancientwarfare.npc.ai.NpcAI;
import net.shadowmage.ancientwarfare.npc.entity.faction.NpcFaction;

import javax.annotation.Nullable;

public class NpcAIFactionFleeSun extends NpcAI<NpcFaction> {
    private final Level world;
    private final double movementSpeed;
    private double shelterX;
    private double shelterY;
    private double shelterZ;

    public NpcAIFactionFleeSun(NpcFaction npc, double movementSpeed) {
        super(npc);
        world = npc.level();
        this.movementSpeed = movementSpeed;
    }

    @Override
    public boolean canUse() {
        if (!super.canUse() || !npc.getNavigation().isDone() || !world.isDay() || !npc.isOnFire()
                || !world.canSeeSky(BlockPos.containing(npc.getX(), npc.getBoundingBox().minY, npc.getZ())) || !npc.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) {
            return false;
        }
        Vec3 vec3d = findPossibleShelter();

        if (vec3d == null) {
            return false;
        } else {
            shelterX = vec3d.x;
            shelterY = vec3d.y;
            shelterZ = vec3d.z;
            return true;
        }
    }

    @Override
    public void start() {
        npc.getNavigation().moveTo(shelterX, shelterY, shelterZ, movementSpeed);
    }

    @Nullable
    private Vec3 findPossibleShelter() {
        RandomSource random = npc.getRandom();
        BlockPos entityPos = BlockPos.containing(npc.getX(), getMinY(), npc.getZ());

        for (int i = 0; i < 10; ++i) {
            BlockPos randomPos = entityPos.offset(random.nextInt(20) - 10, random.nextInt(6) - 3, random.nextInt(20) - 10);

            if (!world.canSeeSky(randomPos) && npc.getWalkTargetValue(randomPos) > 0.0F) {
                return new Vec3(randomPos.getX(), randomPos.getY(), randomPos.getZ());
            }
        }

        return null;
    }

    private double getMinY() {
        //noinspection ConstantConditions
        return npc.isPassenger() ? npc.getVehicle().getBoundingBox().minY : npc.getBoundingBox().minY;
    }
}
