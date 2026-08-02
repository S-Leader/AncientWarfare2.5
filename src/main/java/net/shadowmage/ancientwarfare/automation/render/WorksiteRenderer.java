package net.shadowmage.ancientwarfare.automation.render;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.shadowmage.ancientwarfare.automation.config.AWAutomationStatics;
import net.shadowmage.ancientwarfare.automation.tile.worksite.TileWorksiteBase;
import net.shadowmage.ancientwarfare.core.compat.client.LegacyBlockEntityRenderer;
import net.shadowmage.ancientwarfare.core.interfaces.IBoundedSite;

public class WorksiteRenderer extends LegacyBlockEntityRenderer<TileWorksiteBase> {

    public WorksiteRenderer() {

    }

    @Override
    public void render(TileWorksiteBase worksite, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        if (worksite instanceof IBoundedSite) {
            IBoundedSite boundedSite = (IBoundedSite) worksite;
            if (AWAutomationStatics.renderWorkBounds.getBoolean()) {
                BlockPos min = boundedSite.getWorkBoundsMin();
                BlockPos max = boundedSite.getWorkBoundsMax();
                if (max == null) {
                    max = min;
                }
                if (min != null) {
                    renderBoundingBox(worksite.getPos(), min, max, 1.f, 1.f, 1.f, 0.f);
                }
            }
        }
    }

    private void renderBoundingBox(BlockPos pos, BlockPos min, BlockPos max, float r, float g, float b, float expansion) {
        AABB bb = new AABB(min.getX(), min.getY(), min.getZ(), max.getX() + 1, max.getY() + 1, max.getZ() + 1);
        if (expansion != 0.f) {
            bb = bb.expandTowards(expansion, expansion, expansion);
        }
        bb = bb.move(-pos.getX(), -pos.getY(), -pos.getZ());
        // Use the same buffered line renderer as vanilla block selection boxes.
        // Immediate Tesselator drawing inside a block-entity render pass is not
        // reliably flushed by 1.20 and was why the legacy work bounds vanished.
        LevelRenderer.renderLineBox(getActivePoseStack(),
                getActiveBufferSource().getBuffer(RenderType.lines()), bb, r, g, b, 1.0F);
    }

}
