package net.shadowmage.ancientwarfare.vehicle.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.shadowmage.ancientwarfare.core.compat.client.GlStateManager;
import net.shadowmage.ancientwarfare.core.util.RenderTools;
import net.shadowmage.ancientwarfare.core.util.Trig;
import net.shadowmage.ancientwarfare.vehicle.config.AWVehicleStatics;
import net.shadowmage.ancientwarfare.vehicle.entity.VehicleBase;
import net.shadowmage.ancientwarfare.vehicle.entity.VehicleMovementType;
import net.shadowmage.ancientwarfare.vehicle.missiles.AmmoHwachaRocket;
import net.shadowmage.ancientwarfare.vehicle.registry.VehicleRegistry;
import org.lwjgl.opengl.GL11;

@OnlyIn(Dist.CLIENT)
public class RenderOverlayAdvanced {

    @SubscribeEvent
    public void renderLast(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
            return;
        }
        Player player = Minecraft.getInstance().player;
        if (AWVehicleStatics.clientSettings.renderAdvOverlay && player.getVehicle() instanceof VehicleBase && Minecraft.getInstance().screen == null) {
            RenderOverlayAdvanced.renderAdvancedVehicleOverlay((VehicleBase) player.getVehicle(), player, event.getPartialTick());
        }
    }

    private static void renderAdvancedVehicleOverlay(VehicleBase vehicle, Player player, float partialTick) {
        if (vehicle.vehicleType == VehicleRegistry.BATTERING_RAM) {
            renderBatteringRamOverlay(vehicle, player, partialTick);
        } else if (vehicle.ammoHelper.isCurrentAmmoRocket()) {
            renderOverlay(vehicle, player, partialTick,
                    (buffer, rOffset, speed, accVec, gravity) -> drawRocketFlightPath(vehicle, player, buffer, rOffset, speed, accVec, gravity));
        } else {
            renderOverlay(vehicle, player, partialTick, (buffer, rOffset, speed, accVec, gravity) -> drawNormalTrajectory(vehicle, player, buffer, rOffset, gravity, accVec));
        }
    }

    private static void renderOverlay(VehicleBase vehicle, Player player, float partialTick, IDynamicOverlayPartRenderer dynamicRenderer) {
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        //1.12 glColor/texture toggles are handled per-vertex + by shader selection in the draw methods below

        Vec3 renderOffset = new Vec3(vehicle.getX() - player.getX(), vehicle.getY() - player.getY(), vehicle.getZ() - player.getZ());

        drawStraightLine(vehicle, partialTick, renderOffset);

        drawDynamicPart(vehicle, partialTick, renderOffset, dynamicRenderer);

        GlStateManager.popMatrix();
        GlStateManager.depthMask(true);
        GlStateManager.disableBlend();
    }

    private static void drawStraightLine(VehicleBase vehicle, float partialTick, Vec3 renderOffset) {
        double x2 = renderOffset.x - 20 * Trig.sinDegrees(vehicle.getYRot() + partialTick * vehicle.moveHelper.getRotationSpeed());
        double z2 = renderOffset.z - 20 * Trig.cosDegrees(vehicle.getYRot() + partialTick * vehicle.moveHelper.getRotationSpeed());
        GlStateManager.glLineWidth(3f);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
        buffer.vertex(renderOffset.x, renderOffset.y + 0.12d, renderOffset.z).color(1.f, 1.f, 1.f, 0.6f).endVertex();
        buffer.vertex(x2, renderOffset.y + 0.12d, z2).color(1.f, 1.f, 1.f, 0.6f).endVertex();
        BufferUploader.drawWithShader(buffer.end());
    }

    private static void drawDynamicPart(VehicleBase vehicle, float partialTick, Vec3 renderOffset, IDynamicOverlayPartRenderer dynamicRenderer) {
        GlStateManager.glLineWidth(4f);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);

        double gravity = 9.81d * 0.05d * 0.05d;
        double speed = vehicle.localLaunchPower * 0.05d;
        double angle = 90 - vehicle.localTurretPitch - vehicle.getXRot();
        double yaw = vehicle.localTurretRotation + partialTick * vehicle.moveHelper.getRotationSpeed();

        double vH = -Trig.sinDegrees((float) angle) * speed;
        Vec3 accelerationVector = new Vec3(Trig.sinDegrees((float) yaw) * vH, Trig.cosDegrees((float) angle) * speed, Trig.cosDegrees((float) yaw) * vH);

        dynamicRenderer.render(buffer, renderOffset, speed, accelerationVector, gravity);
        BufferUploader.drawWithShader(buffer.end());
    }

    private static void drawRocketFlightPath(VehicleBase vehicle, Player player, BufferBuilder buffer, Vec3 renderOffset, double speed, Vec3 accelerationVector,
                                             double gravity) {
        int rocketBurnTime = (int) (speed * 20.f * AmmoHwachaRocket.BURN_TIME_FACTOR);

        Vec3 offset = vehicle.getMissileOffset();
        double x2 = renderOffset.x + offset.x;
        double y2 = renderOffset.y + offset.y;
        double z2 = renderOffset.z + offset.z;

        double floorY = renderOffset.y;

        Vec3 adjustedAccelerationVector = accelerationVector;
        if (vehicle.vehicleType.getMovementType() == VehicleMovementType.AIR1 || vehicle.vehicleType.getMovementType() == VehicleMovementType.AIR2) {
            adjustedAccelerationVector = adjustedAccelerationVector.add(vehicle.getDeltaMovement());
            floorY = -player.getY();
        }

        float xAcc = (float) (adjustedAccelerationVector.x / speed) * AmmoHwachaRocket.ACCELERATION_FACTOR;
        float yAcc = (float) (adjustedAccelerationVector.y / speed) * AmmoHwachaRocket.ACCELERATION_FACTOR;
        float zAcc = (float) (adjustedAccelerationVector.z / speed) * AmmoHwachaRocket.ACCELERATION_FACTOR;
        adjustedAccelerationVector = new Vec3(xAcc, yAcc, zAcc);

        while (y2 >= floorY) {
            buffer.vertex(x2, y2, z2).color(1.f, 0.4f, 0.4f, 0.4f).endVertex();
            x2 += adjustedAccelerationVector.x;
            z2 += adjustedAccelerationVector.z;
            y2 += adjustedAccelerationVector.y;
            if (rocketBurnTime > 0) {
                rocketBurnTime--;
                adjustedAccelerationVector = adjustedAccelerationVector.add(xAcc, yAcc, zAcc);
            } else {
                adjustedAccelerationVector = adjustedAccelerationVector.add(0, -gravity, 0);
            }
            buffer.vertex(x2, y2, z2).color(1.f, 0.4f, 0.4f, 0.4f).endVertex();
        }
    }

    private static void drawNormalTrajectory(VehicleBase vehicle, Player player, BufferBuilder buffer, Vec3 renderOffset, double gravity, Vec3 accelerationVector) {
        Vec3 offset = vehicle.getMissileOffset();
        double x2 = renderOffset.x + offset.x;
        double y2 = renderOffset.y + offset.y;
        double z2 = renderOffset.z + offset.z;

        double floorY = renderOffset.y;

        Vec3 adjustedAccelerationVector = accelerationVector;
        if (vehicle.vehicleType.getMovementType() == VehicleMovementType.AIR1 || vehicle.vehicleType.getMovementType() == VehicleMovementType.AIR2) {
            adjustedAccelerationVector = adjustedAccelerationVector.add(vehicle.getDeltaMovement());
            floorY = -player.getY();
        }

        while (y2 >= floorY) {
            buffer.vertex(x2, y2, z2).color(1.f, 0.4f, 0.4f, 0.4f).endVertex();
            x2 += adjustedAccelerationVector.x;
            z2 += adjustedAccelerationVector.z;
            y2 += adjustedAccelerationVector.y;
            adjustedAccelerationVector = adjustedAccelerationVector.add(0, -gravity, 0);
            buffer.vertex(x2, y2, z2).color(1.f, 0.4f, 0.4f, 0.4f).endVertex();
        }
    }

    private static void renderBatteringRamOverlay(VehicleBase vehicle, Player player, float partialTick) {
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        //1.12 glColor/texture toggles are handled per-vertex + by shader selection below

        double x1 = vehicle.getX() - player.getX();
        double y1 = vehicle.getY() - player.getY();
        double z1 = vehicle.getZ() - player.getZ();

        /*
         * vectors for a straight line
         */
        double x2 = x1 - 20 * Trig.sinDegrees(vehicle.getYRot());
        double z2 = z1 - 20 * Trig.cosDegrees(vehicle.getYRot());
        GlStateManager.glLineWidth(3f);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
        buffer.vertex(x1, y1 + 0.12d, z1).color(1.f, 1.f, 1.f, 0.6f).endVertex();
        buffer.vertex(x2, y1 + 0.12d, z2).color(1.f, 1.f, 1.f, 0.6f).endVertex();
        BufferUploader.drawWithShader(buffer.end());

        Vec3 offset = vehicle.getMissileOffset();
        float bx = (float) (vehicle.getX() + offset.x);
        float by = (float) (vehicle.getY() + offset.y);
        float bz = (float) (vehicle.getZ() + offset.z);
        BlockPos blockHit = BlockPos.containing(bx, by, bz);
        AABB bb = new AABB(blockHit.getX() - 1, blockHit.getY(), blockHit.getZ(), blockHit.getX() + 2, blockHit.getY() + 1,
                blockHit.getZ() + 1);
        bb = adjustBBForPlayerPos(bb, player, partialTick);
        RenderTools.drawOutlinedBoundingBox(bb, 1.f, 0.2f, 0.2f);
        bb = new AABB(blockHit.getX(), blockHit.getY(), blockHit.getZ() - 1, blockHit.getX() + 1, blockHit.getY() + 1, blockHit.getZ() + 2);
        bb = adjustBBForPlayerPos(bb, player, partialTick);
        RenderTools.drawOutlinedBoundingBox(bb, 1.f, 0.2f, 0.2f);
        bb = new AABB(blockHit.getX(), blockHit.getY() - 1, blockHit.getZ(), blockHit.getX() + 1, blockHit.getY() + 2, blockHit.getZ() + 1);
        bb = adjustBBForPlayerPos(bb, player, partialTick);
        RenderTools.drawOutlinedBoundingBox(bb, 1.f, 0.2f, 0.2f);
        GlStateManager.popMatrix();

        GlStateManager.depthMask(true);

        GlStateManager.disableBlend();
    }

    private static AABB adjustBBForPlayerPos(AABB bb, Player player, float partialTick) {
        double x = player.xOld + (player.getX() - player.xOld) * partialTick;
        double y = player.yOld + (player.getY() - player.yOld) * partialTick;
        double z = player.zOld + (player.getZ() - player.zOld) * partialTick;
        return bb.move(-x, -y, -z);
    }

    private interface IDynamicOverlayPartRenderer {
        void render(BufferBuilder buffer, Vec3 renderOffset, double speed, Vec3 accelerationVector, double gravity);
    }
}
