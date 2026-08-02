package net.shadowmage.ancientwarfare.core.render.model;

import net.minecraftforge.client.model.data.ModelProperty;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;

/**
 * Named adapter from Forge 1.12 unlisted properties to Forge 1.20 ModelData.
 */
public final class LegacyModelProperty<T> {
    private static final List<LegacyModelProperty<?>> ALL = new CopyOnWriteArrayList<>();

    private final String name;
    private final ModelProperty<T> modelProperty;

    private LegacyModelProperty(String name, Predicate<T> validator) {
        this.name = name;
        this.modelProperty = new ModelProperty<>(validator);
        ALL.add(this);
    }

    public static <T> LegacyModelProperty<T> create(String name) {
        return new LegacyModelProperty<>(name, value -> true);
    }

    public static <T> LegacyModelProperty<T> create(String name, Predicate<T> validator) {
        return new LegacyModelProperty<>(name, validator);
    }

    public String getName() {
        return name;
    }

    public ModelProperty<T> modelProperty() {
        return modelProperty;
    }

    static List<LegacyModelProperty<?>> all() {
        return ALL;
    }

    @Override
    public String toString() {
        return name;
    }
}
