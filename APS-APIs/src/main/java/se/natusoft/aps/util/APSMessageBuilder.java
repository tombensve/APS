package se.natusoft.aps.util;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Builds a Map\<String, Object\> message.
 */
public class APSMessageBuilder {

    private Map<String, Object> message = new LinkedHashMap<>(  );

    private APSMessageBuilder parent = null;

    private APSMessageBuilder root = null;

    public APSMessageBuilder() {}

    Map<String, Object> asMessage() {
        return this.message;
    }

    public APSMessageBuilder endObject() {
        return this.parent != null ? this.parent : this;
    }

    public APSMessageBuilder root() {
        return this.root;
    }

    public APSMessageBuilder addObject(String key) {
        APSMessageBuilder mb = new APSMessageBuilder();
        mb.parent = this;
        if (this.root == null) {
            this.root = this;
            mb.root = this;
        }
        else {
            mb.root = this.root;
        }
        this.message.put(key, mb.message);
        return mb;
    }

    public APSMessageBuilder addString(String key, String value) {
        this.message.put(key, value);
        return this;
    }

    public APSMessageBuilder addBoolean(String key, boolean value) {
        this.message.put(key, value);
        return this;
    }

    public APSMessageBuilder addDouble(String key, double value) {
        this.message.put(key, value);
        return this;
    }

    public APSMessageBuilder addFloat(String key, float value) {
        this.message.put(key, value);
        return this;
    }

    public APSMessageBuilder addLong(String key, long value) {
        this.message.put(key, value);
        return this;
    }

    public APSMessageBuilder addInt(String key, int value) {
        this.message.put(key, value);
        return this;
    }

    public Map<String, Object> toMessage() {
        return this.message;
    }
}
