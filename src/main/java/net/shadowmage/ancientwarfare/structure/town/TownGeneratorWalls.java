package net.shadowmage.ancientwarfare.structure.town;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.shadowmage.ancientwarfare.structure.template.StructureTemplate;
import net.shadowmage.ancientwarfare.structure.template.StructureTemplateManager;
import net.shadowmage.ancientwarfare.structure.template.build.StructureBuilder;
import net.shadowmage.ancientwarfare.structure.town.TownTemplate.TownWallEntry;
import net.shadowmage.ancientwarfare.structure.worldgen.WorldGenTickHandler;

import java.util.Optional;
import java.util.Random;

public class TownGeneratorWalls {
    private TownGeneratorWalls() {
    }

    public static void generateWalls(Level world, TownGenerator gen, TownTemplate template, Random rng) {
        if (template.getWallStyle() <= 0) {
            return;
        }//no walls
        int minX = gen.wallsBounds.min.getX();
        int minZ = gen.wallsBounds.min.getZ();
        int maxX = gen.wallsBounds.max.getX();
        int maxZ = gen.wallsBounds.max.getZ();
        int minY = gen.wallsBounds.min.getY();

        //construct NW corner
        getCornerSection(rng, template).ifPresent(corner -> constructTemplate(world, corner, Direction.SOUTH, new BlockPos(minX, minY, minZ)));

        //construct NE corner
        getCornerSection(rng, template).ifPresent(corner -> constructTemplate(world, corner, Direction.WEST, new BlockPos(maxX, minY, minZ)));

        //construct SE corner
        getCornerSection(rng, template).ifPresent(corner -> constructTemplate(world, corner, Direction.NORTH, new BlockPos(maxX, minY, maxZ)));

        //construct SW corner
        getCornerSection(rng, template).ifPresent(corner -> constructTemplate(world, corner, Direction.EAST, new BlockPos(minX, minY, maxZ)));

        if (template.getWallStyle() > 1)//has wall sections
        {
            int chunkWidth = (maxX - minX + 1) / 16;
            int chunkLength = (maxZ - minZ + 1) / 16;
            //construct N wall
            for (int i = 1; i < chunkWidth - 1; i++) {
                int x = minX + 16 * i;
                getWallSection(rng, template, i, chunkWidth).ifPresent(wall -> constructTemplate(world, wall, Direction.SOUTH, new BlockPos(x, minY, minZ)));
            }

            //construct E wall
            for (int i = 1; i < chunkLength - 1; i++) {
                int z = minZ + 16 * i;
                getWallSection(rng, template, i, chunkLength).ifPresent(wall -> constructTemplate(world, wall, Direction.WEST, new BlockPos(maxX, minY, z)));
            }

            //construct S wall
            for (int i = 1; i < chunkWidth - 1; i++) {
                int x = maxX - 16 * i;
                getWallSection(rng, template, i, chunkWidth).ifPresent(wall -> constructTemplate(world, wall, Direction.NORTH, new BlockPos(x, minY, maxZ)));
            }

            //construct W wall
            for (int i = 1; i < chunkLength - 1; i++) {
                int z = maxZ - 16 * i;
                getWallSection(rng, template, i, chunkLength).ifPresent(wall -> constructTemplate(world, wall, Direction.EAST, new BlockPos(minX, minY, z)));
            }
        }
    }

    private static Optional<StructureTemplate> getWallSection(Random rng, TownTemplate template, int index, int wallLength) {
        if (template.getWallStyle() == 2)//random weighted
        {
            if (wallLength % 2 == 0)//even sized
            {
                int middle = (wallLength / 2);
                if (index == middle)//return a rgate piece
                {
                    return StructureTemplateManager.getTemplate(template.getRandomWeightedGateLeft(rng));
                } else if (index == middle - 1) {
                    return StructureTemplateManager.getTemplate(template.getRandomWeightedGateRight(rng));
                } else {
                    return StructureTemplateManager.getTemplate(template.getRandomWeightedWall(rng));
                }
            } else {
                int middle = (wallLength / 2);
                if (index == middle)//return a gate piece
                {
                    return StructureTemplateManager.getTemplate(template.getRandomWeightedGate(rng));
                } else {
                    return StructureTemplateManager.getTemplate(template.getRandomWeightedWall(rng));
                }
            }
        } else if (template.getWallStyle() == 3)//patterned
        {
            int[] pattern = template.getWallPattern(wallLength);
            if (pattern != null && wallLength <= pattern.length) {
                TownWallEntry entry = template.getWall(template.getWallPattern(wallLength)[index]);
                if (entry != null) {
                    return StructureTemplateManager.getTemplate(entry.templateName);
                }
            }
        }
        return Optional.empty();
    }

    private static Optional<StructureTemplate> getCornerSection(Random rng, TownTemplate template) {
        return StructureTemplateManager.getTemplate(template.getRandomWeightedCorner(rng));
    }

    private static void constructTemplate(Level world, StructureTemplate template, Direction face, BlockPos pos) {
        WorldGenTickHandler.INSTANCE.addStructureForGeneration(new StructureBuilder(world, template, face, pos.relative(face.getCounterClockWise(), 15 - template.getOffset().getX())));
    }

}
