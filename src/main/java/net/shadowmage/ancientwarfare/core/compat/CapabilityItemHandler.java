package net.shadowmage.ancientwarfare.core.compat;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;

/**
 * Legacy field name backed by Forge's modern item-handler capability.
 */
public final class CapabilityItemHandler {
    private CapabilityItemHandler() {
    }

    public static final Capability<IItemHandler> ITEM_HANDLER_CAPABILITY = ForgeCapabilities.ITEM_HANDLER;
}
