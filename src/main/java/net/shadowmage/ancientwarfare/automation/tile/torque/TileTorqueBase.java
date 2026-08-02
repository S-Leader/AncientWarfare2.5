package net.shadowmage.ancientwarfare.automation.tile.torque;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.shadowmage.ancientwarfare.automation.config.AWAutomationStatics;
import net.shadowmage.ancientwarfare.automation.proxy.RFProxy;
import net.shadowmage.ancientwarfare.core.block.BlockRotationHandler.IRotatableTile;
import net.shadowmage.ancientwarfare.core.interfaces.IInteractableTile;
import net.shadowmage.ancientwarfare.core.interfaces.ITickable;
import net.shadowmage.ancientwarfare.core.interfaces.ITorque.ITorqueTile;
import net.shadowmage.ancientwarfare.core.interfaces.ITorque.TorqueCell;
import net.shadowmage.ancientwarfare.core.network.NetworkHandler;
import net.shadowmage.ancientwarfare.core.network.PacketBlockEvent;
import net.shadowmage.ancientwarfare.core.tile.TileUpdatable;
import net.shadowmage.ancientwarfare.core.util.BlockTools;
import net.shadowmage.ancientwarfare.core.util.WorldTools;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map;

public abstract class TileTorqueBase extends TileUpdatable implements ITorqueTile, IInteractableTile, IRotatableTile, ITickable {

    public static final int DIRECTION_LENGTH = Direction.values().length;
    private static final String ORIENTATION_TAG = "orientation";
    /*
     * The primary facing direction for this tile.  Default to north for uninitialized tiles (null is not a valid value)
     */
    protected Direction orientation = Direction.NORTH;

    /*
     * used by server to limit packet sending<br>
     * used by client for lerp-ticks for lerping between power states
     */
    protected int networkUpdateTicks;

    private BlockEntity[] rfCache;
    private final Map<Direction, LazyOptional<IEnergyStorage>> energyCapabilities = new EnumMap<>(Direction.class);
    private boolean capabilitiesInvalidated;
    private ITorqueTile[] torqueCache;

