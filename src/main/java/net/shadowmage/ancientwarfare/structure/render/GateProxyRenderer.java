package net.shadowmage.ancientwarfare.structure.render;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.shadowmage.ancientwarfare.core.client.model.LegacyModelBase;
import net.minecraft.core.BlockPos;
import net.shadowmage.ancientwarfare.core.compat.client.LegacyBlockEntityRenderer;
import net.shadowmage.ancientwarfare.structure.tile.TEGateProxy;

public class GateProxyRenderer extends LegacyBlockEntityRenderer<TEGateProxy> {
    private RenderGateHelper renderGateHelper = null;

    @Override
    public void render(TEGateProxy te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        if (!te.doesRender() || !initRenderHelper()) {
            return;
        }

        BlockPos pos = te.getPos();
        te.getGate().ifPresent(gate -> {
            PoseStack poses = getActivePoseStack();
            MultiBufferSource buffers = getActiveBufferSource();
            ResourceLocation texture = gate.getGateType().getTexture();
            VertexConsumer vertices = buffers.getBuffer(RenderType.entityCutoutNoCull(texture));

            /*
             * Calling RenderGateHelper#doRender directly bypasses Render#render,
             * which is normally responsible for installing LegacyModelBase's
             * VertexConsumer context.  Without this wrapper every ModelGate*
             * call reaches LegacyModelRenderer#render with a null context and
             * silently draws nothing.
             */
            LegacyModelBase.renderWithContext(poses, vertices, getActivePackedLight(), getActivePackedOverlay(),
                    () -> renderGateHelper.doRender(gate,
                            x + gate.getX() - pos.getX(),
                            y + gate.getY() - pos.getY(),
                            z + gate.getZ() - pos.getZ(),
                            0, partialTicks));
        });
    }

    private boolean initRenderHelper() {
        if (renderGateHelper != null) {
            return true;
        }
        EntityRendererProvider.Context renderManager = EntityRenderContextFactory.create();
        renderGateHelper = new RenderGateHelper(renderManager);
        return true;
    }

    @Override
    public boolean isGlobalRenderer(TEGateProxy te) {
        return true;
    }

    @Override
    public int getViewDistance() {
        // Match the old 256-block TESR range; the one rendering proxy may sit at
        // the far end of a large gate.
        return 256;
    }

}
