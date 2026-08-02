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

package net.shadowmage.ancientwarfare.vehicle.gui;

import net.minecraft.client.resources.language.I18n;
import net.shadowmage.ancientwarfare.core.container.ContainerBase;
import net.shadowmage.ancientwarfare.core.gui.GuiContainerBase;
import net.shadowmage.ancientwarfare.core.gui.elements.Label;
import net.shadowmage.ancientwarfare.core.init.AWMenuTypes;
import net.shadowmage.ancientwarfare.core.network.NetworkHandler;
import net.shadowmage.ancientwarfare.core.util.Trig;
import net.shadowmage.ancientwarfare.vehicle.container.ContainerVehicle;
import net.shadowmage.ancientwarfare.vehicle.entity.VehicleBase;

public class GuiVehicleStats extends GuiContainerBase<ContainerVehicle> {
    private final VehicleBase vehicle;

    public GuiVehicleStats(ContainerBase container) {
        super(container);
        this.vehicle = getContainer().vehicle;
        this.shouldCloseOnVanillaKeys = true;
    }

    @Override
    public int getXSize() {
        return 256;
    }

    @Override
    public int getYSize() {
        return 240;
    }

    @Override
    public void initElements() {
        addGuiElement(new Label(10, 4, stat("vehicle_type", vehicle.vehicleType.getLocalizedName())));
        addGuiElement(new Label(10, 14, stat("material_level", vehicle.vehicleMaterialLevel)));
        addGuiElement(new Label(10, 24, stat("health", vehicle.getHealth(), vehicle.baseHealth)));
        addGuiElement(new Label(10, 34, stat("weight", vehicle.currentWeight, vehicle.baseWeight)));
        addGuiElement(new Label(10, 44, stat("speed", Trig.getVelocity(vehicle.getDeltaMovement().x, vehicle.getDeltaMovement().y, vehicle.getDeltaMovement().z) * 20, vehicle.currentForwardSpeedMax * 20)));
        addGuiElement(new Label(10, 54, stat("missile_velocity", vehicle.localLaunchPower, vehicle.currentLaunchSpeedPowerMax)));
        addGuiElement(new Label(10, 64, stat("resists", vehicle.currentFireResist, vehicle.currentExplosionResist, vehicle.currentGenericResist)));
        addGuiElement(new Label(10, 74, stat("mountable", vehicle.isMountable())));
        addGuiElement(new Label(10, 84, stat("drivable", vehicle.isDrivable())));
        addGuiElement(new Label(10, 94, stat("combat", vehicle.isAimable())));
        addGuiElement(new Label(10, 104, stat("rider_sits", vehicle.shouldRiderSit())));
        addGuiElement(new Label(10, 114, stat("rider_on_turret", vehicle.vehicleType.moveRiderWithTurret())));
        addGuiElement(new Label(10, 124, stat("adjustable_yaw", vehicle.canAimRotate())));
        addGuiElement(new Label(10, 134, stat("adjustable_pitch", vehicle.canAimPitch())));
        addGuiElement(new Label(10, 144, stat("adjustable_power", vehicle.canAimPower())));
        addGuiElement(new Label(10, 154, stat("pitch_min", vehicle.currentTurretPitchMin)));
        addGuiElement(new Label(10, 164, stat("pitch_max", vehicle.currentTurretPitchMax)));
        addGuiElement(new Label(10, 174, stat("yaw_min", vehicle.localTurretRotationHome - vehicle.currentTurretRotationMax)));
        addGuiElement(new Label(10, 184, stat("yaw_max", vehicle.localTurretRotationHome + vehicle.currentTurretRotationMax)));
        addGuiElement(new Label(10, 204, stat("team", vehicle.getTeam() == null ? "" : vehicle.getTeam().getName())));
    }

    private String stat(String key, Object... values) {
        return I18n.get("gui.ancientwarfarevehicle.stats." + key, values);
    }

    @Override
    public void setupElements() {
    }

    @Override
    protected boolean onGuiCloseRequested() {
        AWMenuTypes.open(player, NetworkHandler.GUI_VEHICLE_INVENTORY, vehicle.getId());
        return false;
    }
}
