package net.shadowmage.ancientwarfare.automation.tile.worksite;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.scores.Team;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.shadowmage.ancientwarfare.automation.config.AWAutomationStatics;
import net.shadowmage.ancientwarfare.automation.item.ItemWorksiteUpgrade;
import net.shadowmage.ancientwarfare.core.block.BlockRotationHandler.IRotatableTile;
import net.shadowmage.ancientwarfare.core.config.AWCoreStatics;
import net.shadowmage.ancientwarfare.core.interfaces.IInteractableTile;
import net.shadowmage.ancientwarfare.core.interfaces.ITickable;
import net.shadowmage.ancientwarfare.core.interfaces.ITorque.TorqueCell;
import net.shadowmage.ancientwarfare.core.interfaces.IWorkSite;
import net.shadowmage.ancientwarfare.core.interfaces.IWorker;
import net.shadowmage.ancientwarfare.core.owner.IOwnable;
import net.shadowmage.ancientwarfare.core.owner.Owner;
import net.shadowmage.ancientwarfare.core.tile.TileUpdatable;
import net.shadowmage.ancientwarfare.core.upgrade.WorksiteUpgrade;
import net.shadowmage.ancientwarfare.core.util.BlockTools;
import net.shadowmage.ancientwarfare.core.util.InventoryTools;
import org.apache.commons.lang3.math.NumberUtils;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

