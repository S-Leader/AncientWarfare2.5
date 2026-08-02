package net.shadowmage.ancientwarfare.structure.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.shadowmage.ancientwarfare.core.gamedata.AWGameData;
import net.shadowmage.ancientwarfare.structure.AncientWarfareStructure;
import net.shadowmage.ancientwarfare.structure.event.IBoxRenderer;
import net.shadowmage.ancientwarfare.structure.gamedata.StructureEntry;
import net.shadowmage.ancientwarfare.structure.gamedata.StructureMap;
import net.shadowmage.ancientwarfare.structure.template.build.StructureBB;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;

public class StructureEntryBBRenderer {
    public static final String SHOW_BBS_TAG = AncientWarfareStructure.MOD_ID + ":showBBs";
    private static final int BB_RENDER_RANGE = 200;

    @SubscribeEvent
    public void handleRenderLastEvent(RenderLevelStageEvent evt) {
        if (evt.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        renderStructureBoundingBoxes(evt, mc);
    }

    private void renderStructureBoundingBoxes(RenderLevelStageEvent evt, Minecraft mc) {
        LocalPlayer player = mc.player;
        if (!player.getTags().contains(SHOW_BBS_TAG)) {
            return;
        }
        ClientLevel world = mc.level;

        StructureMap map = AWGameData.INSTANCE.getPerWorldData(world, StructureMap.class);
        Collection<StructureEntry> structuresNear = map.getEntriesNear(world, player.blockPosition().getX(), player.blockPosition().getZ(), BB_RENDER_RANGE / 16, true, new ArrayList<>());
        for (StructureEntry structure : structuresNear) {
            StructureBB bb = structure.getBB();
            if (bb.getCenter().distSqr(player.blockPosition()) < (BB_RENDER_RANGE * BB_RENDER_RANGE)) {
                IBoxRenderer.Util.renderBoundingBox(player, bb.min, bb.max, evt.getPartialTick(), Color.BLUE);
            }
        }
    }
}
