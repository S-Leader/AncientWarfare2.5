package net.shadowmage.ancientwarfare.structure.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.shadowmage.ancientwarfare.core.compat.client.LegacyBlockEntityRenderer;
import net.shadowmage.ancientwarfare.structure.tile.TileStake;

import static net.shadowmage.ancientwarfare.core.render.property.CoreProperties.FACING;

public class StakeRenderer extends LegacyBlockEntityRenderer<TileStake> {
    private static Entity entity = null;

    @Override
    public void render(TileStake te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        BlockState state = te.getWorld().getBlockState(te.getPos());

        Direction facing = state.getValue(FACING);

        Minecraft mc = Minecraft.getInstance();
        EntityRenderDispatcher rendermanager = mc.getEntityRenderDispatcher();
        te.getRenderEntity().ifPresent(e -> {
            if (te.isEntityOnFire()) {
                e.setSecondsOnFire(1);
            } else {
                e.clearFire();
            }
            e.setYBodyRot(facing.toYRot());
            e.setYHeadRot(facing.toYRot());
            BlockPos pos = te.getPos();
            Vec3 cam = mc.gameRenderer.getMainCamera().getPosition();
            PoseStack poseStack = new PoseStack();
            MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();
            int packedLight = LevelRenderer.getLightColor(te.getWorld(), pos);
            rendermanager.render(e, pos.getX() - cam.x + x + 0.5 + facing.getStepX() * 0.3, pos.getY() - cam.y + y + 0.6,
                    pos.getZ() - cam.z + z + 0.5 + facing.getStepZ() * 0.3, facing.toYRot(), 1.0F, poseStack, buffer, packedLight);
            buffer.endBatch();
        });
    }
}
