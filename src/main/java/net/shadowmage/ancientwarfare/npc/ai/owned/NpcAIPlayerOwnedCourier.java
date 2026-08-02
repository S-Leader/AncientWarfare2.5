package net.shadowmage.ancientwarfare.npc.ai.owned;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.IItemHandler;
import net.shadowmage.ancientwarfare.core.compat.CapabilityItemHandler;
import net.shadowmage.ancientwarfare.core.owner.IOwnable;
import net.shadowmage.ancientwarfare.core.util.WorldTools;
import net.shadowmage.ancientwarfare.npc.ai.NpcAI;
import net.shadowmage.ancientwarfare.npc.config.AWNPCStatics;
import net.shadowmage.ancientwarfare.npc.entity.NpcCourier;
import net.shadowmage.ancientwarfare.npc.orders.RoutingOrder;

import javax.annotation.Nullable;

public class NpcAIPlayerOwnedCourier extends NpcAI<NpcCourier> {

    private boolean init;
    private int routeIndex;
    private int ticksToWork;
    private int ticksAtSite;
    private RoutingOrder order;
    private ItemStack routeStack;

    public NpcAIPlayerOwnedCourier(NpcCourier npc) {
        super(npc);
        setMutexBits(ATTACK + MOVE);
    }

    @Override
    public boolean canUse() {
        if (!super.canUse()) {
            return false;
        }

        if (!init) {
            init = true;
            routeStack = npc.ordersStack;
            order = RoutingOrder.getRoutingOrder(routeStack);
            if (order == null || routeIndex >= order.size()) {
                routeIndex = 0;
            }
        }
        return !npc.shouldBeAtHome() && npc.backpackInventory != null && order != null && !order.isEmpty();
    }

    @Override
    public void start() {
        npc.addAITask(TASK_WORK);
    }

    @Override
    public void tick() {
        BlockPos pos = order.get(routeIndex).getTarget();
        double dist = npc.getDistanceSq(pos);
        if (dist > AWNPCStatics.npcActionRange * AWNPCStatics.npcActionRange) {
            npc.addAITask(TASK_MOVE);
            ticksAtSite = 0;
            ticksToWork = 0;
            moveToPosition(pos, dist);
        } else {
            moveRetryDelay = 0;
            npc.getNavigation().stop();
            npc.removeAITask(TASK_MOVE);
            workAtSite();
        }
    }

    @Override
    public void stop() {
        ticksToWork = 0;
        ticksAtSite = 0;
        moveRetryDelay = 0;
        npc.getNavigation().stop();
        npc.removeAITask(TASK_WORK + TASK_MOVE);
    }

    private void workAtSite() {
        if (ticksToWork == 0) {
            startWork();
        } else {
            ticksAtSite++;
            if (npc.tickCount % 10 == 0) {
                npc.swing(InteractionHand.MAIN_HAND);
            }
            if (ticksAtSite > ticksToWork) {
                setMoveToNextSite();
            }
        }
    }

    private void startWork() {
        IItemHandler target = getTargetHandler();
        if (target != null) {
            ticksAtSite = 0;
            int moved = order.handleRouteAction(order.get(routeIndex), npc.backpackInventory, target);
            if (moved > 0) {
                ticksToWork = (AWNPCStatics.npcWorkTicks - 30 - npc.getLevelingStats().getLevel()) * moved;
                if (ticksToWork <= 0) {
                    ticksToWork = 0;
                }
                npc.addExperience(moved * AWNPCStatics.npcXpFromMoveItem);
                return;
            }
        }
        setMoveToNextSite();
    }

    @Nullable
    private IItemHandler getTargetHandler() {
        RoutingOrder.RoutePoint point = order.get(routeIndex);
        return WorldTools.getTile(npc.level(), point.getTarget())
                .filter(t -> t.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, point.getBlockSide()).isPresent())
                .map(t -> getTargetHandler(point.getBlockSide(), t)).orElse(null);
    }

    @Nullable
    private IItemHandler getTargetHandler(Direction side, BlockEntity te) {
        if (te instanceof IOwnable) {
            IOwnable ownableTE = (IOwnable) te;
            if (!npc.hasCommandPermissions(ownableTE.getOwner())) {
                return null;
            }
        }
        return te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side).orElse(null);
    }

    private void setMoveToNextSite() {
        ticksAtSite = 0;
        ticksToWork = 0;
        moveRetryDelay = 0;
        routeIndex++;
        if (routeIndex >= order.size()) {
            routeIndex = 0;
        }
    }

    public void onOrdersChanged() {
        routeStack = npc.ordersStack;
        order = RoutingOrder.getRoutingOrder(routeStack);
        routeIndex = 0;
        ticksAtSite = 0;
        ticksToWork = 0;
        moveRetryDelay = 0;
    }

    public void readFromNBT(CompoundTag tag) {
        routeIndex = tag.getInt("routeIndex");
        ticksAtSite = tag.getInt("ticksAtSite");
        ticksToWork = tag.getInt("ticksToWork");
    }

    public CompoundTag writeToNBT(CompoundTag tag) {
        tag.putInt("routeIndex", routeIndex);
        tag.putInt("ticksAtSite", ticksAtSite);
        tag.putInt("ticksToWork", ticksToWork);
        return tag;
    }
}
