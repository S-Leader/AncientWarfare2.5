package net.shadowmage.ancientwarfare.structure.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.registries.ForgeRegistries;
import net.shadowmage.ancientwarfare.core.compat.client.LegacyBlockEntityRenderer;
import net.shadowmage.ancientwarfare.structure.tile.ISpecialLootContainer;
import net.shadowmage.ancientwarfare.structure.tile.LootSettings;
import org.joml.Matrix4f;

import java.util.Optional;

public class RenderLootInfo<T extends BlockEntity & ISpecialLootContainer> extends LegacyBlockEntityRenderer<T> {
    @Override
    public void render(T te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        Minecraft mc = Minecraft.getInstance();
        HitResult hitResult = mc.hitResult;
        if ((mc.player.getAbilities().instabuild || mc.gameMode.getPlayerMode() == GameType.SPECTATOR)
                && hitResult instanceof BlockHitResult && te.getPos().equals(((BlockHitResult) hitResult).getBlockPos())) {
            //nameplates render fullbright which replaces the 1.12 setLightmapDisabled(true/false) wrapping
            drawLootInfo(te, getNameplateOffsetX(te, x), y, getNameplateOffsetZ(te, z));
            te.getLootSettings().getLootTableName().ifPresent(lt -> {
            });
        }
    }

    private void drawLootInfo(T te, double x, double y, double z) {
        float f = Minecraft.getInstance().gameRenderer.getMainCamera().getYRot();
        float f1 = Minecraft.getInstance().gameRenderer.getMainCamera().getXRot();

        LootSettings lootSettings = te.getLootSettings();
        float verticalOffset = 0;

        if (lootSettings.getSpawnEntity()) {
            renderString(te, (float) x, (float) y + verticalOffset, (float) z, f, f1, lootSettings.getEntity().toString());
            verticalOffset += 0.3;
        }

        if (lootSettings.getSplashPotion()) {
            for (MobEffectInstance effect : lootSettings.getEffects()) {
                //noinspection ConstantConditions
                renderString(te, (float) x, (float) y + verticalOffset, (float) z, f, f1,
                        ForgeRegistries.MOB_EFFECTS.getKey(effect.getEffect()).toString() + " " + effect.getAmplifier() + " "
                                + effect.getDuration() / 1200 + ":" + (effect.getDuration() % 1200) / 20);
                verticalOffset += 0.3;
            }
        }

        if (lootSettings.hasLoot()) {
            Optional<ResourceLocation> lt = lootSettings.getLootTableName();
            if (lt.isPresent()) {
                String str = te.getLootSettings().getLootRolls() + " x " + lt.get().toString();
                renderString(te, (float) x, (float) y + verticalOffset, (float) z, f, f1, str);
                verticalOffset += 0.3;
            }
        }

        if (lootSettings.hasMessage()) {
            renderString(te, (float) x, (float) y + verticalOffset, (float) z, f, f1, "Message: \"" + lootSettings.getPlayerMessage() + "\"");
        }
    }

    /**
     * Replacement for the removed 1.12 EntityRenderer.drawNameplate helper.
     */
    private void renderString(T te, float x, float y, float z, float f, float f1, String str) {
        Minecraft mc = Minecraft.getInstance();
        PoseStack poseStack = getActivePoseStack();
        poseStack.pushPose();
        poseStack.translate(x + 0.5F, y + 1.5F, z + 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(-f));
        poseStack.mulPose(Axis.XP.rotationDegrees(f1));
        poseStack.scale(-0.025F, -0.025F, 0.025F);
        Matrix4f matrix = poseStack.last().pose();
        Font font = getFontRenderer();
        float xStart = -font.width(str) / 2f;
        MultiBufferSource buffer = getActiveBufferSource();
        int backgroundColor = (int) (0.25F * 255.0F) << 24;
        //see-through pass with background replaces the 1.12 depth-disabled draw + background quad, normal pass matches the depth-enabled draw
        font.drawInBatch(str, xStart, 0, 0x20FFFFFF, false, matrix, buffer, Font.DisplayMode.SEE_THROUGH, backgroundColor, LightTexture.FULL_BRIGHT);
        font.drawInBatch(str, xStart, 0, 0xFFFFFFFF, false, matrix, buffer, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
        poseStack.popPose();
    }

    protected double getNameplateOffsetZ(T te, double z) {
        return z;
    }

    protected double getNameplateOffsetX(T te, double x) {
        return x;
    }
}
