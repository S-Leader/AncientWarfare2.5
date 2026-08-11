package net.shadowmage.ancientwarfare.structure.render;

import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.shadowmage.ancientwarfare.structure.event.IBoxRenderer;

@OnlyIn(Dist.CLIENT)
public class StructureBoundingBoxRenderer {
    @SuppressWarnings("unused")
    @SubscribeEvent
    public void handleRenderLastEvent(RenderLevelStageEvent evt) {
        if (evt.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) {
            return;
        }

        /*
         * Keep client-only Camera/PoseStack types out of IBoxRenderer's common
         * interface while still giving structure preview rendering the exact
         * matrices from this RenderLevelStageEvent.
         */
        PreviewRenderer.withLevelRenderContext(evt.getPoseStack(), evt.getCamera(), () -> {
            for (InteractionHand hand : InteractionHand.values()) {
                ItemStack stack = player.getItemInHand(hand);
                if (stack.isEmpty()) {
                    continue;
                }
                Item item = stack.getItem();
                if (item instanceof IBoxRenderer renderer) {
                    renderer.renderBox(player, hand, stack, evt.getPartialTick());
                }
            }
        });
    }
}
