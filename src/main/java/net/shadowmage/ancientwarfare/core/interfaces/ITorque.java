package net.shadowmage.ancientwarfare.core.interfaces;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nullable;

public final class ITorque {
    private ITorque() {
    }//noop the class, it is just a container for the interfaces and static methods

    /*
     * Interface for implementation by torque tiles.  Tiles may handle their power internally by any means.<br>
     * Tiles are responsible for outputting their own power, but should not request power from other torque tiles
     * (the other tiles will output power when ready).<br>
     *
     * @author Shadowmage
     */
    public interface ITorqueTile {

        /*
         * Return the maximum amount of energy store-able in the passed in block side
         */
        double getMaxTorque(@Nullable Direction from);

        /*
         * Return the value of energy accessible from the passed in block side
         */
        double getTorqueStored(@Nullable Direction from);

        /*
         * Add energy to the specified block side, up to the specified amount.<br>
         * Return the value of energy actually added, or 0 for none.
         */
        double addTorque(@Nullable Direction from, double energy);

        /*
         * Remove energy from the specified block side, up to the specified amount.<br>
         * Return the value of energy actually removed, or 0 for none.
         */
        double drainTorque(Direction from, double energy);

        /*
         * Return the maximum amount of torque that the given side may output AT THIS TIME.<br>
         * Analogous to the 'simulate' actions from other energy frameworks
         */
        double getMaxTorqueOutput(Direction from);

        /*
         * Return the maximum amount of torque that the given side may accept AT THIS TIME.<br>
         * Analogous to the 'simulate' actions from other energy frameworks
         */
        double getMaxTorqueInput(@Nullable Direction from);

        /*
         * Return true if this tile can output torque from the given block side.<br>
         * Used by tiles for connection status.<br>
         * Must return the same value between calls, or issue a neighbor-block update when the value changes.<br>
         * You may return true from this method but return 0 for getMaxOutput() for 'toggleable' sides (side will connect but not always accept power)
         */
        boolean canOutputTorque(Direction from);

        /*
         * Return true if this tile can input torque into the given block side.<br>
         * Used by tiles for connection status.<br>
         * Must return the same value between calls, or issue a neighbor-block update when the value changes.
         * You may return true from this method but return 0 for getMaxInput() for 'toggleable' sides (side will connect but not always accept power)
         */
        boolean canInputTorque(Direction from);

        /*
         * Used by client for rendering of torque tiles.  If TRUE this tiles neighbor will
         * use this tiles output rotation values for rendering of the corresponding input side on the neighbor.
         */
        boolean useOutputRotation(@Nullable Direction from);

        /*
         * Return output shaft rotation for the given side.  Will only be called if useOutputRotation(from) returns true.
         */
        float getClientOutputRotation(Direction from, float delta);
    }

    /*
     * default (simple) reference implementation of a torque delegate class<br>
     * An ITorqueTile may have one or more of these for internal energy storage (or none, and handle energy entirely differently!).<br>
     * This template class is merely included for convenience.
     *
     * @author Shadowmage
     */
    public static class TorqueCell {
        double maxInput;
        double maxOutput;
        double maxEnergy;
        double efficiency;
        protected double energy;

        public TorqueCell(double in, double out, double max, double eff) {
            maxInput = in;
            maxOutput = out;
            maxEnergy = max;
            efficiency = eff;
        }

        public double getEfficiency() {
            return efficiency;
        }

        public double getEnergy() {
            return energy;
        }

        public double getMaxEnergy() {
            return maxEnergy;
        }

        public void setEnergy(double in) {
            energy = Math.max(0, Math.min(in, maxEnergy));
        }

        public double addEnergy(double in) {
            if (!Double.isFinite(in) || in < 0) {
                throw new IllegalArgumentException("Requested input must be a finite, non-negative value");
            }
            in = Math.min(getMaxTickInput(), in);
            setEnergy(energy + in);
            return in;
        }

        public double drainEnergy(double request) {
            if (!Double.isFinite(request) || request < 0) {
                throw new IllegalArgumentException("Requested drain must be a finite, non-negative value");
            }
            request = Math.min(getMaxTickOutput(), request);
            setEnergy(energy - request);
            return request;
        }

        public double getMaxInput() {
            return maxInput;
        }

        public double getMaxOutput() {
            return maxOutput;
        }

        public double getMaxTickInput() {
            return Math.min(maxInput, getMaxEnergy() - getEnergy());
        }

        public double getMaxTickOutput() {
            return Math.min(maxOutput, getEnergy());
        }

        public CompoundTag writeToNBT(CompoundTag tag) {
            tag.putDouble("energy", energy);
            return tag;
        }

        public void readFromNBT(CompoundTag tag) {
            energy = tag.getDouble("energy");
        }

        public double getPercentFull() {
            return maxEnergy > 0.d ? energy / maxEnergy : 0.d;
        }
    }

    /*
     * Owner-sided aware torque storage cell.  Used by MIMO type torque tiles (conduit, distributor) to maintain a cell for each side
     * without having to create new cells every time block orientation changes.<br>
     * Caveat:  the side maintains energy level regardless of block orientation, the new 'input' side will have the energy from an old 'output' side.
     *
     * @author Shadowmage
     */
    public static class SidedTorqueCell extends TorqueCell {

        Direction dir;
        ITorqueTile owner;

        public SidedTorqueCell(double in, double out, double max, double eff, Direction dir, ITorqueTile owner) {
            super(in, out, max, eff);
            this.dir = dir;
            this.owner = owner;
        }

        @Override
        public double getMaxTickInput() {
            return owner.canInputTorque(dir) ? super.getMaxTickInput() : 0;
        }

        @Override
        public double getMaxTickOutput() {
            return owner.canOutputTorque(dir) ? super.getMaxTickOutput() : 0;
        }

        @Override
        public double addEnergy(double in) {
            return owner.canInputTorque(dir) ? super.addEnergy(in) : 0;
        }

        @Override
        public double drainEnergy(double request) {
            return owner.canOutputTorque(dir) ? super.drainEnergy(request) : 0;
        }

    }

}
