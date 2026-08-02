package net.shadowmage.ancientwarfare.structure.render;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
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
        te.getGate().ifPresent(gate -> renderGateHelper.doRender(gate,
                x + gate.getX() - pos.getX(),
                y + gate.getY() - pos.getY(),
                z + gate.getZ() - pos.getZ(),
                0, partialTicks));
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
}
