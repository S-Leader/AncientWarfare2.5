package net.shadowmage.ancientwarfare.vehicle.input;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.shadowmage.ancientwarfare.vehicle.entity.VehicleBase;

@OnlyIn(Dist.CLIENT)
public class VehicleKeyConflictContext implements IKeyConflictContext {
    public static final VehicleKeyConflictContext INSTANCE = new VehicleKeyConflictContext();

    private VehicleKeyConflictContext() {
    }

    @Override
    public boolean isActive() {
        Minecraft mc = Minecraft.getInstance();
        return mc.screen == null && mc.player != null && mc.level != null && mc.player.getVehicle() instanceof VehicleBase;
    }

    @Override
    public boolean conflicts(IKeyConflictContext other) {
        return this == other;
    }
}
