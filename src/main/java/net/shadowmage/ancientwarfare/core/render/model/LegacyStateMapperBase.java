package net.shadowmage.ancientwarfare.core.render.model;

import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Modern replacement for the removed 1.12 StateMapperBase declaration API.
 */
public abstract class LegacyStateMapperBase {
    protected abstract ModelResourceLocation getModelResourceLocation(BlockState state);

    public final ModelResourceLocation map(BlockState state) {
        return getModelResourceLocation(state);
    }

    protected String getPropertyString(Map<Property<?>, Comparable<?>> values) {
        if (values.isEmpty()) {
            return "normal";
        }
        return values.entrySet().stream()
                .sorted(Comparator.comparing(entry -> entry.getKey().getName()))
                .map(entry -> entry.getKey().getName() + "=" + valueName(entry.getKey(), entry.getValue()))
                .collect(Collectors.joining(","));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static String valueName(Property property, Comparable value) {
        return property.getName(value);
    }
}
