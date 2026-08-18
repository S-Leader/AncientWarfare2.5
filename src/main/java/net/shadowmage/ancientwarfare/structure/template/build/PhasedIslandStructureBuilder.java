package net.shadowmage.ancientwarfare.structure.template.build;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.shadowmage.ancientwarfare.core.util.BlockTools;
import net.shadowmage.ancientwarfare.structure.api.TemplateRuleBlock;
import net.shadowmage.ancientwarfare.structure.template.StructureTemplate;

import java.util.Optional;

/**
 * Large-island-only builder. It consumes one intersecting chunk and one legacy
 * build priority at a time instead of walking the entire AWS in one server tick.
 */
public final class PhasedIslandStructureBuilder extends StructureBuilder {
    public PhasedIslandStructureBuilder(Level world, StructureTemplate template, Direction face, BlockPos pos) {
        super(world, template, face, pos);
    }

    public void buildChunkPass(ChunkPos chunk, int priority) {
        int chunkMinX = chunk.getMinBlockX();
        int chunkMaxX = chunk.getMaxBlockX();
        int chunkMinZ = chunk.getMinBlockZ();
        int chunkMaxZ = chunk.getMaxBlockZ();
        int sizeX = template.getSize().getX();
        int sizeY = template.getSize().getY();
        int sizeZ = template.getSize().getZ();

        for (int z = 0; z < sizeZ; z++) {
            for (int x = 0; x < sizeX; x++) {
                BlockPos column = BlockTools.rotateInArea(new BlockPos(x, 0, z), sizeX, sizeZ, turns).offset(bb.min);
                if (column.getX() < chunkMinX || column.getX() > chunkMaxX
                        || column.getZ() < chunkMinZ || column.getZ() > chunkMaxZ) {
                    continue;
                }
                for (int y = 0; y < sizeY; y++) {
                    BlockPos templatePos = new BlockPos(x, y, z);
                    BlockPos target = BlockTools.rotateInArea(templatePos, sizeX, sizeZ, turns).offset(bb.min);
                    Optional<TemplateRuleBlock> rule = template.getRuleAt(templatePos);
                    if (rule.isPresent()) {
                        TemplateRuleBlock blockRule = rule.get();
                        if (blockRule.shouldPlaceOnBuildPass(world, turns, target, priority)) {
                            destination = target;
                            blockRule.handlePlacement(world, turns, target, this);
                        }
                    } else if (priority == 0 && !template.getValidationSettings().isPreserveBlocks()) {
                        // The ordinary builder used update flag 3 here. For a many-million-cell
                        // island that causes a neighbour-update storm. Air only needs to be sent
                        // to clients while the island is being assembled; final connection/update
                        // work is performed once after all passes finish.
                        if (!world.isEmptyBlock(target)) {
                            world.setBlock(target, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                        }
                    }
                }
            }
        }
    }

    /** Completes BE/rail/redstone updates and entities, intentionally skipping the huge legacy biome scan. */
    public void finishIslandBlocks() {
        finishConstructionWithoutBiome();
    }
}
