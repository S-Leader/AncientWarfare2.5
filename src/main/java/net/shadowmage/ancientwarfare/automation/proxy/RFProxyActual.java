package net.shadowmage.ancientwarfare.automation.proxy;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;
import net.shadowmage.ancientwarfare.automation.config.AWAutomationStatics;
import net.shadowmage.ancientwarfare.core.interfaces.ITorque.ITorqueTile;

/**
 * Forge Energy bridge for the legacy torque network.
 */
public class RFProxyActual extends RFProxy {
    protected RFProxyActual() {
    }

    @Override
    public boolean isRFTile(BlockEntity tile) {
        if (tile == null) {
            return false;
        }
        for (Direction side : Direction.values()) {
            if (tile.getCapability(ForgeCapabilities.ENERGY, side).isPresent()) {
                return true;
            }
        }
        return tile.getCapability(ForgeCapabilities.ENERGY).isPresent();
    }

    @Override
    public double transferPower(ITorqueTile generator, Direction from, BlockEntity target) {
        if (target == null) {
            return 0.0D;
        }
        IEnergyStorage storage = target.getCapability(ForgeCapabilities.ENERGY, from.getOpposite()).orElse(null);
        if (storage == null || !storage.canReceive()) {
            return 0.0D;
        }
        int offered = (int) Math.floor(generator.getMaxTorqueOutput(from) * AWAutomationStatics.torqueToRf);
        if (offered <= 0) {
            return 0.0D;
        }
        int accepted = storage.receiveEnergy(offered, false);
        return generator.drainTorque(from, accepted * AWAutomationStatics.rfToTorque);
    }
}
