package net.shadowmage.ancientwarfare.structure.render;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.shadowmage.ancientwarfare.core.compat.client.GlStateManager;
import net.shadowmage.ancientwarfare.core.compat.client.LegacyBlockEntityRenderer;
import net.shadowmage.ancientwarfare.core.util.RenderTools;
import net.shadowmage.ancientwarfare.structure.tile.TileStructureBuilder;

public class RenderStructureBuilder extends LegacyBlockEntityRenderer<TileStructureBuilder> {

    @Override
    public void render(TileStructureBuilder builder, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        RenderTools.setFullColorLightmap();
        if (builder.clientBB != null) {
            GlStateManager.pushMatrix();
            //glPushAttrib is removed in core profile; enable-state is managed via RenderSystem now.
            GlStateManager.translate(x, y, z);
            BlockPos min = builder.clientBB.min;
            BlockPos max = builder.clientBB.max;
            if (max == null) {
                max = min;
            }
            if (min != null) {
                renderBoundingBox(builder.getPos(), min, max, 1.f, 1.f, 1.f, 0.f);
            }
            GlStateManager.popAttrib();
            GlStateManager.popMatrix();
        }
    }

    private void renderBoundingBox(BlockPos pos, BlockPos min, BlockPos max, float r, float g, float b, float expansion) {
        GlStateManager.disableLighting();
        GlStateManager.color(1.f, 1.f, 1.f, 1.f);
        AABB bb = new AABB(min, max.offset(1, 1, 1));
        if (expansion != 0.f) {
            bb = bb.expandTowards(expansion, expansion, expansion);
        }
        bb = bb.move(-pos.getX(), -pos.getY(), -pos.getZ());
        RenderTools.drawOutlinedBoundingBox2(bb, 1.f, 1.f, 1.f, 0.0625f);
        GlStateManager.enableLighting();
    }

    @Override
    public boolean isGlobalRenderer(TileStructureBuilder te) {
        return true;
    }
}
