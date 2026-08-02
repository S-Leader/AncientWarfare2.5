package net.shadowmage.ancientwarfare.structure.render.statue;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.core.client.model.LegacyModelBase;
import net.shadowmage.ancientwarfare.core.client.model.LegacyModelRenderer;
import net.shadowmage.ancientwarfare.structure.tile.EntityStatueInfo;

import java.util.Map;
import java.util.Set;

@OnlyIn(Dist.CLIENT)
public interface IStatueModel {
    LegacyModelBase getModel();

    Set<String> getModelPartNames();

    LegacyModelRenderer getModelPart(String name);

    void render(float scale);

    Map<String, EntityStatueInfo.Transform> getBaseTransforms();
}
