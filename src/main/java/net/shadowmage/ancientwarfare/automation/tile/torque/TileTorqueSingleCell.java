package net.shadowmage.ancientwarfare.automation.tile.torque;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.automation.config.AWAutomationStatics;
import net.shadowmage.ancientwarfare.core.interfaces.ITorque.TorqueCell;
import net.shadowmage.ancientwarfare.core.util.Trig;

import javax.annotation.Nullable;

/*
 * base template class that includes a single torque cell and rotation synching
 *
 * @author Shadowmage
 */
public abstract class TileTorqueSingleCell extends TileTorqueBase {

    private static final String CLIENT_ENERGY_TAG = "clientEnergy";
    private static final String TORQUE_ENERGY_TAG = "torqueEnergy";
    TorqueCell torqueCell;

    /*
     * client side this == 0.0 -> 100, as a whole number percent of max rotation value
     */ double clientEnergyState;

    /*
     * server side this == 0 -> 100 (integer percent)
     * client side this == 0 -> 100 (integer percent)
     */ int clientDestEnergyState;

    /*
     * used client side for rendering
     */ double rotation;
    double lastRotationDiff;

    @Override
    public void update() {
        if (!world.isClientSide) {
            serverNetworkUpdate();
            torqueIn = torqueCell.getEnergy() - prevEnergy;
            torqueLoss = applyPowerDrain(torqueCell);
            torqueOut = transferPowerTo(getPrimaryFacing());
            prevEnergy = torqueCell.getEnergy();
        } else {
            clientNetworkUpdate();
            updateRotation();
        }
    }

    protected double applyPowerLoss() {
        return applyPowerDrain(torqueCell);
    }

    @Override
    protected void serverNetworkSynch() {
        int percent = (int) (torqueCell.getPercentFull() * 100.d);
        int percent2 = (int) ((torqueOut / torqueCell.getMaxOutput()) * 100.d);
        percent = Math.max(percent, percent2);
        if (percent != clientDestEnergyState) {
            clientDestEnergyState = percent;
            sendSideRotation(getPrimaryFacing(), percent);
        }
    }

    @Override
    protected void updateRotation() {
        if (clientEnergyState > 0) {
            lastRotationDiff = -(AWAutomationStatics.low_rpt * clientEnergyState * 0.01d) * Trig.TORADIANS;
            rotation += lastRotationDiff;
            rotation %= Trig.PI * 2;
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    protected void clientNetworkUpdate() {
        if (!Mth.equal((float) clientEnergyState, clientDestEnergyState)) {
            if (networkUpdateTicks > 0) {
                clientEnergyState += (clientDestEnergyState - clientEnergyState) / ((double) networkUpdateTicks);
                networkUpdateTicks--;
            } else {
                clientEnergyState = clientDestEnergyState;
            }
        }
    }

    @Override
    protected void handleClientRotationData(Direction side, int value) {
        if (side == orientation) {
            clientDestEnergyState = value;
            networkUpdateTicks = AWAutomationStatics.energyMinNetworkUpdateFrequency;
        }
    }

    @Override
    public double getMaxTorque(@Nullable Direction from) {
        return torqueCell.getMaxEnergy();
    }

    @Override
    public double getTorqueStored(@Nullable Direction from) {
        return torqueCell.getEnergy();
    }

    @Override
    public double addTorque(@Nullable Direction from, double energy) {
        return torqueCell.addEnergy(energy);
    }

    @Override
    public double drainTorque(Direction from, double energy) {
        return torqueCell.drainEnergy(energy);
    }

    @Override
    public double getMaxTorqueOutput(Direction from) {
        return canOutputTorque(from) ? torqueCell.getMaxTickOutput() : 0;
    }

    @Override
    public double getMaxTorqueInput(@Nullable Direction from) {
        return canInputTorque(from) ? torqueCell.getMaxTickInput() : 0;
    }

    @Override
    public boolean useOutputRotation(@Nullable Direction from) {
        return true;
    }

    @Override
    protected double getTotalTorque() {
        return torqueCell.getEnergy();
    }

    @Override
    public boolean canOutputTorque(Direction towards) {
        return towards == orientation;
    }

    @Override
    public boolean canInputTorque(@Nullable Direction from) {
        return from == orientation.getOpposite();
    }

    @Override
    public float getClientOutputRotation(Direction from, float delta) {
        return from == orientation ? getRenderRotation(rotation, lastRotationDiff, delta) : 0;
    }

    @Override
    protected void handleUpdateNBT(CompoundTag tag) {
        super.handleUpdateNBT(tag);
        clientDestEnergyState = tag.getInt(CLIENT_ENERGY_TAG);
    }

    @Override
    protected void writeUpdateNBT(CompoundTag tag) {
        super.writeUpdateNBT(tag);
        tag.putInt(CLIENT_ENERGY_TAG, clientDestEnergyState);
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
        super.readFromNBT(tag);
        torqueCell.setEnergy(tag.getDouble(TORQUE_ENERGY_TAG));
        clientDestEnergyState = tag.getInt(CLIENT_ENERGY_TAG);
    }

    @Override
    public CompoundTag writeToNBT(CompoundTag tag) {
        super.writeToNBT(tag);
        tag.putDouble(TORQUE_ENERGY_TAG, torqueCell.getEnergy());
        tag.putInt(CLIENT_ENERGY_TAG, clientDestEnergyState);
        return tag;
    }
}
