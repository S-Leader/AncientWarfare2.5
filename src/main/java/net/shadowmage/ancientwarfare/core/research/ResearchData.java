package net.shadowmage.ancientwarfare.core.research;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.player.Player;
import net.shadowmage.ancientwarfare.core.compat.WorldSavedData;
import net.shadowmage.ancientwarfare.core.datafixes.ResearchEntryIdNameFixer;
import net.shadowmage.ancientwarfare.core.registry.ResearchRegistry;
import net.shadowmage.ancientwarfare.core.util.Constants;
import net.shadowmage.ancientwarfare.core.util.StreamUtils;

import java.util.*;

public class ResearchData extends WorldSavedData {

    private HashMap<String, ResearchEntry> playerResearchEntries = new HashMap<>();

    public ResearchData(String par1Str) {
        super(par1Str);
    }

    public void onPlayerLogin(Player player) {
        if (!playerResearchEntries.containsKey(player.getName().getString())) {
            playerResearchEntries.put(player.getName().getString(), new ResearchEntry());
            this.markDirty();
        }
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
        playerResearchEntries.clear();

        ListTag entryList = tag.getList("entryList", Constants.NBT.TAG_COMPOUND);

        ResearchEntry entry;
        CompoundTag entryTag;
        String name;
        for (int i = 0; i < entryList.size(); i++) {
            entry = new ResearchEntry();
            entryTag = entryList.getCompound(i);
            name = entryTag.getString("playerName");
            entry.readFromNBT(entryTag);
            playerResearchEntries.put(name, entry);
        }
    }

    @Override
    public CompoundTag writeToNBT(CompoundTag tag) {
        ListTag entryList = new ListTag();
        ResearchEntry entry;

        CompoundTag entryTag;
        for (String name : this.playerResearchEntries.keySet()) {
            entry = this.playerResearchEntries.get(name);
            entryTag = new CompoundTag();
            entryTag.putString("playerName", name);
            entry.writeToNBT(entryTag);
            entryList.add(entryTag);
        }
        tag.put("entryList", entryList);
        return tag;
    }

    public void removeResearchFrom(String playerName, String research) {
        if (playerResearchEntries.containsKey(playerName)) {
            playerResearchEntries.get(playerName).removeResearch(research);
            markDirty();
        }
    }

    public void clearResearchFor(String playerName) {
        if (playerResearchEntries.containsKey(playerName)) {
            playerResearchEntries.get(playerName).clearResearch();
            markDirty();
        }
    }

    public void fillResearchFor(String playerName) {
        if (playerResearchEntries.containsKey(playerName)) {
            playerResearchEntries.get(playerName).fillResearch();
            markDirty();
        }
    }

    public Set<String> getResearchableGoals(String playerName) {
        if (playerResearchEntries.containsKey(playerName)) {
            ResearchEntry entry = playerResearchEntries.get(playerName);
            return getResearchableGoalsFor(entry);
        }
        return Collections.emptySet();
    }

    private static Set<String> getResearchableGoalsFor(ResearchEntry researchEntry) {
        Set<String> totalKnowledge = new HashSet<>();
        totalKnowledge.addAll(researchEntry.getCompletedResearch());
        totalKnowledge.addAll(researchEntry.getQueuedResearch());
        Optional<String> inProgress = researchEntry.getCurrentResearch();
        inProgress.ifPresent(totalKnowledge::add);
        Set<String> researchableGoals = new HashSet<>();
        for (ResearchGoal goal : ResearchRegistry.getAllResearchGoals()) {
            if (totalKnowledge.contains(goal.getName())) {
                continue;
            }

            if (goal.canResearch(totalKnowledge)) {
                researchableGoals.add(goal.getName());
            }
        }
        return researchableGoals;
    }

    public Set<String> getResearchFor(String playerName) {
        if (playerResearchEntries.containsKey(playerName)) {
            return playerResearchEntries.get(playerName).getCompletedResearch();
        }
        return Collections.emptySet();
    }

    public void addResearchTo(String playerName, String research) {
        if (!playerResearchEntries.containsKey(playerName)) {
            playerResearchEntries.put(playerName, new ResearchEntry());
        }
        this.playerResearchEntries.get(playerName).addResearch(research);
        markDirty();
    }

