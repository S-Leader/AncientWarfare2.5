package net.shadowmage.ancientwarfare.structure.util;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class RespawnDataCapabilityProvider implements ICapabilitySerializable<CompoundTag> {
    private final IRespawnData respawnData = new RespawnData();
    private final LazyOptional<IRespawnData> optional = LazyOptional.of(() -> respawnData);

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
        return capability == CapabilityRespawnData.RESPAWN_DATA_CAPABILITY ? optional.cast() : LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return CapabilityRespawnData.serialize(respawnData);
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        CapabilityRespawnData.deserialize(respawnData, nbt);
    }

    public void invalidate() {
        optional.invalidate();
    }
}
