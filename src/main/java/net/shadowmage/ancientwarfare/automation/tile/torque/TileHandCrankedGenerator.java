package net.shadowmage.ancientwarfare.automation.tile.torque;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.scores.Team;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.automation.config.AWAutomationStatics;
import net.shadowmage.ancientwarfare.core.config.AWCoreStatics;
import net.shadowmage.ancientwarfare.core.interfaces.ITorque.TorqueCell;
import net.shadowmage.ancientwarfare.core.interfaces.IWorkSite;
import net.shadowmage.ancientwarfare.core.interfaces.IWorker;
import net.shadowmage.ancientwarfare.core.owner.IOwnable;
import net.shadowmage.ancientwarfare.core.owner.Owner;
import net.shadowmage.ancientwarfare.core.upgrade.WorksiteUpgrade;
import net.shadowmage.ancientwarfare.core.util.Trig;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.Set;

public class TileHandCrankedGenerator extends TileTorqueSingleCell implements IWorkSite, IOwnable {

    private Owner owner = Owner.EMPTY;
    private final TorqueCell inputCell;

    /*
     * client side this == 0.0 -> 100.0 (integer percent)
     */
    private double clientInputEnergy;

    /*
     * client side this == 0 -> 100.0 (integer percent)
     */
    private int clientInputDestEnergy;

    /*
     * used client side for rendering
     */
    private double inputRotation;
    private double lastInputRotationDiff;

    public TileHandCrankedGenerator(BlockEntityType<?> type, BlockPos pos, BlockState state) {

        super(type, pos, state);
        double eff = AWAutomationStatics.low_efficiency_factor;
        torqueCell = new TorqueCell(0, 32, 32, eff);
        inputCell = new TorqueCell(32, 0, 150, eff);
    }

    @Override
    public void update() {
        if (!world.isClientSide) {
            serverNetworkUpdate();
            torqueIn = torqueCell.getEnergy() - prevEnergy;
            balancePower();
            torqueOut = transferPowerTo(getPrimaryFacing());
            torqueLoss = applyPowerDrain(torqueCell);
            torqueLoss += applyPowerDrain(inputCell);
            prevEnergy = torqueCell.getEnergy();
        } else {
            clientNetworkUpdate();
            updateRotation();
        }
    }

    private void balancePower() {
        double trans = Math.min(2.d, torqueCell.getMaxEnergy() - torqueCell.getEnergy());
        trans = Math.min(trans, inputCell.getEnergy());
        inputCell.setEnergy(inputCell.getEnergy() - trans);
        torqueCell.setEnergy(torqueCell.getEnergy() + trans);
    }

    @Override
    protected void serverNetworkSynch() {
        super.serverNetworkSynch();
        int percent = (int) (inputCell.getPercentFull() * 100d);
        if (percent != clientInputDestEnergy) {
            clientInputDestEnergy = percent;
            sendSideRotation(Direction.UP, percent);
        }
    }

