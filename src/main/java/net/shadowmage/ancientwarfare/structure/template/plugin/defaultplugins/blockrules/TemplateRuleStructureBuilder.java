package net.shadowmage.ancientwarfare.structure.template.plugin.defaultplugins.blockrules;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.shadowmage.ancientwarfare.core.owner.Owner;
import net.shadowmage.ancientwarfare.core.util.BlockTools;
import net.shadowmage.ancientwarfare.core.util.WorldTools;
import net.shadowmage.ancientwarfare.structure.api.IStructureBuilder;
import net.shadowmage.ancientwarfare.structure.api.TemplateRuleBlock;
import net.shadowmage.ancientwarfare.structure.init.AWStructureItems;
import net.shadowmage.ancientwarfare.structure.template.build.StructureBuilderTicked;
import net.shadowmage.ancientwarfare.structure.tile.TileStructureBuilder;

import java.util.Optional;

public class TemplateRuleStructureBuilder extends TemplateRuleBlock {
    public static final String PLUGIN_NAME = "AWStructureBuilder";

    String templateName;
    Direction facing;

    public TemplateRuleStructureBuilder(Level world, BlockPos pos, BlockState state, int turns) {
        super(state, turns);
        WorldTools.getTile(world, pos, TileStructureBuilder.class).ifPresent(structureBuilder -> {
            StructureBuilderTicked builder = structureBuilder.getBuilder();
            templateName = builder.getTemplate().name;
            facing = rotateFacing(turns, builder.getBuildFace());
        });
    }

    public TemplateRuleStructureBuilder() {
        super();
    }

    @Override
    public boolean shouldReuseRule(Level world, BlockState state, int turns, BlockPos pos) {
        return false;
    }

    @Override
    public void handlePlacement(Level world, int turns, BlockPos pos, IStructureBuilder builder) {
        builder.placeBlock(pos, BlockTools.rotateFacing(state, turns), 0);
        WorldTools.getTile(world, pos, TileStructureBuilder.class).ifPresent(structureBuilder -> {
            structureBuilder.setOwner(Owner.EMPTY);
            Direction placementFacing = rotateFacing(turns, facing);
            AWStructureItems.STRUCTURE_BUILDER_TICKED.setupStructureBuilder(world, pos, structureBuilder, templateName, placementFacing);
        });
    }

    private Direction rotateFacing(int turns, Direction facing) {
        for (int i = 0; i < turns; i++) {
            facing = facing.getClockWise();
        }
        return facing;
    }

    @Override
    protected Optional<ItemStack> getStack() {
        ItemStack stack = new ItemStack(AWStructureItems.STRUCTURE_BUILDER_TICKED);
        stack.getOrCreateTag().put("structureName", StringTag.valueOf(templateName));
        return Optional.of(stack);
    }

    @Override
    public String getPluginName() {
        return PLUGIN_NAME;
    }

    @Override
    public void writeRuleData(CompoundTag tag) {
        super.writeRuleData(tag);
        tag.putString("templateName", templateName);
        tag.putInt("facing", facing.get2DDataValue());
    }

    @Override
    public boolean shouldPlaceOnBuildPass(Level world, int turns, BlockPos pos, int buildPass) {
        return buildPass == 0;
    }

    @Override
    public void parseRule(CompoundTag tag) {
        super.parseRule(tag);
        templateName = tag.getString("templateName");
        facing = Direction.from2DDataValue(tag.getInt("facing"));
    }
}