    public boolean hasPlayerCompletedResearch(String playerName, String research) {
        return playerResearchEntries.containsKey(playerName) && playerResearchEntries.get(playerName).knowsResearch(research);
    }

    public Optional<String> getInProgressResearch(String playerName) {
        if (playerResearchEntries.containsKey(playerName)) {
            return playerResearchEntries.get(playerName).getCurrentResearch();
        }
        return Optional.empty();
    }

    public int getResearchProgress(String playerName) {
        if (playerResearchEntries.containsKey(playerName)) {
            return playerResearchEntries.get(playerName).getResearchProgress();
        }
        return 0;
    }

    public void startResearch(String playerName, String goal) {
        if (playerResearchEntries.containsKey(playerName)) {
            playerResearchEntries.get(playerName).startResearch(goal);
            markDirty();
        }
    }

    public void finishResearch(String playerName, String goal) {
        if (playerResearchEntries.containsKey(playerName)) {
            playerResearchEntries.get(playerName).finishResearch(goal);
            markDirty();
        }
    }

    public void setCurrentResearchProgress(String playerName, int progress) {
        if (playerResearchEntries.containsKey(playerName)) {
            playerResearchEntries.get(playerName).setResearchProgress(progress);
            markDirty();
        }
    }

    public void addQueuedResearch(String playerName, String goal) {
        if (playerResearchEntries.containsKey(playerName)) {
            playerResearchEntries.get(playerName).addQueuedResearch(goal);
            markDirty();
        }
    }

    public void removeQueuedResearch(String playerName, String goal) {
        if (playerResearchEntries.containsKey(playerName)) {
            playerResearchEntries.get(playerName).removeQueuedResearch(goal);
            markDirty();
        }
    }

    public List<String> getQueuedResearch(String playerName) {
        if (playerResearchEntries.containsKey(playerName)) {
            return playerResearchEntries.get(playerName).getResearchQueue();
        }
        return Collections.emptyList();
    }

    public boolean addProgress(String playerName, int amount) {
        boolean ret = false;
        if (playerResearchEntries.containsKey(playerName)) {
            ret = playerResearchEntries.get(playerName).addProgress(amount);
            markDirty();
        }

        return ret;
    }

    public boolean hasResearchStarted(String playerName) {
        return playerResearchEntries.containsKey(playerName) && playerResearchEntries.get(playerName).hasResearchStarted();
    }

    public static final class ResearchEntry {
        private String currentResearch = null;
        private int currentProgress = -1;
        private Set<String> completedResearch = new HashSet<>();
        private List<String> queuedResearch = new ArrayList<>();

        private boolean knowsResearch(String researchName) {
            return getCompletedResearch().contains(researchName);
        }

        public Optional<String> getCurrentResearch() {
            return Optional.ofNullable(currentResearch);
        }

        private void resetCurrentResearch() {
            currentResearch = null;
        }

        public void setCurrentResearch(String currentResearch) {
            if (StringUtil.isNullOrEmpty(currentResearch)) {
                return;
            }
            this.currentResearch = currentResearch;
        }

        public boolean addProgress(int amount) {
            Optional<String> curResearch = getCurrentResearch();
            if (curResearch.isPresent()) {
                currentProgress += amount;
                if (currentProgress >= ResearchRegistry.getResearch(curResearch.get()).getTotalResearchTime()) {
                    finishResearch(curResearch.get());
                }
                return true;
            }
            return false;
        }

        public void finishResearch(String researchName) {
            if (getCurrentResearch().map(r -> r.equals(researchName)).orElse(false)) {
                getCompletedResearch().add(researchName);
                currentProgress = -1;
                resetCurrentResearch();
            }
        }

        /*
         * should only be called after a goal from the queue has sucessfully been started -- items used/etc
         */
        public void startResearch(String goal) {
            if (getCurrentResearch().isPresent() || !queuedResearch.contains(goal)) {
                return;
            }
            queuedResearch.remove(goal);
            setCurrentResearch(goal);
            currentProgress = 0;
        }

        public boolean hasResearchStarted() {
            return currentProgress >= 0 && getCurrentResearch().isPresent();
        }

        private void setResearchProgress(int progress) {
            this.currentProgress = progress;
        }

        private int getResearchProgress() {
            return currentProgress;
        }

