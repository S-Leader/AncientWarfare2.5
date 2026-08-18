package net.shadowmage.ancientwarfare.structure.template.build.validation.properties;

import net.minecraft.nbt.CompoundTag;

public interface IStructureValidationProperty<T> {
    String getName();

    Class<T> getValueClass();

    CompoundTag serializeNBT(CompoundTag tag, T value);

    T deserializeNBT(CompoundTag tag);

    T getDefaultValue();

    String getStringValue(T value);

    T parseValue(String valueString);
}