    /*
     * helper vars to be used by tiles during updating, to cache in/out/loss values<br>
     * IMPORTANT: should NOT be relied upon for calculations, only for use for display purposes<br>
     * E.G. A tile may choose to -not- update these vars.<br>
     * However, best effort should be made to update these vars accurately.<br><br>
     * Generated energy should be counted as 'in'<br>
     * Any directly output energy should be counted as 'out'<br>
     * Only direct power loss from transmission efficiency or per-tick loss should be counted for 'loss'
     */
    protected double torqueIn;
    protected double torqueOut;
    protected double torqueLoss;
    protected double prevEnergy;

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
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ENERGY) {
            if (capabilitiesInvalidated || isRemoved() || side == null || !canConnectEnergy(side)) {
                return LazyOptional.empty();
            }
            return energyCapabilities.computeIfAbsent(side,
                    direction -> LazyOptional.of(() -> new SidedEnergyStorage(direction))).cast();
        }
        return super.getCapability(capability, side);
    }

    @Override
    public void invalidateCaps() {
        /*
         * LazyOptional invalidation listeners are allowed to query capabilities.
         * Iterating the live EnumMap while notifying them can therefore modify the
         * same map and crash exactly when the junction is mined. Detach the values
         * first and reject all new energy capability requests during invalidation.
         */
        capabilitiesInvalidated = true;
        var capabilities = new ArrayList<>(energyCapabilities.values());
        energyCapabilities.clear();
        super.invalidateCaps();
        capabilities.forEach(LazyOptional::invalidate);
    }

    @Override
    public void reviveCaps() {
        energyCapabilities.clear();
        capabilitiesInvalidated = false;
        super.reviveCaps();
    }

    private final class SidedEnergyStorage implements IEnergyStorage {
        private final Direction side;

        private SidedEnergyStorage(Direction side) {
            this.side = side;
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            return TileTorqueBase.this.receiveEnergy(side, maxReceive, simulate);
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            return 0;
        }

        @Override
        public int getEnergyStored() {
            return TileTorqueBase.this.getEnergyStored(side);
        }

        @Override
        public int getMaxEnergyStored() {
            return TileTorqueBase.this.getMaxEnergyStored(side);
        }

        @Override
        public boolean canExtract() {
            return false;
        }

        @Override
        public boolean canReceive() {
            return canInputTorque(side);
        }
    }

    //************************************** NEIGHBOR CACHE UPDATING ***************************************//

    public final ITorqueTile[] getTorqueCache() {
        if (isRemoved() || !hasWorld()) {
            return new ITorqueTile[DIRECTION_LENGTH];
        }
        if (torqueCache == null) {
            buildTorqueCache();
        }
        return torqueCache;
    }

    public final BlockEntity[] getRFCache() {
        if (isRemoved() || !hasWorld()) {
            return new BlockEntity[DIRECTION_LENGTH];
        }
        if (rfCache == null) {
            buildRFCache();
        }
        return rfCache;
    }

    private void buildTorqueCache() {
        if (!hasWorld()) {
            throw new RuntimeException("Attempt to build neighbor cache on null world!!");
        }
        ITorqueTile[] torqueCache = new ITorqueTile[DIRECTION_LENGTH];
        Direction dir;
        for (int i = 0; i < torqueCache.length; i++) {
            dir = Direction.values()[i];
            if (!canOutputTorque(dir) && !canInputTorque(dir)) {
                continue;
            }
            BlockPos offsetPos = pos.relative(dir);
            if (!world.isLoaded(offsetPos)) {
                continue;
            }
            final int index = i;
            WorldTools.getTile(world, offsetPos, ITorqueTile.class)
                    .filter(t -> !(t instanceof BlockEntity blockEntity) || !blockEntity.isRemoved())
                    .ifPresent(t -> torqueCache[index] = t);
        }
        this.torqueCache = torqueCache;
    }

    private void buildRFCache() {
        BlockEntity[] rfCache = new BlockEntity[DIRECTION_LENGTH];
        Direction dir;
        for (int i = 0; i < rfCache.length; i++) {
            dir = Direction.values()[i];
            if (!canOutputTorque(dir) && !canInputTorque(dir)) {
                continue;
            }
            BlockPos offsetPos = pos.relative(dir);
            if (!world.isLoaded(offsetPos)) {
                continue;
            }
            final Direction direction = dir;
            WorldTools.getTile(world, offsetPos).filter(t -> RFProxy.instance.isRFTile(t)).ifPresent(t -> rfCache[direction.ordinal()] = t);
        }
        this.rfCache = rfCache;
    }

    @Override
    public void clearRemoved() {
        super.clearRemoved();
        invalidateTorqueCache();
    }

    @Override
    public void invalidate() {
        /*
         * Adjacent machines may still hold this tile in their torque cache. Clear
         * those references before this block entity is removed so the next machine
         * tick cannot call into a dead junction.
         */
        if (world != null) {
            for (Direction direction : Direction.values()) {
                BlockPos neighborPos = pos.relative(direction);
                if (!world.isLoaded(neighborPos)) {
                    continue;
                }
                BlockEntity neighbor = world.getBlockEntity(neighborPos);
                if (neighbor instanceof TileTorqueBase torque && !torque.isRemoved()) {
                    torque.onNeighborTileChanged();
                }
            }
        }
        super.invalidate();
        invalidateTorqueCache();
    }

    public void onNeighborTileChanged() {
        invalidateTorqueCache();
    }

    private void invalidateTorqueCache() {
        torqueCache = null;
        rfCache = null;
        onNeighborCacheInvalidated();
    }

    protected void onNeighborCacheInvalidated() {

    }

    //************************************** generic stuff ***************************************//

    @Override
    public final void setPrimaryFacing(Direction d) {
        this.orientation = d;
        this.world.updateNeighbourForOutputSignal(pos, getBlockState().getBlock());
        this.invalidateTorqueCache();
        BlockTools.notifyBlockUpdate(this);
    }

    @Override
    public final Direction getPrimaryFacing() {
        return orientation;
    }

    @Override
    public boolean onBlockClicked(Player player, @Nullable InteractionHand hand) {
        ItemStack stack = hand == null ? ItemStack.EMPTY : player.getItemInHand(hand);
        if (stack.isEmpty()) {
            if (!world.isClientSide) {
                player.sendSystemMessage(Component.translatable("guistrings.automation.torque.values", String.format("%.2f", getTotalTorque()), String.format("%.2f", getTorqueIn()), String.format("%.2f", getTorqueOut()), String.format("%.2f", getTorqueLoss())));
            }
            return true;
        }
        return false;
    }

    //************************************** Utility Methods ***************************************//

    protected void updateRotation() {
        throw new UnsupportedOperationException();
    }

    protected void clientNetworkUpdate() {
        throw new UnsupportedOperationException();
    }

    protected void serverNetworkSynch() {
        throw new UnsupportedOperationException();
    }

    protected abstract void handleClientRotationData(Direction side, int value);

    /*
     * @return the TOTAL amount stored in the entire tile (not just one side), used by on-right-click functionality
     */
    protected abstract double getTotalTorque();

    /*
     * @return the total output of torque for the tick
     */
    private double getTorqueOut() {
        return torqueOut;
    }

    /*
     * @return the total input of torque for the tick
     */
    private double getTorqueIn() {
        return torqueIn;
    }

    /*
     * @return the total torque lost (destroyed, gone completely) for the tick
     */
    private double getTorqueLoss() {
        return torqueLoss;
    }

    protected final void serverNetworkUpdate() {
        networkUpdateTicks--;
        if (networkUpdateTicks <= 0) {
            networkUpdateTicks = AWAutomationStatics.energyMinNetworkUpdateFrequency;
            serverNetworkSynch();
        }
    }

    protected void sendSideRotation(Direction side, int value) {
        int valueBits = (value & 0xff);
        sendDataToClient(side.ordinal(), valueBits);
    }

    protected final double transferPowerTo(Direction from) {
        double transferred = 0;
        ITorqueTile[] tc = getTorqueCache();
        if (tc[from.ordinal()] != null) {
            if (tc[from.ordinal()].canInputTorque(from.getOpposite())) {
                return drainTorque(from, tc[from.ordinal()].addTorque(from.getOpposite(), getMaxTorqueOutput(from)));
            }
        } else {
            transferred = RFProxy.instance.transferPower(this, from, getRFCache()[from.ordinal()]);
            if (transferred > 0) {
                return transferred;
            }
        }
        return transferred;
    }

    protected final double applyPowerDrain(TorqueCell cell) {
        double e = cell.getEnergy();
        cell.setEnergy(e * cell.getEfficiency());
        return cell.getEnergy() - e;
    }

    protected final void sendDataToClient(int type, int data) {
        PacketBlockEvent pkt = new PacketBlockEvent(pos, getBlockState().getBlock(), (short) type, (short) data);
        NetworkHandler.sendToAllTrackingChunk(world, pos.getX() >> 4, pos.getZ() >> 4, pkt);
    }

    protected final float getRenderRotation(double rotation, double lastRotationDiff, float partialTicks) {
        return (float) (rotation - (lastRotationDiff * (1 - partialTicks)));
    }

    @Override
    public boolean triggerEvent(int a, int b) {
        if (world.isClientSide && a < DIRECTION_LENGTH) {
            networkUpdateTicks = AWAutomationStatics.energyMinNetworkUpdateFrequency;
            handleClientRotationData(Direction.values()[a], b);
        }
        return true;
    }

    //************************************** NBT / DATA PACKET ***************************************//

    @Override
    public void readFromNBT(CompoundTag tag) {
        super.readFromNBT(tag);
        orientation = Direction.values()[tag.getInt(ORIENTATION_TAG)];
    }

    @Override
    public CompoundTag writeToNBT(CompoundTag tag) {
        super.writeToNBT(tag);
        tag.putInt(ORIENTATION_TAG, orientation.ordinal());

        return tag;
    }

    @Override
    protected void writeUpdateNBT(CompoundTag tag) {
        super.writeUpdateNBT(tag);
        tag.putInt(ORIENTATION_TAG, orientation.ordinal());
    }

    @Override
    protected void handleUpdateNBT(CompoundTag tag) {
        super.handleUpdateNBT(tag);
        orientation = Direction.values()[tag.getInt(ORIENTATION_TAG)];
        BlockTools.notifyBlockUpdate(this);
    }
}
