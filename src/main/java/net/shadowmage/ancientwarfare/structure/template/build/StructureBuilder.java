package net.shadowmage.ancientwarfare.structure.template.build;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import net.minecraft.core.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.Tags;
import net.minecraftforge.registries.ForgeRegistries;
import net.shadowmage.ancientwarfare.core.compat.LegacyMaterial;
import net.shadowmage.ancientwarfare.core.util.BlockTools;
import net.shadowmage.ancientwarfare.core.util.WorldTools;
import net.shadowmage.ancientwarfare.structure.api.IStructureBuilder;
import net.shadowmage.ancientwarfare.structure.api.TemplateRule;
import net.shadowmage.ancientwarfare.structure.api.TemplateRuleBlock;
import net.shadowmage.ancientwarfare.structure.api.TemplateRuleEntityBase;
import net.shadowmage.ancientwarfare.structure.template.StructureTemplate;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import static net.shadowmage.ancientwarfare.structure.template.build.validation.properties.StructureValidationProperties.BIOME_REPLACEMENT;

public class StructureBuilder implements IStructureBuilder {

    protected StructureTemplate template;
    protected Level world;
    BlockPos buildOrigin;
    Direction buildFace;
    protected int turns;
    int maxPriority = 3;
    int currentPriority;//current build priority...may not be needed anymore?
    Vec3i curTempPos;
    BlockPos destination;

    protected StructureBB bb;

    private boolean isFinished = false;
    private boolean isFinalized = false;
    private Holder<Biome> biome;
    private Map<BlockPos, BlockState> statesToSetAgain = new HashMap<>();
    private Map<BlockPos, BlockState> positionsToUpdate = new HashMap<>();

    public StructureBuilder(Level world, StructureTemplate template, Direction face, BlockPos pos) {
        this(world, template, face, pos, new StructureBB(pos, face, template));
    }

    public StructureBuilder(Level world, StructureTemplate template, Direction face, BlockPos buildKey, StructureBB bb) {
        this.world = world;
        biome = world.getBiome(buildKey);
        this.template = template;
        this.buildFace = face;
        this.bb = bb;
        buildOrigin = buildKey;
        destination = BlockPos.ZERO;
        curTempPos = Vec3i.ZERO;
        currentPriority = 0;

        turns = ((face.get2DDataValue() + 2) % 4);
        /*
         * initialize the first target destination so that the structure is ready to start building when called on to build
         */
        incrementDestination();
    }

    public StructureTemplate getTemplate() {
        return template;
    }

    public StructureBB getBoundingBox() {
        return bb;
    }

    public BlockPos getBuildOrigin() {
        return buildOrigin;
    }

