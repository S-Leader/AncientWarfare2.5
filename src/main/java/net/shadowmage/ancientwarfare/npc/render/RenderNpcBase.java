package net.shadowmage.ancientwarfare.npc.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.PlayerTeam;
import net.shadowmage.ancientwarfare.npc.ai.NpcAI;
import net.shadowmage.ancientwarfare.npc.config.AWNPCStatics;
import net.shadowmage.ancientwarfare.npc.entity.NpcBase;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public class RenderNpcBase<T extends NpcBase> extends HumanoidMobRenderer<T, ModelNpc<T>> {
    private final ModelNpc<T> smallArms;
    private final ModelNpc<T> regularArms;
    private boolean isSleeping;

    private List<Integer> renderTasks = new ArrayList<>();

    public RenderNpcBase(EntityRendererProvider.Context renderManager) {
        super(renderManager, new ModelNpc<>(renderManager.bakeLayer(ModelLayers.PLAYER), false), 0.6f);
        regularArms = getModel();
        smallArms = new ModelNpc<>(renderManager.bakeLayer(ModelLayers.PLAYER_SLIM), true);
        //held item layer (1.12 LayerHeldItem) is added by the HumanoidMobRenderer constructor
        addLayer(new HumanoidArmorLayer<T, ModelNpc<T>, HumanoidModel<T>>(this,
                new HumanoidModel<>(renderManager.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
                new HumanoidModel<>(renderManager.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)),
                renderManager.getModelManager()));
    }

    @Override
    protected void setupRotations(T npc, PoseStack poseStack, float parFloat1, float parFloat2, float parFloat3) {
        isSleeping = npc.isSleeping();
        if (isSleeping) {
            float bedDirection = npc.getBedOrientationInDegrees();
            if (bedDirection != -1) {
                poseStack.mulPose(Axis.YP.rotationDegrees(bedDirection));
                poseStack.mulPose(Axis.ZP.rotationDegrees(this.getFlipDegrees(npc)));
                poseStack.mulPose(Axis.YP.rotationDegrees(270.0F));
                return;
            }
            isSleeping = false;
        }
        super.setupRotations(npc, poseStack, parFloat1, parFloat2, parFloat3);
    }

    @Override
    public void render(T npc, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        model = npc.getSkinSettings().renderFemaleModel(npc) ? smallArms : regularArms;

        isSleeping = npc.isSleeping();
        if (isSleeping) {
            // render the body a bit offset because we're manually shifting the bounding box
            poseStack.pushPose();
            poseStack.translate(-(npc.getBedDirection().getStepX() * 0.5), 0, -(npc.getBedDirection().getStepZ() * 0.5));
            super.render(npc, entityYaw, partialTicks, poseStack, buffer, packedLight);
            poseStack.popPose();
        } else {
            super.render(npc, entityYaw, partialTicks, poseStack, buffer, packedLight);
        }

        float yOffset = isSleeping ? -1.5f : 0;
        Player player = Minecraft.getInstance().player;
        if (npc.isHostileTowards(player)) {
            if (AWNPCStatics.renderHostileNames.getBoolean()) {
                String name = getNameForRender(npc, true);
                if (AWNPCStatics.renderTeamColors.getBoolean()) {
                    PlayerTeam playerTeam = player.level().getScoreboard().getPlayerTeam(player.getName().getString());
                    PlayerTeam npcTeam = (PlayerTeam) npc.getTeam();
                    if (npcTeam != null && npcTeam != playerTeam) {
                        name = npcTeam.getPlayerPrefix().getString() + name + npcTeam.getPlayerSuffix().getString();
                    }
                }
                renderColoredLabel(npc, name, poseStack, buffer, packedLight, yOffset, 0x20ff0000, 0xffff0000);
            }
        } else {
            boolean canBeCommandedBy = npc.hasCommandPermissions(player.getUUID(), player.getName().getString());
            if (AWNPCStatics.renderFriendlyNames.getBoolean()) {
                String name = getNameForRender(npc, false);
                if (AWNPCStatics.renderTeamColors.getBoolean()) {
                    PlayerTeam playerTeam = player.level().getScoreboard().getPlayerTeam(player.getName().getString());
                    PlayerTeam npcTeam = (PlayerTeam) npc.getTeam();
                    if (npcTeam != null && npcTeam != playerTeam) {
                        name = npcTeam.getPlayerPrefix().getString() + name + npcTeam.getPlayerSuffix().getString();
                    }
                } else if (!canBeCommandedBy) {
                    name = ChatFormatting.DARK_GRAY.toString() + name;
                }
                renderColoredLabel(npc, name, poseStack, buffer, packedLight, yOffset, 0x20ffffff, 0xffffffff);
            }
            if (canBeCommandedBy && AWNPCStatics.renderAI.getBoolean()) {
                renderNpcAITasks(npc, poseStack, buffer, yOffset);
            }
        }
    }

    @Override
    protected boolean shouldShowName(T npc) {
        // Do not suppress Minecraft's normal entity-name rendering. The old port
        // returned false unconditionally, so even NpcBase#getTypeName() could never
        // display dynamic names such as "Empire Archer" through the normal path.
        // The optional AW nameplate/health overlay below remains independent.
        return super.shouldShowName(npc);
    }

    private String getNameForRender(NpcBase npc, boolean hostile) {
        String name = npc.getNpcName();
        boolean addHealth = (hostile && AWNPCStatics.renderHostileHealth.getBoolean()) || (!hostile && AWNPCStatics.renderFriendlyHealth.getBoolean());
        if (addHealth) {
            name += " " + getHealthForRender(npc);
        }
        return name;
    }

    private String getHealthForRender(NpcBase npc) {
        return String.format("%.1f", npc.getHealth());
    }

    private void renderNpcAITasks(T entity, PoseStack poseStack, MultiBufferSource buffer, float yOffset) {
        double d3 = this.entityRenderDispatcher.distanceToSqr(entity);
        Entity viewEntity = Minecraft.getInstance().getCameraEntity();

        if (d3 <= (double) (64 * 64) && viewEntity != null && entity.hasLineOfSight(viewEntity)) {
            float f = 1.6F;
            float f1 = 0.016666668F * f;
            poseStack.pushPose();
            poseStack.translate(0.0F, entity.getBbHeight() + yOffset + 0.5F, 0.0F);
            poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
            poseStack.scale(-f1, -f1, f1);
            int tasks = entity.getAITasks();
            int mask;
            String icon;

            for (int i = 0; i < NpcAI.NUMBER_OF_TASKS; i++) {
                mask = 1 << i;
                if ((tasks & mask) != 0) {
                    renderTasks.add(mask);
                }
            }

            int offset = (renderTasks.size() * 10 / 2);
            int startX = -offset;

            for (int i = 0; i < renderTasks.size(); i++) {
                icon = getIconFor(renderTasks.get(i));
                if (icon != null) {
                    renderIcon(poseStack, buffer, icon, startX + i * 20, -16);
                }
            }
            poseStack.popPose();
            this.renderTasks.clear();
        }
    }

    private void renderColoredLabel(T entity, String string, PoseStack poseStack, MultiBufferSource buffer, int packedLight, float yOffset, int color1, int color2) {
        double d3 = this.entityRenderDispatcher.distanceToSqr(entity);
        Entity viewEntity = Minecraft.getInstance().getCameraEntity();

        if (d3 <= (double) (64 * 64) && viewEntity != null && entity.hasLineOfSight(viewEntity)) {
            Font fontrenderer = getFont();
            float f = 0.8F;
            float f1 = 0.016666668F * f;
            poseStack.pushPose();
            float heightScalingOffset = entity.getBbHeight() / 6 - 0.3F;
            float verticalOffset = offsetForRiddenEntity(entity, heightScalingOffset);
            poseStack.translate(0.0F, entity.getBbHeight() + verticalOffset + yOffset + 0.5F, 0.0F);
            poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
            poseStack.scale(-f1, -f1, f1);
            Matrix4f matrix = poseStack.last().pose();
            float xStart = -fontrenderer.width(string) / 2f;
            int backgroundColor = (int) (0.25F * 255.0F) << 24;
            //see-through pass with background replaces the 1.12 depth-disabled draw + background quad, normal pass matches the depth-enabled draw
            fontrenderer.drawInBatch(string, xStart, 0, color1, false, matrix, buffer, Font.DisplayMode.SEE_THROUGH, backgroundColor, packedLight);
            fontrenderer.drawInBatch(string, xStart, 0, color2, false, matrix, buffer, Font.DisplayMode.NORMAL, 0, packedLight);
            poseStack.popPose();
        }
    }

    private float offsetForRiddenEntity(NpcBase entity, float heightScalingOffset) {
        float ret = heightScalingOffset;
        if (!entity.isPassenger()) {
            return ret;
        }

        if (entity.getVehicle() instanceof Pig) {
            ret += 0.4f;
        }

        return ret;
    }

    @Override
    public ResourceLocation getTextureLocation(T npc) {
        return npc.getTexture();
    }

    @SuppressWarnings("squid:S2184")
    // the addition / subtraction here only works with small values so no need to cast to double before operation is done
    private void renderIcon(PoseStack poseStack, MultiBufferSource buffer, String tex, int x, int y) {
        VertexConsumer builder = buffer.getBuffer(RenderType.text(new ResourceLocation(tex)));
        Matrix4f matrix = poseStack.last().pose();
        int halfW = 16 / 2;
        int halfH = 16 / 2;
        builder.vertex(matrix, x - halfW, y - halfH, 0).color(255, 255, 255, 255).uv(0, 0).uv2(LightTexture.FULL_BRIGHT).endVertex();
        builder.vertex(matrix, x - halfW, y + halfH, 0).color(255, 255, 255, 255).uv(0, 1).uv2(LightTexture.FULL_BRIGHT).endVertex();
        builder.vertex(matrix, x + halfW, y + halfH, 0).color(255, 255, 255, 255).uv(1, 1).uv2(LightTexture.FULL_BRIGHT).endVertex();
        builder.vertex(matrix, x + halfW, y + -halfH, 0).color(255, 255, 255, 255).uv(1, 0).uv2(LightTexture.FULL_BRIGHT).endVertex();
    }

    private String getIconFor(int task) {
        switch (task) {
            case 0:
                return null;
            case NpcAI.TASK_ATTACK:
                return "ancientwarfare:textures/entity/npc/ai/task_attack.png";
            case NpcAI.TASK_UPKEEP:
                return "ancientwarfare:textures/entity/npc/ai/task_upkeep.png";
            case NpcAI.TASK_IDLE_HUNGRY:
                return "ancientwarfare:textures/entity/npc/ai/task_upkeep2.png";
            case NpcAI.TASK_GO_HOME:
                return "ancientwarfare:textures/entity/npc/ai/task_home.png";
            case NpcAI.TASK_WORK:
                return "ancientwarfare:textures/entity/npc/ai/task_work.png";
            case NpcAI.TASK_PATROL:
                return "ancientwarfare:textures/entity/npc/ai/task_patrol.png";
            case NpcAI.TASK_GUARD:
                return "ancientwarfare:textures/entity/npc/ai/task_guard.png";
            case NpcAI.TASK_FOLLOW:
                return "ancientwarfare:textures/entity/npc/ai/task_follow.png";
            case NpcAI.TASK_WANDER:
                return "ancientwarfare:textures/entity/npc/ai/task_wander.png";
            case NpcAI.TASK_MOVE:
                return "ancientwarfare:textures/entity/npc/ai/task_move.png";
            case NpcAI.TASK_ALARM:
                return "ancientwarfare:textures/entity/npc/ai/task_alarm.png";
            case NpcAI.TASK_FLEE:
                return "ancientwarfare:textures/entity/npc/ai/task_flee.png";
            case NpcAI.TASK_SLEEP:
                return "ancientwarfare:textures/entity/npc/ai/task_sleep.png";
            case NpcAI.TASK_RAIN:
                return "ancientwarfare:textures/entity/npc/ai/task_rain.png";
            default:
                return null;
        }
    }

}
