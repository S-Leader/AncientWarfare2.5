package net.shadowmage.ancientwarfare.automation.tile.torque;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.shadowmage.ancientwarfare.automation.config.AWAutomationStatics;
import net.shadowmage.ancientwarfare.core.interfaces.ITorque.ITorqueTile;
import net.shadowmage.ancientwarfare.core.interfaces.ITorque.TorqueCell;
import net.shadowmage.ancientwarfare.core.util.Trig;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

public abstract class TileTorqueShaft extends TileTorqueSingleCell {

    private static final String SHAFT_LINK_MASK_TAG = "shaftLinkMask";
    private static final int LINK_PREVIOUS = 1;
    private static final int LINK_NEXT = 2;

    private TileTorqueShaft prev, next;

    private boolean prevNeighborInvalid = true;
    private boolean nextNeighborInvalid = true;

    /*
     * Visual topology is server-authoritative.  Querying adjacent client block
     * entities during a chunk rebuild is racy because the newly placed shaft's
     * orientation packet can arrive after the neighbour model was baked.
     */
    private int visualLinkMask;
    private int visualLinkRefreshTicks;

    public TileTorqueShaft(BlockEntityType<?> type, BlockPos pos, BlockState state) {

        super(type, pos, state);
        double max = getMaxTransfer();
        torqueCell = new TorqueCell(max, max, max, getEfficiency());
    }

    protected abstract double getEfficiency();

    protected abstract double getMaxTransfer();

    @Override
    public void update() {
        super.update();
        if (!world.isClientSide && visualLinkRefreshTicks > 0 && --visualLinkRefreshTicks == 0) {
            refreshVisualLinks(true);
        }
    }

    @Override
    protected void serverNetworkSynch() {
        if (prev() == null) {
            TileTorqueShaft last = this;
            TileTorqueShaft n = next();
            double totalPower = torqueCell.getEnergy();
            double num = 1;

            /*
             * A corrupt/stale legacy neighbor link must never turn a newly placed
             * shaft into an infinite traversal.  Identity tracking also makes this
             * safe while adjacent chunks are unloading/reloading.
             */
            Set<TileTorqueShaft> visited = Collections.newSetFromMap(new IdentityHashMap<>());
            visited.add(this);
            while (n != null && visited.add(n)) {
                totalPower += n.torqueCell.getEnergy();
                last = n;
                num++;
                n = n.next;
            }

            double avg = totalPower / num;
            double maxEnergy = torqueCell.getMaxEnergy();
            double perc = maxEnergy > 0D ? avg / maxEnergy : 0D;
            double torqueOut = last.torqueOut;
            double maxOutput = last.torqueCell.getMaxOutput();

            int percent = (int) (perc * 100.d);
            int percent2 = maxOutput > 0D ? (int) ((torqueOut / maxOutput) * 100.d) : 0;
            percent = Math.max(percent, percent2);
            if (percent != clientDestEnergyState) {
                clientDestEnergyState = percent;
                sendSideRotation(getPrimaryFacing(), percent);
            }
        }
    }

    @Override
    protected void updateRotation() {
        if (prev() == null) {
            if (clientEnergyState > 0) {
                lastRotationDiff = -(AWAutomationStatics.low_rpt * clientEnergyState * 0.01d) * Trig.TORADIANS;
                rotation += lastRotationDiff;
                rotation %= Trig.PI * 2;
            }
            TileTorqueShaft n = next;
            Set<TileTorqueShaft> visited = Collections.newSetFromMap(new IdentityHashMap<>());
            visited.add(this);
            while (n != null && visited.add(n)) {
                n.rotation = rotation;
                n.lastRotationDiff = lastRotationDiff;
                n = n.next;
            }
        }
    }

    protected void onNeighborCacheInvalidated() {
        invalidateNeighborCache();
        invalidateLocalCache();
        if (world != null && !world.isClientSide) {
            visualLinkRefreshTicks = 1;
        }
    }

    private void invalidateLocalCache() {
        prevNeighborInvalid = true;
        nextNeighborInvalid = true;
        prev = next = null;
    }

