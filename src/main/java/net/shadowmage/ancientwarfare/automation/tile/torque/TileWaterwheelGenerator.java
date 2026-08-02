package net.shadowmage.ancientwarfare.automation.tile.torque;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.shadowmage.ancientwarfare.automation.config.AWAutomationStatics;
import net.shadowmage.ancientwarfare.core.compat.LegacyMaterial;
import net.shadowmage.ancientwarfare.core.interfaces.ITorque.TorqueCell;
import net.shadowmage.ancientwarfare.core.util.BlockTools;
import net.shadowmage.ancientwarfare.core.util.Trig;

public class TileWaterwheelGenerator extends TileTorqueSingleCell {

    public float wheelRotation;
    public float lastWheelRotationDiff;

    private float rotTick;
    private RotationDirection rotationDirection = RotationDirection.CLOCKWISE;

    private int updateTick;

    public boolean validSetup = false;

    public TileWaterwheelGenerator() {
        torqueCell = new TorqueCell(0, 4, AWAutomationStatics.low_transfer_max, AWAutomationStatics.low_efficiency_factor);
        float maxWheelRpm = 20;
        rotTick = maxWheelRpm * AWAutomationStatics.rpmToRpt;
    }

    @Override
    public void update() {
        super.update();
        if (!world.isClientSide) {
            updateTick--;
            if (updateTick <= 0) {
                updateTick = 20;
                boolean valid = validateBlocks();
                if (valid != validSetup) {
                    validSetup = valid;
                    BlockTools.notifyBlockUpdate(this);
                }
            }
            if (validSetup)//server, update power gen
            {
                torqueCell.setEnergy(torqueCell.getEnergy() + AWAutomationStatics.waterwheel_generator_output);
            }
        }
    }

    @Override
    protected void updateRotation() {
        super.updateRotation();
        if (validSetup) {
            lastWheelRotationDiff = (rotTick * rotationDirection.multiplier) * Trig.TORADIANS;
            wheelRotation += lastWheelRotationDiff;
            wheelRotation %= Trig.PI * 2;
        }
    }

    @Override
    protected void handleUpdateNBT(CompoundTag tag) {
        super.handleUpdateNBT(tag);
        validSetup = tag.getBoolean("validSetup");
        rotationDirection = RotationDirection.fromMultiplier(tag.getByte("rotationDirection"));
    }

    @Override
    protected void writeUpdateNBT(CompoundTag tag) {
        super.writeUpdateNBT(tag);
        tag.putBoolean("validSetup", validSetup);
        tag.putByte("rotationDirection", rotationDirection.multiplier);
    }

    private boolean validateBlocks() {
        Direction d = orientation.getOpposite();
        BlockPos innerPos = pos.relative(d);
        //quick check for invalid setup
        //must have air inside the inner two blocks
        if (getValidationType(innerPos.above()) != ValidationType.AIR || getValidationType(innerPos) != ValidationType.AIR)
            return false;
        BlockPos right = innerPos.relative(d.getClockWise());
        BlockPos left = innerPos.relative(d.getCounterClockWise());

        ValidationType[] validationGrid = new ValidationType[6];
        validationGrid[0] = getValidationType(left.above());
        validationGrid[1] = getValidationType(right.above());
        validationGrid[2] = getValidationType(left);
        validationGrid[3] = getValidationType(right);
        validationGrid[4] = getValidationType(left.below());
        validationGrid[5] = getValidationType(right.below());
        for (ValidationType value : validationGrid) {
            if (value == ValidationType.BLOCKED) {
                return false;
            }
        }
        if (validationGrid[2] == ValidationType.WATER && validationGrid[4] == ValidationType.WATER)//left side water flowing down
        {
            //check opposite side for air (underneath has already been checked by quick block check above)
            if (validationGrid[1] == ValidationType.AIR && validationGrid[3] == ValidationType.AIR) {
                rotationDirection = RotationDirection.COUNTER_CLOCKWISE;
                return true;
            }
            return false;
        } else if (validationGrid[3] == ValidationType.WATER && validationGrid[5] == ValidationType.WATER)//right side water flowing down
        {
            //check opposite side for air (underneath has already been checked by quick block check above)
            if (validationGrid[0] == ValidationType.AIR && validationGrid[2] == ValidationType.AIR) {
                rotationDirection = RotationDirection.CLOCKWISE;
                return true;
            }
            return false;
        } else//not a direct flow downwards, check underneath for flow
        {
            if (validationGrid[4] != ValidationType.WATER || validationGrid[5] != ValidationType.WATER || getValidationType(innerPos.below()) != ValidationType.WATER) {
                return false;
            }
            BlockState stateLeft = world.getBlockState(left.below());
            BlockState stateRight = world.getBlockState(right.below());
            int levelLeft = LegacyMaterial.of(stateLeft) != LegacyMaterial.WATER ? 0 : stateLeft.getValue(LiquidBlock.LEVEL);
            int levelRight = LegacyMaterial.of(stateRight) != LegacyMaterial.WATER ? 0 : stateRight.getValue(LiquidBlock.LEVEL);
            rotationDirection = levelLeft < levelRight ? RotationDirection.COUNTER_CLOCKWISE
                    : levelRight < levelLeft ? RotationDirection.CLOCKWISE : RotationDirection.STOPPED;
            return rotationDirection != RotationDirection.STOPPED;
        }
    }

    private ValidationType getValidationType(BlockPos pos) {
        if (world.isEmptyBlock(pos)) {
            return ValidationType.AIR;
        }
        BlockState state = world.getBlockState(pos);
        if (LegacyMaterial.of(state) == LegacyMaterial.WATER) {
            return ValidationType.WATER;
        }
        return ValidationType.BLOCKED;
    }

    private enum ValidationType {
        BLOCKED,
        AIR,
        WATER
    }

    private enum RotationDirection {
        COUNTER_CLOCKWISE((byte) -1),
        STOPPED((byte) 0),
        CLOCKWISE((byte) 1);

        private final byte multiplier;

        RotationDirection(byte multiplier) {
            this.multiplier = multiplier;
        }

        private static RotationDirection fromMultiplier(byte multiplier) {
            return multiplier < 0 ? COUNTER_CLOCKWISE : multiplier > 0 ? CLOCKWISE : STOPPED;
        }
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(pos.getX() - 3, pos.getY() - 3, pos.getZ() - 3, pos.getX() + 4, pos.getY() + 4, pos.getZ() + 4);
    }

    @Override
    public float getClientOutputRotation(Direction from, float delta) {
        if (from == orientation.getOpposite()) {
            return getRenderRotation(wheelRotation, lastWheelRotationDiff, delta);
        }
        return super.getClientOutputRotation(from, delta);
    }
}
