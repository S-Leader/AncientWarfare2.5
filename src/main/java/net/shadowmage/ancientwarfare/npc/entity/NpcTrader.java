package net.shadowmage.ancientwarfare.npc.entity;


import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.shadowmage.ancientwarfare.core.init.AWMenuTypes;
import net.shadowmage.ancientwarfare.core.network.NetworkHandler;
import net.shadowmage.ancientwarfare.npc.ai.*;
import net.shadowmage.ancientwarfare.npc.ai.owned.*;
import net.shadowmage.ancientwarfare.npc.item.ItemCommandBaton;
import net.shadowmage.ancientwarfare.npc.item.ItemTradeOrder;
import net.shadowmage.ancientwarfare.npc.orders.TradeOrder;
import net.shadowmage.ancientwarfare.npc.trade.POTradeList;

public class NpcTrader extends NpcPlayerOwned {

    private Player trader;//used by guis/containers to prevent further interaction
    private POTradeList tradeList = new POTradeList();
    private NpcAIPlayerOwnedTrader tradeAI;

    public NpcTrader(Level par1World) {
        super(par1World);

        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(0, new NpcAIRestrictOpenDoor(this));
        this.goalSelector.addGoal(0, new NpcAIDoor(this, true));
        this.goalSelector.addGoal(0, (horseAI = new NpcAIPlayerOwnedRideHorse(this)));
        this.goalSelector.addGoal(2, new NpcAIFollowPlayer(this));
        this.goalSelector.addGoal(2, new NpcAIPlayerOwnedFollowCommand(this));
        this.goalSelector.addGoal(3, new NpcAIFleeHostiles(this));
        this.goalSelector.addGoal(3, new NpcAIPlayerOwnedAlarmResponse(this));
        this.goalSelector.addGoal(4, tradeAI = new NpcAIPlayerOwnedTrader(this));
        this.goalSelector.addGoal(5, new NpcAIPlayerOwnedGetFood(this));
        this.goalSelector.addGoal(6, new NpcAIPlayerOwnedIdleWhenHungry(this));
        this.goalSelector.addGoal(7, new NpcAIMoveHome(this, 50F, 3F, 30F, 3F));

        //post-100 -- used by delayed shared tasks (look at random stuff, wander)
        this.goalSelector.addGoal(101, new LookAtPlayerGoal(this, Player.class, 3.0F, 1.0F));
        this.goalSelector.addGoal(102, new NpcAIWander(this));
        this.goalSelector.addGoal(103, new LookAtPlayerGoal(this, Mob.class, 8.0F));
    }

    @Override
    public boolean isValidOrdersStack(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof ItemTradeOrder;
    }

    @Override
    public void onOrdersInventoryChanged() {
        tradeList = null;
        if (isValidOrdersStack(ordersStack)) {
            tradeList = TradeOrder.getTradeOrder(ordersStack).getTradeList();
        }
        tradeAI.onOrdersUpdated();
    }

    @Override
    public String getNpcSubType() {
        return "";
    }

    @Override
    public String getNpcType() {
        return "trader";
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        boolean baton = !player.getItemInHand(hand).isEmpty() && player.getItemInHand(hand).getItem() instanceof ItemCommandBaton;
        if (baton) {
            return InteractionResult.PASS;
        }
        if (player.level().isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (isOwner(player))//owner
        {
            return tryCommand(player) ? InteractionResult.CONSUME : InteractionResult.PASS;
        } else//non-owner
        {
            if (trader != null && !trader.isAlive()) {
                closeTrade();
            }
            if (getFoodRemaining() > 0 && trader == null) {
                startTrade(player);
                openAltGui(player);
            }
        }
        return InteractionResult.CONSUME;
    }

    public void startTrade(Player player) {
        trader = player;
    }

    public void closeTrade() {
        trader = null;
    }

    @Override
    public void openAltGui(Player player) {
        AWMenuTypes.open(player, NetworkHandler.GUI_NPC_PLAYER_OWNED_TRADE, getId(), 0, 0);
    }

    @Override
    public boolean hasAltGui() {
        return true;
    }

    @Override
    public boolean shouldBeAtHome() {
        return (world.dimensionType().hasSkyLight() && !world.isDay()) || world.isRainingAt(blockPosition());
    }

    @Override
    public boolean isHostileTowards(Entity e) {
        return false;
    }

    public POTradeList getTradeList() {
        return tradeList;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.put("tradeAI", tradeAI.writeToNBT(new CompoundTag()));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        tradeAI.readFromNBT(tag.getCompound("tradeAI"));
    }

}
