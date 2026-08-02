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

package net.shadowmage.ancientwarfare.vehicle.helpers;


import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.INBTSerializable;
import net.shadowmage.ancientwarfare.core.init.AWMenuTypes;
import net.shadowmage.ancientwarfare.core.network.NetworkHandler;
import net.shadowmage.ancientwarfare.npc.entity.NpcBase;
import net.shadowmage.ancientwarfare.vehicle.entity.VehicleBase;

public abstract class VehicleFiringVarsHelper implements INBTSerializable<CompoundTag> {

    protected VehicleBase vehicle;

    public VehicleFiringVarsHelper(VehicleBase vehicle) {
        this.vehicle = vehicle;
    }

    /**
     * called on every tick that the vehicle is 'firing' to update the firing animation and to call
     * launchMissile when animation has reached launch point
     */
    public abstract void onFiringUpdate();

    /**
     * called every tick after the vehicle has fired, until reload timer is complete, to update animations
     */
    public abstract void onReloadUpdate();

    /**
     * called every tick after startLaunching() is called, until setFinishedLaunching() is called...
     */
    public abstract void onLaunchingUpdate();

    public abstract void onReloadingFinished();

    public void onTick() {
    }

    public boolean interact(Player player) {
        if (player.level().isClientSide) {
            return true;
        }
        if (!player.isShiftKeyDown() && !vehicle.isVehicle()) {
            player.startRiding(vehicle);
            return true;
        } else if (player.isShiftKeyDown()) {
            AWMenuTypes.open(player, NetworkHandler.GUI_VEHICLE_INVENTORY, vehicle.getId());
        } else if (vehicle.isVehicle() && vehicle.getPassengers().get(0) instanceof NpcBase) {
            NpcBase npc = (NpcBase) vehicle.getPassengers().get(0);
            npc.stopRiding();
        }
        return true;
    }

    public abstract float getVar1();

    public abstract float getVar2();

    public abstract float getVar3();

    public abstract float getVar4();

    public abstract float getVar5();

    public abstract float getVar6();

    public abstract float getVar7();

    public abstract float getVar8();

}
