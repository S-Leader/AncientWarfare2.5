package net.shadowmage.ancientwarfare.structure.template.plugin.defaultplugins.blockrules;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.shadowmage.ancientwarfare.core.block.BlockRotationHandler;
import net.shadowmage.ancientwarfare.core.interfaces.IBoundedSite;
import net.shadowmage.ancientwarfare.core.util.BlockTools;
import net.shadowmage.ancientwarfare.core.util.NBTHelper;
import net.shadowmage.ancientwarfare.core.util.WorldTools;
import net.shadowmage.ancientwarfare.structure.api.IStructureBuilder;
import net.shadowmage.ancientwarfare.structure.api.TemplateRuleBlock;

import java.util.Optional;

public class TemplateRuleRotatable extends TemplateRuleBlock {
    private static final String TE_DATA_TAG = "teData";
    public static final String PLUGIN_NAME = "rotatable";
    private Direction orientation;
    private BlockPos p1;
    private BlockPos p2;
    CompoundTag tag;

    public TemplateRuleRotatable(Level world, BlockPos pos, BlockState state, int turns) {
        super(state, turns);
        Optional<BlockEntity> te = WorldTools.getTile(world, pos);
        if (te.isPresent()) {
            BlockEntity worksite = te.get();
            Direction o = ((BlockRotationHandler.IRotatableTile) worksite).getPrimaryFacing();
            if (o.getAxis() != Direction.Axis.Y) {
                for (int i = 0; i < turns; i++) {
                    o = o.getClockWise();
                }
            }
            this.orientation = o;
            if (worksite instanceof IBoundedSite && ((IBoundedSite) worksite).hasWorkBounds()) {
                p1 = BlockTools.rotateAroundOrigin(((IBoundedSite) worksite).getWorkBoundsMin().offset(-pos.getX(), -pos.getY(), -pos.getZ()), turns);
                p2 = BlockTools.rotateAroundOrigin(((IBoundedSite) worksite).getWorkBoundsMax().offset(-pos.getX(), -pos.getY(), -pos.getZ()), turns);
            }
            tag = worksite.saveWithFullMetadata();
        }
    }

    public TemplateRuleRotatable() {
        super();
    }

    @Override
    public boolean shouldReuseRule(Level world, BlockState state, int turns, BlockPos pos) {
        return false;
    }

    @Override
    public void handlePlacement(Level world, int turns, BlockPos pos, IStructureBuilder builder) {
        if (world.setBlock(pos, state, 2)) {
            Optional<BlockEntity> te = WorldTools.getTile(world, pos);
            if (te.isPresent()) {
                BlockEntity worksite = te.get();
                worksite.load(tag.copy());
                worksite.setChanged();
                Direction o = orientation;
                if (o.getAxis() != Direction.Axis.Y) {
                    for (int i = 0; i < turns; i++) {
                        o = o.getClockWise();
                    }
                }
                ((BlockRotationHandler.IRotatableTile) worksite).setPrimaryFacing(o);
                if (worksite instanceof IBoundedSite && p1 != null && p2 != null) {
                    BlockPos pos1 = BlockTools.rotateAroundOrigin(p1, turns).offset(pos);
                    BlockPos pos2 = BlockTools.rotateAroundOrigin(p2, turns).offset(pos);
                    ((IBoundedSite) worksite).setBounds(pos1, pos2);
                }
                BlockTools.notifyBlockUpdate(world, pos);
            }
        }
    }

    @Override
    public void parseRule(CompoundTag tag) {
        super.parseRule(tag);
        this.orientation = Direction.values()[tag.getInt("orientation")];
        if (tag.contains(TE_DATA_TAG)) {
            this.tag = tag.getCompound(TE_DATA_TAG);
        }
        if (tag.contains("pos1")) {
            this.p1 = NBTHelper.readBlockPosFromNBT(tag.getCompound("pos1"));
        }
        if (tag.contains("pos2")) {
            this.p2 = NBTHelper.readBlockPosFromNBT(tag.getCompound("pos2"));
        }
    }

    @Override
    public void writeRuleData(CompoundTag tag) {
        super.writeRuleData(tag);
        tag.putInt("orientation", orientation.ordinal());
        if (p1 != null) {
            tag.put("pos1", NBTHelper.writeBlockPosToNBT(new CompoundTag(), p1));
        }
        if (p2 != null) {
            tag.put("pos2", NBTHelper.writeBlockPosToNBT(new CompoundTag(), p2));
        }
        if (this.tag != null) {
            tag.put(TE_DATA_TAG, this.tag);
        }
    }

    @Override
    public boolean shouldPlaceOnBuildPass(Level world, int turns, BlockPos pos, int buildPass) {
        return buildPass == 0;
    }

    @Override
    public String getPluginName() {
        return PLUGIN_NAME;
    }
}
