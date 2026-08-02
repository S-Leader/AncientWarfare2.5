/**
 * Copyright 2012 John Cummens (aka Shadowmage, Shadowmage4513)
 * This software is distributed under the terms of the GNU General Public License.
 * Please see COPYING for precise license information.
 * <p>
 * This file is part of Ancient Warfare.
 * <p>
 * Ancient Warfare is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Ancient Warfare is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Ancient Warfare.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.shadowmage.ancientwarfare.vehicle.registry;

import net.shadowmage.ancientwarfare.core.config.legacy.LegacyConfiguration;
import net.shadowmage.ancientwarfare.vehicle.AncientWarfareVehicles;
import net.shadowmage.ancientwarfare.vehicle.entity.IVehicleType;
import net.shadowmage.ancientwarfare.vehicle.entity.types.*;
import net.shadowmage.ancientwarfare.vehicle.missiles.IAmmo;

import java.util.Iterator;

public class VehicleRegistry {
    private static final String VEHICLE_CONFIG_CATEGORY = "05_vehicle_settings";

    public static final IVehicleType CATAPULT_STAND_FIXED = new VehicleTypeCatapultStandFixed(0);
    public static final IVehicleType CATAPULT_STAND_TURRET = new VehicleTypeCatapultStandTurret(1);
    public static final IVehicleType CATAPULT_MOBILE_FIXED = new VehicleTypeCatapultMobileFixed(2);
    public static final IVehicleType CATAPULT_MOBILE_TURRET = new VehicleTypeCatapultMobileTurret(3);

    public static final IVehicleType BALLISTA_STAND_FIXED = new VehicleTypeBallistaStand(4);
    public static final IVehicleType BALLISTA_STAND_TURRET = new VehicleTypeBallistaStandTurret(5);
    public static final IVehicleType BALLISTA_MOBILE_FIXED = new VehicleTypeBallistaMobile(6);
    public static final IVehicleType BALLISTA_MOBILE_TURRET = new VehicleTypeBallistaMobileTurret(7);

    public static final IVehicleType BATTERING_RAM = new VehicleTypeBatteringRam(8);

    public static final IVehicleType CANNON_STAND_FIXED = new VehicleTypeCannonStandFixed(9);
    public static final IVehicleType CANNON_STAND_TURRET = new VehicleTypeCannonStandTurret(10);
    public static final IVehicleType CANNON_MOBILE_FIXED = new VehicleTypeCannonMobileFixed(11);

    public static final IVehicleType HWACHA = new VehicleTypeHwacha(12);

    public static final IVehicleType TREBUCHET_STAND_FIXED = new VehicleTypeTrebuchetStandFixed(13);
    public static final IVehicleType TREBUCHET_STAND_TURRET = new VehicleTypeTrebuchetStandTurret(14);
    public static final IVehicleType TREBUCHET_MOBILE_FIXED = new VehicleTypeTrebuchetMobileFixed(15);
    public static final IVehicleType TREBUCHET_LARGE = new VehicleTypeTrebuchetLarge(16);

    public static final IVehicleType CHEST_CART = new VehicleTypeChestCart(17);

    public static final IVehicleType BOAT_BALLISTA = new VehicleTypeBoatBallista(18);
    public static final IVehicleType BOAT_CATAPULT = new VehicleTypeBoatCatapult(19);
    public static final IVehicleType BOAT_TRANSPORT = new VehicleTypeBoatTransport(20);

    private VehicleRegistry() {
    }

    public static void registerVehicles() {
        LegacyConfiguration config = AncientWarfareVehicles.statics.getConfig();
        for (IVehicleType vehicle : VehicleType.vehicleTypes) {
            if (vehicle != null) {
                String key = vehicle.getConfigName();
                vehicle.setEnabled(config.get(VEHICLE_CONFIG_CATEGORY, key + ".enabled", vehicle.isEnabled(), "Enable this vehicle type.").getBoolean(vehicle.isEnabled()));
                if (!vehicle.isEnabled()) {
                    VehicleType.vehicleTypes[vehicle.getGlobalVehicleType()] = null;
                    continue;
                }
                vehicle.setEnabledForCrafting(config.get(VEHICLE_CONFIG_CATEGORY, key + ".craftable", vehicle.isEnabledForCrafting(), "Allow crafting this vehicle.").getBoolean(vehicle.isEnabledForCrafting()));
                vehicle.setEnabledForLoot(config.get(VEHICLE_CONFIG_CATEGORY, key + ".add_to_chests", vehicle.isEnabledForLoot(), "Allow this vehicle in generated loot.").getBoolean(vehicle.isEnabledForLoot()));
                vehicle.setBaseAccuracy((float) config.get(VEHICLE_CONFIG_CATEGORY, key + ".accuracy", vehicle.getBaseAccuracy(), "Base firing accuracy from 0 to 1.").getDouble(vehicle.getBaseAccuracy()));
                vehicle.setBaseForwardSpeed((float) config.get(VEHICLE_CONFIG_CATEGORY, key + ".forward_speed", vehicle.getBaseForwardSpeed(), "Base forward speed.").getDouble(vehicle.getBaseForwardSpeed()));
                vehicle.setBaseHealth((float) config.get(VEHICLE_CONFIG_CATEGORY, key + ".health", vehicle.getBaseHealth(), "Base maximum health.").getDouble(vehicle.getBaseHealth()));
                vehicle.setBaseMissileVelocity((float) config.get(VEHICLE_CONFIG_CATEGORY, key + ".missile_speed", vehicle.getBaseMissileVelocityMax(), "Base projectile speed.").getDouble(vehicle.getBaseMissileVelocityMax()));
                vehicle.setBasePitchMax((float) config.get(VEHICLE_CONFIG_CATEGORY, key + ".pitch_max", vehicle.getBasePitchMax(), "Maximum aiming pitch.").getDouble(vehicle.getBasePitchMax()));
                vehicle.setBasePitchMin((float) config.get(VEHICLE_CONFIG_CATEGORY, key + ".pitch_min", vehicle.getBasePitchMin(), "Minimum aiming pitch.").getDouble(vehicle.getBasePitchMin()));
                vehicle.setBaseStrafeSpeed((float) config.get(VEHICLE_CONFIG_CATEGORY, key + ".strafe_speed", vehicle.getBaseStrafeSpeed(), "Base turning speed.").getDouble(vehicle.getBaseStrafeSpeed()));
                vehicle.setBaseTurretRotationAmount((float) config.get(VEHICLE_CONFIG_CATEGORY, key + ".turret_rotation", vehicle.getBaseTurretRotationAmount(), "Maximum turret rotation from center.").getDouble(vehicle.getBaseTurretRotationAmount()));

                Iterator<IAmmo> it = vehicle.getValidAmmoTypes().iterator();
                IAmmo t;
                while (it.hasNext()) {
                    t = it.next();
                    if (!t.isEnabled()) {
                        it.remove();
                    }
                }
            }
        }
    }

}
