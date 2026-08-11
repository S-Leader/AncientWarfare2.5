package net.shadowmage.ancientwarfare.automation.tile.torque;

import net.minecraft.core.Direction;
import net.shadowmage.ancientwarfare.automation.config.AWAutomationStatics;
import net.shadowmage.ancientwarfare.core.interfaces.ITorque.ITorqueTile;
import net.shadowmage.ancientwarfare.core.interfaces.ITorque.TorqueCell;
import net.shadowmage.ancientwarfare.core.util.Trig;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

public abstract class TileTorqueShaft extends TileTorqueSingleCell {

    private TileTorqueShaft prev, next;

    private boolean prevNeighborInvalid = true;
    private boolean nextNeighborInvalid = true;

    public TileTorqueShaft() {
        double max = getMaxTransfer();
        torqueCell = new TorqueCell(max, max, max, getEfficiency());
    }

    protected abstract double getEfficiency();

    protected abstract double getMaxTransfer();

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
        return findPreviousShaft() != null;
    }

    public boolean hasNextShaft() {
        return findNextShaft() != null;
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

}
