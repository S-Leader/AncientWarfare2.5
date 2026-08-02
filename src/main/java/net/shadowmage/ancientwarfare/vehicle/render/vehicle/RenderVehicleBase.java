package net.shadowmage.ancientwarfare.vehicle.render.vehicle;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.shadowmage.ancientwarfare.core.compat.client.Render;
import net.shadowmage.ancientwarfare.vehicle.entity.VehicleBase;

import javax.annotation.Nullable;

public abstract class RenderVehicleBase extends Render<VehicleBase> {

    protected RenderVehicleBase(EntityRendererProvider.Context renderManager) {
        super(renderManager);
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(VehicleBase entity) {
        return entity.getTexture();
    }

    public abstract void renderVehicle(VehicleBase entity, double x, double y, double z, float entityYaw, float partialTicks);
}
