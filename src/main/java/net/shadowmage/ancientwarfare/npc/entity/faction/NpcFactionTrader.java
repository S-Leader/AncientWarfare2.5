package net.shadowmage.ancientwarfare.npc.entity.faction;


import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.shadowmage.ancientwarfare.core.init.AWMenuTypes;
import net.shadowmage.ancientwarfare.core.network.NetworkHandler;
import net.shadowmage.ancientwarfare.npc.ai.*;
import net.shadowmage.ancientwarfare.npc.item.ItemCommandBaton;
import net.shadowmage.ancientwarfare.npc.trade.FactionTradeList;


public class NpcFactionTrader extends NpcFaction {

    private FactionTradeList tradeList = new FactionTradeList();
    private Player trader;
    private boolean noTradesDespawn = false;

    @SuppressWarnings("unused")
    public NpcFactionTrader(EntityType<? extends PathfinderMob> type, Level world) {
        super(type, world);
        addAI();
    }

    @SuppressWarnings("unused")


    private void addAI() {
        tasks.addTask(0, new FloatGoal(this));
        tasks.addTask(0, new NpcAIRestrictOpenDoor(this));
        tasks.addTask(0, new NpcAIDoor(this, true));
        tasks.addTask(1, new NpcAIFollowPlayer(this));
        tasks.addTask(2, new NpcAIMoveHome(this, 50F, 5F, 30F, 5F));

        tasks.addTask(101, new LookAtPlayerGoal(this, Player.class, 3.0F, 1.0F));
        tasks.addTask(102, new NpcAIWander(this));
        tasks.addTask(103, new LookAtPlayerGoal(this, Mob.class, 8.0F));
    }

    public FactionTradeList getTradeList() {
        return tradeList;
    }

    public void startTrade(Player player) {
        trader = player;
    }

    public void closeTrade() {
        trader = null;
    }

    @Override
    public void tick() {
        super.tick();
        if (!world.isClientSide) {
            if (noTradesDespawn && tradeList.isEmpty()) {
                discard();
            }
            tradeList.tick(world);
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        boolean baton = !player.getItemInHand(hand).isEmpty() && player.getItemInHand(hand).getItem() instanceof ItemCommandBaton;
        if (!baton && isAlive()) {
            if (!player.level().isClientSide && trader == null && !player.isShiftKeyDown()) {
                startTrade(player);
                AWMenuTypes.open(player, NetworkHandler.GUI_NPC_FACTION_TRADE_VIEW, getId(), 0, 0);
            }
            return InteractionResult.sidedSuccess(level().isClientSide);
        }
        return InteractionResult.PASS;
    }

    @Override
    public String getNpcType() {
        return "trader";
    }

    @Override
    public boolean isHostileTowards(Entity e) {
        return false;
    }

    @Override
    public boolean canTarget(Entity e) {
        return false;
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        tradeList.deserializeNBT(tag.getCompound("tradeList"));
        noTradesDespawn = true;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.put("tradeList", tradeList.serializeNBT());
    }
}