    @Override
    protected void updateRotation() {
        super.updateRotation();
        if (clientInputEnergy > 0) {
            lastInputRotationDiff = -(AWAutomationStatics.low_rpt * clientInputEnergy * 0.01d) * Trig.TORADIANS;
            inputRotation += lastInputRotationDiff;
            inputRotation %= Trig.PI * (float) 2;
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    protected void clientNetworkUpdate() {
        if (!Mth.equal((float) clientEnergyState, clientDestEnergyState) || !Mth
                .equal((float) clientInputEnergy, clientInputDestEnergy)) {
            if (networkUpdateTicks >= 0) {
                clientEnergyState += (clientDestEnergyState - clientEnergyState) / ((double) networkUpdateTicks + 1.d);
                clientInputEnergy += (clientInputDestEnergy - clientInputEnergy) / ((double) networkUpdateTicks + 1.d);
                networkUpdateTicks--;
            } else {
                clientEnergyState = clientDestEnergyState;
                clientInputEnergy = clientInputDestEnergy;
            }
        }
    }

    @Override
    protected void handleClientRotationData(Direction side, int value) {
        super.handleClientRotationData(side, value);
        if (side == Direction.UP) {
            clientInputDestEnergy = value;
        }
    }

    @Override
    public void onBlockBroken(BlockState state) {
        //NOOP
    }

    @Override
    public Set<WorksiteUpgrade> getUpgrades() {
        return EnumSet.noneOf(WorksiteUpgrade.class);
    }

    @Override
    public Set<WorksiteUpgrade> getValidUpgrades() {
        return EnumSet.noneOf(WorksiteUpgrade.class);
    }

    @Override
    public void addUpgrade(WorksiteUpgrade upgrade) {
        // NOOP
    }

    @Override
    public void removeUpgrade(WorksiteUpgrade upgrade) {
        // NOOP
    }

    @Override
    public boolean hasWork() {
        return inputCell.getEnergy() < inputCell.getMaxEnergy();
    }

    @Override
    public void addEnergyFromWorker(IWorker worker) {
        inputCell.setEnergy(inputCell.getEnergy() + AWCoreStatics.energyPerWorkUnit * worker.getWorkEffectiveness(getWorkType()) * AWAutomationStatics.hand_cranked_generator_output);
    }

    @Override
    public void addEnergyFromPlayer(Player player) {
        inputCell.setEnergy(inputCell.getEnergy() + AWCoreStatics.energyPerWorkUnit * AWAutomationStatics.hand_cranked_generator_output);
    }

    @Override
    public WorkType getWorkType() {
        return WorkType.CRAFTING;
    }

    @Override
    @Nullable
    public Team getTeam() {
        return world.getScoreboard().getPlayersTeam(owner.getName());
    }

    @Override
    public boolean isOwner(Player player) {
        return owner.isOwnerOrSameTeamOrFriend(player);
    }

    @Override
    public void setOwner(Player player) {
        owner = new Owner(player);
    }

    @Override
    public void setOwner(Owner owner) {
        this.owner = owner;
    }

    @Override
    public Owner getOwner() {
        return owner;
    }

    @Override
    protected void handleUpdateNBT(CompoundTag tag) {
        super.handleUpdateNBT(tag);
        clientInputDestEnergy = tag.getInt("clientInputDestEnergy");
    }

    @Override
    protected void writeUpdateNBT(CompoundTag tag) {
        super.writeUpdateNBT(tag);
        tag.putInt("clientInputDestEnergy", clientInputDestEnergy);
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
        super.readFromNBT(tag);
        owner = Owner.deserializeFromNBT(tag);
        inputCell.setEnergy(tag.getDouble("inputEnergy"));
        clientInputDestEnergy = tag.getInt("clientInputEnergy");
    }

    @Override
    public CompoundTag writeToNBT(CompoundTag tag) {
        super.writeToNBT(tag);
        owner.serializeToNBT(tag);
        tag.putDouble("inputEnergy", inputCell.getEnergy());
        tag.putInt("clientInputEnergy", clientInputDestEnergy);
        return tag;
    }

    @Override
    public double getMaxTorque(@Nullable Direction from) {
        return inputCell.getMaxEnergy() + torqueCell.getMaxEnergy();
    }

    @Override
    public double getTorqueStored(@Nullable Direction from) {
        return inputCell.getEnergy() + torqueCell.getEnergy();
    }

    @Override
    public double addTorque(@Nullable Direction from, double energy) {
        if (from == getPrimaryFacing()) {
            return 0;
        } else if (from == Direction.UP || from == null) {
            return inputCell.addEnergy(energy);
        }
        return 0;
    }

    @Override
    public double drainTorque(Direction from, double energy) {
        if (from == getPrimaryFacing()) {
            return torqueCell.drainEnergy(energy);
        }
        return 0;
    }

    @Override
    public double getMaxTorqueOutput(Direction from) {
        if (from == getPrimaryFacing()) {
            return torqueCell.getMaxTickOutput();
        }
        return 0;
    }

    @Override
    public double getMaxTorqueInput(@Nullable Direction from) {
        return 0;
    }

    @Override
    public boolean canInputTorque(Direction from) {
        return false;
    }

    @Override
    public float getClientOutputRotation(Direction from, float delta) {
        float ret = 0;
        if (from == getPrimaryFacing()) {
            ret = getRenderRotation(rotation, lastRotationDiff, delta);
        } else if (from == Direction.UP) {
            ret = getRenderRotation(inputRotation, lastInputRotationDiff, delta);
        }
        return ret;
    }

    @Override
    public boolean useOutputRotation(@Nullable Direction from) {
        return true;
    }

    @Override
    protected double getTotalTorque() {
        return inputCell.getEnergy() + torqueCell.getEnergy();
    }

}