        private void addResearch(String researchName) {
            getCompletedResearch().add(researchName);
            if (queuedResearch.contains(researchName)) {
                queuedResearch.remove(researchName);
            }
            if (getCurrentResearch().map(r -> r.equals(researchName)).orElse(false)) {
                resetCurrentResearch();
                currentProgress = -1;
            }
        }

        private void removeResearch(String researchName) {
            this.getCompletedResearch().remove(researchName);
        }

        private void clearResearch() {
            getCompletedResearch().clear();
            currentProgress = -1;
            resetCurrentResearch();
            queuedResearch.clear();
        }

        private void fillResearch() {
            getCompletedResearch().clear();
            currentProgress = -1;
            resetCurrentResearch();
            queuedResearch.clear();
            for (ResearchGoal g : ResearchRegistry.getAllResearchGoals()) {
                getCompletedResearch().add(g.getName());
            }
        }

        private void addQueuedResearch(String researchName) {
            if (!queuedResearch.contains(researchName)) {
                queuedResearch.add(researchName);
            }
        }

        private List<String> getResearchQueue() {
            return queuedResearch;
        }

        private void writeToNBT(CompoundTag tag) {
            if (currentResearch != null) {
                tag.putString("currentResearch", currentResearch);
            }
            tag.putInt("currentProgress", currentProgress);
            tag.put("completedResearch", getCompletedResearch().stream().map(StringTag::valueOf).collect(StreamUtils.toNBTTagList));
            tag.put("queuedResearch", queuedResearch.stream().map(StringTag::valueOf).collect(StreamUtils.toNBTTagList));
        }

        private void readFromNBT(CompoundTag tag) {
            CompoundTag fixedTag = ResearchEntryIdNameFixer.fix(tag);
            removeInvalidEntries(fixedTag);
            if (fixedTag.contains("currentResearch")) {
                currentResearch = fixedTag.getString("currentResearch");
            }
            currentProgress = fixedTag.getInt("currentProgress");
            fixedTag.getList("completedResearch", Constants.NBT.TAG_STRING).forEach(t -> getCompletedResearch().add(((StringTag) t).getAsString()));
            fixedTag.getList("queuedResearch", Constants.NBT.TAG_STRING).forEach(t -> queuedResearch.add(((StringTag) t).getAsString()));
        }

        private void removeInvalidEntries(CompoundTag tag) {
            if (tag.contains("currentResearch") && !ResearchRegistry.researchExists(tag.getString("currentResearch"))) {
                tag.remove("currentResearch");
            }
            removeInvalidEntriesFromList(tag, "completedResearch");
            removeInvalidEntriesFromList(tag, "queuedResearch");
        }

        private void removeInvalidEntriesFromList(CompoundTag tag, String listName) {
            ListTag researchList = tag.getList(listName, Constants.NBT.TAG_STRING);
            Iterator<Tag> it = researchList.iterator();

            while (it.hasNext()) {
                String name = ((StringTag) it.next()).getAsString();
                if (!ResearchRegistry.researchExists(name)) {
                    it.remove();
                }
            }
            tag.put(listName, researchList);
        }

        private void removeQueuedResearch(String goal) {
            if (!queuedResearch.contains(goal)) {
                return;
            }

            List<String> goalsToValidate = new ArrayList<>();

            Iterator<String> it = queuedResearch.iterator();
            String exam;
            boolean found = false;
            while (it.hasNext() && (exam = it.next()) != null) {
                if (found) {
                    goalsToValidate.add(exam);
                    it.remove();
                } else if (exam.equals(goal)) {
                    found = true;
                    it.remove();
                }
            }

            Set<String> totalResearch = new HashSet<>();
            totalResearch.addAll(getCompletedResearch());
            totalResearch.addAll(queuedResearch);
            Optional<String> currentResearch = getCurrentResearch();
            currentResearch.ifPresent(totalResearch::add);

            ResearchGoal g;
            for (String g1 : goalsToValidate) {
                g = ResearchRegistry.getResearch(g1);
                if (g != null && g.canResearch(totalResearch)) {
                    totalResearch.add(g1);
                    queuedResearch.add(g1);
                }
            }
        }

        public Set<String> getCompletedResearch() {
            return completedResearch;
        }

        public List<String> getQueuedResearch() {
            return queuedResearch;
        }
    }

}
