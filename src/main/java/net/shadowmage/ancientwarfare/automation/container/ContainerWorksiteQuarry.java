package net.shadowmage.ancientwarfare.automation.container;

import net.minecraft.world.entity.player.Player;

public class ContainerWorksiteQuarry extends ContainerWorksiteBase {

    public ContainerWorksiteQuarry(Player player, int x, int y, int z) {
        super(player, x, y, z);

        int layerY = 78;

        playerLabel = layerY;
        layerY += LABEL_GAP;
        guiHeight = addPlayerSlots(layerY) + 8;
    }

}
