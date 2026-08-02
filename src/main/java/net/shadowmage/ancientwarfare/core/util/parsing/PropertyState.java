package net.shadowmage.ancientwarfare.core.util.parsing;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

public class PropertyState<T extends Comparable<T>, V extends T> {
    private Property<T> property;
    private V value;

    public PropertyState(Property<T> property, V value) {
        this.property = property;
        this.value = value;
    }

    public Property<T> getProperty() {
        return property;
    }

    public V getValue() {
        return value;
    }

    public BlockState update(BlockState state) {
        return state.setValue(property, value);
    }
}
