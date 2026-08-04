package net.shadowmage.ancientwarfare.vehicle.input;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.core.input.IInputCallback;
import net.shadowmage.ancientwarfare.vehicle.entity.VehicleBase;

import java.util.Objects;
import java.util.function.Consumer;

@OnlyIn(Dist.CLIENT)
public final class VehicleCallback implements IInputCallback {
    private final Consumer<VehicleBase> callback;

    public VehicleCallback(Consumer<VehicleBase> callback) {
        this.callback = Objects.requireNonNull(callback, "callback");
    }

    @Override
    public void onKeyPressed() {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;

        // InputCallbackDispatcher receives global key presses. The dedicated
        // vehicle mappings may therefore fire while no world/player exists or
        // while the player is not mounted. Never pass a null vehicle to the
        // callback lambdas (ammoHelper/firingHelper are accessed immediately).
        if (player == null || !(player.getVehicle() instanceof VehicleBase vehicle)) {
            return;
        }

        callback.accept(vehicle);
    }
}