    /** Exact set of chunks intersected by this rotated structure bounding box. */
    public Set<ChunkPos> getRequiredChunks() {
        Set<ChunkPos> chunks = new LinkedHashSet<>();
        if (bb == null) {
            return chunks;
        }
        int minChunkX = SectionPos.blockToSectionCoord(bb.min.getX());
        int maxChunkX = SectionPos.blockToSectionCoord(bb.max.getX());
        int minChunkZ = SectionPos.blockToSectionCoord(bb.min.getZ());
        int maxChunkZ = SectionPos.blockToSectionCoord(bb.max.getZ());
        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                chunks.add(new ChunkPos(chunkX, chunkZ));
            }
        }
        return chunks;
    }

    /**
     * World-generation structures are allowed to span chunk borders, but they
     * must not synchronously request an unloaded neighbour while Minecraft is
     * updating its distance/ticket graph. The generation queue will retry the
     * ticket after every intersecting chunk has loaded normally.
     */
    public boolean areRequiredChunksLoaded() {
        if (!(world instanceof ServerLevel serverLevel) || bb == null) {
            return true;
        }

        int minChunkX = SectionPos.blockToSectionCoord(bb.min.getX());
        int maxChunkX = SectionPos.blockToSectionCoord(bb.max.getX());
        int minChunkZ = SectionPos.blockToSectionCoord(bb.min.getZ());
        int maxChunkZ = SectionPos.blockToSectionCoord(bb.max.getZ());

        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                if (!serverLevel.hasChunk(chunkX, chunkZ)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Loads a bounded number of missing chunks while processing the normal
     * server-tick generation queue.  This is deliberately not called from a
     * chunk load callback, where synchronous neighbour loading can deadlock the
     * distance manager.  Large town pieces otherwise wait forever because their
     * outer chunks are never naturally loaded by the player.
     */
    public boolean ensureRequiredChunksLoaded(int maxChunksToLoad) {
        if (!(world instanceof ServerLevel serverLevel) || bb == null) {
            return true;
        }

        int minChunkX = SectionPos.blockToSectionCoord(bb.min.getX());
        int maxChunkX = SectionPos.blockToSectionCoord(bb.max.getX());
        int minChunkZ = SectionPos.blockToSectionCoord(bb.min.getZ());
        int maxChunkZ = SectionPos.blockToSectionCoord(bb.max.getZ());
        int loadedThisPass = 0;

        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                if (serverLevel.hasChunk(chunkX, chunkZ)) {
                    continue;
                }
                if (loadedThisPass >= Math.max(1, maxChunksToLoad)) {
                    return false;
                }
                serverLevel.getChunk(chunkX, chunkZ);
                loadedThisPass++;
            }
        }
        return areRequiredChunksLoaded();
    }

    protected StructureBuilder() {
        destination = BlockPos.ZERO;
        buildOrigin = BlockPos.ZERO;
    }

    /**
     * Preserves the original all-at-once construction path used by ordinary
     * world-generation structures and player-triggered construction.
     */
    public void instantConstruction() {
        while (!this.isFinished()) {
            processCurrentPosition();
        }
        finalizeConstruction();
    }

    /**
     * Processes at most {@code maxPositions} template positions while retaining
     * the builder cursor. Only the town-specific queue calls this method.
     *
     * @return number of template positions processed during this call
     */
    public int buildSome(int maxPositions) {
        if (isFinalized) {
            return 0;
        }

        int processed = 0;
        int limit = Math.max(1, maxPositions);
        while (!this.isFinished() && processed < limit) {
            processCurrentPosition();
            processed++;
        }

        if (this.isFinished()) {
            finalizeConstruction();
        }
        return processed;
    }

    private void processCurrentPosition() {
        Optional<TemplateRuleBlock> rule = template.getRuleAt(curTempPos);
        if (rule.isPresent()) {
            placeCurrentPosition(rule.get());
        } else if (currentPriority == 0) {
            placeAir();
        }
        increment();
    }

    private void finalizeConstruction() {
        if (isFinalized) {
            return;
        }
        setStateAgainForSpecialBlocks();
        updateNeighbors();
        changeBiome();
        this.placeEntities();
        isFinalized = true;
    }

    /**
     * Finalization variant used only by phased large-island generation. The
     * normal builder keeps its original behavior. Island biome replacement is
     * performed separately in small quart-column batches so a 300x300 island
     * cannot freeze one server tick here.
     */
    protected final void finishConstructionWithoutBiome() {
        if (isFinalized) {
            return;
        }
        setStateAgainForSpecialBlocks();
        updateNeighbors();
        this.placeEntities();
        isFinalized = true;
    }

    public boolean isFinalized() {
        return isFinalized;
    }

    private void changeBiome() {
        ResourceLocation biomeRegistryName = template.getValidationSettings().getPropertyValue(BIOME_REPLACEMENT);
        if (!ForgeRegistries.BIOMES.containsKey(biomeRegistryName)) {
            return;
        }

        Biome replacementBiome = ForgeRegistries.BIOMES.getValue(biomeRegistryName);

        BlockPos minPos = bb.min;
        BlockPos maxPos = new BlockPos(bb.max.getX(), bb.min.getY(), bb.max.getZ());
        BlockPos.betweenClosedStream(minPos, maxPos).forEach(pos -> {
            if (isTopBlockSolid(world, pos)) {
                //noinspection ConstantConditions
                WorldTools.changeBiome(world, pos.immutable(), replacementBiome);
            }
        });
    }

    private boolean isTopBlockSolid(Level world, BlockPos pos) {
        LevelChunk chunk = world.getChunkAt(pos);
        BlockPos posDown;
        for (BlockPos currentPos = new BlockPos(pos.getX(), chunk.getHighestSectionPosition() + 16, pos.getZ()); currentPos.getY() >= world.getMinBuildHeight(); currentPos = posDown) {
            posDown = currentPos.below();
            BlockState state = chunk.getBlockState(posDown);

            LegacyMaterial material = LegacyMaterial.of(state);
            //material AIR / leaves / foliage checks from 1.12 - skip anything that isn't a real top block
            if (material != LegacyMaterial.AIR && material != LegacyMaterial.LEAVES && material != LegacyMaterial.VINE) {
                return !material.isLiquid();
            }
        }
        return false;
    }

    private void updateNeighbors() {
        for (Map.Entry<BlockPos, BlockState> entry : positionsToUpdate.entrySet()) {
            world.updateNeighborsAt(entry.getKey(), entry.getValue().getBlock());

            if (entry.getValue().hasAnalogOutputSignal()) {
                world.updateNeighbourForOutputSignal(entry.getKey(), entry.getValue().getBlock());
            }
            //schedule update spread in the next 40 ticks in case we have a lot of redstone somewhere
            world.scheduleTick(entry.getKey(), entry.getValue().getBlock(), world.random.nextInt(40));
        }
    }

    private void setStateAgainForSpecialBlocks() {
        for (Map.Entry<BlockPos, BlockState> entry : statesToSetAgain.entrySet()) {
            world.setBlock(entry.getKey(), entry.getValue(), 2);
        }
    }

    private void placeEntities() {
        Vec3i templateSize = template.getSize();
        for (TemplateRuleEntityBase rule : template.getEntityRules().values()) {
            BlockPos templatePos = rule.getPosition();
            if (templatePos.getX() < 0 || templatePos.getX() >= templateSize.getX()
                    || templatePos.getY() < 0 || templatePos.getY() >= templateSize.getY()
                    || templatePos.getZ() < 0 || templatePos.getZ() >= templateSize.getZ()) {
                net.shadowmage.ancientwarfare.structure.AncientWarfareStructure.LOG.error(
                        "Skipping out-of-bounds entity rule {} in structure '{}' at template position {} (size {})",
                        rule.ruleNumber, template.name, templatePos, templateSize);
                continue;
            }

            destination = BlockTools.rotateInArea(templatePos, templateSize.getX(), templateSize.getZ(), turns).offset(bb.min);
            rule.handlePlacement(world, turns, destination, this);
        }
    }

    /*
     * should be called by template-rules to handle block-placement in the world.
     * Handles village-block swapping during world-gen, and chunk-insert for blocks
     * with priority > 0
     */
    @Override
    public boolean placeBlock(BlockPos pos, BlockState state, int priority) {
        if (pos.getY() <= world.getMinBuildHeight() || pos.getY() >= world.getMaxBuildHeight()) {
            return false;
        }

        BlockState adjustedState = state;
        if (template.getValidationSettings().isBlockSwap()) {
            adjustedState = getBiomeSpecificBlockState(biome, state);
        }

        boolean result = world.setBlock(pos, adjustedState, 2);
        if (result) {
            if (DOUBLE_SET_BLOCKS.contains(adjustedState.getBlock())) {
                statesToSetAgain.put(pos, adjustedState);
            }
            if (state.isSignalSource()) {
                positionsToUpdate.put(pos, adjustedState);
            }
        }

        return result;
    }

    private void placeCurrentPosition(TemplateRule rule) {
        if (rule.shouldPlaceOnBuildPass(world, turns, destination, currentPriority)) {
            this.placeRule(rule);
        }
    }

    protected boolean increment() {
        if (isFinished) {
            return false;
        }
        if (incrementPosition()) {
            incrementDestination();
        } else {
            this.isFinished = true;
        }
        return !isFinished;
    }

    private void placeAir() {
        if (!template.getValidationSettings().isPreserveBlocks()) {
            world.setBlock(destination, Blocks.AIR.defaultBlockState(), 3);
        }
    }

    void placeRule(TemplateRule rule) {
        if (destination.getY() <= world.getMinBuildHeight()) {
            return;
        }
        rule.handlePlacement(world, turns, destination, this);
    }

    void incrementDestination() {
        destination = BlockTools.rotateInArea(new BlockPos(curTempPos), template.getSize().getX(), template.getSize().getZ(), turns).offset(bb.min);
    }

    /*
     * return true if could increment position
     * return false if template is finished
     */
    private boolean incrementPosition() {
        int currentX = curTempPos.getX();
        int currentY = curTempPos.getY();
        int currentZ = curTempPos.getZ();
        currentX++;
        if (currentX >= template.getSize().getX()) {
            currentX = 0;
            currentZ++;
            if (currentZ >= template.getSize().getZ()) {
                currentZ = 0;
                currentY++;
                if (currentY >= template.getSize().getY()) {
                    currentY = 0;
                    currentPriority++;
                    if (currentPriority > maxPriority) {
                        currentPriority = 0;
                        return false;
                    }
                }
            }
        }
        curTempPos = new Vec3i(currentX, currentY, currentZ);
        return true;
    }

    public boolean isFinished() {
        return isFinished;
    }

    float getTotalBlocks() {
        return (float) template.getSize().getX() * template.getSize().getZ() * template.getSize().getY();
    }

    public float getPercentDoneWithPass() {
        float max = getTotalBlocks();
        float current = (float) curTempPos.getY() * (template.getSize().getX() * template.getSize().getZ());//add layers done
        current += curTempPos.getZ() * template.getSize().getX();//add rows done
        current += curTempPos.getX();//add blocks done
        return current / max;
    }

    public int getPass() {
        return currentPriority;
    }

    public int getMaxPasses() {
        return maxPriority;
    }

    private BlockState getBiomeSpecificBlockState(Holder<Biome> biome, BlockState originalBlockState) {
        //note: the 1.12 BiomeEvent.GetVillageBlockID hook no longer exists in modern Forge; only the internal swaps below apply

        for (Map.Entry<TagKey<Biome>, Set<IBlockSwapMapping>> entry : BIOME_SWAP_STATES.entrySet()) {
            if (biome.is(entry.getKey())) {
                for (IBlockSwapMapping mapping : entry.getValue()) {
                    if (mapping.matches(originalBlockState.getBlock())) {
                        return mapping.swap(originalBlockState);
                    }
                }
            }
        }

        return originalBlockState;
    }

    private static BlockState copyLogAxis(BlockState source, BlockState target) {
        return source.hasProperty(RotatedPillarBlock.AXIS) ? target.setValue(RotatedPillarBlock.AXIS, source.getValue(RotatedPillarBlock.AXIS)) : target;
    }

    private static final Set<Block> DOUBLE_SET_BLOCKS = ImmutableSet.of(Blocks.RAIL, Blocks.ACTIVATOR_RAIL, Blocks.DETECTOR_RAIL, Blocks.POWERED_RAIL);

    // @formatter:off
	private static final Map<TagKey<Biome>, Set<IBlockSwapMapping>> BIOME_SWAP_STATES = ImmutableMap.of(
			Tags.Biomes.IS_DESERT, ImmutableSet.of(
					new BlockSwapMapping(b -> b.defaultBlockState().is(BlockTags.LOGS), s -> Blocks.SANDSTONE.defaultBlockState()),
					new BlockSwapMapping(b -> b == Blocks.COBBLESTONE, s -> Blocks.SANDSTONE.defaultBlockState()),
					new BlockSwapMapping(b -> b.defaultBlockState().is(BlockTags.PLANKS),
							s -> Blocks.CUT_SANDSTONE.defaultBlockState()),
					new BlockSwapMapping(b -> b == Blocks.OAK_STAIRS,
							s -> Blocks.SANDSTONE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, s.getValue(StairBlock.FACING))),
					new BlockSwapMapping(b -> b == Blocks.COBBLESTONE_STAIRS,
							s -> Blocks.SANDSTONE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, s.getValue(StairBlock.FACING))),
					new BlockSwapMapping(b -> b == Blocks.GRAVEL, s -> Blocks.SANDSTONE.defaultBlockState())),
			BiomeTags.IS_TAIGA, ImmutableSet.of(
					new BlockSwapMapping(b -> b.defaultBlockState().is(BlockTags.LOGS),
							s -> copyLogAxis(s, Blocks.SPRUCE_LOG.defaultBlockState())),
					new BlockSwapMapping(b -> b.defaultBlockState().is(BlockTags.PLANKS),
							s -> Blocks.SPRUCE_PLANKS.defaultBlockState()),
					new BlockSwapMapping(b -> b == Blocks.OAK_STAIRS,
							s -> Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, s.getValue(StairBlock.FACING))),
					new BlockSwapMapping(b -> b == Blocks.OAK_FENCE, s -> Blocks.SPRUCE_FENCE.defaultBlockState())),
			BiomeTags.IS_SAVANNA, ImmutableSet.of(
					new BlockSwapMapping(b -> b.defaultBlockState().is(BlockTags.LOGS),
							s -> copyLogAxis(s, Blocks.ACACIA_LOG.defaultBlockState())),
					new BlockSwapMapping(b -> b.defaultBlockState().is(BlockTags.PLANKS),
							s -> Blocks.ACACIA_PLANKS.defaultBlockState()),
					new BlockSwapMapping(b -> b == Blocks.OAK_STAIRS,
							s -> Blocks.ACACIA_STAIRS.defaultBlockState().setValue(StairBlock.FACING, s.getValue(StairBlock.FACING))),
					new BlockSwapMapping(b -> b == Blocks.COBBLESTONE, s -> Blocks.ACACIA_LOG.defaultBlockState()),
					new BlockSwapMapping(b -> b == Blocks.OAK_FENCE, s -> Blocks.ACACIA_FENCE.defaultBlockState()))
	);

	public Direction getBuildFace() {
		return buildFace;
	}

	// @formatter:on

    private interface IBlockSwapMapping {
        boolean matches(Block block);

        BlockState swap(BlockState state);
    }

    private static class BlockSwapMapping implements IBlockSwapMapping {
        private final Predicate<Block> blockMatcher;
        private final Function<BlockState, BlockState> doSwap;

        private BlockSwapMapping(Predicate<Block> blockMatcher, Function<BlockState, BlockState> doSwap) {
            this.blockMatcher = blockMatcher;
            this.doSwap = doSwap;
        }

        @Override
        public boolean matches(Block block) {
            return blockMatcher.test(block);
        }

        @Override
        public BlockState swap(BlockState state) {
            return doSwap.apply(state);
        }
    }
}
