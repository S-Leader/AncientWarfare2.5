package net.shadowmage.ancientwarfare.npc.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.shadowmage.ancientwarfare.core.util.MathUtils;
import net.shadowmage.ancientwarfare.npc.entity.faction.NpcFaction;

public class RenderNpcFaction extends RenderNpcBase<NpcFaction> {
    public RenderNpcFaction(EntityRendererProvider.Context renderManager) {
        super(renderManager);
    }

    @Override
    protected void scale(NpcFaction npc, PoseStack poseStack, float partialTickTime) {
        float scale = npc.getRenderSizeModifier();
        float widthScale = npc.getWidthModifier();
        if (MathUtils.epsilonEquals(scale, 1.0f) && MathUtils.epsilonEquals(widthScale, 1.0f)) {
            return;
        }
        poseStack.scale(widthScale, scale, widthScale);
        if (npc.isPassenger() && scale < 1f && !(npc.getVehicle() instanceof AbstractHorse)) {
            float ratio = (1f - scale) * (1f - scale);
            poseStack.translate(0, -3f * ratio, 0);
        }
    }
}
