package net.shadowmage.ancientwarfare.structure.item;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.core.item.ItemBlockBase;
import net.shadowmage.ancientwarfare.core.util.BlockTools;
import net.shadowmage.ancientwarfare.core.util.WorldTools;
import net.shadowmage.ancientwarfare.structure.event.IBoxRenderer;
import net.shadowmage.ancientwarfare.structure.render.PreviewRenderer;
import net.shadowmage.ancientwarfare.structure.template.StructureTemplateManager;
import net.shadowmage.ancientwarfare.structure.template.build.StructureBB;
import net.shadowmage.ancientwarfare.structure.template.build.StructureBuilderTicked;
import net.shadowmage.ancientwarfare.structure.tile.TileStructureBuilder;

import javax.annotation.Nullable;
import java.util.List;

public class ItemBlockStructureBuilder extends ItemBlockBase implements IBoxRenderer {
    private static final String STRUCTURE_NAME_TAG = "structureName";

    public ItemBlockStructureBuilder(Block block) {
        super(block);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable Level world, List<String> tooltip, TooltipFlag flagIn) {
        String name = "corrupt_item";
        //noinspection ConstantConditions
        if (stack.hasTag() && stack.getTag().contains(STRUCTURE_NAME_TAG)) {
            name = stack.getTag().getString(STRUCTURE_NAME_TAG);
        }
        tooltip.add(I18n.get("guistrings.structure.structure_name") + ": " + name);
    }

    @Override
    public InteractionResult place(BlockPlaceContext context) {
        ItemStack stack = context.getItemInHand();
        if (!stack.hasTag() || !stack.getOrCreateTag().contains(STRUCTURE_NAME_TAG)) {
            return InteractionResult.FAIL;
        }
        return super.place(context);
    }

    @Override
    protected void onBlockPlaced(BlockPlaceContext context, BlockState placedState) {
        if (!context.getLevel().isClientSide && context.getPlayer() != null) {
            WorldTools.getTile(context.getLevel(), context.getClickedPos(), TileStructureBuilder.class)
                    .ifPresent(tile -> setupStructureBuilder(context.getItemInHand(), context.getPlayer(), context.getLevel(), context.getClickedPos(), tile));
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void setupStructureBuilder(ItemStack stack, Player player, Level world, BlockPos pos, TileStructureBuilder tb) {
        tb.setOwner(player);
        CompoundTag tag = stack.getTag();
        String name = tag.getString(STRUCTURE_NAME_TAG);
        Direction face = player.getDirection();
        setupStructureBuilder(world, pos, tb, name, face);
        if (tag.contains("progress")) {
            StructureBuilderTicked builder = tb.getBuilder();
            builder.deserializeProgressData(tag.getCompound("progress"));
        }
    }

    public void setupStructureBuilder(Level world, BlockPos pos, TileStructureBuilder tb, String name, Direction face) {
        StructureTemplateManager.getTemplate(name).ifPresent(t -> {
            BlockPos p = pos.relative(face, t.getSize().getZ() - 1 - t.getOffset().getZ() + 1);
            tb.setBuilder(new StructureBuilderTicked(world, t, face, p));
        });
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderBox(Player player, InteractionHand hand, ItemStack stack, float delta) {
        //noinspection ConstantConditions
        if (!stack.hasTag() || !stack.getTag().contains(STRUCTURE_NAME_TAG)) {
            return;
        }
        String name = stack.getTag().getString(STRUCTURE_NAME_TAG);
        BlockPos hit = BlockTools.getBlockClickedOn(player, player.level(), true);
        if (hit == null) {
            return;
        }
        StructureTemplateManager.getTemplate(name).ifPresent(t -> {
            Util.renderBoundingBox(player, hit, hit, delta);
            Direction face = player.getDirection();
            BlockPos p2 = hit.relative(face, t.getSize().getZ() - 1 - t.getOffset().getZ() + 1);
            StructureBB bb = new StructureBB(p2, face, t.getSize(), t.getOffset());
            Util.renderBoundingBox(player, bb.min, bb.max, delta);
            PreviewRenderer.renderTemplatePreview(player, hand, stack, delta, t, bb, (face.get2DDataValue() + 2) % 4);
        });
    }
}
