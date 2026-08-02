package net.shadowmage.ancientwarfare.automation.render;

import codechicken.lib.render.CCRenderState;
import net.shadowmage.ancientwarfare.core.render.model.LegacyModelState;

public interface ITESRRenderer {
    void renderTransformedBlockModels(CCRenderState ccrs, LegacyModelState state);
}