public abstract class TileWorksiteBase extends TileUpdatable
        implements ITickable, IWorkSite, IInteractableTile, IOwnable, IRotatableTile, IEnergyStorage {
    private static final String UPGRADES_TAG = "upgrades";
    private static final String ORIENTATION_TAG = "orientation";
    private static final String ACTIVE_TAG = "active";

    private Owner owner = Owner.EMPTY;

    private double efficiencyBonusFactor = 0.f;

    private EnumSet<WorksiteUpgrade> upgrades = EnumSet.noneOf(WorksiteUpgrade.class);

    private Direction orientation = Direction.NORTH;

    private final TorqueCell torqueCell;

    private int workRetryDelay = 20;

    private boolean active = false;
    private int timeSinceLastActiveCheck = 0;
    private final LazyOptional<IEnergyStorage> energyCapability = LazyOptional.of(() -> this);

    public TileWorksiteBase() {
        torqueCell = new TorqueCell(32, 0, AWCoreStatics.energyPerWorkUnit * 3, 1);
    }

    //************************************** COFH RF METHODS ***************************************//
    public final int getEnergyStored(Direction from) {
        return (int) (getTorqueStored(from) * AWAutomationStatics.torqueToRf);
    }

    public final int getMaxEnergyStored(Direction from) {
        return (int) (getMaxTorque(from) * AWAutomationStatics.torqueToRf);
    }

    public final boolean canConnectEnergy(Direction from) {
        return canOutputTorque(from) || canInputTorque(from);
    }

    public final int extractEnergy(Direction from, int maxExtract, boolean simulate) {
        return 0;
    }

    public final int receiveEnergy(Direction from, int maxReceive, boolean simulate) {
        if (!canInputTorque(from)) {
            return 0;
        }
        if (simulate) {
            return Math.min(maxReceive, (int) (AWAutomationStatics.torqueToRf * getMaxTorqueInput(from)));
        }
        return (int) (AWAutomationStatics.torqueToRf * addTorque(from, (double) maxReceive * AWAutomationStatics.rfToTorque));
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        return receiveEnergy(null, maxReceive, simulate);
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        return 0;
    }

    @Override
    public int getEnergyStored() {
        return getEnergyStored(null);
    }

    @Override
    public int getMaxEnergyStored() {
        return getMaxEnergyStored(null);
    }

    @Override
    public boolean canExtract() {
        return false;
    }

    @Override
    public boolean canReceive() {
        return true;
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction side) {
        return capability == ForgeCapabilities.ENERGY ? energyCapability.cast() : super.getCapability(capability, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        energyCapability.invalidate();
    }
    //************************************** UPGRADE HANDLING METHODS ***************************************//

    public boolean isActive() {
        return active;
    }

    @Override
    public final Set<WorksiteUpgrade> getUpgrades() {
        return upgrades;
    }

    @Override
    public Set<WorksiteUpgrade> getValidUpgrades() {
        return EnumSet.of(WorksiteUpgrade.ENCHANTED_TOOLS_1, WorksiteUpgrade.ENCHANTED_TOOLS_2, WorksiteUpgrade.TOOL_QUALITY_1, WorksiteUpgrade.TOOL_QUALITY_2,
                WorksiteUpgrade.TOOL_QUALITY_3);
    }

    @Override
    public void onBlockBroken(BlockState state) {
        for (WorksiteUpgrade ug : this.upgrades) {
            InventoryTools.dropItemInWorld(level, ItemWorksiteUpgrade.getStack(ug), worldPosition);
        }
        efficiencyBonusFactor = 0;
        upgrades.clear();
    }

    @Override
    public void addUpgrade(WorksiteUpgrade upgrade) {
        upgrades.add(upgrade);
        updateEfficiency();
        BlockTools.notifyBlockUpdate(this);
        markDirty();
    }

    @Override
    public void removeUpgrade(WorksiteUpgrade upgrade) {
        upgrades.remove(upgrade);
        updateEfficiency();
        BlockTools.notifyBlockUpdate(this);
        markDirty();
    }

    public int getFortune() {
        if (getUpgrades().contains(WorksiteUpgrade.ENCHANTED_TOOLS_2)) {
            return 2;
        }
        return getUpgrades().contains(WorksiteUpgrade.ENCHANTED_TOOLS_1) ? 1 : 0;
    }

    //************************************** TILE UPDATE METHODS ***************************************//

    protected abstract Optional<IWorksiteAction> getNextAction();

    protected abstract boolean processAction(IWorksiteAction action);

    protected abstract void updateWorksite();

    @Override
    public final void update() {
        if (!hasWorld() || level.isClientSide || level.getBestNeighborSignal(worldPosition) != 0) {
            return;
        }
        if (workRetryDelay > 0) {
            workRetryDelay--;
        } else {
            level.getProfiler().push("Check For Work");
            Optional<IWorksiteAction> nextAction = getNextAction();
            boolean hasWork = nextAction.isPresent() && nextAction.get().getEnergyConsumed(efficiencyBonusFactor) <= getTorqueStored(null);
            if (timeSinceLastActiveCheck < 0) {
                if (active != checkIfActive()) {
                    active = checkIfActive();
                    BlockTools.notifyBlockUpdate(this);
                }
                timeSinceLastActiveCheck = 60;
            } else {
                timeSinceLastActiveCheck--;
            }
            if (hasWork) {
                level.getProfiler().popPush("Process Work");
                IWorksiteAction action = nextAction.get();
                if (processAction(action)) {
                    torqueCell.setEnergy(torqueCell.getEnergy() - action.getEnergyConsumed(efficiencyBonusFactor));
                    markDirty();
                } else {
                    workRetryDelay = 20;
                }
            }
            level.getProfiler().pop();
        }
        level.getProfiler().push("WorksiteBaseUpdate");
        updateWorksite();
        level.getProfiler().pop();
    }

    private boolean checkIfActive() {
        return getTorqueStored(null) > 0;
    }

    private void updateEfficiency() {
        efficiencyBonusFactor = IWorkSite.WorksiteImplementation.getEfficiencyFactor(upgrades);
    }

    //************************************** TILE INTERACTION METHODS ***************************************//

    @Override
    public final Team getTeam() {
        return level == null ? null : level.getScoreboard().getPlayersTeam(owner.getName());
    }

    @Override
    public Owner getOwner() {
        return owner;
    }

    @Override
    public final boolean isOwner(Player player) {
        return owner.isOwnerOrSameTeamOrFriend(player);
    }

    @Override
    public final void setOwner(Player player) {
        owner = new Owner(player);
    }

    @Override
    public final void setOwner(Owner owner) {
        this.owner = owner;
    }

    //************************************** TORQUE INTERACTION METHODS ***************************************//

    @Override
    public final float getClientOutputRotation(Direction from, float delta) {
        return 0;
    }

    @Override
    public final boolean useOutputRotation(@Nullable Direction from) {
        return false;
    }

    @Override
    public final double getMaxTorqueOutput(Direction from) {
        return 0;
    }

    @Override
    public final boolean canOutputTorque(Direction towards) {
        return false;
    }

    @Override
    public final double drainTorque(Direction from, double energy) {
        return 0;
    }

    @Override
    public final void addEnergyFromWorker(IWorker worker) {
        addTorque(null, AWCoreStatics.energyPerWorkUnit * worker.getWorkEffectiveness(getWorkType()) * AWAutomationStatics.hand_cranked_generator_output);
    }

    @Override
    public final void addEnergyFromPlayer(Player player) {
        addTorque(null, AWCoreStatics.energyPerWorkUnit * AWAutomationStatics.hand_cranked_generator_output);
    }

    @Override
    public final double addTorque(@Nullable Direction from, double energy) {
        return torqueCell.addEnergy(energy);
    }

    @Override
    public final double getMaxTorque(@Nullable Direction from) {
        return torqueCell.getMaxEnergy();
    }

    @Override
    public final double getTorqueStored(@Nullable Direction from) {
        return torqueCell.getEnergy();
    }

    @Override
    public final double getMaxTorqueInput(@Nullable Direction from) {
        return torqueCell.getMaxTickInput();
    }

    @Override
    public final boolean canInputTorque(Direction from) {
        return true;
    }

    //************************************** MISC METHODS ***************************************//
    public boolean shouldRenderInPass(int pass) {
        return pass == 1;
    }

    @Override
    public String toString() {
        return "Worksite Base[" + torqueCell.getEnergy() + "]";
    }

    @Override
    public boolean hasWork() {
        return level != null && torqueCell.getEnergy() < torqueCell.getMaxEnergy() && level.getBestNeighborSignal(worldPosition) == 0;
    }

    @Override
    public final Direction getPrimaryFacing() {
        return orientation;
    }

    @Override
    public final void setPrimaryFacing(Direction face) {
        orientation = face;
        BlockTools.notifyBlockUpdate(this);
        markDirty();//notify neighbors of tile change
    }

    //************************************** NBT AND PACKET DATA METHODS ***************************************//

    @Override
    public CompoundTag writeToNBT(CompoundTag tag) {
        super.writeToNBT(tag);
        tag.putDouble("storedEnergy", torqueCell.getEnergy());
        owner.serializeToNBT(tag);
        if (!getUpgrades().isEmpty()) {
            int[] ug = new int[getUpgrades().size()];
            int i = 0;
            for (WorksiteUpgrade u : getUpgrades()) {
                ug[i] = u.ordinal();
                i++;
            }
            tag.putIntArray(UPGRADES_TAG, ug);
        }
        tag.putInt(ORIENTATION_TAG, orientation.ordinal());

        return tag;
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
        super.readFromNBT(tag);
        torqueCell.setEnergy(tag.getDouble("storedEnergy"));
        owner = Owner.deserializeFromNBT(tag);
        if (tag.contains(UPGRADES_TAG)) {
            Tag upgradeTag = tag.get(UPGRADES_TAG);
            if (upgradeTag instanceof IntArrayTag) {
                int[] ug = tag.getIntArray(UPGRADES_TAG);
                for (int anUg : ug) {
                    upgrades.add(WorksiteUpgrade.values()[anUg]);
                }
            } else if (upgradeTag instanceof ListTag)//template parser reads int-arrays as a tag list for some reason
            {
                ListTag list = (ListTag) upgradeTag;
                for (int i = 0; i < list.size(); i++) {
                    String st = list.getString(i);
                    int ug = NumberUtils.toInt(st, -1);
                    if (ug > -1) {
                        upgrades.add(WorksiteUpgrade.values()[ug]);
                    }
                }
            }
        }

        if (tag.contains(ORIENTATION_TAG)) {
            orientation = Direction.values()[tag.getInt(ORIENTATION_TAG)];
        }
        updateEfficiency();
    }

    @Override
    protected void writeUpdateNBT(CompoundTag tag) {
        super.writeUpdateNBT(tag);
        int[] ugs = new int[upgrades.size()];
        int i = 0;
        for (WorksiteUpgrade ug : upgrades) {
            ugs[i] = ug.ordinal();
            i++;
        }
        tag.putIntArray(UPGRADES_TAG, ugs);
        tag.putInt(ORIENTATION_TAG, orientation.ordinal());
        tag.putBoolean(ACTIVE_TAG, active);
        owner.serializeToNBT(tag);
    }

    @Override
    protected void handleUpdateNBT(CompoundTag tag) {
        super.handleUpdateNBT(tag);
        upgrades.clear();
        if (tag.contains(UPGRADES_TAG)) {
            int[] ugs = tag.getIntArray(UPGRADES_TAG);
            for (int ug : ugs) {
                upgrades.add(WorksiteUpgrade.values()[ug]);
            }
        }
        updateEfficiency();
        orientation = Direction.values()[tag.getInt(ORIENTATION_TAG)];
        active = tag.getBoolean(ACTIVE_TAG);
        owner = Owner.deserializeFromNBT(tag);
        BlockTools.notifyBlockUpdate(this);
    }
}
