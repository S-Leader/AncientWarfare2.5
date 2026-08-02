package net.shadowmage.ancientwarfare.automation.container;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.shadowmage.ancientwarfare.automation.tile.torque.TileStirlingGenerator;
import net.shadowmage.ancientwarfare.core.compat.CapabilityItemHandler;
import net.shadowmage.ancientwarfare.core.container.ContainerTileBase;

public class ContainerStirlingGenerator extends ContainerTileBase<TileStirlingGenerator> {

    private static final int GENERATOR_SLOT_COUNT = 1;
    private static final int ENERGY_SYNC_SCALE = 1000;

    public int guiHeight;

    public double energy;

    public int burnTime;
    public int burnTimeBase;

    private final IItemHandler itemHandler;

    public ContainerStirlingGenerator(Player player, int x, int y, int z) {
        super(player, x, y, z);
        this.itemHandler = this.tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).orElse(null);

        if (this.itemHandler == null) {
            throw new IllegalStateException("Stirling generator at " + this.tileEntity.getBlockPos() + " has no item-handler capability");
        }
        this.addSlot(new SlotItemHandler(this.itemHandler, 0, 8 + 4 * 18, 8 + 12) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return AbstractFurnaceBlockEntity.isFuel(stack);
            }
        });

        this.guiHeight = this.addPlayerSlots(8 + 18 + 8 + 12 + 12) + 8;

        this.energy = this.tileEntity.getTorqueStored(this.tileEntity.getPrimaryFacing());
        this.burnTime = this.tileEntity.getBurnTime();
        this.burnTimeBase = this.tileEntity.getBurnTimeBase();

        this.registerDataSlots();
    }

    private void registerDataSlots() {
        this.addDataSlot(new DataSlot() {
            @Override
            public int get() {
                double maxTorque = ContainerStirlingGenerator.this.getMaxTorque();

                if (maxTorque <= 0.0D) {
                    return 0;
                }

                double storedTorque = ContainerStirlingGenerator.this.tileEntity.getTorqueStored(ContainerStirlingGenerator.this.tileEntity.getPrimaryFacing());

                return Mth.clamp((int) Math.round(storedTorque * ENERGY_SYNC_SCALE / maxTorque), 0, ENERGY_SYNC_SCALE);
            }

            @Override
            public void set(int value) {
                double maxTorque = ContainerStirlingGenerator.this.getMaxTorque();

                ContainerStirlingGenerator.this.energy = value / (double) ENERGY_SYNC_SCALE * maxTorque;

                ContainerStirlingGenerator.this.refreshGui();
            }
        });

        this.addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return ContainerStirlingGenerator.this.tileEntity.getBurnTime();
            }

            @Override
            public void set(int value) {
                ContainerStirlingGenerator.this.burnTime = value;
                ContainerStirlingGenerator.this.refreshGui();
            }
        });

        this.addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return ContainerStirlingGenerator.this.tileEntity.getBurnTimeBase();
            }

            @Override
            public void set(int value) {
                ContainerStirlingGenerator.this.burnTimeBase = value;
                ContainerStirlingGenerator.this.refreshGui();
            }
        });
    }

    private double getMaxTorque() {
        return this.tileEntity.getMaxTorque(this.tileEntity.getPrimaryFacing());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        if (slotIndex < 0 || slotIndex >= this.slots.size()) {
            return ItemStack.EMPTY;
        }

        Slot slot = this.slots.get(slotIndex);

        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        ItemStack originalStack = stack.copy();

        int playerInventoryStart = GENERATOR_SLOT_COUNT;
        int playerInventoryEnd = this.slots.size();
        int hotbarStart = playerInventoryEnd - 9;

        if (slotIndex < GENERATOR_SLOT_COUNT) {
            if (!this.moveItemStackTo(stack, playerInventoryStart, playerInventoryEnd, true)) {
                return ItemStack.EMPTY;
            }
        } else if (AbstractFurnaceBlockEntity.isFuel(stack)) {
            if (!this.moveItemStackTo(stack, 0, GENERATOR_SLOT_COUNT, false)) {
                if (!this.moveInsidePlayerInventory(stack, slotIndex, playerInventoryStart, hotbarStart, playerInventoryEnd)) {
                    return ItemStack.EMPTY;
                }
            }
        } else {
            if (!this.moveInsidePlayerInventory(stack, slotIndex, playerInventoryStart, hotbarStart, playerInventoryEnd)) {
                return ItemStack.EMPTY;
            }
        }

        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        if (stack.getCount() == originalStack.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTake(player, stack);
        return originalStack;
    }

    private boolean moveInsidePlayerInventory(ItemStack stack, int slotIndex, int playerInventoryStart, int hotbarStart, int playerInventoryEnd) {
        if (slotIndex >= playerInventoryStart && slotIndex < hotbarStart) {

            return this.moveItemStackTo(stack, hotbarStart, playerInventoryEnd, false);
        }

        if (slotIndex >= hotbarStart && slotIndex < playerInventoryEnd) {

            return this.moveItemStackTo(stack, playerInventoryStart, hotbarStart, false);
        }

        return false;
    }
}