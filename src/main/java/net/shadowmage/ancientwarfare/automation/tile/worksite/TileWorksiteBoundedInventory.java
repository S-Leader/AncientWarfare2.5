package net.shadowmage.ancientwarfare.automation.tile.worksite;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.BlockPos;
import com.google.common.collect.Maps;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.shadowmage.ancientwarfare.core.block.BlockRotationHandler;
import net.shadowmage.ancientwarfare.core.block.BlockRotationHandler.RelativeSide;
import net.shadowmage.ancientwarfare.core.compat.CapabilityItemHandler;
import net.shadowmage.ancientwarfare.core.util.Constants;
import net.shadowmage.ancientwarfare.core.util.InventoryTools;

import javax.annotation.Nullable;
import java.util.Map;

/*
 * abstract base class for worksite based tile-entities (or at least a template to copy from)
 * <p/>
 * handles the management of worker references and work-bounds, as well as inventory bridge methods.
 * <p/>
 * All implementing classes must initialize the inventory field in their constructor, or things
 * will go very crashy when the block is placed in the world.
 *
 * @author Shadowmage
 */
public abstract class TileWorksiteBoundedInventory extends TileWorksiteBounded {
    private static final int MAIN_INVENTORY_SIZE = 27;
    public final ItemStackHandler mainInventory;
    private final Map<RelativeSide, IItemHandler> sideInventories = Maps.newHashMap();
    private final Map<RelativeSide, RelativeSide> inventorySideMappings = Maps.newHashMap();

    public TileWorksiteBoundedInventory(BlockEntityType<?> type, BlockPos pos, BlockState state) {

        super(type, pos, state);
        initSideMappings();
        mainInventory = new ItemStackHandler(MAIN_INVENTORY_SIZE);
        setSideInventory(RelativeSide.TOP, mainInventory, RelativeSide.BOTTOM);
    }

    private void initSideMappings() {
        for (RelativeSide side : BlockRotationHandler.RotationType.FOUR_WAY.getValidSides()) {
            inventorySideMappings.put(side, RelativeSide.NONE);
        }
    }

    public void setSideInventory(RelativeSide inventorySide, @Nullable IItemHandler handler, RelativeSide defaultMachineSide) {
        sideInventories.put(inventorySide, handler);
        setInventorySideMappings(defaultMachineSide, inventorySide);
    }

    public void setInventorySideMappings(RelativeSide machineSide, RelativeSide inventorySide) {
        inventorySideMappings.put(machineSide, inventorySide);
    }

    public Map<RelativeSide, IItemHandler> getSideInventories() {
        return sideInventories;
    }

    public Map<RelativeSide, RelativeSide> getInventorySideMappings() {
        return inventorySideMappings;
    }

    @Nullable
    private IItemHandler getInventoryMappedToFacing(@Nullable Direction facing) {
        RelativeSide machineSide = RelativeSide.getSideViewed(BlockRotationHandler.RotationType.FOUR_WAY, getPrimaryFacing(), facing);

        if (inventorySideMappings.containsKey(machineSide)) {
            return sideInventories.get(inventorySideMappings.get(machineSide));
        }

        return null;
    }

    @Override
    public void onBlockBroken(BlockState state) {
        super.onBlockBroken(state);
        InventoryTools.dropItemsInWorld(world, mainInventory, pos);
    }

    public void openAltGui(Player player) {
        //noop, must be implemented by individual tiles, if they have an alt-control gui
    }

    @Override
    public CompoundTag writeToNBT(CompoundTag tag) {
        super.writeToNBT(tag);
        tag.put("mainInventory", mainInventory.serializeNBT());

        ListTag sideMappings = new ListTag();
        for (Map.Entry<RelativeSide, RelativeSide> mapping : inventorySideMappings.entrySet()) {
            sideMappings.add(new IntArrayTag(new int[]{mapping.getKey().ordinal(), mapping.getValue().ordinal()}));
        }
        tag.put("inventorySideMappings", sideMappings);
        return tag;
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
        super.readFromNBT(tag);
        if (tag.contains("mainInventory")) {
            mainInventory.deserializeNBT(tag.getCompound("mainInventory"));
        }
        for (Tag nbt : tag.getList("inventorySideMappings", Constants.NBT.TAG_INT_ARRAY)) {
            IntArrayTag mapping = (IntArrayTag) nbt;
            setInventorySideMappings(RelativeSide.values()[mapping.getAsIntArray()[0]], RelativeSide.values()[mapping.getAsIntArray()[1]]);
        }
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            IItemHandler handler = getInventoryMappedToFacing(facing);
            if (handler != null) {
                return LazyOptional.of(() -> handler).cast();
            }
        }
        return super.getCapability(capability, facing);
    }
}
