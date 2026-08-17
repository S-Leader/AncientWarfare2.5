package net.shadowmage.ancientwarfare.structure.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.StringTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.core.compat.LegacyMaterial;
import net.shadowmage.ancientwarfare.core.util.NBTBuilder;
import net.shadowmage.ancientwarfare.core.util.WorldTools;
import net.shadowmage.ancientwarfare.structure.template.StructureTemplateManager;
import net.shadowmage.ancientwarfare.structure.tile.TileStructureBuilder;

import javax.annotation.Nullable;
import java.util.Set;

public class BlockStructureBuilder extends BlockBaseStructure {

    private NonNullList<ItemStack> displayCache = null;

    public BlockStructureBuilder() {
        super(LegacyMaterial.ROCK, "structure_builder_ticked");
        setHardness(2.f);
    }

    public void getSubBlocks(CreativeModeTab tab, NonNullList<ItemStack> items) {
        if (displayCache == null || displayCache.isEmpty()) {
            displayCache = NonNullList.create();

            Set<String> templateNames = StructureTemplateManager.getSurvivalStructures().keySet();
            ItemStack item;
            for (String templateName : templateNames) {
                item = new ItemStack(this);
                item.getOrCreateTag().put("structureName", StringTag.valueOf(templateName));
                displayCache.add(item);
            }

        }
        if (!displayCache.isEmpty()) {
            items.addAll(displayCache);
        }
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }


    @Override
    public boolean onBlockActivated(Level world, BlockPos pos, BlockState state, Player player, InteractionHand hand, Direction facing, float hitX, float hitY, float hitZ) {
        if (!world.isClientSide) {
            WorldTools.getTile(world, pos, TileStructureBuilder.class).ifPresent(b -> b.onBlockClicked(player));
        }
        return true;
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, BlockGetter world, BlockPos pos, BlockState state, int fortune) {
        ItemStack drop = new ItemStack(this);
        WorldTools.getTile(world, pos, TileStructureBuilder.class)
                .ifPresent(t -> drop.setTag(new NBTBuilder()
                        .setString("structureName", t.getBuilder().getTemplate().name)
                        .setTag("progress", t.getBuilder().serializeProgressData())
                        .build()));
        drops.add(drop);
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level world, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        //If it will harvest, delay deletion of the block until after getDrops
        return willHarvest || super.onDestroyedByPlayer(state, world, pos, player, willHarvest, fluid);
    }

    @Override
    public void harvestBlock(Level world, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity te, ItemStack tool) {
        super.harvestBlock(world, player, pos, state, te, tool);
        world.removeBlock(pos, false);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void registerClient() {
        super.registerClient();

    }
}
