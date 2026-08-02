package net.shadowmage.ancientwarfare.core.datafixes;

import net.minecraft.nbt.CompoundTag;

import java.util.*;

public final class LegacyDataFixerRegistry {
    public enum Target {
        ENTITY,
        BLOCK_ENTITY,
        ITEM
    }

    private static final Map<Target, List<ILegacyDataFixer>> FIXERS = new EnumMap<>(Target.class);

    static {
        for (Target target : Target.values()) {
            FIXERS.put(target, new ArrayList<>());
        }
    }

    private LegacyDataFixerRegistry() {
    }

    public static void clear() {
        FIXERS.values().forEach(List::clear);
    }

    public static void register(Target target, ILegacyDataFixer fixer) {
        List<ILegacyDataFixer> fixers = FIXERS.get(target);
        fixers.add(fixer);
        fixers.sort(Comparator.comparingInt(ILegacyDataFixer::getFixVersion));
    }

    public static CompoundTag apply(Target target, CompoundTag input, int storedVersion) {
        CompoundTag result = input.copy();
        for (ILegacyDataFixer fixer : FIXERS.get(target)) {
            if (fixer.getFixVersion() > storedVersion) {
                result = fixer.fixTagCompound(result);
            }
        }
        return result;
    }
}
