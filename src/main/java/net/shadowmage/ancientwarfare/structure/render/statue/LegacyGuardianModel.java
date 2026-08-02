package net.shadowmage.ancientwarfare.structure.render.statue;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.core.client.model.LegacyModelBase;
import net.shadowmage.ancientwarfare.core.client.model.LegacyModelRenderer;

/**
 * Replica of the removed 1.12 vanilla ModelGuardian geometry. Boxes that used a second
 * texture offset (or a mirrored flag) on the same 1.12 renderer are child renderers instead.
 */
@OnlyIn(Dist.CLIENT)
public class LegacyGuardianModel extends LegacyModelBase {
    public final LegacyModelRenderer guardianBody;
    public final LegacyModelRenderer guardianEye;
    public final LegacyModelRenderer[] guardianSpines;
    public final LegacyModelRenderer[] guardianTail;

    public LegacyGuardianModel() {
        textureWidth = 64;
        textureHeight = 64;
        guardianSpines = new LegacyModelRenderer[12];
        guardianBody = part(0, 0);
        guardianBody.addBox(-6.0F, 10.0F, -8.0F, 12, 12, 16);
        LegacyModelRenderer leftFin = part(0, 28);
        leftFin.addBox(-8.0F, 10.0F, -6.0F, 2, 12, 12);
        guardianBody.addChild(leftFin);
        LegacyModelRenderer rightFin = part(0, 28);
        rightFin.mirror = true;
        rightFin.addBox(6.0F, 10.0F, -6.0F, 2, 12, 12);
        guardianBody.addChild(rightFin);
        LegacyModelRenderer topPlate = part(16, 40);
        topPlate.addBox(-6.0F, 8.0F, -6.0F, 12, 2, 12);
        guardianBody.addChild(topPlate);
        LegacyModelRenderer bottomPlate = part(16, 40);
        bottomPlate.addBox(-6.0F, 22.0F, -6.0F, 12, 2, 12);
        guardianBody.addChild(bottomPlate);

        for (int i = 0; i < guardianSpines.length; ++i) {
            guardianSpines[i] = part(0, 0);
            guardianSpines[i].addBox(-1.0F, -4.5F, -1.0F, 2, 9, 2);
            guardianBody.addChild(guardianSpines[i]);
        }

        guardianEye = part(8, 0);
        guardianEye.addBox(-1.0F, 15.0F, 0.0F, 2, 2, 1);
        guardianBody.addChild(guardianEye);
        guardianTail = new LegacyModelRenderer[3];
        guardianTail[0] = part(40, 0);
        guardianTail[0].addBox(-2.0F, 14.0F, 7.0F, 4, 4, 8);
        guardianTail[1] = part(0, 54);
        guardianTail[1].addBox(0.0F, 14.0F, 0.0F, 3, 3, 7);
        guardianTail[2] = part(41, 32);
        guardianTail[2].addBox(0.0F, 14.0F, 0.0F, 2, 2, 6);
        LegacyModelRenderer tailFin = part(25, 19);
        tailFin.addBox(1.0F, 10.5F, 3.0F, 1, 9, 9);
        guardianTail[2].addChild(tailFin);
        guardianBody.addChild(guardianTail[0]);
        guardianTail[0].addChild(guardianTail[1]);
        guardianTail[1].addChild(guardianTail[2]);
    }

    private LegacyModelRenderer part(int textureU, int textureV) {
        return new LegacyModelRenderer(this, textureU, textureV).setTextureSize(textureWidth, textureHeight);
    }
}
