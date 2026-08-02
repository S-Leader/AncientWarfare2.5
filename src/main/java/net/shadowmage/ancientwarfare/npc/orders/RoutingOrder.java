package net.shadowmage.ancientwarfare.npc.orders;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.IItemHandler;
import net.shadowmage.ancientwarfare.core.util.Constants;
import net.shadowmage.ancientwarfare.core.util.InventoryTools;
import net.shadowmage.ancientwarfare.core.util.OrderingList;
import net.shadowmage.ancientwarfare.npc.item.ItemRoutingOrder;

import java.util.List;

public class RoutingOrder extends OrderingList<RoutingOrder.RoutePoint> implements INBTSerializable<CompoundTag> {
    private static final String ORDERS_TAG = "orders";

    public void addRoutePoint(Direction side, BlockPos pos) {
        add(new RoutePoint(side, pos));
    }

    private boolean check(int index) {
        return index >= 0 && index < size();
    }

    public void changeRouteType(int index, boolean isRmb) {
        if (check(index)) {
            get(index).changeRouteType(isRmb);
        }
    }

    public void changeBlockSide(int index) {
        if (check(index)) {
            get(index).changeBlockSide();
        }
    }

    public void toggleIgnoreDamage(int index) {
        if (check(index)) {
            get(index).toggleIgnoreDamage();
        }
    }

    public void toggleIgnoreTag(int index) {
        if (check(index)) {
            get(index).toggleIgnoreTag();
        }
    }

    public static RoutingOrder getRoutingOrder(ItemStack stack) {
        if (!stack.isEmpty() && stack.getItem() instanceof ItemRoutingOrder) {
            RoutingOrder order = new RoutingOrder();
            //noinspection ConstantConditions
            if (stack.hasTag() && stack.getTag().contains(ORDERS_TAG)) {
                order.deserializeNBT(stack.getTag().getCompound(ORDERS_TAG));
            }
            return order;
        }
        return null;
    }

    public void write(ItemStack stack) {
        if (!stack.isEmpty() && stack.getItem() instanceof ItemRoutingOrder) {
            stack.getOrCreateTag().put(ORDERS_TAG, serializeNBT());
        }
    }

    @Override
    public CompoundTag serializeNBT() {
        ListTag list = new ListTag();
        for (RoutePoint p : points) {
            list.add(p.writeToNBT(new CompoundTag()));
        }
        CompoundTag tag = new CompoundTag();
        tag.put("entryList", list);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        clear();
        ListTag entryList = tag.getList("entryList", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < entryList.size(); i++) {
            add(new RoutePoint(entryList.getCompound(i)));
        }
    }

    public static class RoutePoint {
        private boolean ignoreDamage;
        private boolean ignoreTag;
        private RouteType routeType = RouteType.FILL_TARGET_TO;
        private BlockPos target = BlockPos.ZERO;
        private Direction blockSide = Direction.DOWN;
        private NonNullList<ItemStack> filters = NonNullList.withSize(12, ItemStack.EMPTY);

        private RoutePoint(CompoundTag tag) {
            readFromNBT(tag);
        }

        private RoutePoint(Direction side, BlockPos target) {
            this.target = target;
            this.blockSide = side;
        }

        private void changeBlockSide() {
            blockSide = Direction.values()[(blockSide.ordinal() + 1) % Direction.values().length];
        }

        private void changeRouteType(boolean isRmb) {
            routeType = isRmb ? routeType.previous() : routeType.next();
        }

        public void setFilter(int index, ItemStack stack) {
            filters.set(index, stack);
        }

        public Direction getBlockSide() {
            return blockSide;
        }

        public RouteType getRouteType() {
            return routeType;
        }

        public BlockPos getTarget() {
            return target;
        }

        public ItemStack getFilterInSlot(int slot) {
            return filters.get(slot);
        }

        public int getFilterSize() {
            return filters.size();
        }

        public boolean getIgnoreDamage() {
            return ignoreDamage;
        }

        public boolean getIgnoreTag() {
            return ignoreTag;
        }

        private void toggleIgnoreDamage() {
            ignoreDamage = !ignoreDamage;
        }

        private void toggleIgnoreTag() {
            ignoreTag = !ignoreTag;
        }

        private int depositAllItems(IItemHandler from, IItemHandler to) {
            float movedStacks = 0;
            for (ItemStack filter : filters) {
                if (filter.isEmpty()) {
                    continue;
                }
                int movedSize = InventoryTools.transferItems(from, to, filter, Integer.MAX_VALUE, ignoreDamage, ignoreTag);
                movedStacks += (float) movedSize / filter.getMaxStackSize();
            }
            return (int) Math.ceil(movedStacks);
        }

