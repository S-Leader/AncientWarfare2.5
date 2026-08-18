package net.shadowmage.ancientwarfare.structure.template.build;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.shadowmage.ancientwarfare.core.util.BlockTools;
import net.shadowmage.ancientwarfare.core.util.MathUtils;
import net.shadowmage.ancientwarfare.structure.api.TemplateRuleBlock;
import net.shadowmage.ancientwarfare.structure.template.StructureTemplate;
import net.shadowmage.ancientwarfare.structure.template.StructureTemplateManager;

import java.util.Optional;

public class StructureBuilderTicked extends StructureBuilder {
    private static final String CLEAR_POS_TAG = "clearPos";
    private static final String CLEARED_TAG = "cleared";
    private static final String CURRENT_PRIORITY_TAG = "currentPriority";
    private static final String DESTINATION_TAG = "destination";
    public boolean invalid = false;
    private boolean hasClearedArea;
    private BlockPos clearPos;

    public StructureBuilderTicked(Level world, StructureTemplate template, Direction face, BlockPos pos) {
        super(world, template, face, pos);
        clearPos = new BlockPos(bb.min.getX(), bb.max.getY(), bb.min.getZ());
    }

    public StructureBuilderTicked()//nbt-constructor
    {

    }

    public void tick() {
        if (!hasClearedArea) {
            while (!breakClearTargetBlock()) {
                if (!incrementClear()) {
                    hasClearedArea = true;
                    break;
                }
            }
            if (!incrementClear()) {
                hasClearedArea = true;
            }
        } else if (!this.isFinished()) {
            while (!this.isFinished()) {
                if (placeAtCurrentPos())
                    break;
            }
            increment();//finally, increment to next position (will trigger isFinished if actually done, has no problems if already finished)
        }
    }

    private boolean placeAtCurrentPos() {
        Optional<TemplateRuleBlock> rule = template.getRuleAt(curTempPos);
        if (!rule.isPresent() || !rule.get().placeInSurvival() || !rule.get().shouldPlaceOnBuildPass(world, turns, destination, currentPriority)) {
            increment();//skip that position, was either air/null rule, or could not be placed on current pass, auto-increment to next
        } else//place it...
        {
            rule.ifPresent(this::placeRule);
            return true;
        }
        return false;
    }

    private boolean breakClearTargetBlock() {
        return BlockTools.breakBlockAndDrop(world, clearPos);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean incrementClear() {
        clearPos = clearPos.east();
        if (clearPos.getX() > bb.max.getX()) {
            clearPos = new BlockPos(bb.min.getX(), clearPos.getY(), clearPos.getZ());
            clearPos = clearPos.south();
            if (clearPos.getZ() > bb.max.getZ()) {
                clearPos = new BlockPos(bb.min.getX(), clearPos.below().getY(), bb.min.getZ());
                return clearPos.getY() >= bb.min.getY();
            }
        }
        return true;
    }

    public void setWorld(Level world)//should be called on first-update of the TE (after its world is set)
    {
        this.world = world;
    }

    public Level getWorld() {
        return world;
    }

    public void readFromNBT(CompoundTag tag)//should be called immediately after construction
    {
        String name = tag.getString("name");
        Optional<StructureTemplate> template = StructureTemplateManager.getTemplate(name);
        if (template.isPresent()) {
            this.template = template.get();
            curTempPos = MathUtils.fromLong(tag.getLong("pos"));
            this.clearPos = BlockPos.of(tag.getLong(CLEAR_POS_TAG));
            this.hasClearedArea = tag.getBoolean(CLEARED_TAG);
            this.turns = tag.getInt("turns");
            this.buildFace = Direction.values()[tag.getByte("buildFace")];
            this.maxPriority = tag.getInt("maxPriority");
            this.currentPriority = tag.getInt(CURRENT_PRIORITY_TAG);
            this.destination = BlockPos.of(tag.getLong(DESTINATION_TAG));

            this.bb = new StructureBB(BlockPos.of(tag.getLong("bbMin")), BlockPos.of(tag.getLong("bbMax")));
            this.buildOrigin = BlockPos.of(tag.getLong("buildOrigin"));
            this.incrementDestination();
        } else {
            invalid = true;
        }
    }

    public void writeToNBT(CompoundTag tag) {
        tag.putString("name", template.name);
        tag.putByte("face", (byte) getBuildFace().ordinal());
        tag.putInt("turns", turns);
        tag.putInt("maxPriority", maxPriority);
        tag.putInt(CURRENT_PRIORITY_TAG, currentPriority);
        tag.putLong("pos", MathUtils.toLong(curTempPos));
        tag.putLong(CLEAR_POS_TAG, clearPos.asLong());
        tag.putBoolean(CLEARED_TAG, hasClearedArea);
        tag.putLong(DESTINATION_TAG, destination.asLong());

        tag.putLong("buildOrigin", buildOrigin.asLong());
        tag.putLong("bbMin", bb.min.asLong());
        tag.putLong("bbMax", bb.max.asLong());
    }

    @Override
    public StructureTemplate getTemplate() {
        return template;
    }

    public CompoundTag serializeProgressData() {
        CompoundTag tag = new CompoundTag();
        tag.putInt(CURRENT_PRIORITY_TAG, currentPriority);
        tag.putLong("pos", MathUtils.toLong(curTempPos));
        tag.putLong(CLEAR_POS_TAG, clearPos.asLong());
        tag.putBoolean(CLEARED_TAG, hasClearedArea);
        tag.putLong(DESTINATION_TAG, destination.asLong());
        return tag;
    }

    public void deserializeProgressData(CompoundTag tag) {
        currentPriority = tag.getInt(CURRENT_PRIORITY_TAG);
        curTempPos = MathUtils.fromLong(tag.getLong("pos"));
        clearPos = BlockPos.of(tag.getLong(CLEAR_POS_TAG));
        hasClearedArea = tag.getBoolean(CLEARED_TAG);
        destination = BlockPos.of(tag.getLong(DESTINATION_TAG));
    }

    public float getPercentDoneClearing() {
        float max = getTotalBlocks();
        BlockPos relativeClearPos = clearPos.offset(-bb.min.getX(), -bb.max.getY(), -bb.min.getZ());
        float current = (float) -relativeClearPos.getY() * (bb.getXSize() * bb.getZSize());//add layers done
        current += relativeClearPos.getZ() * bb.getXSize();//add rows done
        current += relativeClearPos.getX();//add blocks done
        return current / max;
    }

    public boolean hasClearedArea() {
        return hasClearedArea;
    }
}
