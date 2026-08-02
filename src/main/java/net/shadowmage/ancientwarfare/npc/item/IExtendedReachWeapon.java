package net.shadowmage.ancientwarfare.npc.item;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.shadowmage.ancientwarfare.core.network.NetworkHandler;
import net.shadowmage.ancientwarfare.npc.network.PacketExtendedReachAttack;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public interface IExtendedReachWeapon {
    float getReach();

    @OnlyIn(Dist.CLIENT)
    class MouseClickHandler {
        @SubscribeEvent(priority = EventPriority.NORMAL, receiveCanceled = true)
        public void onMouseClick(InputEvent.MouseButton.Pre event) {
            if (!(event.getButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT && event.getAction() == GLFW.GLFW_PRESS)) {
                return;
            }
            Player player = Minecraft.getInstance().player;
            if (player == null || !(player.getMainHandItem().getItem() instanceof IExtendedReachWeapon)) {
                return;
            }
            IExtendedReachWeapon weapon = (IExtendedReachWeapon) player.getMainHandItem().getItem();
            Optional<HitResult> result = getMouseOverExtended(weapon.getReach());

            result.ifPresent(r -> {
                if (r instanceof EntityHitResult entityHitResult) {
                    Entity entityHit = entityHitResult.getEntity();
                    if (entityHit.invulnerableTime == 0 && entityHit != player) {
                        NetworkHandler.sendToServer(new PacketExtendedReachAttack(entityHit.getId()));
                    }
                }
            });
        }

        //Based on EntityRenderer.getMouseOver
        @OnlyIn(Dist.CLIENT)
        private Optional<HitResult> getMouseOverExtended(float reach) {
            HitResult ret = null;
            Minecraft mc = Minecraft.getInstance();
            Entity renderViewEntity = mc.getCameraEntity();

            if (renderViewEntity != null && mc.level != null) {
                mc.getProfiler().push("pick");
                double d0 = reach;
                HitResult pick = renderViewEntity.pick(d0, 0, false);
                ret = pick.getType() == HitResult.Type.MISS ? null : pick;
                Vec3 positionEyes = renderViewEntity.getEyePosition(0);
                double calcDist = d0;

                if (ret != null) {
                    calcDist = ret.getLocation().distanceTo(positionEyes);
                }

                Vec3 vec3d1 = renderViewEntity.getViewVector(1.0F);
                Vec3 vec3d2 = positionEyes.add(vec3d1.x * d0, vec3d1.y * d0, vec3d1.z * d0);
                List<Entity> list = mc.level.getEntities(renderViewEntity,
                        renderViewEntity.getBoundingBox().expandTowards(vec3d1.x * d0, vec3d1.y * d0, vec3d1.z * d0).inflate(1.0D, 1.0D, 1.0D),
                        EntitySelector.NO_SPECTATORS.and(e -> e != null && e.isPickable()));

                ret = getEntityHit(ret, renderViewEntity, positionEyes, calcDist, vec3d2, list);
                mc.getProfiler().pop();
            }
            return Optional.ofNullable(ret);
        }

        @Nullable
        private HitResult getEntityHit(
                @Nullable HitResult ret, Entity renderViewEntity, Vec3 positionEyes, double calcDist, Vec3 vec3d2, List<Entity> list) {
            double d = calcDist;
            Entity pointedEntity = null;
            for (Entity entity : list) {
                AABB axisalignedbb = entity.getBoundingBox().inflate((double) entity.getPickRadius());
                Optional<Vec3> intercept = axisalignedbb.clip(positionEyes, vec3d2);

                if (axisalignedbb.contains(positionEyes)) {
                    if (d >= 0.0D) {
                        pointedEntity = entity;
                        d = 0.0D;
                    }
                } else if (intercept.isPresent()) {
                    double d1 = positionEyes.distanceTo(intercept.get());

                    if (d1 < d || d == 0.0D) {
                        if (entity.getRootVehicle() == renderViewEntity.getRootVehicle() && !entity.canRiderInteract()) {
                            if (d == 0.0D) {
                                pointedEntity = entity;
                            }
                        } else {
                            pointedEntity = entity;
                            d = d1;
                        }
                    }
                }
            }

            if (pointedEntity != null && (d < calcDist || ret == null)) {
                ret = new EntityHitResult(pointedEntity);
            }
            return ret;
        }
    }
}
