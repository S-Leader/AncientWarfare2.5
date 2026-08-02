package net.shadowmage.ancientwarfare.automation.tile.torque;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fml.ModList;
import net.shadowmage.ancientwarfare.automation.config.AWAutomationStatics;
import net.shadowmage.ancientwarfare.core.interfaces.ITorque.ITorqueTile;
import net.shadowmage.ancientwarfare.core.interfaces.ITorque.SidedTorqueCell;
import net.shadowmage.ancientwarfare.core.interfaces.ITorque.TorqueCell;
import net.shadowmage.ancientwarfare.core.util.Constants;
import net.shadowmage.ancientwarfare.core.util.Trig;

import javax.annotation.Nullable;

public abstract class TileTorqueSidedCell extends TileTorqueBase {

    private static final String CLIENT_ENERGY_TAG = "clientEnergy";
    private boolean[] connections = null;
    final SidedTorqueCell[] storage = new SidedTorqueCell[DIRECTION_LENGTH];

    /*
     * client side this == 0.0 -> 100.0
     */
    private double clientEnergyState;

    /*
     * server side this == 0 -> 100 (integer percent)
     * client side this == 0 -> 100 (integer percent)
     */
    private int clientDestEnergyState;

    /*
     * used client side for rendering
     */
    private double rotation;
    private double lastRotationDiff;

    public TileTorqueSidedCell() {
        double max = getMaxTransfer();
        double eff = getEfficiency();
        for (int i = 0; i < storage.length; i++) {
            storage[i] = new SidedTorqueCell(max, max, max, eff, Direction.values()[i], this);
        }
    }

    protected abstract double getEfficiency();

    protected abstract double getMaxTransfer();

    @Override
    public void update() {
        if (!world.isClientSide) {
            serverNetworkUpdate();
            torqueIn = getTotalTorque() - prevEnergy;
            balanceStorage();
            torqueLoss = applyPowerLoss();
            torqueOut = transferPower();
            prevEnergy = getTotalTorque();
        } else {
            clientNetworkUpdate();
            updateRotation();
        }
    }

    private double applyPowerLoss() {
        double loss = 0;
        for (SidedTorqueCell aStorage : storage) {
            loss += applyPowerDrain(aStorage);
        }
        return loss;
    }

    protected double transferPower() {
        return transferPowerTo(getPrimaryFacing());
    }

    protected void balanceStorage() {
        int face = getPrimaryFacing().ordinal();
        TorqueCell out = storage[face];
        double total = 0;
        TorqueCell in;
        for (int i = 0; i < storage.length; i++) {
            if (i == face) {
                continue;
            }
            total += storage[i].getEnergy();
        }
        if (total > 0) {
            double transfer = Math.min(total, out.getMaxEnergy() - out.getEnergy());
            double percent = transfer / total;
            transfer = 0;
            double fromEach;
            for (int i = 0; i < storage.length; i++) {
                if (i == face) {
                    continue;
                }
                in = storage[i];
                fromEach = in.getEnergy() * percent;
                transfer += fromEach;
                in.setEnergy(in.getEnergy() - fromEach);
            }
            out.setEnergy(out.getEnergy() + transfer);
        }
    }

