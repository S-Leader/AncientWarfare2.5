package net.shadowmage.ancientwarfare.structure.worldgen;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.shadowmage.ancientwarfare.structure.AncientWarfareStructure;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Forge 1.20.1 level capability used by the AW territory allocator.
 */
public final class CapabilityTerritoryData {
    private CapabilityTerritoryData() {
    }

    public static final Capability<ITerritoryData> TERRITORY_DATA =
            CapabilityManager.get(new CapabilityToken<>() {
            });

    public static void register(RegisterCapabilitiesEvent event) {
        event.register(ITerritoryData.class);
    }

    public static void onAttach(AttachCapabilitiesEvent<Level> event) {
        TerritoryDataCapabilityProvider provider = new TerritoryDataCapabilityProvider();
        event.addCapability(new ResourceLocation(AncientWarfareStructure.MOD_ID, "territory_data"), provider);
        event.addListener(provider::invalidate);
    }

    public static Optional<ITerritoryData> get(Level level) {
        return level.getCapability(TERRITORY_DATA).resolve();
    }

    public static final class TerritoryDataCapabilityProvider implements ICapabilitySerializable<CompoundTag> {
        private final ITerritoryData territoryData = new TerritoryData();
        private final LazyOptional<ITerritoryData> optional = LazyOptional.of(() -> territoryData);

        @Nonnull
        @Override
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
            return capability == TERRITORY_DATA ? optional.cast() : LazyOptional.empty();
        }

        @Override
        public CompoundTag serializeNBT() {
            return territoryData.serializeNBT();
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            territoryData.deserializeNBT(nbt);
        }

        public void invalidate() {
            optional.invalidate();
        }
    }
}
