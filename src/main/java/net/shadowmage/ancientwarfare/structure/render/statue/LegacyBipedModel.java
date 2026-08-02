package net.shadowmage.ancientwarfare.structure.render.statue;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.core.client.model.LegacyModelBase;
import net.shadowmage.ancientwarfare.core.client.model.LegacyModelRenderer;

/**
 * Replica of the removed 1.12 vanilla ModelBiped geometry built on the legacy model bridge,
 * so the statue system can keep posing individual named parts.
 */
@OnlyIn(Dist.CLIENT)
public class LegacyBipedModel extends LegacyModelBase {
    public LegacyModelRenderer bipedHead;
    public LegacyModelRenderer bipedHeadwear;
    public LegacyModelRenderer bipedBody;
    public LegacyModelRenderer bipedRightArm;
    public LegacyModelRenderer bipedLeftArm;
    public LegacyModelRenderer bipedRightLeg;
    public LegacyModelRenderer bipedLeftLeg;

    public LegacyBipedModel() {
        this(0.0F);
    }

    public LegacyBipedModel(float modelSize) {
        this(modelSize, 0.0F, 64, 32);
    }

    public LegacyBipedModel(float modelSize, float yOffset, int textureWidthIn, int textureHeightIn) {
        textureWidth = textureWidthIn;
        textureHeight = textureHeightIn;
        bipedHead = part(0, 0);
        bipedHead.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, modelSize);
        bipedHead.setRotationPoint(0.0F, 0.0F + yOffset, 0.0F);
        bipedHeadwear = part(32, 0);
        bipedHeadwear.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, modelSize + 0.5F);
        bipedHeadwear.setRotationPoint(0.0F, 0.0F + yOffset, 0.0F);
        bipedBody = part(16, 16);
        bipedBody.addBox(-4.0F, 0.0F, -2.0F, 8, 12, 4, modelSize);
        bipedBody.setRotationPoint(0.0F, 0.0F + yOffset, 0.0F);
        bipedRightArm = part(40, 16);
        bipedRightArm.addBox(-3.0F, -2.0F, -2.0F, 4, 12, 4, modelSize);
        bipedRightArm.setRotationPoint(-5.0F, 2.0F + yOffset, 0.0F);
        bipedLeftArm = part(40, 16);
        bipedLeftArm.mirror = true;
        bipedLeftArm.addBox(-1.0F, -2.0F, -2.0F, 4, 12, 4, modelSize);
        bipedLeftArm.setRotationPoint(5.0F, 2.0F + yOffset, 0.0F);
        bipedRightLeg = part(0, 16);
        bipedRightLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, modelSize);
        bipedRightLeg.setRotationPoint(-1.9F, 12.0F + yOffset, 0.0F);
        bipedLeftLeg = part(0, 16);
        bipedLeftLeg.mirror = true;
        bipedLeftLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, modelSize);
        bipedLeftLeg.setRotationPoint(1.9F, 12.0F + yOffset, 0.0F);
    }

    protected final LegacyModelRenderer part(int textureU, int textureV) {
        return new LegacyModelRenderer(this, textureU, textureV).setTextureSize(textureWidth, textureHeight);
    }
}
