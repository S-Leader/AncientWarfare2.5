package net.shadowmage.ancientwarfare.npc.trade;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.shadowmage.ancientwarfare.core.util.Constants;

import java.util.ArrayList;
import java.util.List;

public final class POTradeRoute {

    private List<POTradePoint> route = new ArrayList<>();

    public int size() {
        return route.size();
    }

    public POTradePoint get(int index) {
        return route.get(index);
    }

    public void decrementRoutePoint(int index) {
        if (index <= 0 || index >= route.size()) {
            return;
        }
        POTradePoint p = route.remove(index);
        route.add(index - 1, p);
    }

    public void incrementRoutePoint(int index) {
        if (index < 0 || index >= route.size() - 1) {
            return;
        }
        POTradePoint p = route.remove(index);
        route.add(index + 1, p);
    }

    public void deleteRoutePoint(int index) {
        if (index < 0 || index >= route.size()) {
            return;
        }
        route.remove(index);
    }

    public void addRoutePoint(BlockPos pos) {
        POTradePoint p = new POTradePoint();
        p.position = pos;
        p.delay = 20 * 60;
        p.shouldUpkeep = false;
        route.add(p);
    }

    public void setPointDelay(int index, int delay) {
        route.get(index).setDelay(delay);
    }

    public void setUpkeep(int index, boolean val) {
        route.get(index).setShouldUpkeep(val);
    }

    public void readFromNBT(CompoundTag tag) {
        route.clear();
        ListTag list = tag.getList("route", Constants.NBT.TAG_COMPOUND);
        POTradePoint p;
        for (int i = 0; i < list.size(); i++) {
            p = new POTradePoint();
            p.readFromNBT(list.getCompound(i));
            route.add(p);
        }
    }

    public CompoundTag writeToNBT(CompoundTag tag) {
        ListTag list = new ListTag();
        for (POTradePoint point : route) {
            list.add(point.writeToNBT(new CompoundTag()));
        }
        tag.put("route", list);
        return tag;
    }

}
