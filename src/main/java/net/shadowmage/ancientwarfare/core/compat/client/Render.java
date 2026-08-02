package net.shadowmage.ancientwarfare.core.compat.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.shadowmage.ancientwarfare.core.client.model.LegacyModelBase;

import javax.annotation.Nullable;

/**
 * EntityRenderer adapter shared by the old vehicle and projectile renderers.
 * The dispatcher has already translated the supplied pose stack to the entity,
 * so legacy doRender receives a zero local origin.
 */
public abstract class Render<T extends Entity> extends EntityRenderer<T> {
    protected final LegacyRenderManagerView renderManager;
    protected float shadowSize;

    protected Render(EntityRendererProvider.Context context) {
        super(context);
        renderManager = new LegacyRenderManagerView();
    }

    public void doRender(T entity, double x, double y, double z, float entityYaw, float partialTicks) {
    }

    @Override
    public void render(T entity, float entityYaw, float partialTicks, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight) {
        renderManager.update();
        ResourceLocation texture = getTextureLocation(entity);
        if (texture != null) {
            LegacyModelBase.renderWithContext(poseStack,
                    bufferSource.getBuffer(RenderType.entityCutoutNoCull(texture)),
                    packedLight, OverlayTexture.NO_OVERLAY,
                    () -> LegacyRenderContext.run(poseStack,
                            () -> doRender(entity, 0.0D, 0.0D, 0.0D, entityYaw, partialTicks)));
        } else {
            LegacyRenderContext.run(poseStack,
                    () -> doRender(entity, 0.0D, 0.0D, 0.0D, entityYaw, partialTicks));
        }
        super.render(entity, entityYaw, partialTicks, poseStack, bufferSource, packedLight);
    }

    @Nullable
    protected abstract ResourceLocation getEntityTexture(T entity);

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return getEntityTexture(entity);
    }

    protected void bindTexture(ResourceLocation texture) {
        if (texture != null) {
            RenderSystem.setShaderTexture(0, texture);
        }
    }

    protected boolean bindEntityTexture(T entity) {
        ResourceLocation texture = getEntityTexture(entity);
        bindTexture(texture);
        return texture != null;
    }

    protected Font getFontRendererFromRenderManager() {
        return Minecraft.getInstance().font;
    }

    public static final class LegacyRenderManagerView {
        public float playerViewY;
        public float playerViewX;
        public Entity renderViewEntity;

        private void update() {
            Minecraft minecraft = Minecraft.getInstance();
            playerViewY = minecraft.gameRenderer.getMainCamera().getYRot();
            playerViewX = minecraft.gameRenderer.getMainCamera().getXRot();
            renderViewEntity = minecraft.getCameraEntity();
        }
    }
}
