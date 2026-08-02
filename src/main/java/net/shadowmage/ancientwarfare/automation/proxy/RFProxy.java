package net.shadowmage.ancientwarfare.automation.proxy;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.shadowmage.ancientwarfare.automation.AncientWarfareAutomation;
import net.shadowmage.ancientwarfare.core.interfaces.ITorque.ITorqueTile;

/**
 * Compatibility facade retained under its old name. In 1.20.1 all energy
 * integration is provided by Forge Energy, so no optional CoFH API is needed.
 */
public class RFProxy {
    public static RFProxy instance = new RFProxyActual();

    public static void loadInstance() {
        instance = new RFProxyActual();
        AncientWarfareAutomation.LOG.info("Forge Energy compatibility loaded successfully");
    }

    protected RFProxy() {
    }

    public boolean isRFTile(BlockEntity tile) {
        return false;
    }

    public double transferPower(ITorqueTile generator, Direction from, BlockEntity target) {
        return 0.0D;
    }
}
