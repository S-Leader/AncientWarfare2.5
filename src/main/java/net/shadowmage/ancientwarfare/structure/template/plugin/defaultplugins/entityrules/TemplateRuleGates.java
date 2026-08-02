package net.shadowmage.ancientwarfare.structure.template.plugin.defaultplugins.entityrules;

import com.google.common.primitives.Ints;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.shadowmage.ancientwarfare.core.owner.Owner;
import net.shadowmage.ancientwarfare.core.util.BlockTools;
import net.shadowmage.ancientwarfare.core.util.NBTHelper;
import net.shadowmage.ancientwarfare.structure.AncientWarfareStructure;
import net.shadowmage.ancientwarfare.structure.api.IStructureBuilder;
import net.shadowmage.ancientwarfare.structure.api.TemplateRuleEntityBase;
import net.shadowmage.ancientwarfare.structure.entity.EntityGate;
import net.shadowmage.ancientwarfare.structure.gates.types.Gate;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class TemplateRuleGates extends TemplateRuleEntityBase {

    public static final String PLUGIN_NAME = "awGate";
    private String owner;
    private String gateType;
    private Direction orientation;
    private BlockPos pos1;
    private BlockPos pos2;

    public TemplateRuleGates(Level world, Entity entity, int turns, int x, int y, int z) {
        super();
        EntityGate gate = (EntityGate) entity;

        this.pos1 = BlockTools.rotateAroundOrigin(gate.pos1.offset(-x, -y, -z), turns);
        this.pos2 = BlockTools.rotateAroundOrigin(gate.pos2.offset(-x, -y, -z), turns);
        this.orientation = Direction.from2DDataValue((gate.gateOrientation.get2DDataValue() + turns) % 4);
        this.gateType = Gate.getGateNameFor(gate);
        this.owner = gate.getOwner().getName();
    }

    public TemplateRuleGates() {
        super();
    }

    @Override
    public void handlePlacement(Level world, int turns, BlockPos pos, IStructureBuilder builder) {
        BlockPos p1 = BlockTools.rotateAroundOrigin(pos1, turns).offset(pos);
        BlockPos p2 = BlockTools.rotateAroundOrigin(pos2, turns).offset(pos);

        BlockPos min = BlockTools.getMin(p1, p2);
        BlockPos max = BlockTools.getMax(p1, p2);
        for (int x1 = min.getX(); x1 <= max.getX(); x1++) {
            for (int y1 = min.getY(); y1 <= max.getY(); y1++) {
                for (int z1 = min.getZ(); z1 <= max.getZ(); z1++) {
                    world.removeBlock(new BlockPos(x1, y1, z1), false);
                }
            }
        }

        Optional<EntityGate> gate = Gate.constructGate(world, p1, p2, Gate.getGateByName(gateType),
                Direction.from2DDataValue(Ints.constrainToRange((orientation.get2DDataValue() + turns) % 4, 0, 4)),
                owner.isEmpty() ? Owner.EMPTY : new Owner(world, owner));
        if (!gate.isPresent()) {
            AncientWarfareStructure.LOG.warn("Could not create gate for type: {}", gateType);
            return;
        }
        world.addFreshEntity(gate.get());
    }

    @Override
    public void parseRule(CompoundTag tag) {
        super.parseRule(tag);
        gateType = tag.getString("gateType");
        orientation = Direction.values()[tag.getByte("orientation")];
        pos1 = NBTHelper.readBlockPosFromNBT(tag.getCompound("pos1"));
        pos2 = NBTHelper.readBlockPosFromNBT(tag.getCompound("pos2"));
        owner = tag.getString("owner");
    }

    @Override
    public void writeRuleData(CompoundTag tag) {
        super.writeRuleData(tag);
        tag.putString("gateType", gateType);
        tag.putByte("orientation", (byte) orientation.ordinal());
        tag.put("pos1", NBTHelper.writeBlockPosToNBT(new CompoundTag(), pos1));
        tag.put("pos2", NBTHelper.writeBlockPosToNBT(new CompoundTag(), pos2));
        tag.putString("owner", owner);
    }

    @Override
    public List<ItemStack> getResources() {
        return Collections.singletonList(Gate.getItemToConstruct(Gate.getGateByName(gateType).getGlobalID()));
    }

    @Override
    public boolean shouldPlaceOnBuildPass(Level world, int turns, BlockPos pos, int buildPass) {
        return buildPass == 3;
    }

    @Override
    public String getPluginName() {
        return PLUGIN_NAME;
    }
}
