package net.shadowmage.ancientwarfare.vehicle.input;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.core.input.IInputCallback;
import net.shadowmage.ancientwarfare.vehicle.entity.VehicleBase;

import java.util.function.Consumer;

@OnlyIn(Dist.CLIENT)
public class VehicleCallback implements IInputCallback {
    private final Consumer<VehicleBase> callback;

    public VehicleCallback(Consumer<VehicleBase> callback) {
        this.callback = callback;
    }

    @Override
    public void onKeyPressed() {
        Minecraft mc = Minecraft.getInstance();
        VehicleBase vehicle = (VehicleBase) mc.player.getVehicle();
        callback.accept(vehicle);
    }
}
