package net.shadowmage.ancientwarfare.structure.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.shadowmage.ancientwarfare.core.gamedata.AWGameData;
import net.shadowmage.ancientwarfare.structure.AncientWarfareStructure;
import net.shadowmage.ancientwarfare.structure.config.AWStructureStatics;
import net.shadowmage.ancientwarfare.structure.gamedata.StructureEntry;
import net.shadowmage.ancientwarfare.structure.gamedata.StructureMap;
import net.shadowmage.ancientwarfare.structure.gamedata.TownMap;
import net.shadowmage.ancientwarfare.structure.registry.TerritorySettingRegistry;
import net.shadowmage.ancientwarfare.structure.template.StructureTemplate;
import net.shadowmage.ancientwarfare.structure.template.WorldGenStructureManager;
import net.shadowmage.ancientwarfare.structure.template.build.StructureBB;
import net.shadowmage.ancientwarfare.structure.template.build.StructureBuilderWorldGen;
import net.shadowmage.ancientwarfare.structure.worldgen.stats.PlacementRejectionReason;
import net.shadowmage.ancientwarfare.structure.worldgen.stats.WorldGenStatistics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

public class WorldStructureGenerator {

    public static final WorldStructureGenerator INSTANCE = new WorldStructureGenerator();

    private static final int MAX_DISTANCE_WITHIN_CLUSTER = 150;
    private static final int VALIDATION_CHUNK_TICKET_DISTANCE = 2;
    private static final int MAX_VALIDATION_CHUNK_REQUESTS_PER_TICK = 2;
    private static final TicketType<ChunkPos> STRUCTURE_VALIDATION_TICKET =
            TicketType.create(
                    "ancientwarfare_structure_validation",
                    Comparator.comparingLong((ChunkPos chunkPos) -> chunkPos.toLong())
            );

    private final Random rng;
    private boolean debugTerritoryBorders = false;

    private WorldStructureGenerator() {
        rng = new Random();
    }

    /**
     * Called once for newly-created chunks by {@link WorldGenerationEventHandler}.
     * The expensive structure validation and placement still runs through the
     * existing tick queue so chunk generation is not blocked by a full template build.
     */
    public void queueChunkForGeneration(int chunkX, int chunkZ, Level world) {
        TerritoryManager.getTerritory(chunkX, chunkZ, world).ifPresent(territory -> {
            if (debugTerritoryBorders) {
                generateTerritoryBorders(chunkX, chunkZ, world, territory.getTerritoryId());
            }
            BlockPos spawn = world.getSharedSpawnPos();
            BlockPos chunkOrigin = new BlockPos(chunkX * 16, spawn.getY(), chunkZ * 16);
            double distSq = spawn.distSqr(chunkOrigin);
            if (AWStructureStatics.withinProtectionRange(distSq)) {
                return;
            }
            if (rng.nextFloat() < (AWStructureStatics.randomGenerationChance
                    * TerritorySettingRegistry.getTerritorySettings(territory.getTerritoryName()).getStructureGenerationChanceMultiplier())) {
                WorldGenTickHandler.INSTANCE.addChunkForGeneration(world, chunkX, chunkZ);
            }
        });
    }

    void generateAt(int chunkX, int chunkZ, Level world) {
        long t1 = System.currentTimeMillis();
        long seed = (((long) chunkX) << 32) | (((long) chunkZ) & 0xffffffffL);
        rng.setSeed(seed);
        int x = chunkX * 16 + rng.nextInt(16);
        int z = chunkZ * 16 + rng.nextInt(16);
        int y = getTargetY(world, x, z, false) + 1;
        if (y <= 0) {
            return;
        }

        TerritoryManager.getTerritory(chunkX, chunkZ, world).ifPresent(territory -> {
            Direction face = Direction.from2DDataValue(rng.nextInt(4));
            world.getProfiler().push("AWTemplateSelection");
            Optional<StructureTemplate> t = WorldGenStructureManager.INSTANCE.selectTemplateForGeneration(world, rng, x, y, z, face, territory);
            world.getProfiler().pop();
            AncientWarfareStructure.LOG.debug("Template selection took: {} ms.", System.currentTimeMillis() - t1);
            if (!t.isPresent()) {
                return;
            }
            StructureTemplate template = t.get();
            StructureMap map = AWGameData.INSTANCE.getPerWorldData(world, StructureMap.class);

            BlockPos initialPos = new BlockPos(x, y, z);
            if (world instanceof ServerLevel serverLevel) {
                /*
                 * Validation can scan the complete template footprint. For very
                 * large ocean islands that footprint spans hundreds of chunks.
                 * Queue a ticket which asks the normal chunk pipeline to load a
                 * small number of chunks per tick before any validator calls
                 * getBlockState/getHeight. Never synchronously call getChunk here.
                 */
                WorldGenTickHandler.INSTANCE.addStructureGenCallback(
                        new PlacementValidationTicket(
                                serverLevel,
                                initialPos,
                                face,
                                template,
                                map,
                                territory,
                                t1
                        )
                );
            } else {
                attemptAndRecord(world, initialPos, face, template, map, territory, t1);
            }
        });
    }

