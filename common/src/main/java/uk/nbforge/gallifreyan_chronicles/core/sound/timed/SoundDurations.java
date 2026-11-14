package uk.nbforge.gallifreyan_chronicles.core.sound.timed;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.Identifier;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public final class SoundDurations {
    private static final Map<Identifier, Double> SECONDS = new HashMap<>();

    private SoundDurations() {
    }

    public static synchronized double seconds(Identifier event) {
        return SECONDS.computeIfAbsent(event, SoundDurations::compute);
    }

    private static double compute(Identifier event) {
        String soundsJson = "/assets/" + event.getNamespace() + "/sounds.json";
        try (InputStream in = SoundDurations.class.getResourceAsStream(soundsJson)) {
            if (in == null) return 0.0;
            JsonObject root = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
            JsonObject entry = root.has(event.getPath()) ? root.getAsJsonObject(event.getPath()) : null;
            if (entry == null || !entry.has("sounds")) return 0.0;

            double longest = 0.0;
            for (JsonElement element : entry.getAsJsonArray("sounds")) {
                String name = element.isJsonObject() ? element.getAsJsonObject().get("name").getAsString() : element.getAsString();
                Identifier file = Identifier.parse(name);
                String path = "/assets/" + file.getNamespace() + "/sounds/" + file.getPath() + ".ogg";
                try (InputStream ogg = SoundDurations.class.getResourceAsStream(path)) {
                    if (ogg != null) longest = Math.max(longest, oggSeconds(ogg.readAllBytes()));
                }
            }
            return longest;
        } catch (IOException | RuntimeException e) {
            return 0.0;
        }
    }

    private static double oggSeconds(byte[] data) {
        int header = indexOf(data, new byte[]{1, 'v', 'o', 'r', 'b', 'i', 's'}, 0);
        if (header < 0 || header + 16 > data.length) return 0.0;
        long rate = readLittleEndian(data, header + 12, 4);
        if (rate <= 0) return 0.0;

        byte[] page = {'O', 'g', 'g', 'S'};
        int last = -1;
        int from = 0;
        while (true) {
            int next = indexOf(data, page, from);
            if (next < 0) break;
            last = next;
            from = next + 1;
        }
        if (last < 0 || last + 14 > data.length) return 0.0;
        long granule = readLittleEndian(data, last + 6, 8);
        return granule <= 0 ? 0.0 : (double) granule / rate;
    }

    private static long readLittleEndian(byte[] data, int offset, int length) {
        long value = 0;
        for (int i = length - 1; i >= 0; i--) {
            value = (value << 8) | (data[offset + i] & 0xFF);
        }
        return value;
    }

    private static int indexOf(byte[] data, byte[] pattern, int from) {
        outer:
        for (int i = from; i <= data.length - pattern.length; i++) {
            for (int j = 0; j < pattern.length; j++) {
                if (data[i + j] != pattern[j]) continue outer;
            }
            return i;
        }
        return -1;
    }
}
