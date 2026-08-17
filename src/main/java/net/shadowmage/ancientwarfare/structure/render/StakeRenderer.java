package net.shadowmage.ancientwarfare.structure.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.LivingEntity;
import net.shadowmage.ancientwarfare.core.compat.client.LegacyBlockEntityRenderer;
import net.shadowmage.ancientwarfare.structure.init.AWStructureBlocks;
import net.shadowmage.ancientwarfare.structure.tile.TileStake;

import static net.shadowmage.ancientwarfare.core.render.property.CoreProperties.FACING;

public class StakeRenderer extends LegacyBlockEntityRenderer<TileStake> {
    @Override
    public void render(TileStake te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        // A block entity may remain in the client render list for one frame after
        // its block has already been replaced.  Querying the level in that window
        // returns AIR, which has no FACING property and used to crash the client.
        if (te == null || te.isRemoved() || te.getLevel() == null
                || te.getLevel().getBlockEntity(te.getPos()) != te
                || !te.getLevel().getBlockState(te.getPos()).is(AWStructureBlocks.STAKE.get())) {
            return;
        }

        BlockState state = te.getBlockState();
        if (!state.is(AWStructureBlocks.STAKE.get()) || !state.hasProperty(FACING)) {
            return;
        }

        Direction facing = state.getValue(FACING);

        Minecraft mc = Minecraft.getInstance();
        EntityRenderDispatcher rendermanager = mc.getEntityRenderDispatcher();
        te.getRenderEntity().ifPresent(e -> {
            if (te.isEntityOnFire()) {
                e.setSecondsOnFire(1);
            } else {
                e.clearFire();
            }

            /*
             * The display entity is never added to the level and therefore never
             * ticks. Passing the real render partial tick to a normal living-entity
             * renderer makes ageInTicks/rotation interpolation repeatedly run from
             * 0 -> 1 and then snap back to 0 every game tick. That is the visible
             * 20 Hz "twitch" on stake victims. Freeze both ends of every yaw/pitch
             * interpolation and render the synthetic entity at partialTick 0.
             */
            float yaw = facing.toYRot();
            e.setYRot(yaw);
            e.yRotO = yaw;
            e.setXRot(0.0F);
            e.xRotO = 0.0F;
            e.tickCount = 0;
            if (e instanceof LivingEntity living) {
                living.setYBodyRot(yaw);
                living.yBodyRotO = yaw;
                living.setYHeadRot(yaw);
                living.yHeadRotO = yaw;
            }

            BlockPos pos = te.getPos();
            PoseStack poseStack = getActivePoseStack();
            MultiBufferSource buffer = getActiveBufferSource();
            int packedLight = LevelRenderer.getLightColor(te.getWorld(), pos);

            /*
             * Modern BER PoseStacks are already translated to the block-local
             * origin. The old port subtracted camera/world coordinates a second
             * time and rendered the displayed entity far away from the stake.
             * EntityRenderDispatcher applies the local x/y/z translation itself.
             * Reuse the world renderer's buffer instead of opening/endBatch-ing a
             * second global buffer in the middle of block-entity rendering.
             */
            poseStack.pushPose();
            rendermanager.render(e,
                    x + 0.5 + facing.getStepX() * 0.3,
                    y + 0.6,
                    z + 0.5 + facing.getStepZ() * 0.3,
                    facing.toYRot(), 0.0F, poseStack, buffer, packedLight);
            poseStack.popPose();
        });
    }
}
