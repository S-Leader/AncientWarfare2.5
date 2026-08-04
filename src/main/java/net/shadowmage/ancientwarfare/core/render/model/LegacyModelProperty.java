package net.shadowmage.ancientwarfare.core.render.model;

import net.minecraftforge.client.model.data.ModelProperty;

import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;

/**
 * Named adapter from Forge 1.12 unlisted properties to Forge 1.20 ModelData.
 *
 * <p>Third-party model consumers such as map renderers are allowed to ask a
 * baked model for quads with {@link net.minecraftforge.client.model.data.ModelData#EMPTY}.
 * Legacy AW renderers used to assume that every property was present and would
 * auto-unbox a null Boolean/Integer/Float.  A property can now carry an explicit
 * default so an empty model state remains safe.</p>
 */
public final class LegacyModelProperty<T> {
    private static final List<LegacyModelProperty<?>> ALL = new CopyOnWriteArrayList<>();

    private final String name;
    private final ModelProperty<T> modelProperty;
    @Nullable
    private final T defaultValue;

    private LegacyModelProperty(String name, @Nullable T defaultValue, Predicate<T> validator) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.modelProperty = new ModelProperty<>(validator);
        ALL.add(this);
    }

    public static <T> LegacyModelProperty<T> create(String name) {
        return new LegacyModelProperty<>(name, null, value -> true);
    }

    public static <T> LegacyModelProperty<T> create(String name, T defaultValue) {
        return new LegacyModelProperty<>(name, defaultValue, value -> true);
    }

    public static <T> LegacyModelProperty<T> create(String name, Predicate<T> validator) {
        return new LegacyModelProperty<>(name, null, validator);
    }

    public static <T> LegacyModelProperty<T> create(String name, T defaultValue, Predicate<T> validator) {
        return new LegacyModelProperty<>(name, defaultValue, validator);
    }

    public String getName() {
        return name;
    }

    public ModelProperty<T> modelProperty() {
        return modelProperty;
    }

    @Nullable
    public T defaultValue() {
        return defaultValue;
    }

    static List<LegacyModelProperty<?>> all() {
        return ALL;
    }

    @Override
    public String toString() {
        return name;
    }
}
