package net.shadowmage.ancientwarfare.automation.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.shadowmage.ancientwarfare.automation.tile.warehouse2.TileWarehouseStockViewer;
import net.shadowmage.ancientwarfare.automation.tile.warehouse2.TileWarehouseStockViewer.WarehouseStockFilter;
import net.shadowmage.ancientwarfare.core.render.property.CoreProperties;

import java.util.List;

public class WarehouseStockViewerRenderer implements BlockEntityRenderer<TileWarehouseStockViewer> {

    public WarehouseStockViewerRenderer() {

    }

    @Override
    public void render(TileWarehouseStockViewer tile, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        Direction d = tile.getBlockState().getValue(CoreProperties.FACING).getOpposite();
        float r = getRotationFromDirection(d);

        poseStack.pushPose();
        poseStack.translate(0.5f, 1.f, 0.5f);//translate the point to the top-center of the block
        poseStack.mulPose(Axis.YP.rotationDegrees(r));//rotate for rotation
        poseStack.translate(0.5f, 0, 0.5f);//translate to top-left corner
        poseStack.translate(0, -0.125f, -0.127f);//move out and down for front-face of sign
        renderSignContents(tile, poseStack, buffer);

        poseStack.popPose();
    }

    float getRotationFromDirection(Direction d) {
        switch (d) {
            case NORTH:
                return 180.f;
            case SOUTH:
                return 0.f;
            case EAST:
                return 90.f;
            case WEST:
                return 270.f;
            default:
                return 0.f;
        }
    }

    /*
     * matrix should be setup so that 0,0 is upper-left-hand corner of the sign-board, with a
     * transformation of 1 being 1 BLOCK
     */

    private void renderSignContents(TileWarehouseStockViewer tile, PoseStack poseStack, MultiBufferSource buffer) {
        poseStack.pushPose();
        poseStack.translate(0, 0, -0.002f);//move out from block face slightly, for depth-buffer/z-fighting
        poseStack.scale(-1, -1, -1);//rescale for gui rendering axis flip
        poseStack.scale(0.0050f, 0.0050f, 0.0050f);//this scale puts it at 200 units(pixels) per block
        poseStack.scale(1f, 1f, 0.0001f);//squash Z axis for 'flat' rendering of 3d blocks/items..LOLS
        Font fr = Minecraft.getInstance().font;
        ItemStack filterItem;
        WarehouseStockFilter filter;
        String name = "";
        List<WarehouseStockFilter> filters = tile.getFilters();
        int max = filters.size();
        if (10 < max) {
            max = 10;
        }
        for (int i = 0; i < max; i++) {
            filter = filters.get(i);
            filterItem = filter.getFilterItem();
            if (!filterItem.isEmpty()) {
                renderItemIntoGui(filter.getFilterItem(), 0 + 24, i * 18 + 14, tile, poseStack, buffer);
            }
            name = filterItem.isEmpty() ? "Empty Filter" : filterItem.getHoverName().getString();
            if (name.length() > 20) {
                name = name.substring(0, 20);
            }
            drawString(fr, name, 20 + 12 + 12, i * 18 + 4 + 14, 0xffffffff, poseStack, buffer);
            name = String.valueOf(filter.getQuantity());
            drawString(fr, name, 200 - 25 - fr.width(name), i * 18 + 4 + 14, 0xffffffff, poseStack, buffer);
        }
        poseStack.popPose();
    }

    private void drawString(Font fr, String text, int x, int y, int color, PoseStack poseStack, MultiBufferSource buffer) {
        fr.drawInBatch(text, x, y, color, false, poseStack.last().pose(), buffer, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
    }

    private void renderItemIntoGui(ItemStack stack, int x, int y, TileWarehouseStockViewer tile, PoseStack poseStack, MultiBufferSource buffer) {
        poseStack.pushPose();
        poseStack.translate(x + 8, y + 8, 0);//center of the 16x16 gui slot
        poseStack.scale(16f, -16f, 16f);//gui item quads are unit-sized with +y up
        Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.GUI, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, poseStack, buffer, tile.getLevel(), 0);
        poseStack.popPose();
    }
}
