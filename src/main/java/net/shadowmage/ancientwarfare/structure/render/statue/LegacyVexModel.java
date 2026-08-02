package net.shadowmage.ancientwarfare.structure.render.statue;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.core.client.model.LegacyModelRenderer;

/**
 * Replica of the removed 1.12 vanilla ModelVex geometry.
 */
@OnlyIn(Dist.CLIENT)
public class LegacyVexModel extends LegacyBipedModel {
    public LegacyModelRenderer leftWing;
    public LegacyModelRenderer rightWing;

    public LegacyVexModel() {
        this(0.0F);
    }

    public LegacyVexModel(float modelSize) {
        super(modelSize, 0.0F, 64, 64);
        bipedLeftLeg.showModel = false;
        bipedHeadwear.showModel = false;
        bipedRightLeg = part(32, 0);
        bipedRightLeg.addBox(-1.0F, -1.0F, -2.0F, 6, 10, 4, 0.0F);
        bipedRightLeg.setRotationPoint(-1.9F, 12.0F, 0.0F);
        rightWing = part(0, 32);
        rightWing.addBox(-20.0F, 0.0F, 0.0F, 20, 12, 1);
        leftWing = part(0, 32);
        leftWing.mirror = true;
        leftWing.addBox(0.0F, 0.0F, 0.0F, 20, 12, 1);
    }
}
