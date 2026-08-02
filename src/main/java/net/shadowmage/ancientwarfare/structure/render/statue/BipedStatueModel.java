package net.shadowmage.ancientwarfare.structure.render.statue;

import com.google.common.collect.ImmutableMap;
import net.shadowmage.ancientwarfare.core.client.model.LegacyModelRenderer;

import java.util.Map;

public class BipedStatueModel<T extends LegacyBipedModel> extends StatueModelBase<T> {
    BipedStatueModel(T model) {
        super(model);
    }

    @Override
    protected Map<String, LegacyModelRenderer> getNameRendererMap() {
        return new ImmutableMap.Builder<String, LegacyModelRenderer>()
                .put("Head", model.bipedHead)
                .put("Headwear", model.bipedHeadwear)
                .put("Body", model.bipedBody)
                .put("Right Arm", model.bipedRightArm)
                .put("Left Arm", model.bipedLeftArm)
                .put("Right Leg", model.bipedRightLeg)
                .put("Left Leg", model.bipedLeftLeg)
                .build();
    }
}
