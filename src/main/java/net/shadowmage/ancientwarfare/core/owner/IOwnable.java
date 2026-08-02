package net.shadowmage.ancientwarfare.core.owner;

import net.minecraft.world.entity.player.Player;

/*
 * Tile entities/Entities that are owned by a player -- called by spawning/placing items to set owner
 *
 * @author Shadowmage
 */
public interface IOwnable {
    void setOwner(Player player);

    void setOwner(Owner owner);

    Owner getOwner();

    boolean isOwner(Player player);
}
