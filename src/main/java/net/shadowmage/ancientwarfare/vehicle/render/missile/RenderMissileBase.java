package net.shadowmage.ancientwarfare.vehicle.render.missile;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.shadowmage.ancientwarfare.core.compat.client.GlStateManager;
import net.shadowmage.ancientwarfare.core.compat.client.Render;
import net.shadowmage.ancientwarfare.vehicle.missiles.IAmmo;
import net.shadowmage.ancientwarfare.vehicle.missiles.MissileBase;

import javax.annotation.Nullable;

public abstract class RenderMissileBase extends Render<MissileBase> {
    static Minecraft mc = Minecraft.getInstance();

    protected RenderMissileBase(EntityRendererProvider.Context renderManager) {
        super(renderManager);
    }

    @Override
    public void doRender(MissileBase missile, double x, double y, double z, float entityYaw, float partialTicks) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.rotate(entityYaw - 90, 0, 1, 0);
        GlStateManager.rotate(missile.getXRot() - 90, 1, 0, 0);
        GlStateManager.scale(-1, -1, 1);
        float scale = missile.ammoType.getRenderScale();
        GlStateManager.scale(scale, scale, scale);

        bindTexture(missile.getTexture());
        renderMissile(missile, missile.ammoType, x, y, z, entityYaw, partialTicks);
        GlStateManager.popMatrix();
    }

    public abstract void renderMissile(MissileBase missile, IAmmo ammo, double x, double y, double z, float yaw, float tick);

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(MissileBase entity) {
        return entity.getTexture();
    }
}
