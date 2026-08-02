package net.shadowmage.ancientwarfare.core.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

/**
 * Runtime bridge retaining the geometry and animation methods of generated 1.12 models.
 */
public class LegacyModelBase {
    public int textureWidth = 64;
    public int textureHeight = 32;
    private static final ThreadLocal<RenderContext> CONTEXT = new ThreadLocal<>();

    public void render(Entity entity, float limbSwing, float limbSwingAmount, float age, float yaw, float pitch, float scale) {
    }

    public void setRotationAngles(float limbSwing, float limbSwingAmount, float age, float yaw, float pitch, float scale, Entity entity) {
    }

    public void setLivingAnimations(LivingEntity entity, float limbSwing, float limbSwingAmount, float partialTick) {
    }

    public static void renderWithContext(PoseStack poses, VertexConsumer vertices, int light, int overlay, Runnable renderer) {
        RenderContext previous = CONTEXT.get();
        CONTEXT.set(new RenderContext(poses, vertices, light, overlay));
        try {
            renderer.run();
        } finally {
            if (previous == null) CONTEXT.remove();
            else CONTEXT.set(previous);
        }
    }

    static RenderContext context() {
        return CONTEXT.get();
    }

    record RenderContext(PoseStack poses, VertexConsumer vertices, int light, int overlay) {
    }
}
