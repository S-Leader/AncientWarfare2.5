package net.shadowmage.ancientwarfare.structure.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.particles.ParticleTypes;

/**
 * Creates a vanilla flame particle and applies the altar's RGB tint.
 */
public final class ParticleColoredFlame {
    private ParticleColoredFlame() {
    }

    public static void spawn(double x, double y, double z, int color) {
        Particle particle = Minecraft.getInstance().particleEngine.createParticle(
                ParticleTypes.FLAME, x, y, z, 0.0D, 0.0D, 0.0D);
        if (particle != null) {
            particle.setColor(
                    (color >> 16 & 255) / 255.0F,
                    (color >> 8 & 255) / 255.0F,
                    (color & 255) / 255.0F);
        }
    }
}
