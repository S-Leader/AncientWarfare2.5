package net.shadowmage.ancientwarfare.automation.tile.torque;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.BlockPos;
import net.shadowmage.ancientwarfare.automation.config.AWAutomationStatics;

public final class TileFlywheelControllerMedium extends TileFlywheelController {
    public TileFlywheelControllerMedium(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }


    @Override
    protected double getEfficiency() {
        return AWAutomationStatics.med_efficiency_factor;
    }

    @Override
    protected double getMaxTransfer() {
        return AWAutomationStatics.med_transfer_max;
    }
}
