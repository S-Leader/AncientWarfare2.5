package net.shadowmage.ancientwarfare.core.util.parsing;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class BlockStateMatcher implements Predicate<BlockState> {
    private final Block block;
    private final PropertyMapMatcher propertyMatcher = new PropertyMapMatcher();

    public BlockStateMatcher(BlockState fullState) {
        this(fullState.getBlock());
        fullState.getValues().forEach(this::addProperty);
    }

    public BlockStateMatcher(Block block) {
        this.block = block;
    }

    BlockStateMatcher addProperty(Property<?> property, Comparable<?> value) {
        propertyMatcher.addProperty(property, value);
        return this;
    }

    @Override
    public boolean test(BlockState state) {
        return block == state.getBlock() && propertyMatcher.test(state.getValues());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        BlockStateMatcher that = (BlockStateMatcher) o;

        return block.equals(that.block) && propertyMatcher.equals(that.propertyMatcher);
    }

    @Override
    public int hashCode() {
        int result = block.hashCode();
        result = 31 * result + propertyMatcher.hashCode();
        return result;
    }

    public static class PropertyMapMatcher implements Predicate<Map<Property<?>, Comparable<?>>> {
        private final Map<Property<?>, Comparable<?>> propertyValues = new HashMap<>();

        void addProperty(Property<?> property, Comparable<?> value) {
            //noinspection unchecked
            propertyValues.put(property, value);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            PropertyMapMatcher that = (PropertyMapMatcher) o;

            return propertyValues.equals(that.propertyValues);
        }

        @Override
        public int hashCode() {
            return propertyValues.hashCode();
        }

        @Override
        public boolean test(Map<Property<?>, Comparable<?>> properties) {
            for (Map.Entry<Property<?>, Comparable<?>> property : propertyValues.entrySet()) {
                if (!properties.containsKey(property.getKey()) || !property.getValue().equals(properties.get(property.getKey()))) {
                    return false;
                }
            }
            return true;
        }
    }
}
