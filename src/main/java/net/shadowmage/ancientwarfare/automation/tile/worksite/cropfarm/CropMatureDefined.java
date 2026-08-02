package net.shadowmage.ancientwarfare.automation.tile.worksite.cropfarm;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.shadowmage.ancientwarfare.core.util.parsing.BlockStateMatcher;
import net.shadowmage.ancientwarfare.core.util.parsing.PropertyStateMatcher;

import java.util.Collections;
import java.util.List;

public class CropMatureDefined extends CropDefault {
    private BlockStateMatcher stateMatcher;
    private PropertyStateMatcher matureStateMatcher;

    public CropMatureDefined(BlockStateMatcher stateMatcher, PropertyStateMatcher matureStateMatcher) {
        this.stateMatcher = stateMatcher;
        this.matureStateMatcher = matureStateMatcher;
    }

    @Override
    public boolean matches(BlockState state) {
        return stateMatcher.test(state);
    }

    @Override
    public List<BlockPos> getPositionsToHarvest(Level world, BlockPos pos, BlockState state) {
        return matureStateMatcher.test(state) ? Collections.singletonList(pos) : Collections.emptyList();
    }
}
