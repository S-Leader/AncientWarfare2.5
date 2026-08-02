package net.shadowmage.ancientwarfare.structure.render.statue;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.shadowmage.ancientwarfare.core.client.model.LegacyModelBase;
import net.shadowmage.ancientwarfare.core.client.model.LegacyModelRenderer;
import net.shadowmage.ancientwarfare.core.compat.client.LegacyBlockEntityRenderer;
import net.shadowmage.ancientwarfare.core.util.MathUtils;
import net.shadowmage.ancientwarfare.structure.AncientWarfareStructure;
import net.shadowmage.ancientwarfare.structure.tile.EntityStatueInfo;
import net.shadowmage.ancientwarfare.structure.tile.TileStatue;

import javax.annotation.Nullable;
import java.util.Map;

public class StatueRenderer extends LegacyBlockEntityRenderer<TileStatue> {
    @Override
    public void render(TileStatue te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        EntityStatueInfo statueInfo = te.getEntityStatueInfo();

        if (statueInfo.getRenderType() == EntityStatueInfo.RenderType.MODEL) {
            StatueEntityRegistry.StatueEntity statueEntity = StatueEntityRegistry.getStatueEntity(statueInfo.getStatueEntityName());
            LivingEntity entity = statueEntity.instantiateEntity(Minecraft.getInstance().level);

            EntityRenderer<? super LivingEntity> render = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(entity);

            IStatueModel statueModel = statueEntity.getStatueModel();
            applyPartTransforms(statueModel, statueInfo.getPartTransforms());

            doRender(te, statueModel, statueInfo.getOverallTransform(), entity, (float) x + 0.5f, (float) y, (float) z + 0.5f, render.getTextureLocation(entity), te.getPrimaryFacing());
        }
    }

    private void applyPartTransforms(IStatueModel statueModel, Map<String, EntityStatueInfo.Transform> partTransforms) {
        for (String partName : statueModel.getModelPartNames()) {
            LegacyModelRenderer part = statueModel.getModelPart(partName);
            EntityStatueInfo.Transform transform = partTransforms.getOrDefault(partName, new EntityStatueInfo.Transform());
            EntityStatueInfo.Transform baseTransform = statueModel.getBaseTransforms().getOrDefault(partName, new EntityStatueInfo.Transform());
            part.offsetX = baseTransform.getOffsetX() + transform.getOffsetX();
            part.offsetY = baseTransform.getOffsetY() + transform.getOffsetY();
            part.offsetZ = baseTransform.getOffsetZ() + transform.getOffsetZ();

            part.rotateAngleX = baseTransform.getRotationX() + transform.getRotationX();
            part.rotateAngleY = baseTransform.getRotationY() + transform.getRotationY();
            part.rotateAngleZ = baseTransform.getRotationZ() + transform.getRotationZ();
        }
    }

    private void doRender(TileStatue te, IStatueModel statueModel, EntityStatueInfo.Transform overallTransform, LivingEntity entity, float x, float y, float z, ResourceLocation entityTexture, Direction primaryFacing) {
        Minecraft mc = Minecraft.getInstance();
        BlockPos pos = te.getPos();
        Vec3 cam = mc.gameRenderer.getMainCamera().getPosition();
        PoseStack poseStack = new PoseStack();
        poseStack.translate(pos.getX() - cam.x, pos.getY() - cam.y, pos.getZ() - cam.z);

        try {
            float f = entity.yBodyRot + overallTransform.getRotationY() + primaryFacing.toYRot();

            poseStack.translate(x + overallTransform.getOffsetX(), y + overallTransform.getOffsetY(), z + overallTransform.getOffsetZ());
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - f));
            if (!MathUtils.epsilonEquals(overallTransform.getRotationX(), 0)) {
                poseStack.mulPose(Axis.XP.rotationDegrees(overallTransform.getRotationX()));
            }
            if (!MathUtils.epsilonEquals(overallTransform.getRotationZ(), 0)) {
                poseStack.mulPose(Axis.ZP.rotationDegrees(overallTransform.getRotationZ()));
            }
            float scale = overallTransform.getScale();
            if (!MathUtils.epsilonEquals(scale, 1)) {
                poseStack.scale(scale, scale, scale);
            }
            //equivalent of the 1.12 RenderLivingBase.prepareScale flip and offset
            poseStack.scale(-1.0F, -1.0F, 1.0F);
            poseStack.translate(0.0F, -1.501F, 0.0F);
            float f4 = 0.0625F;
            float limbSwingAmount = 0.0F;
            float limbSwing = 0.0F;

            if (!entity.isPassenger()) {
                limbSwingAmount = entity.walkAnimation.speed();
                limbSwing = entity.walkAnimation.position() - entity.walkAnimation.speed();

                if (entity.isBaby()) {
                    limbSwing *= 3.0F;
                }

                if (limbSwingAmount > 1.0F) {
                    limbSwingAmount = 1.0F;
                }
            }

            statueModel.getModel().setLivingAnimations(entity, limbSwing, limbSwingAmount, 0);

            renderModel(te, statueModel, entityTexture, f4, poseStack);
        } catch (Exception exception) {
            AncientWarfareStructure.LOG.error("Couldn't render entity", (Throwable) exception);
        }
    }

    private void renderModel(TileStatue te, IStatueModel model, @Nullable ResourceLocation entityTexture, float scale, PoseStack poseStack) {
        if (entityTexture == null) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();
        VertexConsumer vertices = buffer.getBuffer(RenderType.entityCutoutNoCull(entityTexture));
        int packedLight = te.getWorld() != null ? LevelRenderer.getLightColor(te.getWorld(), te.getPos()) : LightTexture.FULL_BRIGHT;
        poseStack.pushPose();
        LegacyModelBase.renderWithContext(poseStack, vertices, packedLight, OverlayTexture.NO_OVERLAY, () -> model.render(scale));
        poseStack.popPose();
        buffer.endBatch();
    }
}
