package net.shadowmage.ancientwarfare.structure.template.plugin.defaultplugins.blockrules;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.shadowmage.ancientwarfare.core.render.property.CoreProperties;
import net.shadowmage.ancientwarfare.core.util.BlockTools;
import net.shadowmage.ancientwarfare.core.util.WorldTools;
import net.shadowmage.ancientwarfare.structure.api.IStructureBuilder;
import net.shadowmage.ancientwarfare.structure.api.TemplateRuleBlock;
import net.shadowmage.ancientwarfare.structure.block.BlockTotemPart;
import net.shadowmage.ancientwarfare.structure.init.AWStructureBlocks;
import net.shadowmage.ancientwarfare.structure.tile.TileTotemPart;

import javax.annotation.Nullable;
import java.util.Optional;

public class TemplateRuleTotemPart extends TemplateRuleBlock {
    public static final String PLUGIN_NAME = "totemPart";
    private BlockTotemPart.Variant variant;
    private boolean mainBlock = false;

    public TemplateRuleTotemPart(Level world, BlockPos pos, BlockState state, int turns) {
        super(state, turns);

        Optional<TileTotemPart> te = WorldTools.getTile(world, pos, TileTotemPart.class);
        if (!te.isPresent()) {
            return;
        }
        TileTotemPart totem = te.get();
        variant = totem.getVariant();
        mainBlock = !totem.getMainBlockPos().isPresent();
    }

    public TemplateRuleTotemPart() {
        super();
    }

    @Override
    public void handlePlacement(Level world, int turns, BlockPos pos, IStructureBuilder builder) {
        if (mainBlock) {
            BlockState rotatedState = BlockTools.rotateFacing(state, turns);
            world.setBlock(pos, rotatedState, 3);
            WorldTools.getTile(world, pos, TileTotemPart.class).ifPresent(te -> te.setVariant(variant));
            variant.placeAdditionalParts(world, pos, rotatedState.getValue(CoreProperties.FACING));
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

    @Override
    public boolean shouldReuseRule(Level world, BlockState state, int turns, BlockPos pos) {
        return false;
    }

    @Override
    public void writeRuleData(CompoundTag tag) {
        super.writeRuleData(tag);
        if (mainBlock) {
            tag.putByte("variant", (byte) variant.getId());
            tag.putBoolean("mainBlock", mainBlock);
        }
    }

    @Override
    public void parseRule(CompoundTag tag) {
        super.parseRule(tag);
        mainBlock = tag.getBoolean("mainBlock");
        if (mainBlock) {
            variant = BlockTotemPart.Variant.fromId(tag.getByte("variant"));
        }
    }

    @Nullable
    @Override
    public BlockEntity getTileEntity(int turns) {
        TileTotemPart te = AWStructureBlocks.TOTEM_PART_TILE.get().create(BlockPos.ZERO, getState(turns));
        if (te != null) {
            te.setVariant(variant != null ? variant : BlockTotemPart.Variant.WINGS);
        }
        return te;
    }
}
