package net.shadowmage.ancientwarfare.core.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.world.item.ItemStack;

public class ItemTools {
    private ItemTools() {
    }

    public static JsonElement serializeToJson(ItemStack stack) {
        JsonObject ret = new JsonObject();
        //noinspection ConstantConditions
        ret.addProperty("name", RegistryTools.getRegistryName(stack.getItem()).toString());
        if (stack.getDamageValue() != 0) {
            ret.addProperty("data", stack.getDamageValue());
        }
        if (stack.getCount() > 1) {
            ret.addProperty("count", stack.getCount());
        }

        if (stack.hasTag()) {

            //noinspection ConstantConditions
            ret.addProperty("nbt", stack.getTag().toString());
        }

        return ret;
    }

}
