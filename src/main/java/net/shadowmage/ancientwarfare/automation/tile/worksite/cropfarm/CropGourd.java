package net.shadowmage.ancientwarfare.automation.tile.worksite.cropfarm;

import net.minecraft.world.level.block.state.BlockState;
import net.shadowmage.ancientwarfare.core.compat.LegacyMaterial;

public class CropGourd extends CropBreakOnly {
    @Override
    public boolean matches(BlockState state) {
        return LegacyMaterial.of(state) == LegacyMaterial.GOURD;
    }
}
