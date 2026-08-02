package net.shadowmage.ancientwarfare.core.interfaces;

import net.minecraft.nbt.CompoundTag;

/*
 * blind entity packet handling
 * should be implemented by any entity that is a target of
 * network packets
 *
 * @author Shadowmage
 */
public interface IEntityPacketHandler {

    public void handlePacketData(CompoundTag tag);

}
