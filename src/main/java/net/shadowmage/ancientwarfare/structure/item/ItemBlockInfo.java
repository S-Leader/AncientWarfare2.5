package net.shadowmage.ancientwarfare.structure.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import net.shadowmage.ancientwarfare.core.util.BlockTools;
import net.shadowmage.ancientwarfare.core.util.LegacyBlockState;
import net.shadowmage.ancientwarfare.core.util.WorldTools;
import net.shadowmage.ancientwarfare.structure.AncientWarfareStructure;

public class ItemBlockInfo extends ItemBaseStructure {

    public ItemBlockInfo(String name) {
        super(name);
    }

    @Override
    public InteractionResultHolder<ItemStack> onItemRightClick(Level world, Player player, InteractionHand hand) {
        if (!world.isClientSide) {
            BlockPos pos = BlockTools.getBlockClickedOn(player, player.level(), false);
            if (pos != null) {
                BlockState state = world.getBlockState(pos);
                Block block = state.getBlock();
                //noinspection ConstantConditions
                AncientWarfareStructure.LOG.info("block: " + ForgeRegistries.BLOCKS.getKey(block).toString() + ", meta: " + LegacyBlockState.toMeta(state));
                if (state.hasBlockEntity()) {
                    AncientWarfareStructure.LOG.info("tile: " + WorldTools.getTile(world, pos).map(t -> t.getClass().toString()).orElse(""));
                }
            }
        }
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, player.getItemInHand(hand));
    }
}
