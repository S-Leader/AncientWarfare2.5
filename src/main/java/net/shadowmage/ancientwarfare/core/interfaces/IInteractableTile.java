package net.shadowmage.ancientwarfare.core.interfaces;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;

public interface IInteractableTile {
    boolean onBlockClicked(Player player, @Nullable InteractionHand hand);
}
