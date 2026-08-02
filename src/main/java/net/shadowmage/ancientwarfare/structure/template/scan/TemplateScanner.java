package net.shadowmage.ancientwarfare.structure.template.scan;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.shadowmage.ancientwarfare.core.util.BlockTools;
import net.shadowmage.ancientwarfare.structure.api.TemplateRuleBlock;
import net.shadowmage.ancientwarfare.structure.api.TemplateRuleEntityBase;
import net.shadowmage.ancientwarfare.structure.config.AWStructureStatics;
import net.shadowmage.ancientwarfare.structure.template.StructurePluginManager;
import net.shadowmage.ancientwarfare.structure.template.StructureTemplate;

import java.util.*;

public class TemplateScanner {
    private TemplateScanner() {
    }

    /*
     * @param turns # of turns for proper orientation
     */

    public static StructureTemplate scan(Level world, Set<String> modDependencies, BlockPos min, BlockPos max, BlockPos key, int turns, String name) {
        int xSize = max.getX() - min.getX() + 1;
        int ySize = max.getY() - min.getY() + 1;
        int zSize = max.getZ() - min.getZ() + 1;

        int xOutSize = xSize;
        int zOutSize = zSize;
        int swap;
        for (int i = 0; i < turns; i++) {
            swap = xOutSize;
            xOutSize = zOutSize;
            zOutSize = swap;
        }
        key = BlockTools.rotateInArea(key.subtract(min.offset(0, -1, 0)), xSize, zSize, turns);

        short[] templateRuleData = new short[xSize * ySize * zSize];

        HashMap<String, List<TemplateRuleBlock>> pluginBlockRuleMap = new HashMap<>();
        Map<Integer, TemplateRuleBlock> blockRules = new HashMap<>();
        Block scannedBlock;
        List<TemplateRuleBlock> pluginBlockRules;
        int index;
        int nextRuleID = 1;
        BlockPos destination;
        for (int scanY = min.getY(); scanY <= max.getY(); scanY++) {
            for (int scanZ = min.getZ(); scanZ <= max.getZ(); scanZ++) {
                for (int scanX = min.getX(); scanX <= max.getX(); scanX++) {
                    destination = BlockTools.rotateInArea(new BlockPos(scanX, scanY, scanZ).subtract(min), xSize, zSize, turns);

                    BlockPos scannedPos = new BlockPos(scanX, scanY, scanZ);
                    BlockState scannedState = world.getBlockState(scannedPos);
                    scannedBlock = scannedState.getBlock();

                    if (!AWStructureStatics.shouldSkipScan(scannedBlock) && !world.isEmptyBlock(scannedPos)) {
                        Optional<String> pluginId = StructurePluginManager.INSTANCE.getPluginNameFor(world, scannedPos, scannedState);
                        if (pluginId.isPresent()) {
                            pluginBlockRules = pluginBlockRuleMap.computeIfAbsent(pluginId.get(), k -> new ArrayList<>());
                            Optional<TemplateRuleBlock> scannedBlockRule = Optional.empty();
                            for (TemplateRuleBlock rule : pluginBlockRules) {
                                if (rule.shouldReuseRule(world, scannedState, turns, scannedPos)) {
                                    scannedBlockRule = Optional.of(rule);
                                    break;
                                }
                            }
                            if (!scannedBlockRule.isPresent()) {
                                scannedBlockRule = StructurePluginManager.INSTANCE.getRuleForBlock(world, scannedState, turns, scannedPos);
                                if (scannedBlockRule.isPresent()) {
                                    scannedBlockRule.get().ruleNumber = nextRuleID;
                                    nextRuleID++;
                                    pluginBlockRules.add(scannedBlockRule.get());
                                    blockRules.put(scannedBlockRule.get().ruleNumber, scannedBlockRule.get());
                                }
                            }
                            index = StructureTemplate.getIndex(destination, new Vec3i(xOutSize, ySize, zOutSize));
                            templateRuleData[index] = scannedBlockRule.map(r -> (short) r.ruleNumber).orElse((short) -1);
                        }
                    }
                }//end scan x-level for
            }//end scan z-level for
        }//end scan y-level for

        Tuple<short[], Integer> smallerResult = removeTopAirOnlyLayers(xOutSize, ySize, zOutSize, templateRuleData);
        templateRuleData = smallerResult.getA();
        ySize = smallerResult.getB();

        Map<Integer, TemplateRuleEntityBase> entityRules = new HashMap<>();
        List<Entity> entitiesInAABB = world.getEntitiesOfClass(Entity.class, new AABB(min.getX(), min.getY(), min.getZ(), max.getX() + 1, max.getY() + 1, max.getZ() + 1));
        nextRuleID = 0;
        for (Entity e : entitiesInAABB) {
            int ex = Mth.floor(e.getX());
            int ey = Mth.floor(e.getY());
            int ez = Mth.floor(e.getZ());
            Optional<TemplateRuleEntityBase> scannedEntityRule = StructurePluginManager.INSTANCE.getRuleForEntity(world, e, turns, ex, ey, ez);
            if (scannedEntityRule.isPresent()) {
                destination = BlockTools.rotateInArea(new BlockPos(ex, ey, ez).subtract(min), xSize, zSize, turns);
                scannedEntityRule.get().ruleNumber = nextRuleID;
                scannedEntityRule.get().setPosition(destination);
                entityRules.put(nextRuleID, scannedEntityRule.get());
                nextRuleID++;
            }
        }

        StructureTemplate template = new StructureTemplate(name, modDependencies, new Vec3i(xOutSize, ySize, zOutSize), key);
        template.setTemplateData(templateRuleData);
        template.setBlockRules(blockRules);
        template.setEntityRules(entityRules);
        return template;
    }

    private static Tuple<short[], Integer> removeTopAirOnlyLayers(int xSize, int ySize, int zSize, short[] templateRuleData) {
        Vec3i size = new Vec3i(xSize, ySize, zSize);
        for (int y = ySize - 1; y >= 0; y--) {
            if (!areAllAirInLayer(size, y, templateRuleData)) {
                if (y == ySize - 1) {
                    return new Tuple<>(templateRuleData, ySize);
                }
                return new Tuple<>(Arrays.copyOf(templateRuleData, xSize * (y + 1) * zSize), y + 1);
            }
        }
        return new Tuple<>(new short[0], 0);
    }

    private static boolean areAllAirInLayer(Vec3i size, int yLayer, short[] templateRuleData) {
        for (int z = 0; z < size.getZ(); z++) {
            for (int x = 0; x < size.getX(); x++) {
                int index = StructureTemplate.getIndex(new Vec3i(x, yLayer, z), size);
                if (templateRuleData[index] != 0) {
                    return false;
                }
            }
        }
        return true;
    }
}
