package net.shadowmage.ancientwarfare.core.manual;

import com.google.gson.JsonObject;
import net.shadowmage.ancientwarfare.core.util.JsonUtils;

public class TextElement implements IContentElement {
    private String text;

    public TextElement(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public static TextElement parse(JsonObject elementJson) {
        return new TextElement(JsonUtils.getString(elementJson, "text"));
    }
}
