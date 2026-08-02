package net.shadowmage.ancientwarfare.structure.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.shadowmage.ancientwarfare.core.util.SongPlayData;

import java.util.HashMap;
import java.util.Map;

public class BlockSongPlayData extends SongPlayData {
    public static final String SOUND_RANGE_TAG = "soundRange";
    private boolean playOnce = false;
    private int playerRange = 20;
    private boolean limitedRepetitions = false;
    private int repetitions = 1;
    private boolean whenInRange = false;
    private TimeOfDay timeOfDay = TimeOfDay.ANY;
    private boolean protectionFlagTurnOff = false;
    private int soundRange = 16;

    @Override
    public void readFromNBT(CompoundTag tag) {
        super.readFromNBT(tag);
        playOnce = tag.getBoolean("playOnce");
        playerRange = tag.getInt("playerRange");
        limitedRepetitions = tag.getBoolean("limitedRepetitions");
        repetitions = tag.getInt("repetitions");
        whenInRange = tag.getBoolean("whenInRange");
        timeOfDay = TimeOfDay.getById(tag.getInt("timeOfDay"));
        protectionFlagTurnOff = tag.getBoolean("protectionFlagTurnOff");
        setSoundRange(tag.contains(SOUND_RANGE_TAG) ? tag.getInt(SOUND_RANGE_TAG) : 64);
    }

    @Override
    public CompoundTag writeToNBT(CompoundTag tag) {
        tag = super.writeToNBT(tag);
        tag.putBoolean("playOnce", playOnce);
        tag.putInt("playerRange", playerRange);
        tag.putBoolean("limitedRepetitions", limitedRepetitions);
        tag.putInt("repetitions", repetitions);
        tag.putBoolean("whenInRange", whenInRange);
        tag.putInt("timeOfDay", timeOfDay.getId());
        tag.putBoolean("protectionFlagTurnOff", protectionFlagTurnOff);
        tag.putInt(SOUND_RANGE_TAG, soundRange);
        return tag;
    }

    public void setPlayerRange(int playerRange) {
        this.playerRange = playerRange;
    }

    public int getPlayerRange() {
        return playerRange;
    }

    public void setPlayOnce(boolean playOnce) {
        this.playOnce = playOnce;
    }

    public boolean getPlayOnce() {
        return playOnce;
    }

    public void setLimitedRepetitions(boolean limitedRepetitions) {
        this.limitedRepetitions = limitedRepetitions;
    }

    public boolean getLimitedRepetitions() {
        return limitedRepetitions;
    }

    public int getRepetitions() {
        return repetitions;
    }

    public void setRepetitions(int repetitions) {
        this.repetitions = repetitions;
    }

    public boolean getWhenInRange() {
        return whenInRange;
    }

    public void setWhenInRange(boolean whenInRange) {
        this.whenInRange = whenInRange;
    }

    public void setTimeOfDay(TimeOfDay timeOfDay) {
        this.timeOfDay = timeOfDay;
    }

    public TimeOfDay getTimeOfDay() {
        return timeOfDay;
    }

    public void setProtectionFlagTurnOff(boolean protectionFlagTurnOff) {
        this.protectionFlagTurnOff = protectionFlagTurnOff;
    }

    public boolean getProtectionFlagTurnOff() {
        return protectionFlagTurnOff;
    }

    public void setSoundRange(int value) {
        soundRange = Math.max(16, value);
    }

    public int getSoundRange() {
        return soundRange;
    }

    public enum TimeOfDay {
        ANY(0),
        DAY(1) {
            @Override
            public boolean takesPlaceNow(Level world) {
                return isDayTimeClient(world);
            }
        },
        NIGHT(2) {
            @Override
            public boolean takesPlaceNow(Level world) {
                return !isDayTimeClient(world);
            }
        };

        private int id;

        TimeOfDay(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

        private static final Map<Integer, TimeOfDay> VALUES = new HashMap<>();

        static {
            for (TimeOfDay value : values()) {
                VALUES.put(value.getId(), value);
            }
        }

        public static TimeOfDay getById(int id) {
            return VALUES.get(id);
        }

        @SuppressWarnings("squid:S1172") // used in overrides
        public boolean takesPlaceNow(Level world) {
            return true;
        }

        private static boolean isDayTimeClient(Level world) {
            long time = world.getDayTime();
            return time >= 23500 || time <= 12500;
        }
    }
}