    private void invalidateNeighborCache() {
        if (next != null) {
            next.invalidateLocalCache();
        }
        if (prev != null) {
            prev.invalidateLocalCache();
        }
    }

    @Nullable
    public TileTorqueShaft prev() {
        if (prevNeighborInvalid) {
            prevNeighborInvalid = false;
            prev = findPreviousShaft();
            if (prev != null) {
                prev.next = this;
            }
        }
        return prev;
    }

    @Nullable
    public TileTorqueShaft next() {
        if (nextNeighborInvalid) {
            nextNeighborInvalid = false;
            next = findNextShaft();
            if (next != null) {
                next.prev = this;
            }
        }
        return next;
    }

    /**
     * Renderer-safe connection tests.  These deliberately do not mutate prev/next
     * links and never emit block updates.  The old renderer called prev(), which
     * could call sendBlockUpdated while the chunk renderer was already resolving
     * the just-placed shaft model.
     */
    public boolean hasPreviousShaft() {
        return (visualLinkMask & LINK_PREVIOUS) != 0;
    }

    public boolean hasNextShaft() {
        return (visualLinkMask & LINK_NEXT) != 0;
    }

    private void refreshVisualLinks(boolean synchronize) {
        int newMask = 0;
        if (findPreviousShaft() != null) {
            newMask |= LINK_PREVIOUS;
        }
        if (findNextShaft() != null) {
            newMask |= LINK_NEXT;
        }

        if (newMask == visualLinkMask) {
            return;
        }

        visualLinkMask = newMask;
        if (synchronize && world != null && !world.isClientSide) {
            setChanged();
            requestVisualModelRefresh();
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (world != null && !world.isClientSide) {
            // Wait until adjacent BEs and their saved orientations are attached.
            visualLinkRefreshTicks = 2;
        } else {
            requestModelDataUpdate();
        }
    }

    @Nullable
    private TileTorqueShaft findPreviousShaft() {
        ITorqueTile input = getTorqueCache()[orientation.getOpposite().ordinal()];
        if (input instanceof TileTorqueShaft shaft
                && !shaft.isRemoved()
                && shaft.getClass() == this.getClass()
                && shaft.canOutputTorque(orientation)) {
            return shaft;
        }
        return null;
    }

    @Nullable
    private TileTorqueShaft findNextShaft() {
        ITorqueTile output = getTorqueCache()[orientation.ordinal()];
        if (output instanceof TileTorqueShaft shaft
                && !shaft.isRemoved()
                && shaft.getClass() == this.getClass()
                && shaft.canInputTorque(orientation.getOpposite())) {
            return shaft;
        }
        return null;
    }

    @Override
    public float getClientOutputRotation(Direction from, float delta) {
        return getRenderRotation(rotation, lastRotationDiff, delta);
    }

    @Override
    protected void writeUpdateNBT(CompoundTag tag) {
        super.writeUpdateNBT(tag);
        tag.putInt(SHAFT_LINK_MASK_TAG, visualLinkMask);
    }

    @Override
    protected void handleUpdateNBT(CompoundTag tag) {
        super.handleUpdateNBT(tag);
        if (tag.contains(SHAFT_LINK_MASK_TAG)) {
            int oldMask = visualLinkMask;
            visualLinkMask = tag.getInt(SHAFT_LINK_MASK_TAG) & (LINK_PREVIOUS | LINK_NEXT);
            if (oldMask != visualLinkMask && world != null && world.isClientSide) {
                requestVisualModelRefresh();
            }
        }
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
        super.readFromNBT(tag);
        visualLinkMask = tag.contains(SHAFT_LINK_MASK_TAG)
                ? tag.getInt(SHAFT_LINK_MASK_TAG) & (LINK_PREVIOUS | LINK_NEXT)
                : 0;
        visualLinkRefreshTicks = 2;
    }

    @Override
    public CompoundTag writeToNBT(CompoundTag tag) {
        super.writeToNBT(tag);
        tag.putInt(SHAFT_LINK_MASK_TAG, visualLinkMask);
        return tag;
    }

}
