package net.shadowmage.ancientwarfare.npc.ai.vehicle;

import net.shadowmage.ancientwarfare.core.util.Trig;
import net.shadowmage.ancientwarfare.npc.ai.NpcAI;
import net.shadowmage.ancientwarfare.npc.entity.NpcBase;
import net.shadowmage.ancientwarfare.npc.entity.vehicle.ITarget;
import net.shadowmage.ancientwarfare.npc.entity.vehicle.IVehicleUser;
import net.shadowmage.ancientwarfare.vehicle.entity.VehicleBase;

public class NpcAIAimVehicle<T extends NpcBase & IVehicleUser> extends NpcAI<T> {
    public NpcAIAimVehicle(T npc) {
        super(npc);
    }

    @Override
    @SuppressWarnings("squid:S3655")
    public boolean canUse() {
        return super.canUse() && npc.getVehicleTarget().isPresent() && npc.canContinueRidingVehicle() && npc.isRidingVehicle()
                && npc.getVehicleTarget().map(t -> !npc.getUsedVehicle().map(v -> v.firingHelper.isAimedAt(t)).orElse(false)).orElse(false);
    }

    @Override
    @SuppressWarnings("squid:S3655")
    public void tick() {
        npc.getUsedVehicle().ifPresent(vehicle -> npc.getVehicleTarget().ifPresent(target -> {
                    if (turnVehicleIfYawDifferenceGreat(vehicle, target)) {
                        return;
                    }
                    vehicle.moveHelper.stopTurning();
                    vehicle.moveHelper.stopForwardMovement();
                    vehicle.firingHelper.handleSoldierTargetInput(target.getX(), target.getY(), target.getZ());
                }
        ));
    }

    @SuppressWarnings("squid:S1066")
    private boolean turnVehicleIfYawDifferenceGreat(VehicleBase vehicle, ITarget target) {
        float yawDiff = Trig.getAngleDiffSigned(vehicle.getYRot(), vehicle.firingHelper.getAimYaw(target));

        if (!vehicle.vehicleType.canAdjustYaw() && Math.abs(yawDiff) < 2) {
            //if there's a minor difference in the rotation just set the rotation to it instead of continues steps to one side and back
            vehicle.setYRot(vehicle.getYRot() + yawDiff);
            vehicle.moveHelper.stopMotion();
        } else if (vehicle.vehicleType.getBaseTurretRotationAmount() < 180 || Math.abs(yawDiff) > 120) {
            //if turret cannot rotate fully around, or if it can but yaw diff is great, turn towards target
            if (!Trig.isAngleBetween(vehicle.getYRot() + yawDiff, vehicle.localTurretRotationHome - getMaxRotDifference(vehicle),
                    vehicle.localTurretRotationHome + getMaxRotDifference(vehicle))) {
                if (yawDiff < 0) {
                    vehicle.moveHelper.turnLeft();
                } else {
                    vehicle.moveHelper.turnRight();
                }
                vehicle.moveHelper.stopForwardMovement();
                vehicle.firingHelper.handleSoldierTargetInput(target.getX(), target.getY(), target.getZ());
                return true;
            }
        }
        return false;
    }

    private float getMaxRotDifference(VehicleBase vehicle) {
        return Math.min(vehicle.currentTurretRotationMax + 1.5f, 180f);
    }

    @Override
    public void stop() {
        npc.getUsedVehicle().ifPresent(v -> v.moveHelper.stopTurning());
    }
}
