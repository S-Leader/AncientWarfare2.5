package net.shadowmage.ancientwarfare.automation.tile.torque;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.shadowmage.ancientwarfare.automation.tile.torque.multiblock.TileFlywheelStorage;
import net.shadowmage.ancientwarfare.core.interfaces.ITorque.TorqueCell;
import net.shadowmage.ancientwarfare.core.util.WorldTools;

import javax.annotation.Nullable;

public abstract class TileFlywheelController extends TileTorqueSingleCell {
    private static final String POWERED_TAG = "powered";
    private boolean powered;

    private final TorqueCell inputCell;

    public TileFlywheelController(BlockEntityType<?> type, BlockPos pos, BlockState state) {

        super(type, pos, state);
        double max = getMaxTransfer();
        double eff = getEfficiency();
        inputCell = new TorqueCell(max, max, max, eff);
        torqueCell = new TorqueCell(max, max, max, eff);
    }

    protected abstract double getEfficiency();

    protected abstract double getMaxTransfer();

    @Override
    public void update() {
        if (!world.isClientSide) {
            serverNetworkUpdate();
            torqueIn = torqueCell.getEnergy() - prevEnergy;
            torqueLoss = applyPowerDrain(torqueCell);
            torqueLoss += applyPowerDrain(inputCell);
            torqueLoss += applyDrainToStorage();
            torqueOut = transferPowerTo(getPrimaryFacing());
            balancePower();
            prevEnergy = torqueCell.getEnergy();
        } else {
            clientNetworkUpdate();
            updateRotation();
        }
    }

    private double applyDrainToStorage() {
        TileFlywheelStorage storage = getControlledFlywheel();
        if (storage == null) {
            return 0;
        }
        return storage.torqueLoss;
    }

    /*
     * fill output from input
     * fill output from storage
     * fill storage from input
     */
    private void balancePower() {
        TileFlywheelStorage storage = getControlledFlywheel();
        double in = inputCell.getEnergy();
        double out = torqueCell.getEnergy();
        double outputGap = torqueCell.getMaxEnergy() - out;
        double addOutput = Math.min(in, outputGap);
        in -= addOutput;
        out += addOutput;
        if (storage != null) {
            double store = storage.storedEnergy;
            double storeToTransfer = Math.min(store, torqueCell.getMaxEnergy() - out);
            store -= storeToTransfer;
            out += storeToTransfer;

            double addToStore = Math.min(in, storage.maxEnergyStored - store);
            in -= addToStore;
            store += addToStore;
            storage.storedEnergy = store;
            torqueLoss += storage.torqueLoss;
        }
        torqueCell.setEnergy(out);
        inputCell.setEnergy(in);
    }

    @Override
    protected void updateRotation() {
        if (!powered) {
            super.updateRotation();
        }
    }

    @Nullable
    private TileFlywheelStorage getControlledFlywheel() {
        BlockPos controllerPos = pos.relative(Direction.DOWN);
        return WorldTools.getTile(world, controllerPos, TileFlywheelStorage.class).map(t -> {
                    if (t.controllerPos != null) {
                        BlockPos nextControllerPos = t.controllerPos;
                        return WorldTools.getTile(world, nextControllerPos, TileFlywheelStorage.class).orElse(null);
                    }
                    return null;
                }
        ).orElse(null);
    }

    public float getFlywheelRotation(float delta) {
        TileFlywheelStorage storage = getControlledFlywheel();
        return storage == null ? 0 : getRenderRotation(storage.rotation, storage.lastRotationDiff, delta);
    }

    private double getFlywheelEnergy() {
        TileFlywheelStorage storage = getControlledFlywheel();
        return storage == null ? 0 : storage.storedEnergy;
    }

    @Override
    protected double getTotalTorque() {
        return inputCell.getEnergy() + torqueCell.getEnergy() + getFlywheelEnergy();
    }

    @Override
    public void onNeighborTileChanged() {
        super.onNeighborTileChanged();
        if (!world.isClientSide) {
            boolean p = world.getDirectSignalTo(pos) > 0;
            if (p != powered) {
                powered = p;
                sendDataToClient(7, powered ? 1 : 0);
            }
        }
    }

    @Override
    public boolean triggerEvent(int a, int b) {
        if (world.isClientSide && a == 7) {
            powered = b == 1;
        }
        return super.triggerEvent(a, b);
    }

    @Override
    public double getMaxTorqueOutput(Direction from) {
        if (powered) {
            return 0;
        }
        return torqueCell.getMaxTickOutput();
    }

    @Override
    public double getMaxTorque(@Nullable Direction from) {
        TorqueCell cell = getCell(from);
        return cell == null ? 0 : cell.getMaxEnergy();
    }

    @Override
    public double getTorqueStored(@Nullable Direction from) {
        TorqueCell cell = getCell(from);
        return cell == null ? 0 : cell.getEnergy();
    }

    @Override
    public double addTorque(@Nullable Direction from, double energy) {
        TorqueCell cell = getCell(from);
        return cell == null ? 0 : cell.addEnergy(energy);
    }

    @Override
    public double drainTorque(@Nullable Direction from, double energy) {
        TorqueCell cell = getCell(from);
        return cell == null ? 0 : cell.drainEnergy(energy);
    }

    @Override
    public double getMaxTorqueInput(@Nullable Direction from) {
        TorqueCell cell = getCell(from);
        return cell == null ? 0 : cell.getMaxTickInput();
    }

    @Nullable
    private TorqueCell getCell(@Nullable Direction from) {
        if (from == orientation) {
            return torqueCell;
        } else if (from == orientation.getOpposite()) {
            return inputCell;
        }
        return null;
    }

    //************************************** NBT / DATA PACKET ***************************************//

    @Override
    protected void handleUpdateNBT(CompoundTag tag) {
        super.handleUpdateNBT(tag);
        powered = tag.getBoolean(POWERED_TAG);
    }

    @Override
    protected void writeUpdateNBT(CompoundTag tag) {
        super.writeUpdateNBT(tag);
        tag.putBoolean(POWERED_TAG, powered);
    }

    @Override
    public CompoundTag writeToNBT(CompoundTag tag) {
        super.writeToNBT(tag);
        tag.putBoolean(POWERED_TAG, powered);
        tag.putDouble("torqueEnergyIn", inputCell.getEnergy());
        return tag;
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
        super.readFromNBT(tag);
        powered = tag.getBoolean(POWERED_TAG);
        inputCell.setEnergy(tag.getDouble("torqueEnergyIn"));
    }

}
