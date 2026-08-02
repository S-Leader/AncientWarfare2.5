package net.shadowmage.ancientwarfare.core.render;

import com.google.common.collect.Maps;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.registries.ForgeRegistries;
import net.shadowmage.ancientwarfare.core.render.model.LegacyModelProperty;
import net.shadowmage.ancientwarfare.core.render.model.LegacyModelState;

import java.util.Map;
import java.util.StringJoiner;
import java.util.function.Function;

public class BlockStateKeyGenerator {
    private final Map<Property, Function<Object, String>> propertyFormats;
    private final Map<LegacyModelProperty, Function<Object, String>> unlistedPropertyFormats;

    private BlockStateKeyGenerator(Map<Property, Function<Object, String>> propertyFormats, Map<LegacyModelProperty, Function<Object, String>> unlistedPropertyFormats) {

        this.propertyFormats = propertyFormats;
        this.unlistedPropertyFormats = unlistedPropertyFormats;
    }

    public String generateKey(LegacyModelState state) {
        StringJoiner stringJoiner = new StringJoiner("|");

        stringJoiner.add(String.valueOf(ForgeRegistries.BLOCKS.getKey(state.getBlock())));
        for (Map.Entry<Property, Function<Object, String>> entry : propertyFormats.entrySet()) {
            stringJoiner.add(entry.getValue().apply(state.getValue(entry.getKey())));
        }

        for (Map.Entry<LegacyModelProperty, Function<Object, String>> entry : unlistedPropertyFormats.entrySet()) {
            stringJoiner.add(entry.getValue().apply(state.getValue(entry.getKey())));
        }

        return stringJoiner.toString();
    }

    public static class Builder {
        private Map<Property, Function<Object, String>> propertyFormats = Maps.newHashMap();
        private Map<LegacyModelProperty, Function<Object, String>> unlistedPropertyFormats = Maps.newHashMap();

        public Builder addKeyProperties(Property... properties) {
            addKeyProperties(Object::toString, properties);
            return this;
        }

        public Builder addKeyProperties(Function<Object, String> formatValue, Property... properties) {
            for (Property property : properties) {
                this.propertyFormats.put(property, formatValue);
            }
            return this;
        }

        public Builder addKeyProperties(LegacyModelProperty... properties) {
            addKeyProperties(Object::toString, properties);
            return this;
        }

        public BlockStateKeyGenerator build() {
            return new BlockStateKeyGenerator(this.propertyFormats, this.unlistedPropertyFormats);
        }

        public Builder addKeyProperties(Function<Object, String> getFormat, LegacyModelProperty... properties) {
            for (LegacyModelProperty property : properties) {
                this.unlistedPropertyFormats.put(property, getFormat);
            }
            return this;
        }
    }
}
