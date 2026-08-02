package net.shadowmage.ancientwarfare.core.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

/**
 * A lightweight cuboid renderer for Techne/Tabula models generated against 1.12.
 */
public final class LegacyModelRenderer {
    public float rotateAngleX, rotateAngleY, rotateAngleZ;
    public float rotationPointX, rotationPointY, rotationPointZ;
    public float offsetX, offsetY, offsetZ;
    public boolean mirror;
    public boolean showModel = true;

    private final LegacyModelBase owner;
    private final List<Box> boxes = new ArrayList<>();
    private final List<LegacyModelRenderer> children = new ArrayList<>();
    private int textureU, textureV, textureWidth = 64, textureHeight = 32;

    public LegacyModelRenderer(LegacyModelBase owner, String name) {
        this.owner = owner;
        //1.12 ModelRenderer copied the owner ModelBase texture size at construction.
        if (owner != null) {
            textureWidth = owner.textureWidth;
            textureHeight = owner.textureHeight;
        }
    }

    public LegacyModelRenderer(LegacyModelBase owner) {
        this(owner, "");
    }

    public LegacyModelRenderer(LegacyModelBase owner, int textureU, int textureV) {
        this(owner, "");
        setTextureOffset(textureU, textureV);
    }

    public LegacyModelRenderer setTextureOffset(int u, int v) {
        textureU = u;
        textureV = v;
        return this;
    }

    public LegacyModelRenderer setTextureSize(int width, int height) {
        textureWidth = width;
        textureHeight = height;
        return this;
    }

    public void setRotationPoint(float x, float y, float z) {
        rotationPointX = x;
        rotationPointY = y;
        rotationPointZ = z;
    }

    public void addChild(LegacyModelRenderer child) {
        children.add(child);
    }

    public LegacyModelRenderer addBox(float x, float y, float z, int width, int height, int depth) {
        boxes.add(new Box(x, y, z, width, height, depth, 0));
        return this;
    }

    public LegacyModelRenderer addBox(float x, float y, float z, int width, int height, int depth, float inflate) {
        boxes.add(new Box(x, y, z, width, height, depth, inflate));
        return this;
    }

    public void render(float scale) {
        LegacyModelBase.RenderContext context = LegacyModelBase.context();
        if (context == null || !showModel) return;
        PoseStack poses = context.poses();
        poses.pushPose();
        poses.translate(offsetX + rotationPointX * scale, offsetY + rotationPointY * scale, offsetZ + rotationPointZ * scale);
        if (rotateAngleZ != 0) poses.mulPose(Axis.ZP.rotation(rotateAngleZ));
        if (rotateAngleY != 0) poses.mulPose(Axis.YP.rotation(rotateAngleY));
        if (rotateAngleX != 0) poses.mulPose(Axis.XP.rotation(rotateAngleX));
        for (Box box : boxes)
            box.render(poses.last(), context.vertices(), context.light(), context.overlay(), scale, textureU, textureV, textureWidth, textureHeight, mirror);
        for (LegacyModelRenderer child : children) child.render(scale);
        poses.popPose();
    }

    private record Box(float x, float y, float z, float width, float height, float depth, float inflate) {
        void render(PoseStack.Pose pose, VertexConsumer out, int light, int overlay, float scale, int u, int v, int texW, int texH, boolean mirror) {
            float x0 = (x - inflate) * scale, y0 = (y - inflate) * scale, z0 = (z - inflate) * scale;
            float x1 = (x + width + inflate) * scale, y1 = (y + height + inflate) * scale, z1 = (z + depth + inflate) * scale;
            if (mirror) {
                float swap = x1;
                x1 = x0;
                x0 = swap;
            }

            // The 1.12 ModelBox texture is an unfolded cuboid, not one rectangle
            // repeated on every face. These offsets match ModelBox/ModelPart.Cube.
            float u0 = u;
            float u1 = u + depth;
            float u2 = u + depth + width;
            float u3 = u + depth + width + width;
            float u4 = u + depth + width + depth;
            float u5 = u + depth + width + depth + width;
            float v0 = v;
            float v1 = v + depth;
            float v2 = v + depth + height;

            polygon(pose, out, light, overlay, x1, y0, z1, x0, y0, z1, x0, y0, z0, x1, y0, z0, u1, v0, u2, v1, texW, texH, mirror, 0, -1, 0); // down
            polygon(pose, out, light, overlay, x1, y1, z0, x0, y1, z0, x0, y1, z1, x1, y1, z1, u2, v1, u3, v0, texW, texH, mirror, 0, 1, 0);  // up
            polygon(pose, out, light, overlay, x0, y0, z0, x0, y0, z1, x0, y1, z1, x0, y1, z0, u0, v1, u1, v2, texW, texH, mirror, -1, 0, 0); // west
            polygon(pose, out, light, overlay, x1, y0, z0, x0, y0, z0, x0, y1, z0, x1, y1, z0, u1, v1, u2, v2, texW, texH, mirror, 0, 0, -1); // north
            polygon(pose, out, light, overlay, x1, y0, z1, x1, y0, z0, x1, y1, z0, x1, y1, z1, u2, v1, u4, v2, texW, texH, mirror, 1, 0, 0);  // east
            polygon(pose, out, light, overlay, x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, y1, z1, u4, v1, u5, v2, texW, texH, mirror, 0, 0, 1);  // south
        }

        private static void polygon(PoseStack.Pose pose, VertexConsumer out, int light, int overlay,
                                    float ax, float ay, float az, float bx, float by, float bz,
                                    float cx, float cy, float cz, float dx, float dy, float dz,
                                    float u1, float v1, float u2, float v2, int texW, int texH,
                                    boolean mirror, float nx, float ny, float nz) {
            float au = u2 / texW, av = v1 / texH, bu = u1 / texW, bv = v1 / texH;
            float cu = u1 / texW, cv = v2 / texH, du = u2 / texW, dv = v2 / texH;
            if (mirror) {
                nx = -nx;
                vertex(pose, out, light, overlay, dx, dy, dz, du, dv, nx, ny, nz);
                vertex(pose, out, light, overlay, cx, cy, cz, cu, cv, nx, ny, nz);
                vertex(pose, out, light, overlay, bx, by, bz, bu, bv, nx, ny, nz);
                vertex(pose, out, light, overlay, ax, ay, az, au, av, nx, ny, nz);
            } else {
                vertex(pose, out, light, overlay, ax, ay, az, au, av, nx, ny, nz);
                vertex(pose, out, light, overlay, bx, by, bz, bu, bv, nx, ny, nz);
                vertex(pose, out, light, overlay, cx, cy, cz, cu, cv, nx, ny, nz);
                vertex(pose, out, light, overlay, dx, dy, dz, du, dv, nx, ny, nz);
            }
        }

        private static void vertex(PoseStack.Pose pose, VertexConsumer out, int light, int overlay, float x, float y, float z, float u, float v, float nx, float ny, float nz) {
            Matrix4f matrix = pose.pose();
            Matrix3f normal = pose.normal();
            out.vertex(matrix, x, y, z).color(255, 255, 255, 255).uv(u, v).overlayCoords(overlay).uv2(light).normal(normal, nx, ny, nz).endVertex();
        }
    }
}