    private void generateTerritoryBorders(int chunkX, int chunkZ, Level world, String territoryId) {
        ITerritoryData territoryData = CapabilityTerritoryData.get(world).orElse(null);
        if (territoryData == null) {
            return;
        }
        if (territoryData.isDifferentTerritory(territoryId, chunkX - 1, chunkZ)) {
            for (int z = chunkZ * 16 + 1; z < (chunkZ * 16 + 15); z++) {
                int x = chunkX * 16 + 1;
                world.setBlock(new BlockPos(x, WorldStructureGenerator.getTargetY(world, x, z, false), z), Blocks.WHITE_CONCRETE.defaultBlockState(), 2);
            }
        }

        if (territoryData.isDifferentTerritory(territoryId, chunkX + 1, chunkZ)) {
            for (int z = chunkZ * 16 + 1; z < (chunkZ * 16 + 15); z++) {
                int x = chunkX * 16 + 14;
                world.setBlock(new BlockPos(x, WorldStructureGenerator.getTargetY(world, x, z, false), z), Blocks.WHITE_CONCRETE.defaultBlockState(), 2);
            }
        }

        if (territoryData.isDifferentTerritory(territoryId, chunkX, chunkZ - 1)) {
            for (int x = chunkX * 16 + 1; x < (chunkX * 16 + 15); x++) {
                int z = chunkZ * 16 + 1;
                world.setBlock(new BlockPos(x, WorldStructureGenerator.getTargetY(world, x, z, false), z), Blocks.WHITE_CONCRETE.defaultBlockState(), 2);
            }
        }

        if (territoryData.isDifferentTerritory(territoryId, chunkX, chunkZ + 1)) {
            for (int x = chunkX * 16 + 1; x < (chunkX * 16 + 15); x++) {
                int z = chunkZ * 16 + 14;
                world.setBlock(new BlockPos(x, WorldStructureGenerator.getTargetY(world, x, z, false), z), Blocks.WHITE_CONCRETE.defaultBlockState(), 2);
            }
        }
    }


    public static int getTargetY(Level world, int x, int z, boolean skipWater) {
        return getTargetY(world, x, z, skipWater, world.getMaxBuildHeight() - 1);
    }

    public static int getTargetY(Level world, int x, int z, boolean skipWater, int startAtY) {
        if (world.dimension() == Level.NETHER) {
            // Keep the original Nether placement convention.
            return 31;
        }

        /*
         * A height probe must never become a synchronous neighbouring-chunk
         * load. World-generation validation tickets prepare those chunks first;
         * selection probes simply reject a candidate until its chunk is ready.
         */
        int chunkX = SectionPos.blockToSectionCoord(x);
        int chunkZ = SectionPos.blockToSectionCoord(z);
        if (!world.hasChunk(chunkX, chunkZ)) {
            return -1;
        }

        int minimumY = world.getMinBuildHeight();
        int maximumY = Math.min(startAtY, world.getMaxBuildHeight() - 1);
        for (int y = maximumY; y >= minimumY; y--) {
            BlockState state = world.getBlockState(new BlockPos(x, y, z));
            if (AWStructureStatics.isSkippable(state) || (skipWater && state.getFluidState().is(FluidTags.WATER))) {
                continue;
            }
            return y;
        }
        return -1;
    }

