package net.shadowmage.ancientwarfare.core.compat.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.GameRenderer;
import org.lwjgl.opengl.GL11;

/**
 * Narrow compatibility facade for the fixed-function calls used by AW2's
 * legacy renderers. Matrix operations are applied to Minecraft's model-view
 * stack; state that still exists is routed through RenderSystem.
 */
public final class GlStateManager {
    private GlStateManager() {
    }

    public static void pushMatrix() {
        PoseStack pose = LegacyRenderContext.pose();
        if (pose != null) {
            pose.pushPose();
            return;
        }
        modelView().pushPose();
        RenderSystem.applyModelViewMatrix();
    }

    public static void popMatrix() {
        PoseStack pose = LegacyRenderContext.pose();
        if (pose != null) {
            pose.popPose();
            return;
        }
        modelView().popPose();
        RenderSystem.applyModelViewMatrix();
    }

    public static void translate(double x, double y, double z) {
        PoseStack pose = LegacyRenderContext.pose();
        if (pose != null) {
            pose.translate(x, y, z);
            return;
        }
        modelView().translate(x, y, z);
        RenderSystem.applyModelViewMatrix();
    }

    public static void rotate(float angleDegrees, float x, float y, float z) {
        PoseStack pose = LegacyRenderContext.pose();
        if (pose != null) {
            pose.mulPose(Axis.of(new org.joml.Vector3f(x, y, z)).rotationDegrees(angleDegrees));
            return;
        }
        modelView().mulPose(Axis.of(new org.joml.Vector3f(x, y, z)).rotationDegrees(angleDegrees));
        RenderSystem.applyModelViewMatrix();
    }

    public static void scale(double x, double y, double z) {
        PoseStack pose = LegacyRenderContext.pose();
        if (pose != null) {
            pose.scale((float) x, (float) y, (float) z);
            return;
        }
        modelView().scale((float) x, (float) y, (float) z);
        RenderSystem.applyModelViewMatrix();
    }

    public static void color(float red, float green, float blue, float alpha) {
        RenderSystem.setShaderColor(red, green, blue, alpha);
    }

    public static void enableBlend() {
        RenderSystem.enableBlend();
    }

    public static void disableBlend() {
        RenderSystem.disableBlend();
    }

    public static void blendFunc(int source, int destination) {
        RenderSystem.blendFunc(source, destination);
    }

    public static void blendFunc(SourceFactor source, DestFactor destination) {
        RenderSystem.blendFunc(source.value, destination.value);
    }

    public static void tryBlendFuncSeparate(SourceFactor sourceRgb, DestFactor destinationRgb,
                                            SourceFactor sourceAlpha, DestFactor destinationAlpha) {
        RenderSystem.blendFuncSeparate(sourceRgb.value, destinationRgb.value, sourceAlpha.value, destinationAlpha.value);
    }

    public static void enableDepth() {
        RenderSystem.enableDepthTest();
    }

    public static void disableDepth() {
        RenderSystem.disableDepthTest();
    }

    public static void depthMask(boolean enabled) {
        RenderSystem.depthMask(enabled);
    }

    public static void enableCull() {
        RenderSystem.enableCull();
    }

    public static void disableCull() {
        RenderSystem.disableCull();
    }

    public static void setActiveTexture(int texture) {
        RenderSystem.activeTexture(texture);
    }

    public static void glLineWidth(float width) {
        RenderSystem.lineWidth(width);
    }

    /*
     * Immediate-mode emulation. Raw GL11 glBegin/glVertex/glEnd do not exist in
     * the core-profile context 1.20 uses, so vertices are buffered through the
     * Tesselator and drawn with the untextured position shader, which is tinted
     * by the shader color that color() sets (matching legacy glColor behavior).
     */
    private static BufferBuilder immediateBuffer;
    private static int immediateMode;
    private static int immediateVertexCount;
    private static float immediateFirstX;
    private static float immediateFirstY;
    private static float immediateFirstZ;

    public static void glBegin(int mode) {
        RenderSystem.setShader(GameRenderer::getPositionShader);
        immediateMode = mode;
        immediateVertexCount = 0;
        immediateBuffer = Tesselator.getInstance().getBuilder();
        immediateBuffer.begin(vertexMode(mode), DefaultVertexFormat.POSITION);
    }

    public static void glVertex2f(float x, float y) {
        glVertex3f(x, y, 0.0F);
    }

    public static void glVertex2d(double x, double y) {
        glVertex3f((float) x, (float) y, 0.0F);
    }

    public static void glVertex3d(double x, double y, double z) {
        glVertex3f((float) x, (float) y, (float) z);
    }