    @Override
    protected void serverNetworkSynch() {
        int percent = (int) (storage[getPrimaryFacing().ordinal()].getPercentFull() * 100.d);
        int percent2 = (int) ((torqueOut / storage[getPrimaryFacing().ordinal()].getMaxOutput()) * 100.d);
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
    protected void clientNetworkUpdate() {
        if (clientEnergyState != clientDestEnergyState) {
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
        clientDestEnergyState = value;
        this.networkUpdateTicks = AWAutomationStatics.energyMinNetworkUpdateFrequency;
    }

    @Override
    public boolean canInputTorque(Direction from) {
        return from != orientation;
    }

    @Override
    public boolean canOutputTorque(Direction towards) {
        return towards == orientation;
    }

    public boolean[] getConnections() {
        if (connections == null) {
            buildConnections();
        }
        return connections;
    }

    @Override
    public void onNeighborTileChanged() {
        super.onNeighborTileChanged();
        connections = null;
    }

    private void buildConnections() {
        boolean[] updatedConnections = new boolean[DIRECTION_LENGTH];
        ITorqueTile[] cache = getTorqueCache();
        Direction dir;
        for (int i = 0; i < cache.length; i++) {
            dir = Direction.values()[i];
            if (cache[i] != null) {
                updatedConnections[i] = (cache[i].canInputTorque(dir.getOpposite()) && canOutputTorque(dir)) || (cache[i].canOutputTorque(dir.getOpposite()) && canInputTorque(dir));
            }
        }
        if (ModList.get().isLoaded("redstoneflux")) {
            BlockEntity[] tes = getRFCache();
            for (int i = 0; i < tes.length; i++) {
                if (cache[i] != null) {
                    continue;
                }//already examined that side..
                if (tes[i] != null) {
                    updatedConnections[i] = true;
                }
            }
        }
        this.connections = updatedConnections;
    }

    @Override
    public double getMaxTorque(@Nullable Direction from) {
        return from == null ? 0 : storage[from.ordinal()].getMaxEnergy();
    }

    @Override
    public double getTorqueStored(@Nullable Direction from) {
        if (from == null) {
            return 0D; // some mods pass null into RF compat so let's return 0 in that case
        }
        return storage[from.ordinal()].getEnergy();
    }

    @Override
    public double addTorque(@Nullable Direction from, double energy) {
        return from == null ? 0 : storage[from.ordinal()].addEnergy(energy);
    }

    @Override
    public double drainTorque(Direction from, double energy) {
        return storage[from.ordinal()].drainEnergy(energy);
    }

    @Override
    public double getMaxTorqueOutput(Direction from) {
        return storage[from.ordinal()].getMaxTickOutput();
    }

    @Override
    public double getMaxTorqueInput(@Nullable Direction from) {
        return from == null ? 0 : storage[from.ordinal()].getMaxTickInput();
    }

    @Override
    public boolean useOutputRotation(@Nullable Direction from) {
        return true;
    }

    @Override
    public float getClientOutputRotation(Direction from, float delta) {
        return getRenderRotation(rotation, lastRotationDiff, delta);
    }

    @Override
    protected void writeUpdateNBT(CompoundTag tag) {
        super.writeUpdateNBT(tag);
        tag.putInt(CLIENT_ENERGY_TAG, clientDestEnergyState);
    }

    @Override
    protected void handleUpdateNBT(CompoundTag tag) {
        super.handleUpdateNBT(tag);
        clientDestEnergyState = tag.getInt(CLIENT_ENERGY_TAG);
        connections = null; //clear connections on update
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
        super.readFromNBT(tag);
        ListTag list = tag.getList("energyList", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < storage.length; i++) {
            storage[i].readFromNBT(list.getCompound(i));
        }
        clientDestEnergyState = tag.getInt(CLIENT_ENERGY_TAG);
    }

    @Override
    public CompoundTag writeToNBT(CompoundTag tag) {
        super.writeToNBT(tag);
        ListTag list = new ListTag();
        for (SidedTorqueCell aStorage : storage) {
            list.add(aStorage.writeToNBT(new CompoundTag()));
        }
        tag.put("energyList", list);
        tag.putInt(CLIENT_ENERGY_TAG, clientDestEnergyState);

        return tag;
    }

    @Override
    protected double getTotalTorque() {
        double d = 0;
        Direction dir;
        for (int i = 0; i < storage.length; i++) {
            dir = Direction.values()[i];
            if (canInputTorque(dir) || canOutputTorque(dir)) {
                d += storage[i].getEnergy();
            }
        }
        return d;
    }

}
