/*
 Copyright 2012 John Cummens (aka Shadowmage, Shadowmage4513)
 This software is distributed under the terms of the GNU General Public License.
 Please see COPYING for precise license information.

 This file is part of Ancient Warfare.

 Ancient Warfare is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 Ancient Warfare is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with Ancient Warfare.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.shadowmage.ancientwarfare.structure.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.shadowmage.ancientwarfare.core.compat.client.GlStateManager;
import net.shadowmage.ancientwarfare.core.compat.client.Render;
import net.shadowmage.ancientwarfare.structure.entity.DualBoundingBox;
import net.shadowmage.ancientwarfare.structure.entity.EntityGate;
import net.shadowmage.ancientwarfare.structure.render.gate.RenderGateBasic;
import net.shadowmage.ancientwarfare.structure.render.gate.RenderGateDouble;
import net.shadowmage.ancientwarfare.structure.render.gate.RenderGateRotatingBridge;
import net.shadowmage.ancientwarfare.structure.render.gate.RenderGateSingle;

import java.util.HashMap;

public final class RenderGateHelper extends Render<EntityGate> {

    private HashMap<Integer, Render> gateRenders = new HashMap<>();

    public RenderGateHelper(EntityRendererProvider.Context renderManager) {
        super(renderManager);
        this.addGateRender(0, new RenderGateBasic(renderManager));
        this.addGateRender(1, new RenderGateBasic(renderManager));
        this.addGateRender(4, new RenderGateSingle(renderManager));
        this.addGateRender(5, new RenderGateSingle(renderManager));
        this.addGateRender(8, new RenderGateDouble(renderManager));
        this.addGateRender(9, new RenderGateDouble(renderManager));
        this.addGateRender(12, new RenderGateRotatingBridge(renderManager));
    }

    protected void addGateRender(int type, Render rend) {
        this.gateRenders.put(type, rend);
    }

    @Override
    public void doRender(EntityGate gate, double d0, double d1, double d2, float f, float f1) {
        GlStateManager.pushMatrix();
        if (Minecraft.getInstance().getEntityRenderDispatcher().shouldRenderHitBoxes() && !gate.isInvisible()) {
            double x = d0 - gate.xOld, y = d1 - gate.yOld, z = d2 - gate.zOld;
            // Entity gates use their three-part collision box; other entities render one regular box.
            if (gate.edgePosition > 0 && gate.getBoundingBox() instanceof DualBoundingBox) {
                renderOffsetAABB(((DualBoundingBox) gate.getBoundingBox()).getTop(), x, y, z);
                renderOffsetAABB(((DualBoundingBox) gate.getBoundingBox()).getMin(), x, y, z);
                renderOffsetAABB(((DualBoundingBox) gate.getBoundingBox()).getMax(), x, y, z);
            } else
                renderOffsetAABB(gate.getBoundingBox(), x, y, z);

            GlStateManager.popMatrix();
            return;
        }

        if (gate.hurtAnimationTicks > 0) {
            float percent = ((float) gate.hurtAnimationTicks / 20.f);
            GlStateManager.color(1.f, 1.f - percent, 1.f - percent, 1.f);
        }
        this.bindEntityTexture(gate);
        GlStateManager.translate(d0, d1, d2);
        GlStateManager.rotate(f, 0, 1, 0);
        GlStateManager.scale(-1, -1, 1);
        this.gateRenders.get(gate.getGateType().getGlobalID()).doRender(gate, d0, d1, d2, f, f1);
        GlStateManager.color(1.f, 1.f, 1.f, 1.f);
        GlStateManager.popMatrix();
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityGate entity) {
        return entity.getGateType().getTexture();
    }

    /**
     * Replacement for the removed 1.12 Render.renderOffsetAABB debug helper - draws a solid white box.
     */
    private static void renderOffsetAABB(AABB boundingBox, double x, double y, double z) {
        AABB b = boundingBox.move(x, y, z);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        vertex(buffer, b.minX, b.maxY, b.minZ);
        vertex(buffer, b.maxX, b.maxY, b.minZ);
        vertex(buffer, b.maxX, b.minY, b.minZ);
        vertex(buffer, b.minX, b.minY, b.minZ);
        vertex(buffer, b.minX, b.minY, b.maxZ);
        vertex(buffer, b.maxX, b.minY, b.maxZ);
        vertex(buffer, b.maxX, b.maxY, b.maxZ);
        vertex(buffer, b.minX, b.maxY, b.maxZ);
        vertex(buffer, b.minX, b.minY, b.minZ);
        vertex(buffer, b.maxX, b.minY, b.minZ);
        vertex(buffer, b.maxX, b.minY, b.maxZ);
        vertex(buffer, b.minX, b.minY, b.maxZ);
        vertex(buffer, b.minX, b.maxY, b.maxZ);
        vertex(buffer, b.maxX, b.maxY, b.maxZ);
        vertex(buffer, b.maxX, b.maxY, b.minZ);
        vertex(buffer, b.minX, b.maxY, b.minZ);
        vertex(buffer, b.minX, b.minY, b.maxZ);
        vertex(buffer, b.minX, b.maxY, b.maxZ);
        vertex(buffer, b.minX, b.maxY, b.minZ);
        vertex(buffer, b.minX, b.minY, b.minZ);
        vertex(buffer, b.maxX, b.minY, b.minZ);
        vertex(buffer, b.maxX, b.maxY, b.minZ);
        vertex(buffer, b.maxX, b.maxY, b.maxZ);
        vertex(buffer, b.maxX, b.minY, b.maxZ);
        BufferUploader.drawWithShader(buffer.end());
    }

    private static void vertex(BufferBuilder buffer, double x, double y, double z) {
        buffer.vertex(x, y, z).color(255, 255, 255, 255).endVertex();
    }

}
