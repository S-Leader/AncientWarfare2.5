package net.shadowmage.ancientwarfare.structure.render;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;

/**
 * 1.20.1 replacement for the old manually constructed Particle subclass.
 * Vanilla dust particles provide the same short-lived glowing mote without
 * depending on removed particle texture-index APIs.
 */
public final class ParticleSun {
    private static final Vector3f COLOR = new Vector3f(1.0F, 213.0F / 255.0F, 74.0F / 255.0F);

    private ParticleSun() {
    }

    public static void spawn(Level level, double x, double y, double z) {
        float scale = 0.35F + level.getRandom().nextFloat() * 0.2F;
        level.addParticle(new DustParticleOptions(COLOR, scale), x, y, z, 0.0D, 0.0D, 0.0D);
    }
}
