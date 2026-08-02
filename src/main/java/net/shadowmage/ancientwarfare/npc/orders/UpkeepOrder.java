package net.shadowmage.ancientwarfare.npc.orders;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.INBTSerializable;
import net.shadowmage.ancientwarfare.core.util.InventoryTools;
import net.shadowmage.ancientwarfare.core.util.WorldTools;
import net.shadowmage.ancientwarfare.npc.block.BlockTownHall;
import net.shadowmage.ancientwarfare.npc.config.AWNPCStatics;
import net.shadowmage.ancientwarfare.npc.item.ItemUpkeepOrder;

import java.util.Optional;

public class UpkeepOrder implements INBTSerializable<CompoundTag> {
    private static final String UPKEEP_POSITION_TAG = "upkeepPosition";
    private static final String ORDERS_TAG = "orders";
    private BlockPos upkeepPosition;
    private int upkeepDimension;
    private Direction blockSide = Direction.DOWN;
    private int upkeepAmount = 6000;

    public void changeBlockSide() {
        blockSide = Direction.values()[(blockSide.ordinal() + 1) % Direction.values().length];
    }

    public void removeUpkeepPoint() {
        upkeepPosition = null;
        blockSide = Direction.DOWN;
        upkeepDimension = 0;
        upkeepAmount = 6000;
    }

    public void setUpkeepAmount(int amt) {
        this.upkeepAmount = amt;
    }

    public Direction getUpkeepBlockSide() {
        return blockSide;
    }

    public int getUpkeepDimension() {
        return upkeepDimension;
    }

    public Optional<BlockPos> getUpkeepPosition() {
        return Optional.ofNullable(upkeepPosition);
    }

    public final int getUpkeepAmount() {
        return upkeepAmount;
    }

    public boolean addUpkeepPosition(Level world, BlockPos pos) {
        if (WorldTools.getTile(world, pos, BlockEntity.class).map(InventoryTools::isInventory).orElse(false)) {
            if (!AWNPCStatics.npcAllowUpkeepAnyInventory && (!(world.getBlockState(pos).getBlock() instanceof BlockTownHall)))
                return false;
            upkeepPosition = pos;
            upkeepDimension = getLegacyDimensionId(world);
            blockSide = Direction.DOWN;
            upkeepAmount = 6000;
            return true;
        }
        return false;
    }

    private static int getLegacyDimensionId(Level world) {
        if (Level.OVERWORLD.equals(world.dimension())) {
            return 0;
        }
        if (Level.NETHER.equals(world.dimension())) {
            return -1;
        }
        if (Level.END.equals(world.dimension())) {
            return 1;
        }
        return world.dimension().location().toString().hashCode();
    }

    @Override
    public String toString() {
        return "Upkeep Orders[" + upkeepPosition + "]";
    }

    public static Optional<UpkeepOrder> getUpkeepOrder(ItemStack stack) {
        if (!stack.isEmpty() && stack.getItem() instanceof ItemUpkeepOrder) {
            UpkeepOrder order = new UpkeepOrder();
            //noinspection ConstantConditions
            if (stack.hasTag() && stack.getTag().contains(ORDERS_TAG)) {
                order.deserializeNBT(stack.getTag().getCompound(ORDERS_TAG));
            }
            return Optional.of(order);
        }
        return Optional.empty();
    }

    public void write(ItemStack stack) {
        if (!stack.isEmpty() && stack.getItem() instanceof ItemUpkeepOrder) {
            stack.getOrCreateTag().put(ORDERS_TAG, serializeNBT());
        }
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        if (getUpkeepPosition().isPresent()) {
            tag.putLong(UPKEEP_POSITION_TAG, upkeepPosition.asLong());
            tag.putInt("dim", upkeepDimension);
            tag.putByte("side", (byte) blockSide.ordinal());
            tag.putInt("upkeepAmount", upkeepAmount);
        }
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        if (tag.contains(UPKEEP_POSITION_TAG)) {
            upkeepPosition = BlockPos.of(tag.getLong(UPKEEP_POSITION_TAG));
            upkeepDimension = tag.getInt("dim");
            blockSide = Direction.values()[tag.getByte("side")];
            upkeepAmount = tag.getInt("upkeepAmount");
        }
    }
}
