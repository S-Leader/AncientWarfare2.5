package net.shadowmage.ancientwarfare.structure.event;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.core.util.RenderTools;

import java.awt.*;

/*
 * Created by Olivier on 05/02/2015.
 */
public interface IBoxRenderer {
    void renderBox(Player player, InteractionHand hand, ItemStack itemStack, float partialTick);

    final class Util {
        private Util() {
        }

        @OnlyIn(Dist.CLIENT)
        public static void renderBoundingBoxTopSide(Player player, BlockPos min, BlockPos max, float delta, Color color) {
            AABB bb = new AABB(min.getX(), min.getY(), min.getZ(), max.getX() + 1, max.getY() + 1, max.getZ() + 1);
            bb = RenderTools.adjustBBForPlayerPos(bb, player, delta);
            RenderTools.drawTopSideOverlay(bb, color);
        }

        @OnlyIn(Dist.CLIENT)
        public static void renderBoundingBox(Player player, BlockPos min, BlockPos max, float delta, Color color) {
            AABB bb = new AABB(min.getX(), min.getY(), min.getZ(), max.getX() + 1, max.getY() + 1, max.getZ() + 1);
            bb = RenderTools.adjustBBForPlayerPos(bb, player, delta);
            RenderTools.drawOutlinedBoundingBox(bb, color);
        }

        @OnlyIn(Dist.CLIENT)
        public static void renderBoundingBox(Player player, BlockPos min, BlockPos max, float delta) {
            AABB bb = new AABB(min.getX(), min.getY(), min.getZ(), max.getX() + 1, max.getY() + 1, max.getZ() + 1);
            bb = RenderTools.adjustBBForPlayerPos(bb, player, delta);
            RenderTools.drawOutlinedBoundingBox(bb, 1.f, 1.f, 1.f);
        }

        @OnlyIn(Dist.CLIENT)
        public static void renderBoundingBox(Player player, BlockPos min, BlockPos max, float delta, float r, float g, float b) {
            AABB bb = new AABB(min.getX(), min.getY(), min.getZ(), max.getX() + 1, max.getY() + 1, max.getZ() + 1);
            bb = RenderTools.adjustBBForPlayerPos(bb, player, delta);
            RenderTools.drawOutlinedBoundingBox(bb, r, g, b);
        }
    }
}
