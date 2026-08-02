package net.shadowmage.ancientwarfare.core.util;

import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;

import java.util.stream.Collector;

public class StreamUtils {
    private StreamUtils() {
    }

    public static final Collector<StringTag, ListTag, ListTag> toNBTTagList = Collector.of(
            ListTag::new,
            ListTag::add,
            (a, b) -> {
                a.addAll(b);
                return a;
            },
            l -> l,
            Collector.Characteristics.UNORDERED);
}
