package net.shadowmage.ancientwarfare.structure.town;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.shadowmage.ancientwarfare.structure.template.StructureTemplate;
import net.shadowmage.ancientwarfare.structure.template.StructureTemplateManager;
import net.shadowmage.ancientwarfare.structure.template.build.StructureBB;
import net.shadowmage.ancientwarfare.structure.template.build.StructureBuilder;

import java.util.*;

/**
 * Deterministic, side-effect-free town build plan.
 * <p>
 * The original 1.12 town layout code is still responsible for deciding quadrants,
 * plots, wall patterns, building orientation and lamp locations. During planning
 * we intercept the old queue calls and road writes, turning them into immutable
 * placements. The plan can therefore be rebuilt after a server restart from only
 * the town template + bounding area, while the actual world mutation is advanced
 * by the persistent town state machine.
 */
public final class TownGenerationPlan {
    public enum Section {WALLS, BUILDINGS, LAMPS}

    private static final ThreadLocal<TownGenerationPlan> CAPTURE = new ThreadLocal<>();
    private static final ThreadLocal<Section> SECTION = ThreadLocal.withInitial(() -> Section.BUILDINGS);

    private final List<PlannedStructure> walls = new ArrayList<>();
    private final List<PlannedStructure> buildings = new ArrayList<>();
    private final List<PlannedStructure> lamps = new ArrayList<>();
    private final Set<BlockPos> roads = new LinkedHashSet<>();

    static void begin(TownGenerationPlan plan) {
        if (CAPTURE.get() != null) {
            throw new IllegalStateException("Nested town plan capture is not supported");
        }
        CAPTURE.set(plan);
        SECTION.set(Section.BUILDINGS);
    }

    static void end() {
        CAPTURE.remove();
        SECTION.remove();
    }

    public static boolean isCapturing() {
        return CAPTURE.get() != null;
    }

    public static Section getSection() {
        return SECTION.get();
    }

    public static void setSection(Section section) {
        if (isCapturing()) {
            SECTION.set(section);
        }
    }

    public static boolean captureBuilder(StructureBuilder builder) {
        TownGenerationPlan plan = CAPTURE.get();
        if (plan == null) {
            return false;
        }
        StructureBB bb = builder.getBoundingBox();
        PlannedStructure placement = new PlannedStructure(
                builder.getTemplate().name,
                builder.getBuildFace(),
                builder.getBuildOrigin().immutable(),
                bb.min.immutable(),
                bb.max.immutable());
        switch (SECTION.get()) {
            case WALLS -> plan.walls.add(placement);
            case LAMPS -> plan.lamps.add(placement);
            default -> plan.buildings.add(placement);
        }
        return true;
    }

    public static boolean captureRoad(BlockPos pos) {
        TownGenerationPlan plan = CAPTURE.get();
        if (plan == null) {
            return false;
        }
        plan.roads.add(pos.immutable());
        return true;
    }

    public List<PlannedStructure> walls() {
        return walls;
    }

    public List<PlannedStructure> buildings() {
        return buildings;
    }

    public List<PlannedStructure> lamps() {
        return lamps;
    }

    public List<BlockPos> roads() {
        return List.copyOf(roads);
    }

    public record PlannedStructure(String templateName, Direction face, BlockPos buildOrigin,
                                   BlockPos bbMin, BlockPos bbMax) {
        public Optional<StructureBuilder> createBuilder(net.minecraft.world.level.Level world) {
            Optional<StructureTemplate> template = StructureTemplateManager.getTemplate(templateName);
            return template.map(value -> new StructureBuilder(world, value, face, buildOrigin,
                    new StructureBB(bbMin, bbMax)));
        }
    }
}
