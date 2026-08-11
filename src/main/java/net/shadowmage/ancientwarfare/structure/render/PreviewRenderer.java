package net.shadowmage.ancientwarfare.structure.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.core.util.BlockTools;
import net.shadowmage.ancientwarfare.structure.api.TemplateRuleBlock;
import net.shadowmage.ancientwarfare.structure.template.StructureTemplate;
import net.shadowmage.ancientwarfare.structure.template.build.StructureBB;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

/**
 * Client-side structure preview renderer.
 *
 * <p>The 1.12 renderer mixed an OpenGL model-view translation with vertices emitted
 * through the old BufferBuilder. That cannot be carried over literally to 1.20.1:
 * block models now receive their transform from a PoseStack. Keeping the camera
 * translation on the legacy GL stack while giving every block a fresh PoseStack
 * produces world-space vertices that are later interpreted as camera-space vertices,
 * which is the giant smeared polygon seen in the preview.</p>
 */
@OnlyIn(Dist.CLIENT)
public final class PreviewRenderer {
    private static final int PREVIEW_BUFFER_SIZE = 2 * 1024 * 1024;

    /*
     * IBoxRenderer is a common-side interface and must not reference client-only
     * Camera/PoseStack classes in its method descriptor. StructureBoundingBoxRenderer
     * supplies the active level-render context here just for the duration of the
     * item preview callback instead.
     */
    @Nullable
    private static PoseStack activePoseStack;
    @Nullable
    private static Camera activeCamera;

    private PreviewRenderer() {
    }

    public static void withLevelRenderContext(PoseStack poseStack, Camera camera, Runnable renderer) {
        PoseStack previousPoseStack = activePoseStack;
        Camera previousCamera = activeCamera;
        activePoseStack = poseStack;
        activeCamera = camera;
        try {
            renderer.run();
        } finally {
            activePoseStack = previousPoseStack;
            activeCamera = previousCamera;
        }
    }

    /**
     * Vertex-state snapshots no longer exist in the modern BufferBuilder API.
     */
    public static void clearCache() {
        // Rendering is rebuilt each frame; retained for callers of the old API.
    }

    public static void renderTemplatePreview(Player player, InteractionHand hand, ItemStack stack, float delta,
                                             StructureTemplate structure, StructureBB bb, int turns) {
        Minecraft minecraft = Minecraft.getInstance();
        Camera camera = activeCamera != null ? activeCamera : minecraft.gameRenderer.getMainCamera();
        PoseStack poseStack = activePoseStack != null ? activePoseStack : new PoseStack();
        Vec3 cameraPos = camera.getPosition();

        /*
         * Use a private BufferSource. Reusing Tesselator.getInstance().getBuilder()
         * can collide with another level-render batch that is still open when this
         * AFTER_ENTITIES callback runs.
         */
        MultiBufferSource.BufferSource buffers = MultiBufferSource.immediate(new BufferBuilder(PREVIEW_BUFFER_SIZE));
        Map<BlockPos, TemplateRuleBlock> dynamicRenderRules = new HashMap<>();

        poseStack.pushPose();
        try {
            // RenderLevelStageEvent gives us the level render PoseStack. Put template
            // world coordinates into the same camera-relative coordinate system used
            // by vanilla's entity/block-entity renderers.
            poseStack.translate(-cameraPos.x + 0.005D, -cameraPos.y + 0.005D, -cameraPos.z + 0.005D);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

            renderPreview(structure, bb, turns, poseStack, buffers, dynamicRenderRules);
            buffers.endBatch();

            renderDynamicRules(turns, dynamicRenderRules, poseStack, buffers);
            buffers.endBatch();
        } finally {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            poseStack.popPose();
        }
    }

