package net.shadowmage.ancientwarfare.structure.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Replacement for the removed 1.12 Minecraft#getRenderManager lookup - builds an
 * EntityRendererProvider.Context from the client instance for renderers that are
 * constructed outside of the entity renderer registration event.
 */
@OnlyIn(Dist.CLIENT)
public final class EntityRenderContextFactory {
    private EntityRenderContextFactory() {
    }

    public static EntityRendererProvider.Context create() {
        Minecraft mc = Minecraft.getInstance();
        return new EntityRendererProvider.Context(mc.getEntityRenderDispatcher(), mc.getItemRenderer(), mc.getBlockRenderer(),
                mc.getEntityRenderDispatcher().getItemInHandRenderer(), mc.getResourceManager(), mc.getEntityModels(), mc.font);
    }
}
