package se.natusoft.aps.util;

import java.util.Map;

/**
 * This exists due to that we use Map\<String, Object\> to represent a JSON structure
 * in combination with that Java sucks at using such structures. In Groovy this is not
 * a problem.
 */
public class APSMessageReader {

    private Map<String, Object> message;

    public APSMessageReader(Map<String, Object> message) {
        this.message = message;
    }

    public APSMessageReader asObject(String key) {
        //noinspection unchecked
        return new APSMessageReader( (Map<String, Object>)this.message.get(key) );
    }

    public String asString(String key) {
        return (String)this.message.get(key);
    }

    public long asLong(String key) {
        return (long)this.message.get(key);
    }

    public int asInt(String key) {
        return (int)this.message.get(key);
    }

    public double asDouble(String key) {
        return (double)this.message.get(key);
    }

    public float asFloat(String key) {
        return (float)this.message.get(key);
    }

    public Number asNumber(String key) {
        return (Number)this.message.get(key);
    }

    public boolean asBoolean(String key) {
        return (boolean)this.message.get(key);
    }
}
