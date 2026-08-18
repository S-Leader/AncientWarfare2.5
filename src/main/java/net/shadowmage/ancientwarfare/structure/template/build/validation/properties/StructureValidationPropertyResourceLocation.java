package net.shadowmage.ancientwarfare.structure.template.build.validation.properties;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public class StructureValidationPropertyResourceLocation implements IStructureValidationProperty<ResourceLocation> {
    private String name;
    private ResourceLocation defaultValue;

    public StructureValidationPropertyResourceLocation(String name, ResourceLocation defaultValue) {
        this.name = name;
        this.defaultValue = defaultValue;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Class<ResourceLocation> getValueClass() {
        return ResourceLocation.class;
    }

    @Override
    public CompoundTag serializeNBT(CompoundTag tag, ResourceLocation value) {
        tag.putString(name, value.toString());
        return tag;
    }

    @Override
    public ResourceLocation deserializeNBT(CompoundTag tag) {
        return parseValue(tag.getString(name));
    }

    @Override
    public ResourceLocation getDefaultValue() {
        return defaultValue;
    }

    @Override
    public String getStringValue(ResourceLocation value) {
        return value.toString();
    }

    @Override
    public ResourceLocation parseValue(String valueString) {
        String value = valueString == null ? "" : valueString.trim();
        // A handful of bundled 1.12 templates accidentally serialized the
        // namespace twice ("minecraft:minecraft:..."). Keep those templates
        // usable and treat an empty legacy biome replacement as the default.
        if (value.startsWith("minecraft:minecraft:")) {
            value = value.substring("minecraft:".length());
        }
        ResourceLocation parsed = ResourceLocation.tryParse(value);
        return parsed == null || parsed.getPath().isEmpty() ? defaultValue : parsed;
    }

}
