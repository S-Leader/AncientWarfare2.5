package net.shadowmage.ancientwarfare.structure.render;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.core.compat.client.GlStateManager;
import net.shadowmage.ancientwarfare.core.util.BlockTools;
import net.shadowmage.ancientwarfare.structure.api.TemplateRuleBlock;
import net.shadowmage.ancientwarfare.structure.template.StructureTemplate;
import net.shadowmage.ancientwarfare.structure.template.build.StructureBB;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

import static net.shadowmage.ancientwarfare.core.util.RenderTools.*;

/**
 * Client-side structure preview renderer ported to the 1.20.1 buffer API.
 */
@OnlyIn(Dist.CLIENT)
public final class PreviewRenderer {
    private PreviewRenderer() {
    }

    /**
     * Vertex-state snapshots no longer exist in the modern BufferBuilder API.
     */
    public static void clearCache() {
        // Rendering is rebuilt each frame; retained for callers of the old API.
    }

    public static void renderTemplatePreview(Player player, InteractionHand hand, ItemStack stack, float delta,
                                             StructureTemplate structure, StructureBB bb, int turns) {
        Minecraft.getInstance().getTextureManager().bindForSetup(TextureAtlas.LOCATION_BLOCKS);
        GlStateManager.pushMatrix();
        if (Minecraft.useAmbientOcclusion()) {
            GlStateManager.shadeModel(GL11.GL_SMOOTH);
        } else {
            GlStateManager.shadeModel(GL11.GL_FLAT);
        }

        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder buffer = tessellator.getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
        Map<BlockPos, TemplateRuleBlock> dynamicRenderRules = new HashMap<>();
        renderPreview(structure, bb, turns, buffer, dynamicRenderRules);

        GlStateManager.translate(-getRenderOffsetX(player, delta) + 0.005F,
                -getRenderOffsetY(player, delta) + 0.005F,
                -getRenderOffsetZ(player, delta) + 0.005F);
        tessellator.end();
        renderDynamicRules(turns, dynamicRenderRules);
        GlStateManager.popMatrix();
    }

    private static void renderDynamicRules(int turns, Map<BlockPos, TemplateRuleBlock> dynamicRenderRules) {
        Iterator<Map.Entry<BlockPos, TemplateRuleBlock>> iterator = dynamicRenderRules.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<BlockPos, TemplateRuleBlock> entry = iterator.next();
            try {
                entry.getValue().renderRuleDynamic(turns, entry.getKey());
            } catch (Exception ignored) {
                iterator.remove();
            }
        }
    }

    private static void renderPreview(StructureTemplate structure, StructureBB bb, int turns, BufferBuilder buffer,
                                      Map<BlockPos, TemplateRuleBlock> dynamicRenderRules) {
        TemplateBlockAccess blockAccess = new TemplateBlockAccess(structure, bb, turns);
        //1.20 renderBatched emits every render layer in one call, so the 1.12 three-pass loop would triple the geometry.
        for (int y = 0; y < structure.getSize().getY(); y++) {
            for (int x = 0; x < structure.getSize().getX(); x++) {
                for (int z = 0; z < structure.getSize().getZ(); z++) {
                    renderPreviewAt(structure, bb, turns, buffer, dynamicRenderRules, blockAccess, new BlockPos(x, y, z));
                }
            }
        }
    }

    private static void renderPreviewAt(StructureTemplate structure, StructureBB bb, int turns, BufferBuilder buffer,
                                        Map<BlockPos, TemplateRuleBlock> dynamicRenderRules, TemplateBlockAccess blockAccess, BlockPos pos) {
        BlockPos translateTo = BlockTools.rotateInArea(pos, structure.getSize().getX(), structure.getSize().getZ(), turns).offset(bb.min);
        structure.getRuleAt(pos).ifPresent(rule -> {
            rule.renderRule(turns, translateTo, blockAccess, buffer);
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
