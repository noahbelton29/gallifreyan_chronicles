package uk.nbforge.gallifreyan_chronicles.datagen.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

/** Tiny JSON builder so datagen data reads like the files it produces. */
public final class GCJson {
    private GCJson() {
    }

    /** Key/value pairs: obj("a", 1, "b", obj(...)). */
    public static JsonObject obj(Object... keyValues) {
        if (keyValues.length % 2 != 0) throw new IllegalArgumentException("obj needs key/value pairs");
        JsonObject out = new JsonObject();
        for (int i = 0; i < keyValues.length; i += 2) {
            out.add((String) keyValues[i], toJson(keyValues[i + 1]));
        }
        return out;
    }

    public static JsonArray arr(Object... values) {
        JsonArray out = new JsonArray();
        for (Object value : values) out.add(toJson(value));
        return out;
    }

    private static JsonElement toJson(Object value) {
        return switch (value) {
            case null -> JsonNull.INSTANCE;
            case JsonElement element -> element;
            case String s -> new com.google.gson.JsonPrimitive(s);
            case Boolean b -> new com.google.gson.JsonPrimitive(b);
            case Number n -> new com.google.gson.JsonPrimitive(n);
            default -> throw new IllegalArgumentException("Unsupported JSON value " + value.getClass());
        };
    }
}
