package net.shadowmage.ancientwarfare.core.container;

import net.minecraft.world.entity.player.Player;

public class ContainerResearchBook extends ContainerBase {

    public ContainerResearchBook(Player player, int x, int y, int z) {
        super(player);
        addPlayerSlots();
        removeSlots();
    }

}
