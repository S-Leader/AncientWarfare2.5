package net.shadowmage.ancientwarfare.structure.render.statue;

import net.shadowmage.ancientwarfare.core.client.model.LegacyModelBase;
import net.shadowmage.ancientwarfare.core.client.model.LegacyModelRenderer;
import net.shadowmage.ancientwarfare.structure.AncientWarfareStructure;
import net.shadowmage.ancientwarfare.structure.tile.EntityStatueInfo;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

public abstract class StatueModelBase<T extends LegacyModelBase> implements IStatueModel {
    protected final T model;
    private Map<String, LegacyModelRenderer> nameRenderer;

    public StatueModelBase(T model) {
        this.model = model;
        initiateNameRendererMap();
    }

    private void initiateNameRendererMap() {
        nameRenderer = getNameRendererMap();
    }

    protected static LegacyModelRenderer getObfuscatedRenderer(LegacyModelBase model, Field rendererField) {
        try {
            return (LegacyModelRenderer) rendererField.get(model);
        } catch (IllegalAccessException e) {
            AncientWarfareStructure.LOG.error("Unable to get {} model renderer of {}: {}", rendererField.getName(), model.getClass().getSimpleName(), e);
            return new LegacyModelRenderer(model);
        }
    }

    protected static LegacyModelRenderer[] getObfuscatedRendererArray(LegacyModelBase model, Field rendererField) {
        try {
            return (LegacyModelRenderer[]) rendererField.get(model);
        } catch (IllegalAccessException e) {
            AncientWarfareStructure.LOG.error("Unable to get {} model renderer array of {}: {}", rendererField.getName(), model.getClass().getSimpleName(), e);
            return new LegacyModelRenderer[]{new LegacyModelRenderer(model)};
        }
    }

    @Override
    public LegacyModelBase getModel() {
        return model;
    }

    protected abstract Map<String, LegacyModelRenderer> getNameRendererMap();

    @Override
    public Set<String> getModelPartNames() {
        return nameRenderer.keySet();
    }

    @Override
    public LegacyModelRenderer getModelPart(String name) {
        return nameRenderer.getOrDefault(name, new LegacyModelRenderer(model));
    }

    @Override
    public void render(float scale) {
        nameRenderer.forEach((name, renderer) -> renderer.render(scale));
    }

    @Override
    public Map<String, EntityStatueInfo.Transform> getBaseTransforms() {
        return Collections.emptyMap();
    }
}
