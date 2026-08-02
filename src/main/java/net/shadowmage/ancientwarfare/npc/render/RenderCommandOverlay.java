package net.shadowmage.ancientwarfare.npc.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.shadowmage.ancientwarfare.core.util.EntityTools;
import net.shadowmage.ancientwarfare.core.util.RayTraceUtils;
import net.shadowmage.ancientwarfare.core.util.RenderTools;
import net.shadowmage.ancientwarfare.npc.item.ItemCommandBaton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public final class RenderCommandOverlay {
    public static final RenderCommandOverlay INSTANCE = new RenderCommandOverlay();

    private List<Entity> targetEntities = Collections.emptyList();
    private HitResult target;
    private String targetString;

    private RenderCommandOverlay() {
    }

    @SubscribeEvent
    public void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.screen != null) {
            return;
        }
        ItemStack commandBaton = EntityTools.getItemFromEitherHand(mc.player, ItemCommandBaton.class);
        if (commandBaton.isEmpty()) {
            target = null;
            targetEntities = Collections.emptyList();
            return;
        }

        target = RayTraceUtils.getPlayerTarget(mc.player, 120, 0);
        targetString = null;
        if (target instanceof BlockHitResult blockHit) {
            targetString = blockHit.getBlockPos().getX() + "," + blockHit.getBlockPos().getY() + "," + blockHit.getBlockPos().getZ();
        } else if (target instanceof EntityHitResult entityHit) {
            targetString = entityHit.getEntity().getName().getString();
        }
        targetEntities = ItemCommandBaton.getCommandedEntities(mc.level, commandBaton);

        if (mc.options.renderDebug) {
            return;
        }
        GuiGraphics graphics = event.getGuiGraphics();
        int x = mc.getWindow().getGuiScaledWidth() - 10;
        String header = I18n.get("guistrings.npc.target");
        graphics.drawString(mc.font, header, x - mc.font.width(header), 0, 0xffffffff, true);
        if (targetString != null) {
            graphics.drawString(mc.font, targetString, x - mc.font.width(targetString), 10, 0xffffffff, true);
        }
        graphics.drawString(mc.font, I18n.get("guistrings.npc.commanded"), 10, 0, 0xffffffff, true);
        List<String> entityNames = new ArrayList<>();
        for (Entity entity : targetEntities) {
            entityNames.add(entity.getName().getString());
        }
        for (int i = 0; i < entityNames.size(); i++) {
            graphics.drawString(mc.font, entityNames.get(i), 10, 10 + 10 * i, 0xffffffff, true);
        }
    }

    @SubscribeEvent
    public void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen != null || mc.player == null) {
            return;
        }
        ItemStack commandBaton = EntityTools.getItemFromEitherHand(mc.player, ItemCommandBaton.class);
        if (commandBaton.isEmpty()) {
            return;
        }

        Vec3 camera = event.getCamera().getPosition();
        AABB targetBox = null;
        if (target instanceof BlockHitResult blockHit) {
            targetBox = new AABB(blockHit.getBlockPos()).inflate(0.1D);
        } else if (target instanceof EntityHitResult entityHit && entityHit.getEntity() instanceof LivingEntity) {
            targetBox = entityHit.getEntity().getBoundingBox();
        }
        if (targetBox != null) {
            RenderTools.drawOutlinedBoundingBox(targetBox.move(-camera.x, -camera.y, -camera.z), 1.0F, 1.0F, 1.0F);
        }
        for (Entity entity : targetEntities) {
            RenderTools.drawOutlinedBoundingBox(entity.getBoundingBox().move(-camera.x, -camera.y, -camera.z), 1.0F, 0.0F, 0.0F);
        }
    }
}
