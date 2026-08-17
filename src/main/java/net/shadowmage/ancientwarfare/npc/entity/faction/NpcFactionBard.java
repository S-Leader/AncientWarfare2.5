package net.shadowmage.ancientwarfare.npc.entity.faction;


import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.shadowmage.ancientwarfare.core.init.AWMenuTypes;
import net.shadowmage.ancientwarfare.core.interfaces.ISinger;
import net.shadowmage.ancientwarfare.core.network.NetworkHandler;
import net.shadowmage.ancientwarfare.core.util.SongPlayData;
import net.shadowmage.ancientwarfare.npc.ai.*;

public class NpcFactionBard extends NpcFaction implements ISinger {

    private SongPlayData tuneData = new SongPlayData();

    @SuppressWarnings("unused")
    public NpcFactionBard(EntityType<? extends PathfinderMob> type, Level world) {
        super(type, world);
        addAI();
    }

    @SuppressWarnings("unused")


    private void addAI() {
        tasks.addTask(0, new FloatGoal(this));
        tasks.addTask(0, new NpcAIRestrictOpenDoor(this));
        tasks.addTask(0, new NpcAIDoor(this, true));
        tasks.addTask(1, new NpcAIFollowPlayer(this));
        tasks.addTask(2, new NpcAIMoveHome(this, 50F, 3F, 30F, 3F));
        tasks.addTask(3, new NpcAISing(this));

        tasks.addTask(101, new LookAtPlayerGoal(this, Player.class, 3.0F, 1.0F));
        tasks.addTask(102, new NpcAIWander(this));
        tasks.addTask(103, new LookAtPlayerGoal(this, Mob.class, 8.0F));
    }

    @Override
    public SongPlayData getSongs() {
        return tuneData;
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
    public boolean hasAltGui() {
        return true;
    }

    @Override
    public String getNpcType() {
        return "bard";
    }

    @Override
    public void openAltGui(Player player) {
        AWMenuTypes.open(player, NetworkHandler.GUI_NPC_FACTION_BARD, getId(), 0, 0);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        tuneData.readFromNBT(tag.getCompound("tuneData"));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.put("tuneData", tuneData.writeToNBT(new CompoundTag()));
    }
}
