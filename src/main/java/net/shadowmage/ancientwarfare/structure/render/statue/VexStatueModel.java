package net.shadowmage.ancientwarfare.structure.render.statue;

import com.google.common.collect.ImmutableMap;
import net.shadowmage.ancientwarfare.core.client.model.LegacyModelRenderer;

import java.util.Map;

public class VexStatueModel extends BipedStatueModel<LegacyVexModel> {
    public VexStatueModel() {
        super(new LegacyVexModel());
    }

    @Override
    protected Map<String, LegacyModelRenderer> getNameRendererMap() {
        return new ImmutableMap.Builder<String, LegacyModelRenderer>()
                .putAll(super.getNameRendererMap())
                .put("Left Wing", model.leftWing)
                .put("Right Wing", model.rightWing)
                .build();
    }
}
