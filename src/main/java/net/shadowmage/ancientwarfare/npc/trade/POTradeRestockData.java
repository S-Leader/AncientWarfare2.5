package net.shadowmage.ancientwarfare.npc.trade;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraftforge.items.IItemHandler;
import net.shadowmage.ancientwarfare.core.util.Constants;

import java.util.ArrayList;
import java.util.List;

public class POTradeRestockData {
    private BlockPos withdrawPoint;
    private Direction withdrawSide = Direction.DOWN;
    private List<POTradeWithdrawEntry> withdrawList = new ArrayList<>();
    private BlockPos depositPoint;
    private Direction depositSide = Direction.UP;
    private List<POTradeDepositEntry> depositList = new ArrayList<>();

    public BlockPos getDepositPoint() {
        return depositPoint;
    }

    public BlockPos getWithdrawPoint() {
        return withdrawPoint;
    }

    public Direction getDepositSide() {
        return depositSide;
    }

    public Direction getWithdrawSide() {
        return withdrawSide;
    }

    public void deleteDepositPoint() {
        depositPoint = null;
    }

    public void deleteWithdrawPoint() {
        withdrawPoint = null;
    }

    public List<POTradeWithdrawEntry> getWithdrawList() {
        return withdrawList;
    }

    public List<POTradeDepositEntry> getDepositList() {
        return depositList;
    }

    public void addDepositEntry() {
        depositList.add(new POTradeDepositEntry());
    }

    public void addWithdrawEntry() {
        withdrawList.add(new POTradeWithdrawEntry());
    }

    public void removeDepositEntry(int index) {
        depositList.remove(index);
    }

    public void removeWithdrawEntry(int index) {
        withdrawList.remove(index);
    }

    public void setDepositPoint(BlockPos pos, Direction side) {
        depositPoint = pos;
        depositSide = side;
    }

    public void setWithdrawPoint(BlockPos pos, Direction side) {
        withdrawPoint = pos;
        withdrawSide = side;
    }

    public void doDeposit(IItemHandler storage, IItemHandler deposit) {
        for (POTradeDepositEntry aDeposit : depositList) {
            aDeposit.process(storage, deposit);
        }
    }

    public void doWithdraw(IItemHandler storage, IItemHandler withdraw) {
        for (POTradeWithdrawEntry aWithdraw : withdrawList) {
            aWithdraw.process(storage, withdraw);
        }
    }

    public void readFromNBT(CompoundTag tag) {
        if (tag.contains("withdrawPoint")) {
            withdrawPoint = BlockPos.of(tag.getLong("withdrawPoint"));
            withdrawSide = Direction.values()[tag.getByte("withdrawSide")];
        }
        if (tag.contains("depositPoint")) {
            depositPoint = BlockPos.of(tag.getLong("depositPoint"));
            depositSide = Direction.values()[tag.getByte("depositSide")];
        }

        ListTag deposit = tag.getList("depositList", Constants.NBT.TAG_COMPOUND);
        POTradeDepositEntry de;
        for (int i = 0; i < deposit.size(); i++) {
            de = new POTradeDepositEntry();
            de.readFromNBT(deposit.getCompound(i));
            this.depositList.add(de);
        }

        ListTag withdraw = tag.getList("withdrawList", Constants.NBT.TAG_COMPOUND);
        POTradeWithdrawEntry we;
        for (int i = 0; i < withdraw.size(); i++) {
            we = new POTradeWithdrawEntry();
            we.readFromNBT(withdraw.getCompound(i));
            this.withdrawList.add(we);
        }
    }

    public CompoundTag writeToNBT(CompoundTag tag) {
        if (withdrawPoint != null) {
            tag.putLong("withdrawPoint", withdrawPoint.asLong());
            tag.putByte("withdrawSide", (byte) withdrawSide.ordinal());
        }
        if (depositPoint != null) {
            tag.putLong("depositPoint", depositPoint.asLong());
            tag.putByte("depositSide", (byte) depositSide.ordinal());
        }

        ListTag depositTagList = new ListTag();
        for (POTradeDepositEntry aDeposit : this.depositList) {
            depositTagList.add(aDeposit.writeToNBT(new CompoundTag()));
        }
        tag.put("depositList", depositTagList);

        ListTag withdrawTagList = new ListTag();
        for (POTradeWithdrawEntry aWithdraw : this.withdrawList) {
            withdrawTagList.add(aWithdraw.writeToNBT(new CompoundTag()));
        }
        tag.put("withdrawList", withdrawTagList);
        return tag;
    }
}
