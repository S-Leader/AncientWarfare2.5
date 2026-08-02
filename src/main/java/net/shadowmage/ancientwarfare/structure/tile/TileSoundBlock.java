package net.shadowmage.ancientwarfare.structure.tile;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.registries.ForgeRegistries;
import net.shadowmage.ancientwarfare.core.interfaces.ISinger;
import net.shadowmage.ancientwarfare.core.interfaces.ITickable;
import net.shadowmage.ancientwarfare.core.network.NetworkHandler;
import net.shadowmage.ancientwarfare.core.tile.TileUpdatable;
import net.shadowmage.ancientwarfare.core.util.BlockTools;
import net.shadowmage.ancientwarfare.core.util.Constants;
import net.shadowmage.ancientwarfare.core.util.LegacyBlockState;
import net.shadowmage.ancientwarfare.structure.AncientWarfareStructure;
import net.shadowmage.ancientwarfare.structure.init.AWStructureBlocks;
import net.shadowmage.ancientwarfare.structure.network.PacketSoundBlockPlayerSpecValues;
import net.shadowmage.ancientwarfare.structure.util.BlockSongPlayData;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class TileSoundBlock extends TileUpdatable implements ISinger, ITickable {
    private static final long MIN_TIME_BETWEEN_ENTRY_PLAYS = 1200;
    private int currentDelay;
    private int tuneIndex = -1;
    private int playerCheckDelay;
    private BlockSongPlayData tuneData;
    private BlockState disguiseState;

    private boolean stoppedForAll = false;
    private Map<UUID, PersistentValues> playerSpecificValues = new HashMap<>();

    public TileSoundBlock() {
        tuneData = new BlockSongPlayData();
    }

    @Override
    public void update() {
        if (!world.isClientSide || isStopped() || validateAndGetPlaying() || !tuneData.getTimeOfDay().takesPlaceNow(world)) {
            return;
        }

        if (tuneData.getPlayOnPlayerEntry()) {
            processPlayerEntry();
        } else {
            if (tuneData.getWhenInRange() && !isPlayerInRange()) {
                return;
            }

            if (--currentDelay <= 0) {
                startSong();
                if (tuneData.getLimitedRepetitions() && incNumberOfTimesRepeated() >= tuneData.getRepetitions()) {
                    setStopped();
                }
            }
        }
    }

    private int incNumberOfTimesRepeated() {
        PersistentValues values = getOrCreatePlayerSpecificValues();
        values.numberOfTimesRepeated++;
        syncValuesToServer(values);
        return values.numberOfTimesRepeated;
    }

    private void setStopped() {
        if (world.isClientSide) {
            PersistentValues values = getOrCreatePlayerSpecificValues();
            values.stopped = true;
            syncValuesToServer(values);
        }
    }

    private void syncValuesToServer(PersistentValues values) {
        NetworkHandler.sendToServer(new PacketSoundBlockPlayerSpecValues(pos, values));
    }

    private boolean isStopped() {
        return stoppedForAll || world.isClientSide && getPlayerSpecificValues().map(values -> values.stopped).orElse(false);
    }

    private PersistentValues getOrCreatePlayerSpecificValues() {
        return AncientWarfareStructure.proxy.getPlayer().map(player -> {
            if (!playerSpecificValues.containsKey(player.getUUID())) {
                playerSpecificValues.put(player.getUUID(), new PersistentValues());
            }
            return playerSpecificValues.get(player.getUUID());
        }).orElse(new PersistentValues());
    }

    private Optional<PersistentValues> getPlayerSpecificValues() {
        return AncientWarfareStructure.proxy.getPlayer().map(player -> Optional.ofNullable(playerSpecificValues.get(player.getUUID())))
                .orElse(Optional.empty());
    }

    public void turnOffByProtectionFlag() {
        if (tuneData.getProtectionFlagTurnOff()) {
            stoppedForAll = true;
            markDirty();
            BlockTools.notifyBlockUpdate(this);
        }
    }

    private void processPlayerEntry() {
        if (playerCheckDelay-- <= 0) {
            playerCheckDelay = 20;
            if (isPlayerInRange()) {
                if (getLastTimePlayerNear() < 0 || world.getGameTime() - getLastTimePlayerNear() > MIN_TIME_BETWEEN_ENTRY_PLAYS) {
                    startSong();
                    if (tuneData.getPlayOnce()) {
                        setStopped();
                    } else {
                        setLastTimePlayerNear();
                    }
                } else {
                    setLastTimePlayerNear();
                }
                markDirty();
            }
        }
    }

    private void setLastTimePlayerNear() {
        PersistentValues values = getOrCreatePlayerSpecificValues();
        values.lastTimePlayerNear = world.getGameTime();
        syncValuesToServer(values);
    }

    private long getLastTimePlayerNear() {
        return getPlayerSpecificValues().map(PersistentValues::getLastTimePlayerNear).orElse(Long.MAX_VALUE);
    }

    private boolean isPlayerInRange() {
        return AncientWarfareStructure.proxy.getClientPlayerDistanceTo(pos) <= tuneData.getPlayerRange();
    }

    private void startSong() {
        if (tuneData.size() == 0) {
            return;
        }

        if (tuneData.getIsRandom()) {
            tuneIndex = 0;
            if (tuneData.size() > 0) {
                tuneIndex = world.getRandom().nextInt(tuneData.size());
                tuneData.get(tuneIndex).getSound().ifPresent(s -> AncientWarfareStructure.proxy.setSoundAt(pos, s, tuneData.getSoundRange() / 16f));
            }
        } else {
            tuneIndex = tuneIndex + 1 < tuneData.size() ? tuneIndex + 1 : 0;
            tuneData.get(tuneIndex).getSound().ifPresent(s -> AncientWarfareStructure.proxy.setSoundAt(pos, s, tuneData.getSoundRange() / 16f));
        }
        AncientWarfareStructure.proxy.playSoundAt(pos);
    }

    @Override
    protected void writeUpdateNBT(CompoundTag tag) {
        super.writeUpdateNBT(tag);
        writeToNBT(tag);
    }

    @Override
    protected void handleUpdateNBT(CompoundTag tag) {
        super.handleUpdateNBT(tag);
        readFromNBT(tag);

        if (isStopped() && AncientWarfareStructure.proxy.isSoundPlayingAt(pos)) {
            AncientWarfareStructure.proxy.stopSoundAt(pos);
        }
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
        super.readFromNBT(tag);
        tuneData.readFromNBT(tag.getCompound("tuneData"));
        tuneIndex = tag.getInt("tuneIndex");
        if (tag.contains("range")) {
            tuneData.setPlayerRange(tag.getInt("range"));
        }
        String id = tag.getString("block");
        if (!id.isEmpty()) {
            ResourceLocation blockId = ResourceLocation.tryParse(id);
            Block block = blockId == null ? null : ForgeRegistries.BLOCKS.getValue(blockId);
            if (block != null) {
                disguiseState = LegacyBlockState.fromMeta(block, tag.getInt("meta"));
            }
        }
        stoppedForAll = tag.getBoolean("stoppedForAll");
        readPlayerSpecificValues(tag);
    }

    private void readPlayerSpecificValues(CompoundTag tag) {
        playerSpecificValues.clear();
        ListTag list = tag.getList("playerSpecificValues", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag pair = list.getCompound(i);
            PersistentValues values = new PersistentValues();
            values.deserializeNBT(pair.getCompound("values"));
            playerSpecificValues.put(pair.getUUID("playerId"), values);
        }
    }

    @Override
    public CompoundTag writeToNBT(CompoundTag tag) {
        super.writeToNBT(tag);
        tag.put("tuneData", tuneData.writeToNBT(new CompoundTag()));
        tag.putInt("tuneIndex", tuneIndex);
        if (disguiseState != null) {
            ResourceLocation blockId = ForgeRegistries.BLOCKS.getKey(disguiseState.getBlock());
            if (blockId != null) {
                tag.putString("block", blockId.toString());
            }
            tag.putInt("meta", LegacyBlockState.toMeta(disguiseState));
        }
        tag.putBoolean("stoppedForAll", stoppedForAll);
        writePlayerSpecificValues(tag);
        return tag;
    }

    private void writePlayerSpecificValues(CompoundTag tag) {
        ListTag list = new ListTag();
        for (Map.Entry<UUID, PersistentValues> entry : playerSpecificValues.entrySet()) {
            CompoundTag pair = new CompoundTag();
            pair.putUUID("playerId", entry.getKey());
            pair.put("values", entry.getValue().serializeNBT());
            list.add(pair);
        }
        tag.put("playerSpecificValues", list);
    }

    public BlockSongPlayData getSongs() {
        return tuneData;
    }

    public BlockState getDisguiseState() {
        return disguiseState;
    }

    public void setDisguiseState(ItemStack itemStack) {
        Block block = Block.byItem(itemStack.getItem());
        BlockState state = LegacyBlockState.fromMeta(block, itemStack.getDamageValue());
        if (block != AWStructureBlocks.SOUND_BLOCK && state.isCollisionShapeFullBlock(world, pos) && state.canOcclude()) {
            disguiseState = state;
            BlockTools.notifyBlockUpdate(this);
            world.updateNeighborsAt(pos, getBlockState().getBlock());
            markDirty();
        }
    }

    private boolean validateAndGetPlaying() {
        if (!AncientWarfareStructure.proxy.hasSoundAt(pos)) {
            return false;
        }
        boolean isPlaying = AncientWarfareStructure.proxy.isSoundPlayingAt(pos);
        if (!isPlaying) {
            resetCurrentTune();
        }

        return isPlaying;
    }

    private void resetCurrentTune() {
        tuneIndex = -1;
        AncientWarfareStructure.proxy.resetSoundAt(pos);
        if (!tuneData.getPlayOnPlayerEntry()) {
            int diff = Math.abs(tuneData.getMaxDelay() - tuneData.getMinDelay()) * 20;
            currentDelay = tuneData.getMinDelay() * 20 + (diff > 0 ? world.getRandom().nextInt(diff) : 0);
        }
    }

    @Override
    public void invalidate() {
        AncientWarfareStructure.proxy.stopSoundAt(pos);
        super.invalidate();
    }

    public void updatePlayerSpecValues(UUID playerId, boolean stopped, long lastTimePlayerNear, int numberOfTimesRepeated) {
        if (!playerSpecificValues.containsKey(playerId)) {
            playerSpecificValues.put(playerId, new PersistentValues());
        }
        PersistentValues values = playerSpecificValues.get(playerId);
        values.stopped = stopped;
        values.lastTimePlayerNear = lastTimePlayerNear;
        values.numberOfTimesRepeated = numberOfTimesRepeated;
        markDirty();
    }

    public void resetStateValues() {
        stoppedForAll = false;
        playerSpecificValues.clear();
        BlockTools.notifyBlockUpdate(this);
    }

    public class PersistentValues implements INBTSerializable<CompoundTag> {
        private long lastTimePlayerNear = -1;
        private boolean stopped = false;
        private int numberOfTimesRepeated = 0;

        public boolean isStopped() {
            return stopped;
        }

        public int getNumberOfTimesRepeated() {
            return numberOfTimesRepeated;
        }

        public long getLastTimePlayerNear() {
            return lastTimePlayerNear;
        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag tag = new CompoundTag();
            tag.putLong("lastTimePlayerNear", lastTimePlayerNear);
            tag.putBoolean("stopped", stopped);
            tag.putInt("numberOfTimesRepeated", numberOfTimesRepeated);
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            lastTimePlayerNear = tag.getLong("lastTimePlayerNear");
            stopped = tag.getBoolean("stopped");
            numberOfTimesRepeated = tag.getInt("numberOfTimesRepeated");
        }
    }
}
