package net.shadowmage.ancientwarfare.structure.render.statue;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Replica of the removed 1.12 vanilla ModelZombie geometry.
 */
@OnlyIn(Dist.CLIENT)
public class LegacyZombieModel extends LegacyBipedModel {
    public LegacyZombieModel() {
        super(0.0F, 0.0F, 64, 64);
    }
}