        private int depositAllItemsExcept(IItemHandler from, IItemHandler to) {
            float movedStacks = 0;
            for (ItemStack stack : InventoryTools.getIterator(from)) {
                if (stack.isEmpty() || matchesFilter(stack)) {
                    continue;
                }
                ItemStack filter = stack.copy();
                ItemStack remaining = InventoryTools.insertItem(to, filter, true);
                int toMove = stack.getCount() - remaining.getCount();
                if (!InventoryTools.removeItems(from, filter, toMove, true).isEmpty()) {
                    ItemStack removedStack = InventoryTools.removeItems(from, filter, toMove);
                    InventoryTools.insertItem(to, removedStack);
                    movedStacks += ((float) removedStack.getCount()) / ((float) filter.getMaxStackSize());
                }
            }
            return (int) Math.ceil(movedStacks);
        }

        private boolean matchesFilter(ItemStack stack) {
            return filters.stream().anyMatch(s -> !s.isEmpty() && InventoryTools.doItemStacksMatch(stack, s, ignoreDamage, ignoreTag));
        }

        private int fillTo(IItemHandler from, IItemHandler to) {
            float moved = 0;
            for (ItemStack filter : filters) {
                if (filter.isEmpty()) {
                    continue;
                }
                int foundCount = InventoryTools.getCountOf(to, filter);
                int toMove = filter.getCount();
                if (foundCount > toMove) {
                    continue;
                }
                toMove -= foundCount;
                int m1 = InventoryTools.transferItems(from, to, filter, toMove, ignoreDamage, ignoreTag);
                moved += (float) m1 / filter.getMaxStackSize();
            }
            return (int) Math.ceil(moved);
        }

        private int depositRatio(IItemHandler from, IItemHandler to) {
            float movedTotal = 0;
            for (ItemStack filter : filters) {
                if (filter.isEmpty()) {
                    continue;
                }
                int foundCount = InventoryTools.getCountOf(from, filter);
                int toMove = (int) (foundCount * (1f / (float) filter.getCount()));
                int moved = InventoryTools.transferItems(from, to, filter, toMove, ignoreDamage, ignoreTag);
                movedTotal += (float) moved / filter.getMaxStackSize();
            }

            return (int) Math.ceil(movedTotal);
        }

        private int depositExact(IItemHandler from, IItemHandler to) {
            float movedTotal = 0;
            for (ItemStack filter : filters) {
                if (filter.isEmpty()) {
                    continue;
                }
                int foundCount = InventoryTools.getCountOf(from, filter);
                int toMove = filter.getCount();
                if (foundCount < toMove) {
                    continue;
                }
                if (!InventoryTools.canInventoryHold(to, filter))
                    continue;
                int moved = InventoryTools.transferItems(from, to, filter, toMove, ignoreDamage, ignoreTag);
                movedTotal += (float) moved / filter.getMaxStackSize();
            }
            return (int) Math.ceil(movedTotal);
        }

        private int fillAtLeast(IItemHandler from, IItemHandler to) {
            float movedTotal = 0;
            for (ItemStack filter : filters) {
                if (filter.isEmpty()) {
                    continue;
                }
                int foundCount = InventoryTools.getCountOf(from, filter);
                int existingCount = InventoryTools.getCountOf(to, filter);
                int toMove = filter.getCount() - existingCount; // we only want to move items up to the specified filter size
                if (toMove < 1 || foundCount < toMove) {
                    // the target already has more than the filter specifies
                    // or the source doesn't have enough to fulfill the minimum requirement
                    continue;
                }

                ItemStack filterAdjusted = filter.copy();
                filterAdjusted.setCount(toMove);
                if (!InventoryTools.canInventoryHold(to, filterAdjusted)) {
                    continue;
                }
                int moved = InventoryTools.transferItems(from, to, filterAdjusted, foundCount, ignoreDamage, ignoreTag);
                movedTotal += (float) moved / filter.getMaxStackSize();
            }
            return (int) Math.ceil(movedTotal);
        }

        private void readFromNBT(CompoundTag tag) {
            routeType = RouteType.values()[tag.getInt("type")];
            target = BlockPos.of(tag.getLong("position"));
            blockSide = Direction.values()[tag.getByte("blockSide")];
            ignoreDamage = tag.getBoolean("ignoreDamage");
            ignoreTag = tag.getBoolean("ignoreTag");
            ListTag filterList = tag.getList("filterList", Constants.NBT.TAG_COMPOUND);
            int[] filterCounts = tag.getIntArray("filterCounts");
            for (int i = 0; i < filterList.size(); i++) {
                CompoundTag itemTag = filterList.getCompound(i);
                int slot = itemTag.getInt("slot");

                while (slot >= filters.size()) {
                    filters.add(ItemStack.EMPTY);
                }

                ItemStack filterStack = ItemStack.of(itemTag);
                filterStack.setCount(filterCounts[slot]);
                filters.set(slot, filterStack);
            }
        }

