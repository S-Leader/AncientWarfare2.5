package net.shadowmage.ancientwarfare.npc.ai.vehicle;

import net.minecraft.world.entity.Entity;
import net.shadowmage.ancientwarfare.npc.ai.NpcAI;
import net.shadowmage.ancientwarfare.npc.entity.NpcBase;
import net.shadowmage.ancientwarfare.npc.entity.vehicle.IVehicleUser;

public class NpcAIMountVehicle<T extends NpcBase & IVehicleUser> extends NpcAI<T> {
    private static final double MOUNT_REACH = 1.0D;

    public NpcAIMountVehicle(T npc) {
        super(npc);
    }

    @Override
    public boolean canUse() {
        return super.canUse() && !npc.isPassenger() && npc.canContinueRidingVehicle() && !npc.getUsedVehicle().map(Entity::isVehicle).orElse(false);
    }

    @Override
    public void tick() {
        npc.getUsedVehicle().ifPresent(vehicle -> {
            double distance = npc.getDistanceSq(vehicle.blockPosition());

            if (npc.getBoundingBox().inflate(MOUNT_REACH).intersects(vehicle.getBoundingBox())) {
                npc.startRiding(vehicle);
            } else {
                moveToPosition(vehicle.blockPosition(), distance);
                npc.addAITask(TASK_MOVE);
            }
        });
    }

    @Override
    public void stop() {
        super.stop();
        npc.resetVehicle();
        npc.removeAITask(TASK_MOVE);
    }
}
