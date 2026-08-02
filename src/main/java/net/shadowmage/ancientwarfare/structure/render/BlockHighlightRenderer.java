package net.shadowmage.ancientwarfare.structure.render;

import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.shadowmage.ancientwarfare.core.util.RenderTools;
import net.shadowmage.ancientwarfare.structure.util.BlockHighlightInfo;

import java.awt.*;

public class BlockHighlightRenderer {
    private static BlockHighlightInfo blockHighlightInfo = BlockHighlightInfo.EXPIRED;

    public static void setBlockHighlightInfo(BlockHighlightInfo blockHighlightInfo) {
        BlockHighlightRenderer.blockHighlightInfo = blockHighlightInfo;
    }

    @SuppressWarnings("unused")
    @SubscribeEvent
    public void handleRenderLastEvent(RenderLevelStageEvent evt) {
        if (evt.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null && mc.level.getGameTime() < blockHighlightInfo.getExpirationTime()) {
            AABB bb = new AABB(blockHighlightInfo.getPos()).expandTowards(0.1, 0.1, 0.1);
            bb = RenderTools.adjustBBForPlayerPos(bb, Minecraft.getInstance().player, evt.getPartialTick());
            RenderTools.drawOutlinedBoundingBox(bb, Color.WHITE, true);
        }
    }
}