    public static void sprinkleSnow(Level world, StructureBB bb, int border) {
        BlockPos p1 = bb.min.offset(-border, 0, -border);
        BlockPos p2 = bb.max.offset(border, 0, border);
        for (int x = p1.getX(); x <= p2.getX(); x++) {
            for (int z = p1.getZ(); z <= p2.getZ(); z++) {
                BlockPos snowPos = world.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, new BlockPos(x, world.getMinBuildHeight(), z));
                BlockPos supportPos = snowPos.below();
                if (snowPos.getY() <= p2.getY()
                        && snowPos.getY() > world.getMinBuildHeight()
                        && world.getBiome(snowPos).value().coldEnoughToSnow(snowPos)) {
                    BlockState support = world.getBlockState(supportPos);
                    BlockState snow = Blocks.SNOW.defaultBlockState();
                    if (!support.isAir() && support.isFaceSturdy(world, supportPos, Direction.UP) && snow.canSurvive(world, snowPos)) {
                        world.setBlock(snowPos, snow, 3);
                    }
                }
            }
        }
    }

    public static int getStepNumber(int x, int z, int minX, int maxX, int minZ, int maxZ) {
        int steps = 0;
        if (x < minX - 1) {
            steps += (minX - 1) - x;
        } else if (x > maxX + 1) {
            steps += x - (maxX + 1);
        }
        if (z < minZ - 1) {
            steps += (minZ - 1) - z;
        } else if (z > maxZ + 1) {
            steps += z - (maxZ + 1);
        }
        return steps;
    }

    private void attemptAndRecord(
            Level world,
            BlockPos initialPos,
            Direction face,
            StructureTemplate template,
            StructureMap map,
            Territory territory,
            long selectionStartTime
    ) {
        world.getProfiler().push("AWTemplateGeneration");
        try {
            PreparedPlacement placement = preparePlacement(world, initialPos, face, template);
            if (attemptStructureGenerationAt(world, placement, face, template, map)) {
                territory.addClusterValue(template.getValidationSettings().getClusterValue());
                BlockPos pos = placement.pos();
                AncientWarfareStructure.LOG.info(
                        "Generated structure: {} at {}, {}, {}, time: {}ms",
                        template.name,
                        pos.getX(),
                        pos.getY(),
                        pos.getZ(),
                        System.currentTimeMillis() - selectionStartTime
                );
            }
        } finally {
            world.getProfiler().pop();
        }
    }

    private PreparedPlacement preparePlacement(
            Level world,
            BlockPos initialPos,
            Direction face,
            StructureTemplate template
    ) {
        StructureBB bb = new StructureBB(initialPos, face, template.getSize(), template.getOffset());
        int adjustedY = template.getValidationSettings().getAdjustedSpawnY(
                world,
                initialPos.getX(),
                initialPos.getY(),
                initialPos.getZ(),
                face,
                template,
                bb
        );
        int verticalOffset = adjustedY - initialPos.getY();
        if (verticalOffset != 0) {
            bb.add(0, verticalOffset, 0);
        }
        return new PreparedPlacement(
                new BlockPos(initialPos.getX(), adjustedY, initialPos.getZ()),
                bb
        );
    }

    private boolean attemptStructureGenerationAt(
            Level world,
            PreparedPlacement placement,
            Direction face,
            StructureTemplate template,
            StructureMap map
    ) {
        long t1 = System.currentTimeMillis();
        BlockPos pos = placement.pos();
        StructureBB bb = placement.bb();
        int xs = bb.getXSize();
        int zs = bb.getZSize();
        int size = ((Math.max(xs, zs)) / 16) + 3;
        if (!checkOtherStructureCrossAndCloseness(world, pos, map, bb, size, template.getValidationSettings().getBorderSize())) {
            WorldGenStatistics.addStructurePlacementRejection(template.name, PlacementRejectionReason.STRUCTURE_BB_OVERLAP);
            WorldGenDetailedLogHelper.log("Structure \"{}\" failed placement, because its bounding box {} intersects an existing structure", () -> template.name, () -> bb);
            return false;
        }

        TownMap townMap = AWGameData.INSTANCE.getPerWorldData(world, TownMap.class);
        if (townMap.intersectsWithTown(bb)) {
            WorldGenStatistics.addStructurePlacementRejection(template.name, PlacementRejectionReason.TOWN_BB_OVERLAP);
            WorldGenDetailedLogHelper.log("Structure \"{}\" failed placement, because its bounding box {} intersects an existing town", () -> template.name, () -> bb);
            return false;
        }
        if (template.getValidationSettings().validatePlacement(world, pos.getX(), pos.getY(), pos.getZ(), face, template, bb)) {
            WorldGenStatistics.addStructureGeneratedInfo(template.name, world, pos);
            AncientWarfareStructure.LOG.debug("Validation took: {} ms", System.currentTimeMillis() - t1);
            generateStructureAt(world, pos, face, template, map);
            return true;
        }
        return false;
    }

    private record PreparedPlacement(BlockPos pos, StructureBB bb) {
    }

    private final class PlacementValidationTicket implements WorldGenTickHandler.StructureTicket {
        private final ServerLevel world;
        private final BlockPos initialPos;
        private final Direction face;
        private final StructureTemplate template;
        private final StructureMap map;
        private final Territory territory;
        private final long selectionStartTime;
        private final StructureBB rawBoundingBox;
        private final Set<Long> requestedChunks = new HashSet<>();
        private boolean complete;

        private PlacementValidationTicket(
                ServerLevel world,
                BlockPos initialPos,
                Direction face,
                StructureTemplate template,
                StructureMap map,
                Territory territory,
                long selectionStartTime
        ) {
            this.world = world;
            this.initialPos = initialPos;
            this.face = face;
            this.template = template;
            this.map = map;
            this.territory = territory;
            this.selectionStartTime = selectionStartTime;
            this.rawBoundingBox = new StructureBB(
                    initialPos,
                    face,
                    template.getSize(),
                    template.getOffset()
            );
        }

        @Override
        public boolean isReady() {
            if (complete || !WorldGenTickHandler.INSTANCE.isAcceptingTickets()) {
                return false;
            }

            int[] bounds = getRequiredChunkBounds(
                    rawBoundingBox,
                    template.getValidationSettings().getBorderSize()
            );
            int requestsThisTick = 0;
            boolean allLoaded = true;

            for (int chunkX = bounds[0]; chunkX <= bounds[1]; chunkX++) {
                for (int chunkZ = bounds[2]; chunkZ <= bounds[3]; chunkZ++) {
                    if (world.hasChunk(chunkX, chunkZ)) {
                        continue;
                    }

                    allLoaded = false;
                    ChunkPos chunkPos = new ChunkPos(chunkX, chunkZ);
                    long packed = chunkPos.toLong();
                    if (!requestedChunks.contains(packed)
                            && requestsThisTick < MAX_VALIDATION_CHUNK_REQUESTS_PER_TICK) {
                        requestedChunks.add(packed);
                        world.getChunkSource().addRegionTicket(
                                STRUCTURE_VALIDATION_TICKET,
                                chunkPos,
                                VALIDATION_CHUNK_TICKET_DISTANCE,
                                chunkPos
                        );
                        requestsThisTick++;
                    }
                }
            }
            return allLoaded;
        }

        @Override
        public void call() {
            if (complete) {
                return;
            }
            try {
                attemptAndRecord(
                        world,
                        initialPos,
                        face,
                        template,
                        map,
                        territory,
                        selectionStartTime
                );
            } finally {
                complete = true;
                releaseChunkTickets();
            }
        }

        @Override
        public int getBlocksToGenerate() {
            return 1;
        }

        @Override
        public boolean isComplete() {
            return complete;
        }

        @Override
        public void cancel(boolean releaseChunkTickets) {
            complete = true;
            if (releaseChunkTickets) {
                releaseChunkTickets();
            } else {
                // ServerChunkCache is shutting down. Do not mutate tickets now.
                requestedChunks.clear();
            }
        }

        private void releaseChunkTickets() {
            for (long packed : requestedChunks) {
                ChunkPos chunkPos = new ChunkPos(packed);
                world.getChunkSource().removeRegionTicket(
                        STRUCTURE_VALIDATION_TICKET,
                        chunkPos,
                        VALIDATION_CHUNK_TICKET_DISTANCE,
                        chunkPos
                );
            }
            requestedChunks.clear();
        }
    }

    private static int[] getRequiredChunkBounds(StructureBB bb, int borderSize) {
        int padding = Math.max(1, borderSize + 11);
        return new int[] {
                SectionPos.blockToSectionCoord(bb.min.getX() - padding),
                SectionPos.blockToSectionCoord(bb.max.getX() + padding),
                SectionPos.blockToSectionCoord(bb.min.getZ() - padding),
                SectionPos.blockToSectionCoord(bb.max.getZ() + padding)
        };
    }

    private boolean checkOtherStructureCrossAndCloseness(Level world, BlockPos pos, StructureMap map, StructureBB bb, int size, int borderSize) {
        Collection<StructureEntry> bbCheckList = map.getEntriesNear(world, pos.getX(), pos.getZ(), size, true, new ArrayList<>());
        double maxDistance = 0;
        StructureBB bbWithBorder = new StructureBB(bb.min, bb.max).expand(borderSize, 0, borderSize);
        for (StructureEntry entry : bbCheckList) {
            if (bbWithBorder.intersects(entry.getBB())) {
                return false;
            }
            double distance = bb.getDistanceTo(entry.getBB());
            if (distance < MAX_DISTANCE_WITHIN_CLUSTER && distance > maxDistance) {
                maxDistance = distance;
            }
        }
        return !(maxDistance > 30 && world.getRandom().nextFloat() * (MAX_DISTANCE_WITHIN_CLUSTER - maxDistance) > 30);
    }

    private void generateStructureAt(Level world, BlockPos pos, Direction face, StructureTemplate template, StructureMap map) {
        map.setGeneratedAt(world, pos.getX(), pos.getZ(), new StructureEntry(pos.getX(), pos.getY(), pos.getZ(), face, template), template.getValidationSettings().isUnique());
        WorldGenTickHandler.INSTANCE.addStructureForGeneration(new StructureBuilderWorldGen(world, template, face, pos));
    }

}
