package net.shadowmage.ancientwarfare.npc.entity.vehicle;

import net.shadowmage.ancientwarfare.vehicle.entity.VehicleBase;

import java.util.Optional;

public interface IVehicleUser {
    void resetTarget();

    /**
     * Renamed from 1.12 getVehicle() - clashes with Entity#getVehicle in 1.20.
     */
    Optional<VehicleBase> getUsedVehicle();

    void setVehicle(VehicleBase vehicle);

    void resetVehicle();

    boolean isRidingVehicle();

    boolean canContinueRidingVehicle();

    /**
     * Renamed from 1.12 getTarget() - clashes with Mob#getTarget in 1.20.
     */
    Optional<ITarget> getVehicleTarget();
}
