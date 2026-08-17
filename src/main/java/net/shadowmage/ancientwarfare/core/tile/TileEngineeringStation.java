package net.shadowmage.ancientwarfare.core.tile;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.items.ItemStackHandler;
import net.shadowmage.ancientwarfare.core.block.BlockRotationHandler.IRotatableTile;
import net.shadowmage.ancientwarfare.core.util.BlockTools;
import net.shadowmage.ancientwarfare.core.util.InventoryTools;

public class TileEngineeringStation extends TileUpdatable implements IRotatableTile {

    public CraftingRecipeMemory craftingRecipeMemory = new CraftingRecipeMemory(this);
    Direction facing = Direction.NORTH;
    public final ItemStackHandler extraSlots;

    public TileEngineeringStation(BlockEntityType<?> type, BlockPos pos, BlockState state) {

        super(type, pos, state);
        extraSlots = new ItemStackHandler(18) {
            @Override
            protected void onContentsChanged(int slot) {
                markDirty();
            }
        };
    }

    @Override
    protected void writeUpdateNBT(CompoundTag tag) {
        super.writeUpdateNBT(tag);
        tag.putInt("facing", facing.ordinal());
        craftingRecipeMemory.writeToNBT(tag);
    }

    @Override
    protected void handleUpdateNBT(CompoundTag tag) {
        super.handleUpdateNBT(tag);
        facing = Direction.values()[tag.getInt("facing")];
        BlockTools.notifyBlockUpdate(this);
        craftingRecipeMemory.readFromNBT(tag);
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
        super.readFromNBT(tag);
        craftingRecipeMemory.readFromNBT(tag);
        extraSlots.deserializeNBT(tag.getCompound("extraInventory"));
        facing = Direction.values()[tag.getInt("facing")];
    }

    @Override
    public CompoundTag writeToNBT(CompoundTag tag) {
        super.writeToNBT(tag);

        craftingRecipeMemory.writeToNBT(tag);
        tag.put("extraInventory", extraSlots.serializeNBT());
        tag.putInt("facing", facing.ordinal());
        return tag;
    }

    @Override
    public Direction getPrimaryFacing() {
        return facing;
    }

    @Override
    public void setPrimaryFacing(Direction face) {
        this.facing = face;
        BlockTools.notifyBlockUpdate(this);
    }

    public void onBlockBreak() {
        craftingRecipeMemory.dropInventory();
        InventoryTools.dropItemsInWorld(world, extraSlots, pos);
    }
}
