package net.shadowmage.ancientwarfare.structure.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.level.GameType;
import net.shadowmage.ancientwarfare.core.compat.client.LegacyBlockEntityRenderer;
import net.shadowmage.ancientwarfare.structure.tile.SpawnerSettings;
import net.shadowmage.ancientwarfare.structure.tile.TileAdvancedSpawner;
import org.joml.Matrix4f;

import java.util.List;

public class RenderAdvancedSpawner extends LegacyBlockEntityRenderer<TileAdvancedSpawner> {
    @Override
    public void render(TileAdvancedSpawner te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        Minecraft mc = Minecraft.getInstance();
        if (!mc.player.isCreative() && mc.gameMode.getPlayerMode() != GameType.SPECTATOR) {
            return;
        }

        List<SpawnerSettings.EntitySpawnGroup> spawnGroups = te.getSettings().getSpawnGroups();
        if (spawnGroups.isEmpty()) {
            return;
        }
        List<SpawnerSettings.EntitySpawnSettings> spawnSettings = spawnGroups.get(0).getEntitiesToSpawn();
        if (spawnSettings.isEmpty()) {
            return;
        }
        String string = spawnSettings.get(0).getCustomName().orElse(I18n.get(spawnSettings.get(0).getEntityName()));

        Font fontrenderer = mc.font;
        float f = 1.6F;
        float f1 = 0.016666668F * f;
        PoseStack poseStack = getActivePoseStack();
        poseStack.pushPose();
        poseStack.translate(0.5F, 1.5F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(-mc.gameRenderer.getMainCamera().getYRot()));
        poseStack.mulPose(Axis.XP.rotationDegrees(mc.gameRenderer.getMainCamera().getXRot()));
        poseStack.scale(-f1, -f1, f1);
        Matrix4f matrix = poseStack.last().pose();
        float xStart = -fontrenderer.width(string) / 2f;
        MultiBufferSource buffer = getActiveBufferSource();
        int backgroundColor = (int) (0.25F * 255.0F) << 24;
        //see-through pass with background replaces the 1.12 depth-disabled draw + background quad, normal pass matches the depth-enabled draw
        fontrenderer.drawInBatch(string, xStart, 0, 0x20ffffff, false, matrix, buffer, Font.DisplayMode.SEE_THROUGH, backgroundColor, LightTexture.FULL_BRIGHT);
        fontrenderer.drawInBatch(string, xStart, 0, 0xffffffff, false, matrix, buffer, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
        poseStack.popPose();
    }
}
