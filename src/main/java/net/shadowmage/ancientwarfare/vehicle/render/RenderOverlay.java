package net.shadowmage.ancientwarfare.vehicle.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.shadowmage.ancientwarfare.core.util.Trig;
import net.shadowmage.ancientwarfare.vehicle.config.AWVehicleStatics;
import net.shadowmage.ancientwarfare.vehicle.entity.VehicleBase;
import net.shadowmage.ancientwarfare.vehicle.entity.VehicleMovementType;
import net.shadowmage.ancientwarfare.vehicle.missiles.IAmmo;
import net.shadowmage.ancientwarfare.vehicle.registry.AmmoRegistry;

import java.awt.*;

public class RenderOverlay {
    private void renderVehicleOverlay(GuiGraphics graphics) {
        Minecraft mc = Minecraft.getInstance();
        Font fontRenderer = mc.font;

        VehicleBase vehicle = (VehicleBase) mc.player.getVehicle();
        int white = Color.WHITE.getRGB();
        int red = Color.RED.getRGB();
        //noinspection ConstantConditions
        if (vehicle.vehicleType.getMovementType() == VehicleMovementType.AIR1 || vehicle.vehicleType.getMovementType() == VehicleMovementType.AIR2) {
            graphics.drawString(fontRenderer, "Throttle: " + vehicle.moveHelper.throttle, 10, 10, white);
            graphics.drawString(fontRenderer, "Pitch: " + vehicle.getXRot(), 10, 20, white);
            graphics.drawString(fontRenderer, "Climb Rate: " + vehicle.getDeltaMovement().y * 20, 10, 30, white);
            graphics.drawString(fontRenderer, "Elevation: " + vehicle.getY(), 10, 40, white);
        } else {
            graphics.drawString(fontRenderer, "Range: " + vehicle.firingHelper.clientHitRange, 10, 10, white);
            graphics.drawString(fontRenderer, "Pitch: " + vehicle.firingHelper.clientTurretPitch, 10, 20, white);
            graphics.drawString(fontRenderer, "Yaw: " + vehicle.firingHelper.clientTurretYaw, 10, 30, white);
            graphics.drawString(fontRenderer, "Velocity: " + vehicle.firingHelper.clientLaunchSpeed, 10, 40, white);
        }
        IAmmo ammo = vehicle.ammoHelper.getCurrentAmmoType().orElse(null);
        if (ammo != null) {
            int count = vehicle.ammoHelper.getCurrentAmmoCount();
            graphics.drawString(fontRenderer, "Ammo: " + I18n.get(AmmoRegistry.getItemForAmmo(ammo).getDescriptionId() + ".name"), 10, 50,
                    count > 0 ? white : red);
            graphics.drawString(fontRenderer, "Count: " + count, 10, 60, count > 0 ? white : red);
        } else {
            graphics.drawString(fontRenderer, "No Ammo Selected", 10, 50, red);
        }
        if (AWVehicleStatics.clientSettings.renderAdvOverlay) {
            float velocity = Trig.getVelocity(vehicle.getDeltaMovement().x, 0, vehicle.getDeltaMovement().z);
            graphics.drawString(fontRenderer, "Velocity: " + velocity * 20.f + "m/s  max: " + vehicle.currentForwardSpeedMax * 20, 10, 70, white);
            graphics.drawString(fontRenderer, "Yaw Rate: " + vehicle.moveHelper.getRotationSpeed() * 20.f, 10, 80, white);
        }
    }

    @SubscribeEvent
    public void tickEnd(RenderGuiEvent.Post event) {
        if (!AWVehicleStatics.clientSettings.renderOverlay) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen == null && mc.player != null && mc.player.getVehicle() instanceof VehicleBase) {
            this.renderVehicleOverlay(event.getGuiGraphics());
        }
    }
}