        private CompoundTag writeToNBT(CompoundTag tag) {
            tag.putInt("type", routeType.ordinal());
            tag.putLong("position", target.asLong());
            tag.putByte("blockSide", (byte) blockSide.ordinal());
            tag.putBoolean("ignoreDamage", ignoreDamage);
            tag.putBoolean("ignoreTag", ignoreTag);
            ListTag filterList = new ListTag();
            int[] filterCounts = new int[filters.size()];
            for (int i = 0; i < filters.size(); i++) {
                if (filters.get(i).isEmpty()) {
                    filterCounts[i] = 0;
                    continue;
                }
                filterCounts[i] = filters.get(i).getCount();
                ItemStack filterCopy = filters.get(i).copy();
                filterCopy.setCount(1);
                CompoundTag itemTag = filterCopy.save(new CompoundTag());
                itemTag.putInt("slot", i);
                filterList.add(itemTag);
            }
            tag.put("filterList", filterList);
            tag.putIntArray("filterCounts", filterCounts);
            return tag;
        }

    }

    public enum RouteType {
        /*
         * fill target up to the specified quantity from couriers inventory
         */
        FILL_TARGET_TO("route.fill.upto"),

        /*
         * fill courier up to the specified quantity from targets inventory
         */
        FILL_COURIER_TO("route.take.upto"),

        /*
         * deposit any of the specified items from courier into target inventory
         * (no quantity limit)
         */
        DEPOSIT_ALL_OF("route.deposit.match"),

        /*
         * withdraw any of the specified items from target inventory into courier inventory
         * (no quantity limit)
         */
        WITHDRAW_ALL_OF("route.withdraw.match"),

        /*
         * deposit all items in courier inventory, except those matching filter items
         */
        DEPOSIT_ALL_EXCEPT("route.deposit.no_match"),

        /*
         * withdraw all items in target inventory except those matching filters
         */
        WITHDRAW_ALL_EXCEPT("route.withdraw.no_match"),

        /*
         * deposit specified ratio of items (ratio is 1/filterStacksize)
         */
        DEPOSIT_RATIO("route.deposit.ratio"),

        /*
         * withdraw specified ratio of items (ratio is 1/filterStacksize)
         */
        WITHDRAW_RATIO("route.withdraw.ratio"),

        /*
         * deposit exact number of items (or none at all if not possible)
         */
        DEPOSIT_EXACT("route.deposit.exact"),

        /*
         * withdraw exact number of items (or none at all if not possible)
         */
        WITHDRAW_EXACT("route.withdraw.exact"),

        /*
         * deposit a minimum of items
         */
        FILL_MINIMUM("route.fill.minimum"),

        /*
         * withdraw a minimum of items
         */
        TAKE_MINIMUM("route.take.minimum");

        final String key;

        RouteType(String key) {
            this.key = key;
        }

        public String getTranslationKey() {
            return key;
        }

        public static RouteType next(RouteType type) {
            return type == null ? RouteType.FILL_TARGET_TO : type.next();
        }

        public static RouteType previous(RouteType type) {
            return type == null ? RouteType.FILL_TARGET_TO : type.previous();
        }

        public RouteType next() {
            int ordinal = ordinal() + 1;
            if (ordinal >= RouteType.values().length) {
                ordinal = 0;
            }
            return RouteType.values()[ordinal];
        }

        public RouteType previous() {
            int ordinal = ordinal() - 1;
            if (ordinal < 0) {
                ordinal = RouteType.values().length - 1;
            }
            return RouteType.values()[ordinal];
        }

    }

    /*
     * do the routing action for the courier at the given route-point.  position/distance is not checked, should check in AI before calling<br>
     * returns the number of stacks processed for determining the length the courier should 'work' at the point
     */
    public int handleRouteAction(RoutePoint p, IItemHandler npc, IItemHandler target) {
        switch (p.routeType) {
            case FILL_COURIER_TO:
                return p.fillTo(target, npc);

            case FILL_TARGET_TO:
                return p.fillTo(npc, target);

            case DEPOSIT_ALL_EXCEPT:
                return p.depositAllItemsExcept(npc, target);

            case DEPOSIT_ALL_OF:
                return p.depositAllItems(npc, target);

            case WITHDRAW_ALL_EXCEPT:
                return p.depositAllItemsExcept(target, npc);

            case WITHDRAW_ALL_OF:
                return p.depositAllItems(target, npc);

            case DEPOSIT_RATIO:
                return p.depositRatio(npc, target);

            case WITHDRAW_RATIO:
                return p.depositRatio(target, npc);

            case DEPOSIT_EXACT:
                return p.depositExact(npc, target);

            case WITHDRAW_EXACT:
                return p.depositExact(target, npc);

            case FILL_MINIMUM:
                return p.fillAtLeast(npc, target);

            case TAKE_MINIMUM:
                return p.fillAtLeast(target, npc);

            default:
                return 0;
        }
    }

    public List<RoutePoint> getEntries() {
        return points;
    }

}