    private static void renderDynamicRules(int turns, Map<BlockPos, TemplateRuleBlock> dynamicRenderRules,
                                           PoseStack poseStack, MultiBufferSource bufferSource) {
        Iterator<Map.Entry<BlockPos, TemplateRuleBlock>> iterator = dynamicRenderRules.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<BlockPos, TemplateRuleBlock> entry = iterator.next();
            try {
                entry.getValue().renderRuleDynamic(turns, entry.getKey(), poseStack, bufferSource);
            } catch (RuntimeException | LinkageError exception) {
                // One broken third-party BER must not destroy the whole preview frame.
                iterator.remove();
            }
        }
    }

    private static void renderPreview(StructureTemplate structure, StructureBB bb, int turns, PoseStack poseStack,
                                      MultiBufferSource bufferSource,
                                      Map<BlockPos, TemplateRuleBlock> dynamicRenderRules) {
        TemplateBlockAccess blockAccess = new TemplateBlockAccess(structure, bb, turns);
        for (int y = 0; y < structure.getSize().getY(); y++) {
            for (int x = 0; x < structure.getSize().getX(); x++) {
                for (int z = 0; z < structure.getSize().getZ(); z++) {
                    renderPreviewAt(structure, bb, turns, poseStack, bufferSource,
                            dynamicRenderRules, blockAccess, new BlockPos(x, y, z));
                }
            }
        }
    }

    private static void renderPreviewAt(StructureTemplate structure, StructureBB bb, int turns, PoseStack poseStack,
                                        MultiBufferSource bufferSource,
                                        Map<BlockPos, TemplateRuleBlock> dynamicRenderRules,
                                        TemplateBlockAccess blockAccess, BlockPos pos) {
        BlockPos translateTo = BlockTools.rotateInArea(pos, structure.getSize().getX(), structure.getSize().getZ(), turns)
                .offset(bb.min);
        structure.getRuleAt(pos).ifPresent(rule -> {
            rule.renderRule(turns, translateTo, blockAccess, poseStack, bufferSource);
            if (rule.isDynamicallyRendered(turns)) {
                dynamicRenderRules.put(translateTo, rule);
            }
        });
    }

    private static final class TemplateBlockAccess implements BlockGetter {
        private final int templateXSize;
        private final int templateZSize;
        private final StructureTemplate template;
        private final StructureBB bb;
        private final int turns;
        private final Map<Long, BlockState> positionStates = new HashMap<>();
        private final Map<Long, BlockEntity> positionTiles = new HashMap<>();

        private TemplateBlockAccess(StructureTemplate template, StructureBB bb, int turns) {
            this.template = template;
            this.bb = bb;
            this.turns = turns;
            int xSize = template.getSize().getX();
            int zSize = template.getSize().getZ();
            if ((turns & 1) != 0) {
                templateXSize = zSize;
                templateZSize = xSize;
            } else {
                templateXSize = xSize;
                templateZSize = zSize;
            }
        }

        @Nullable
        @Override
        public BlockEntity getBlockEntity(BlockPos pos) {
            long key = pos.asLong();
            if (!positionTiles.containsKey(key)) {
                BlockEntity blockEntity = null;
                if (bb.contains(pos)) {
                    blockEntity = getBlockRuleAt(pos).map(rule -> rule.getTileEntity(turns)).orElse(null);
                }
                positionTiles.put(key, blockEntity);
            }
            return positionTiles.get(key);
        }

        @Override
        public BlockState getBlockState(BlockPos pos) {
            return positionStates.computeIfAbsent(pos.asLong(), ignored -> {
                if (!bb.contains(pos)) {
                    return Blocks.AIR.defaultBlockState();
                }
                return getBlockRuleAt(pos).map(rule -> rule.getState(turns)).orElse(Blocks.AIR.defaultBlockState());
            });
        }

        @Override
        public FluidState getFluidState(BlockPos pos) {
            return getBlockState(pos).getFluidState();
        }

        @Override
        public int getHeight() {
            return Math.max(1, template.getSize().getY());
        }

        @Override
        public int getMinBuildHeight() {
            return 0;
        }

        private Optional<TemplateRuleBlock> getBlockRuleAt(BlockPos pos) {
            BlockPos relative = pos.offset(-bb.min.getX(), -bb.min.getY(), -bb.min.getZ());
            BlockPos templatePos = BlockTools.rotateInArea(relative, templateXSize, templateZSize, -turns);
            return template.getRuleAt(templatePos);
        }
    }
}
