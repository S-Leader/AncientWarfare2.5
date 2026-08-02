package net.shadowmage.ancientwarfare.structure.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.ChestRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.shadowmage.ancientwarfare.structure.tile.TileAdvancedLootChest;

public class RenderAdvancedLootChest extends RenderLootInfo<TileAdvancedLootChest> {
    private ChestRenderer<TileAdvancedLootChest> chestRenderer = null;

    @Override
    public void render(TileAdvancedLootChest te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        super.render(te, x, y, z, partialTicks, destroyStage, alpha);

        if (te.getClass() == TileAdvancedLootChest.class) {
            Minecraft mc = Minecraft.getInstance();
            if (chestRenderer == null) {
                chestRenderer = new ChestRenderer<>(new BlockEntityRendererProvider.Context(mc.getBlockEntityRenderDispatcher(),
                        mc.getBlockRenderer(), mc.getItemRenderer(), mc.getEntityRenderDispatcher(), mc.getEntityModels(), mc.font));
            }
            BlockPos pos = te.getPos();
            PoseStack poseStack = getActivePoseStack();
            MultiBufferSource buffer = getActiveBufferSource();
            int packedLight = te.getLevel() != null ? LevelRenderer.getLightColor(te.getLevel(), pos) : LightTexture.FULL_BRIGHT;
            chestRenderer.render(te, partialTicks, poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);
        }
    }
}
