package net.shadowmage.ancientwarfare.core.render.model;

import com.google.common.collect.ImmutableMap;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Immutable view combining a vanilla BlockState with Forge 1.20 ModelData.
 */
public final class LegacyModelState {
    private static final ModelProperty<Map<Property<?>, Comparable<?>>> PROPERTY_OVERRIDES = new ModelProperty<>();

    private final BlockState state;
    private final Map<LegacyModelProperty<?>, Object> modelValues;
    private final Map<Property<?>, Comparable<?>> propertyOverrides;

    private LegacyModelState(BlockState state, Map<LegacyModelProperty<?>, Object> modelValues,
                             Map<Property<?>, Comparable<?>> propertyOverrides) {
        this.state = state;
        this.modelValues = modelValues;
        this.propertyOverrides = propertyOverrides;
    }

    public static LegacyModelState of(BlockState state) {
        return new LegacyModelState(state, Map.of(), Map.of());
    }

    public static LegacyModelState of(BlockState state, ModelData data) {
        Map<LegacyModelProperty<?>, Object> values = new HashMap<>();
        for (LegacyModelProperty<?> property : LegacyModelProperty.all()) {
            copyFromModelData(data, property, values);
        }
        Map<Property<?>, Comparable<?>> overrides = data.has(PROPERTY_OVERRIDES)
                ? data.get(PROPERTY_OVERRIDES)
                : Map.of();
        return new LegacyModelState(state, Map.copyOf(values),
                overrides == null ? Map.of() : Map.copyOf(overrides));
    }

    private static <T> void copyFromModelData(ModelData data, LegacyModelProperty<T> property,
                                              Map<LegacyModelProperty<?>, Object> values) {
        if (data.has(property.modelProperty())) {
            T value = data.get(property.modelProperty());
            if (value != null) {
                values.put(property, value);
            }
        }
    }

    public BlockState blockState() {
        return state;
    }

    public Block getBlock() {
        return state.getBlock();
    }

    @SuppressWarnings("unchecked")
    public <T> T getValue(LegacyModelProperty<T> property) {
        Object value = modelValues.get(property);
        return value != null ? (T) value : property.defaultValue();
    }

    @SuppressWarnings("unchecked")
    public <T extends Comparable<T>> T getValue(Property<T> property) {
        Comparable<?> override = propertyOverrides.get(property);
        if (override != null) {
            return (T) override;
        }
        if (state.hasProperty(property)) {
            return state.getValue(property);
        }
        return property.getPossibleValues().iterator().next();
    }

    public <T> LegacyModelState setValue(LegacyModelProperty<T> property, T value) {
        Map<LegacyModelProperty<?>, Object> values = new HashMap<>(modelValues);
        values.put(property, value);
        return new LegacyModelState(state, Map.copyOf(values), propertyOverrides);
    }

    public <T extends Comparable<T>> LegacyModelState setValue(Property<T> property, T value) {
        // ModelData is the only state that survives from BlockEntity#getModelData
        // to BakedModel#getQuads. Keep every legacy property change here, even
        // when the property also exists on the outer vanilla BlockState.
        Map<Property<?>, Comparable<?>> values = new HashMap<>(propertyOverrides);
        values.put(property, value);
        return new LegacyModelState(state, modelValues, Map.copyOf(values));
    }

    public ImmutableMap<LegacyModelProperty<?>, Optional<?>> getUnlistedProperties() {
        ImmutableMap.Builder<LegacyModelProperty<?>, Optional<?>> builder = ImmutableMap.builder();
        modelValues.forEach((key, value) -> builder.put(key, Optional.ofNullable(value)));
        return builder.build();
    }

    public ModelData toModelData() {
        ModelData.Builder builder = ModelData.builder();
        modelValues.forEach((key, value) -> addToModelData(builder, key, value));
        if (!propertyOverrides.isEmpty()) {
            builder.with(PROPERTY_OVERRIDES, propertyOverrides);
        }
        return builder.build();
    }

    @SuppressWarnings("unchecked")
    private static <T> void addToModelData(ModelData.Builder builder, LegacyModelProperty<T> property, Object value) {
        builder.with(property.modelProperty(), (T) value);
    }
}