    public static void glVertex3f(float x, float y, float z) {
        BufferBuilder buffer = immediateBuffer;
        if (buffer == null) {
            return;
        }
        if (immediateVertexCount == 0) {
            immediateFirstX = x;
            immediateFirstY = y;
            immediateFirstZ = z;
        }
        emitVertex(buffer, x, y, z);
        immediateVertexCount++;
    }

    public static void glEnd() {
        BufferBuilder buffer = immediateBuffer;
        immediateBuffer = null;
        if (buffer == null) {
            return;
        }
        if (immediateMode == GL11.GL_LINE_LOOP && immediateVertexCount > 2) {
            // DEBUG_LINE_STRIP has no implicit closing segment; emit it manually.
            emitVertex(buffer, immediateFirstX, immediateFirstY, immediateFirstZ);
        }
        if (immediateVertexCount == 0) {
            buffer.discard();
            return;
        }
        BufferUploader.drawWithShader(buffer.end());
    }

    private static void emitVertex(BufferBuilder buffer, float x, float y, float z) {
        PoseStack pose = LegacyRenderContext.pose();
        if (pose != null) {
            buffer.vertex(pose.last().pose(), x, y, z).endVertex();
        } else {
            buffer.vertex(x, y, z).endVertex();
        }
    }

    private static VertexFormat.Mode vertexMode(int glMode) {
        switch (glMode) {
            case GL11.GL_LINES:
                return VertexFormat.Mode.DEBUG_LINES;
            case GL11.GL_LINE_STRIP:
            case GL11.GL_LINE_LOOP:
                return VertexFormat.Mode.DEBUG_LINE_STRIP;
            case GL11.GL_TRIANGLES:
                return VertexFormat.Mode.TRIANGLES;
            case GL11.GL_TRIANGLE_STRIP:
                return VertexFormat.Mode.TRIANGLE_STRIP;
            case GL11.GL_TRIANGLE_FAN:
            case GL11.GL_POLYGON:
                return VertexFormat.Mode.TRIANGLE_FAN;
            case GL11.GL_QUADS:
            default:
                return VertexFormat.Mode.QUADS;
        }
    }

    public static void glNormal3f(float x, float y, float z) {
        // No-op: the position shader used by the immediate-mode emulation has no
        // normal attribute and fixed-function lighting no longer exists.
    }

    public static void shadeModel(int mode) {
        // No-op: GL_FLAT/GL_SMOOTH shade model is fixed-function state removed in
        // the core profile; interpolation is defined by the active shader.
    }

    public static void popAttrib() {
        // No-op: the GL attribute stack (glPushAttrib/glPopAttrib) was removed in
        // the core profile; state is managed explicitly through RenderSystem.
    }

    public static void enableTexture2D() {
        // No-op: GL_TEXTURE_2D enable state is fixed-function. Whether a draw is
        // textured is decided by the shader bound via RenderSystem.setShader.
    }

    public static void disableTexture2D() {
        // No-op: untextured drawing comes from the position shader selected in
        // glBegin(), not from disabling GL_TEXTURE_2D (invalid in core profile).
    }

    public static void enableLighting() {
        // Modern Minecraft shaders own lighting state.
    }

    public static void disableLighting() {
        // Modern Minecraft shaders own lighting state.
    }

    public static void enableRescaleNormal() {
        // Normal transformation is shader-driven in 1.20.1.
    }

    public static void disableRescaleNormal() {
        // Normal transformation is shader-driven in 1.20.1.
    }

    public static void enableAlpha() {
        // Alpha testing is represented by RenderType in modern rendering.
    }

    public static void disableAlpha() {
        // Alpha testing is represented by RenderType in modern rendering.
    }

    public static void enableColorMaterial() {
        // Shader color replaces fixed-function color material.
    }

    public static void enableBlendProfile(Profile profile) {
        enableBlend();
        RenderSystem.defaultBlendFunc();
    }

    private static PoseStack modelView() {
        return RenderSystem.getModelViewStack();
    }

    public enum SourceFactor {
        ZERO(GL11.GL_ZERO),
        ONE(GL11.GL_ONE),
        SRC_ALPHA(GL11.GL_SRC_ALPHA);

        private final int value;

        SourceFactor(int value) {
            this.value = value;
        }
    }

    public enum DestFactor {
        ZERO(GL11.GL_ZERO),
        ONE(GL11.GL_ONE),
        ONE_MINUS_SRC_ALPHA(GL11.GL_ONE_MINUS_SRC_ALPHA);

        private final int value;

        DestFactor(int value) {
            this.value = value;
        }
    }

    public enum Profile {
        PLAYER_SKIN
    }
}
