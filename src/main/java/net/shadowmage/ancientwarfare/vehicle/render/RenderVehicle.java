package net.shadowmage.ancientwarfare.vehicle.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.shadowmage.ancientwarfare.core.compat.client.GlStateManager;
import net.shadowmage.ancientwarfare.core.compat.client.Render;
import net.shadowmage.ancientwarfare.vehicle.config.AWVehicleStatics;
import net.shadowmage.ancientwarfare.vehicle.entity.IVehicleType;
import net.shadowmage.ancientwarfare.vehicle.entity.VehicleBase;
import net.shadowmage.ancientwarfare.vehicle.registry.VehicleRegistry;
import net.shadowmage.ancientwarfare.vehicle.render.vehicle.*;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.util.HashMap;

public class RenderVehicle extends Render<VehicleBase> {

    private HashMap<IVehicleType, RenderVehicleBase> vehicleRenders = new HashMap<>();

    public RenderVehicle(EntityRendererProvider.Context renderManager) {
        super(renderManager);

        vehicleRenders.put(VehicleRegistry.CATAPULT_STAND_FIXED, new RenderCatapultStandFixed(renderManager));
        vehicleRenders.put(VehicleRegistry.CATAPULT_STAND_TURRET, new RenderCatapultStandTurret(renderManager));
        vehicleRenders.put(VehicleRegistry.CATAPULT_MOBILE_FIXED, new RenderCatapultMobileFixed(renderManager));
        vehicleRenders.put(VehicleRegistry.CATAPULT_MOBILE_TURRET, new RenderCatapultMobileTurret(renderManager));
        vehicleRenders.put(VehicleRegistry.BALLISTA_STAND_FIXED, new RenderBallistaStand(renderManager));
        vehicleRenders.put(VehicleRegistry.BALLISTA_STAND_TURRET, new RenderBallistaStand(renderManager));
        vehicleRenders.put(VehicleRegistry.BALLISTA_MOBILE_FIXED, new RenderBallistaMobile(renderManager));
        vehicleRenders.put(VehicleRegistry.BALLISTA_MOBILE_TURRET, new RenderBallistaMobile(renderManager));
        vehicleRenders.put(VehicleRegistry.BATTERING_RAM, new RenderBatteringRam(renderManager));
        vehicleRenders.put(VehicleRegistry.CANNON_STAND_FIXED, new RenderCannonStandFixed(renderManager));
        vehicleRenders.put(VehicleRegistry.CANNON_STAND_TURRET, new RenderCannonStandTurret(renderManager));
        vehicleRenders.put(VehicleRegistry.CANNON_MOBILE_FIXED, new RenderCannonMobileFixed(renderManager));
        vehicleRenders.put(VehicleRegistry.HWACHA, new RenderHwacha(renderManager));
        vehicleRenders.put(VehicleRegistry.TREBUCHET_STAND_FIXED, new RenderTrebuchetStandFixed(renderManager));
        vehicleRenders.put(VehicleRegistry.TREBUCHET_STAND_TURRET, new RenderTrebuchetStandTurret(renderManager));
        vehicleRenders.put(VehicleRegistry.TREBUCHET_MOBILE_FIXED, new RenderTrebuchetMobileFixed(renderManager));
        vehicleRenders.put(VehicleRegistry.TREBUCHET_LARGE, new RenderTrebuchetLarge(renderManager));
        vehicleRenders.put(VehicleRegistry.CHEST_CART, new RenderChestCart(renderManager));
        vehicleRenders.put(VehicleRegistry.BOAT_BALLISTA, new RenderBoatBallista(renderManager));
        vehicleRenders.put(VehicleRegistry.BOAT_CATAPULT, new RenderBoatCatapult(renderManager));
        vehicleRenders.put(VehicleRegistry.BOAT_TRANSPORT, new RenderBoatTransport(renderManager));
    }

    @Override
    public void doRender(VehicleBase vehicle, double x, double y, double z, float renderYaw, float partialTicks) {
        boolean useAlpha = false;
        if (!AWVehicleStatics.clientSettings.renderVehiclesInFirstPerson && vehicle.getControllingPassenger() == Minecraft.getInstance().player && Minecraft.getInstance().options.getCameraType().isFirstPerson()) {
            useAlpha = true;
            GlStateManager.color(1.f, 1.f, 1.f, 0.2f);
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        }
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.rotate(renderYaw, 0, 1, 0);
        GlStateManager.scale(-1, -1, 1);
        if (vehicle.hitAnimationTicks > 0) {
            float percent = ((float) vehicle.hitAnimationTicks / 20.f);
            GlStateManager.color(1.f, 1.f - percent, 1.f - percent, 1.f);
        }
        bindTexture(vehicle.getTexture());
        RenderVehicleBase render = vehicleRenders.get(vehicle.vehicleType);
        render.renderVehicle(vehicle, x, y, z, renderYaw, partialTicks);
        GlStateManager.color(1.f, 1.f, 1.f, 1.f);
        GlStateManager.popMatrix();
        if (useAlpha) {
            GlStateManager.disableBlend();
        }

        // dont' render nameplate for the vehicle that thePlayer is on
        if (isInWorld(vehicle) && AWVehicleStatics.clientSettings.renderVehicleNameplates && vehicle.getControllingPassenger() != Minecraft.getInstance().player) {
            renderNamePlate(vehicle, x, y, z);
        }

    }

    private boolean isInWorld(VehicleBase vehicle) {
        return vehicle.getY() > 0;
    }

    private DecimalFormat formatter1d = new DecimalFormat("#.#");

    private void renderNamePlate(VehicleBase vehicle, double x, double y, double z) {
        double var10 = vehicle.distanceToSqr(this.renderManager.renderViewEntity);
        int par9 = 64;
        String par2Str = vehicle.vehicleType.getLocalizedName();
        if (AWVehicleStatics.clientSettings.renderVehicleNameplateHealth) {
            par2Str = par2Str + " " + formatter1d.format(vehicle.getHealth()) + "/" + formatter1d.format(vehicle.baseHealth);
        }
        if (var10 <= (double) (par9 * par9)) {
            Font var12 = this.getFontRendererFromRenderManager();
            float var13 = 1.6F;
            float var14 = 0.016666668F * var13;
            float namePlateHeight = vehicle.getBbHeight() + 0.75f;
            GlStateManager.pushMatrix();
            GlStateManager.translate((float) x + 0.0F, (float) y + namePlateHeight, (float) z);
            GlStateManager.glNormal3f(0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(-this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
            GlStateManager.scale(-var14, -var14, var14);
            GlStateManager.disableLighting();
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            byte var16 = 0;
            float xStart = -var12.width(par2Str) / 2f;
            int backgroundColor = (int) (0.25F * 255.0F) << 24;
            MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
            //see-through pass with background replaces the 1.12 depth-disabled draw + background quad, normal pass matches the depth-enabled draw
            var12.drawInBatch(par2Str, xStart, var16, 553648127, false, new Matrix4f(), buffer, Font.DisplayMode.SEE_THROUGH, backgroundColor, LightTexture.FULL_BRIGHT);
            var12.drawInBatch(par2Str, xStart, var16, -1, false, new Matrix4f(), buffer, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
            buffer.endBatch();
            GlStateManager.enableLighting();
            GlStateManager.disableBlend();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

            GlStateManager.popMatrix();
        }
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(VehicleBase entity) {
        return entity.getTexture();
    }

}
