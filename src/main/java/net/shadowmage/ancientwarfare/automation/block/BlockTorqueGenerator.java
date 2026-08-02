package net.shadowmage.ancientwarfare.automation.block;

import net.shadowmage.ancientwarfare.core.block.BlockRotationHandler.RotationType;
import net.shadowmage.ancientwarfare.core.compat.LegacyMaterial;

public abstract class BlockTorqueGenerator extends BlockTorqueBase {

    protected BlockTorqueGenerator(String regName) {
        super(LegacyMaterial.ROCK, regName);
    }

    @Override
    public RotationType getRotationType() {
        return RotationType.FOUR_WAY;
    }

    @Override
    public boolean invertFacing() {
        return false;
    }

}
