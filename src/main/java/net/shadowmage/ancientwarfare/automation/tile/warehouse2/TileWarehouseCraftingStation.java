package net.shadowmage.ancientwarfare.automation.tile.warehouse2;


import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.shadowmage.ancientwarfare.core.init.AWMenuTypes;
import net.shadowmage.ancientwarfare.core.interfaces.IInteractableTile;
import net.shadowmage.ancientwarfare.core.network.NetworkHandler;
import net.shadowmage.ancientwarfare.core.tile.CraftingRecipeMemory;
import net.shadowmage.ancientwarfare.core.tile.IBlockBreakHandler;
import net.shadowmage.ancientwarfare.core.tile.TileUpdatable;
import net.shadowmage.ancientwarfare.core.util.WorldTools;

import javax.annotation.Nullable;

public class TileWarehouseCraftingStation extends TileUpdatable implements IInteractableTile, IBlockBreakHandler {
    public TileWarehouseCraftingStation(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }


    public CraftingRecipeMemory craftingRecipeMemory = new CraftingRecipeMemory(this);

    @Nullable
    public final TileWarehouse getWarehouse() {
        if (pos.getY() <= 1)//could not possibly be a warehouse below...
        {
            return null;
        }
        return WorldTools.getTile(world, pos.below(), TileWarehouse.class).orElse(null);
    }

    @Override
    protected void writeUpdateNBT(CompoundTag tag) {
        super.writeUpdateNBT(tag);
        craftingRecipeMemory.writeToNBT(tag);
    }

    @Override
    protected void handleUpdateNBT(CompoundTag tag) {
        super.handleUpdateNBT(tag);
        craftingRecipeMemory.readFromNBT(tag);
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
        super.readFromNBT(tag);
        craftingRecipeMemory.readFromNBT(tag);
    }

    @Override
    public CompoundTag writeToNBT(CompoundTag tag) {
        super.writeToNBT(tag);
        craftingRecipeMemory.writeToNBT(tag);
        return tag;
    }

    @Override
    public boolean onBlockClicked(Player player, InteractionHand hand) {
        if (!player.level().isClientSide) {
            AWMenuTypes.open(player, NetworkHandler.GUI_WAREHOUSE_CRAFTING, pos);
        }
        return true;
    }

    @Override
    public void onBlockBroken(BlockState state) {
        craftingRecipeMemory.dropInventory();
    }
}
