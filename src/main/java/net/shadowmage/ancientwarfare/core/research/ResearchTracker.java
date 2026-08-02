package net.shadowmage.ancientwarfare.core.research;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.shadowmage.ancientwarfare.core.gamedata.AWGameData;
import net.shadowmage.ancientwarfare.core.network.NetworkHandler;
import net.shadowmage.ancientwarfare.core.network.PacketResearchInit;
import net.shadowmage.ancientwarfare.core.network.PacketResearchStart;
import net.shadowmage.ancientwarfare.core.network.PacketResearchUpdate;
import net.shadowmage.ancientwarfare.core.registry.ResearchRegistry;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public final class ResearchTracker {

    public static final ResearchTracker INSTANCE = new ResearchTracker();
    private final ResearchData clientData;

    private ResearchTracker() {
        clientData = new ResearchData("AWResearchData");
    }

    /*
     * SERVER ONLY
     */
    @SubscribeEvent
    public void playerLogInEvent(PlayerEvent.PlayerLoggedInEvent evt) {
        getResearchData(evt.getEntity().level()).onPlayerLogin(evt.getEntity());
        PacketResearchInit init = new PacketResearchInit(getResearchData(evt.getEntity().level()));
        NetworkHandler.sendToPlayer((ServerPlayer) evt.getEntity(), init);
    }

    public void clearResearch(Level world, String playerName) {
        if (world.isClientSide) {
            clientData.clearResearchFor(playerName);
        } else {
            getResearchData(world).clearResearchFor(playerName);
            PacketResearchInit pkt = new PacketResearchInit(getResearchData(world));
            NetworkHandler.sendToAllPlayers(pkt);
        }
    }

    public void removeResearch(Level world, String playerName, String research) {
        if (world.isClientSide) {
            clientData.removeResearchFrom(playerName, research);
        } else {
            getResearchData(world).removeResearchFrom(playerName, research);
            PacketResearchInit pkt = new PacketResearchInit(getResearchData(world));
            NetworkHandler.sendToAllPlayers(pkt);
        }
    }

    public void fillResearch(Level world, String playerName) {
        if (world.isClientSide) {
            clientData.fillResearchFor(playerName);
        } else {
            getResearchData(world).fillResearchFor(playerName);
            PacketResearchInit pkt = new PacketResearchInit(getResearchData(world));
            NetworkHandler.sendToAllPlayers(pkt);
        }
    }

    public void addResearch(Level world, String playerName, String research) {
        if (world.isClientSide) {
            clientData.addResearchTo(playerName, research);
        } else {
            getResearchData(world).addResearchTo(playerName, research);
            PacketResearchUpdate pkt = new PacketResearchUpdate(playerName, research, true, true);
            NetworkHandler.sendToAllPlayers(pkt);
        }
    }

    /*
     * @param world
     * @param player
     * @param research
     * @return
     */
    public boolean hasPlayerCompleted(Level world, String player, String research) {
        if (world.isClientSide) {
            return clientData.hasPlayerCompletedResearch(player, research);
        }
        return getResearchData(world).hasPlayerCompletedResearch(player, research);
    }

    public boolean addResearchFromNotes(Level world, String player, String research) {
        if (hasPlayerCompleted(world, player, research)) {
            return false;
        }
        addResearch(world, player, research);
        return true;
    }

    public boolean addProgressFromNotes(Level world, String player, String research) {
        if (world.isClientSide) {
            return false;
        }
        ResearchGoal goal = ResearchRegistry.getResearch(research);
        return getResearchData(world).addProgress(player, goal.getTotalResearchTime() / 4);
    }

    /*
     * @param world
     * @param playerName
     * @return
     */
    public Set<String> getCompletedResearchFor(Level world, String playerName) {
        if (world.isClientSide) {
            return clientData.getResearchFor(playerName);
        }
        return getResearchData(world).getResearchFor(playerName);
    }

    public List<String> getResearchQueueFor(Level world, String playerName) {
        if (world.isClientSide) {
            return Collections.emptyList();
        }
        return getResearchData(world).getQueuedResearch(playerName);
    }

    public Set<String> getResearchableGoals(Level world, String playerName) {
        if (world.isClientSide) {
            return clientData.getResearchableGoals(playerName);
        } else {
            return getResearchData(world).getResearchableGoals(playerName);
        }
    }

    /*
     * @param world
     * @return
     */
    private ResearchData getResearchData(Level world) {
        if (world.isClientSide) {
            return clientData;
        }
        return AWGameData.INSTANCE.getData(world, ResearchData.class);
    }

    /*
     * CLIENT ONLY
     */
    public void onClientResearchReceived(CompoundTag researchDataTag) {
        this.clientData.readFromNBT(researchDataTag);
    }

    public Optional<String> getCurrentGoal(Level world, String playerName) {
        if (world.isClientSide) {
            return clientData.getInProgressResearch(playerName);
        }
        return getResearchData(world).getInProgressResearch(playerName);
    }

    public int getProgress(Level world, String playerName) {
        if (world.isClientSide) {
            return clientData.getResearchProgress(playerName);
        }
        return getResearchData(world).getResearchProgress(playerName);
    }

    public void setProgress(Level world, String playerName, int progress) {
        if (world.isClientSide) {
            clientData.setCurrentResearchProgress(playerName, progress);
        } else {
            getResearchData(world).setCurrentResearchProgress(playerName, progress);
        }
    }

    public void removeQueuedGoal(Level world, String playerName, String goal) {
        if (world.isClientSide) {
            clientData.removeQueuedResearch(playerName, goal);
        } else {
            getResearchData(world).removeQueuedResearch(playerName, goal);
            PacketResearchUpdate pkt = new PacketResearchUpdate(playerName, goal, false, false);
            NetworkHandler.sendToAllPlayers(pkt);
        }
    }

    public void addQueuedGoal(Level world, String playerName, String goal) {
        if (world.isClientSide) {
            clientData.addQueuedResearch(playerName, goal);
        } else {
            getResearchData(world).addQueuedResearch(playerName, goal);
            PacketResearchUpdate pkt = new PacketResearchUpdate(playerName, goal, true, false);
            NetworkHandler.sendToAllPlayers(pkt);
        }
    }

    public void startResearch(Level world, String playerName, String goal) {
        if (world.isClientSide) {
            clientData.startResearch(playerName, goal);
        } else {
            getResearchData(world).startResearch(playerName, goal);
            PacketResearchStart pkt = new PacketResearchStart(playerName, goal, true);
            NetworkHandler.sendToAllPlayers(pkt);
        }
    }

    public void finishResearch(Level world, String playerName, String goal) {
        if (world.isClientSide) {
            clientData.finishResearch(playerName, goal);
        } else {
            getResearchData(world).finishResearch(playerName, goal);
            PacketResearchStart pkt = new PacketResearchStart(playerName, goal, false);
            NetworkHandler.sendToAllPlayers(pkt);
        }
    }

}
