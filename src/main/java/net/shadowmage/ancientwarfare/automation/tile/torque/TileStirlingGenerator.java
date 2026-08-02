package net.shadowmage.ancientwarfare.automation.tile.torque;


import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.shadowmage.ancientwarfare.automation.config.AWAutomationStatics;
import net.shadowmage.ancientwarfare.core.compat.CapabilityItemHandler;
import net.shadowmage.ancientwarfare.core.init.AWMenuTypes;
import net.shadowmage.ancientwarfare.core.interfaces.ITorque.TorqueCell;
import net.shadowmage.ancientwarfare.core.network.NetworkHandler;
import net.shadowmage.ancientwarfare.core.tile.IBlockBreakHandler;
import net.shadowmage.ancientwarfare.core.util.InventoryTools;

import javax.annotation.Nullable;

public class TileStirlingGenerator extends TileTorqueSingleCell implements IBlockBreakHandler {

    private final ItemStackHandler fuelHandler = new ItemStackHandler(1) {
        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return ForgeHooks.getBurnTime(stack, RecipeType.SMELTING) > 0 ? super.insertItem(slot, stack, simulate) : stack;
        }
    };

    private final LazyOptional<IItemHandler> fuelHandlerCap = LazyOptional.of(() -> fuelHandler);

    private int burnTime = 0;
    private int burnTimeBase = 0;

    public TileStirlingGenerator() {
        torqueCell = new TorqueCell(0, 4, 1600, AWAutomationStatics.med_efficiency_factor);
    }

    @Override
    public void update() {
        super.update();
        if (!world.isClientSide) {
            if (burnTime <= 0 && torqueCell.getEnergy() < torqueCell.getMaxEnergy()) {
                //if fueled, consume one, set burn-ticks to fuel value
                int ticks = ForgeHooks.getBurnTime(fuelHandler.getStackInSlot(0), RecipeType.SMELTING);
                if (ticks > 0) {
                    fuelHandler.extractItem(0, 1, false);
                    burnTime = ticks;
                    burnTimeBase = ticks;
                }
            } else if (burnTime > 0) {
                torqueCell.setEnergy(torqueCell.getEnergy() + AWAutomationStatics.stirling_generator_output);
                burnTime--;
            }
        }
    }

    @Override
    public boolean onBlockClicked(Player player, @Nullable InteractionHand hand) {
        if (!player.level().isClientSide) {
            AWMenuTypes.open(player, NetworkHandler.GUI_STIRLING_GENERATOR, pos);
        }
        return true;
    }

    public int getBurnTime() {
        return burnTime;
    }

    public int getBurnTimeBase() {
        return burnTimeBase;
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
        super.readFromNBT(tag);
        burnTime = tag.getInt("burnTicks");
        burnTimeBase = tag.getInt("burnTicksBase");
        if (tag.contains("inventory")) {
            fuelHandler.deserializeNBT(tag.getCompound("inventory"));
        }
    }

    @Override
    public CompoundTag writeToNBT(CompoundTag tag) {
        super.writeToNBT(tag);
        tag.putInt("burnTicks", burnTime);
        tag.putInt("burnTicksBase", burnTimeBase);
        tag.put("inventory", fuelHandler.serializeNBT());
        return tag;
    }

    @Override
    public boolean canInputTorque(Direction from) {
        return false;
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return fuelHandlerCap.cast();
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        fuelHandlerCap.invalidate();
    }

    @Override
    public void onBlockBroken(BlockState state) {
        InventoryTools.dropItemsInWorld(world, fuelHandler, pos);
    }
}
