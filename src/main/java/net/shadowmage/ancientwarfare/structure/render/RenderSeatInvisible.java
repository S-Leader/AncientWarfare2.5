package net.shadowmage.ancientwarfare.structure.render;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.shadowmage.ancientwarfare.core.compat.client.Render;
import net.shadowmage.ancientwarfare.structure.entity.EntitySeat;

import javax.annotation.Nullable;

/**
 * The seat is only a mount point for chair-like blocks and must not render.
 * It still needs a registered renderer: Minecraft 1.20 no longer tolerates a
 * client-side entity type without one and crashes while culling the level.
 */
public class RenderSeatInvisible extends Render<EntitySeat> {
    public RenderSeatInvisible(EntityRendererProvider.Context renderManager) {
        super(renderManager);
    }

    @Override
    public void doRender(EntitySeat entity, double x, double y, double z, float entityYaw, float partialTicks) {
        // Invisible mount point.
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(EntitySeat entity) {
        return null;
    }
}
