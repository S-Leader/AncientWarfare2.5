package net.shadowmage.ancientwarfare.structure.template.plugin.defaultplugins.blockrules;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.core.block.BlockRotationHandler;
import net.shadowmage.ancientwarfare.core.datafixes.ComponentItemFixer;
import net.shadowmage.ancientwarfare.core.util.BlockTools;
import net.shadowmage.ancientwarfare.core.util.WorldTools;
import net.shadowmage.ancientwarfare.structure.AncientWarfareStructure;
import net.shadowmage.ancientwarfare.structure.api.IStructureBuilder;

import javax.annotation.Nullable;

public class TemplateRuleBlockTile<T extends BlockEntity> extends TemplateRuleVanillaBlocks {
    private static final String FACING_TAG = "facing";
    public static final String PLUGIN_NAME = "blockTile";
    public CompoundTag tag = new CompoundTag();
    public Direction facing = null;

    private Tuple<Integer, T> tileCache = null;

    public TemplateRuleBlockTile(Level world, BlockPos pos, BlockState state, int turns) {
        super(world, pos, state, turns);
        WorldTools.getTile(world, pos).ifPresent(t -> {
            tag = t.saveWithFullMetadata();
            tag.remove("x");
            tag.remove("y");
            tag.remove("z");
            if (t instanceof BlockRotationHandler.IRotatableTile rotatable) {
                facing = rotateFacing(turns, rotatable.getPrimaryFacing());
            }
        });
    }

    public TemplateRuleBlockTile() {
        super();
    }

    private Direction rotateFacing(int turns, Direction original) {
        Direction rotated = original;
        if (rotated.getAxis() != Direction.Axis.Y) {
            for (int i = 0; i < turns; i++) {
                rotated = rotated.getClockWise();
            }
        }
        return rotated;
    }

    @Override
    public void handlePlacement(Level world, int turns, BlockPos pos, IStructureBuilder builder) {
        super.handlePlacement(world, turns, pos, builder);
        WorldTools.getTile(world, pos).ifPresent(tile -> {
            try {
                tile.load(ComponentItemFixer.fixRecursively(tag.copy()));
                rotateTe(tile, turns);
                tile.setChanged();
                if (!world.isClientSide) {
                    // The block update sent by super.handlePlacement predates the
                    // restored tile NBT.  Send the final data so dynamically
                    // rendered blocks (notably coffins) do not stay at defaults.
                    BlockTools.notifyBlockUpdate(tile);
                }
            } catch (Exception e) {
                AncientWarfareStructure.LOG.error("Error loading block entity data from template for {}", tile.getClass(), e);
            }
        });
    }

    @Override
    public boolean shouldReuseRule(Level world, BlockState state, int turns, BlockPos pos) {
        return false;
    }

    @Override
    public void writeRuleData(CompoundTag output) {
        super.writeRuleData(output);
        output.put("teData", tag.copy());
        if (facing != null) {
            output.putString(FACING_TAG, facing.getName());
        }
    }

    @Override
    public void parseRule(CompoundTag input) {
        super.parseRule(input);
        tag = input.getCompound("teData").copy();
        if (input.contains(FACING_TAG)) {
            facing = Direction.byName(input.getString(FACING_TAG));
        }
    }

    @Override
    public String getPluginName() {
        return PLUGIN_NAME;
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public BlockEntity getTileEntity(int turns) {
        if (tileCache == null || tileCache.getA() != turns) {
            try {
                CompoundTag previewTag = ComponentItemFixer.fixRecursively(tag.copy());
                previewTag.putInt("x", 0);
                previewTag.putInt("y", 0);
                previewTag.putInt("z", 0);
                T tile = (T) BlockEntity.loadStatic(BlockPos.ZERO, getState(turns), previewTag);
                if (tile != null) {
                    rotateTe(tile, turns);
                }
                tileCache = new Tuple<>(turns, tile);
            } catch (Exception e) {
                AncientWarfareStructure.LOG.error("Error creating preview block entity from template NBT", e);
                tileCache = new Tuple<>(turns, null);
            }
        }
        return tileCache.getB();
    }

    @SuppressWarnings("squid:S1172")
    protected void rotateTe(BlockEntity tile, int turns) {
        if (facing != null && tile instanceof BlockRotationHandler.IRotatableTile rotatable) {
            rotatable.setPrimaryFacing(rotateFacing(turns, facing));
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean isDynamicallyRendered(int turns) {
        BlockEntity tile = getTileEntity(turns);
        return tile != null && Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(tile) != null;
    }
}
