package net.shadowmage.ancientwarfare.structure.template.datafixes.fixers.json;

import net.minecraft.nbt.*;

/*
 * Reads NBT tags from JSON formatted strings.<br>
 * All tags will have the outer value of "JSON:{}", with all data enclosed by the set of brackets<br>
 * All names and values will be enclosed by double quotes<br>.
 * All NBT types are represented as an object with two value fields, a type-code to denote how to deserialize, and the field containing the value of the object<br>
 * JSON values should be type-coded with the following codes:<br>
 * <li>ByteTag = pb
 * <li>ByteArrayTag = ab
 * <li>ShortTag = ps
 * <li>IntTag = pi
 * <li>IntArrayTag = ai
 * <li>LongTag = pl
 * <li>FloatTag = pf
 * <li>DoubleTag = pd
 * <li>StringTag = ss
 * <li>ListTag = ls
 * <li>CompoundTag = ct
 *
 * @author Shadowmage
 */
@SuppressWarnings("ConstantConditions")
public class JsonTagReader {
    private JsonTagReader() {
    }

    public static CompoundTag parseTagCompound(String tag) {
        return Json.parseJson(tag).map(JsonTagReader::getTagFrom).orElse(new CompoundTag());
    }

    private static CompoundTag getTagFrom(Json.JsonObject compoundTagObject) {
        return (CompoundTag) getTagFor(compoundTagObject);
    }

    private static Tag getTagFor(Json.JsonObject jsonTagBase) {
        Json.JsonValue val = jsonTagBase.getValue("id");
        String id = val.getStringValue();
        Json.JsonAbstract value = jsonTagBase.getAbstract();
        if ("ct".equals(id)) {
            return getCompoundTagFor((Json.JsonObject) value);
        } else if ("ls".equals(id)) {
            return getListTagFor((Json.JsonArray) value);
        } else if ("pb".equals(id)) {
            return getByteTagFor((Json.JsonValue) value);
        } else if ("ps".equals(id)) {
            return getShortTagFor((Json.JsonValue) value);
        } else if ("pi".equals(id)) {
            return getIntTagFor((Json.JsonValue) value);
        } else if ("pl".equals(id)) {
            return getLongTagFor((Json.JsonValue) value);
        } else if ("pf".equals(id)) {
            return getFloatTagFor((Json.JsonValue) value);
        } else if ("pd".equals(id)) {
            return getDoubleTagFor((Json.JsonValue) value);
        } else if ("ab".equals(id)) {
            return getByteArrayTagFor((Json.JsonArray) value);
        } else if ("ai".equals(id)) {
            return getIntArrayTagFor((Json.JsonArray) value);
        } else if ("ss".equals(id)) {
            return getStringTagFor((Json.JsonValue) value);
        }
        return null;
    }

    private static CompoundTag getCompoundTagFor(Json.JsonObject compoundTagValues) {
        CompoundTag tag = new CompoundTag();
        for (String key : compoundTagValues.keySet()) {
            tag.put(key, getTagFor(compoundTagValues.getObject(key)));
        }
        return tag;
    }

    private static ListTag getListTagFor(Json.JsonArray listTagValues) {
        ListTag list = new ListTag();
        for (int i = 0; i < listTagValues.size(); i++) {
            list.add(getTagFor(listTagValues.getObject(i)));
        }
        return list;
    }

    private static IntArrayTag getIntArrayTagFor(Json.JsonArray value) {
        int[] array = new int[value.size()];
        for (int i = 0; i < value.size(); i++) {
            array[i] = (int) value.getValue(i).getIntegerValue();
        }
        return new IntArrayTag(array);
    }

    private static ByteArrayTag getByteArrayTagFor(Json.JsonArray value) {
        byte[] array = new byte[value.size()];
        for (int i = 0; i < value.size(); i++) {
            array[i] = (byte) value.getValue(i).getIntegerValue();
        }
        return new ByteArrayTag(array);
    }

    private static ByteTag getByteTagFor(Json.JsonValue value) {
        return ByteTag.valueOf((byte) value.getIntegerValue());
    }

    private static ShortTag getShortTagFor(Json.JsonValue value) {
        return ShortTag.valueOf((short) value.getIntegerValue());
    }

    private static IntTag getIntTagFor(Json.JsonValue value) {
        return IntTag.valueOf((int) value.getIntegerValue());
    }

    private static LongTag getLongTagFor(Json.JsonValue value) {
        return LongTag.valueOf(value.getIntegerValue());
    }

    private static FloatTag getFloatTagFor(Json.JsonValue value) {
        return FloatTag.valueOf((float) value.getFloatValue());
    }

    private static DoubleTag getDoubleTagFor(Json.JsonValue value) {
        return DoubleTag.valueOf(value.getFloatValue());
    }

    private static StringTag getStringTagFor(Json.JsonValue value) {
        return StringTag.valueOf(value.getStringValue());
    }

}
