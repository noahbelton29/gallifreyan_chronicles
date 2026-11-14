package uk.nbforge.gallifreyan_chronicles.tardis.console;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import uk.nbforge.gallifreyan_chronicles.Constants;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Reads control positions from a console model's "&lt;control_id&gt;_hitbox" bones, so the model is the single
 * source of truth for where each control sits.
 *
 * <p>A bone's first cube is the hitbox. Its eight corners are carried through the cube's own rotation and then
 * through the rotation of its bone and every parent bone, exactly the way GeckoLib draws the model (x mirrored,
 * x and y angles negated, Z then Y then X order). The centre of the moved cube is the control position
 * (/16, y minus 0.5625 like the rest of the console space) and the cube's own size is the hitbox size, since a
 * control hitbox can't be tilted.
 */
final class ConsoleHitboxBones {
    static final String SUFFIX = "_hitbox";
    private static final double Y_OFFSET = 0.5625;
    private static final Map<String, Map<String, Box>> CACHE = new ConcurrentHashMap<>();

    record Box(Vec3 center, float width, float height, float depth) {
    }

    private ConsoleHitboxBones() {
    }

    static Map<String, Box> read(String consoleId) {
        return CACHE.computeIfAbsent(consoleId, ConsoleHitboxBones::load);
    }

    private static Map<String, Box> load(String consoleId) {
        Map<String, Box> boxes = new HashMap<>();
        Identifier parsed = Identifier.tryParse(consoleId.contains(":") ? consoleId : Constants.MOD_ID + ":" + consoleId);
        if (parsed == null) return boxes;
        String resource = "/assets/" + parsed.getNamespace() + "/geckolib/models/" + parsed.getPath() + ".json";
        try (InputStream in = ConsoleHitboxBones.class.getResourceAsStream(resource)) {
            if (in == null) {
                Constants.LOG.warn("No model {} found to read hitbox bones of console {}", resource, consoleId);
                return boxes;
            }
            JsonObject root = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
            JsonArray geometries = root.getAsJsonArray("minecraft:geometry");
            if (geometries == null) return boxes;

            Map<String, JsonObject> bones = new HashMap<>();
            for (JsonElement geometry : geometries) {
                JsonArray list = geometry.getAsJsonObject().getAsJsonArray("bones");
                if (list == null) continue;
                for (JsonElement element : list) {
                    JsonObject bone = element.getAsJsonObject();
                    if (bone.has("name")) bones.put(bone.get("name").getAsString(), bone);
                }
            }
            for (Map.Entry<String, JsonObject> entry : bones.entrySet()) {
                String name = entry.getKey();
                if (!name.endsWith(SUFFIX)) continue;
                Box box = boxOf(entry.getValue(), bones);
                if (box != null) boxes.put(name.substring(0, name.length() - SUFFIX.length()), box);
            }
        } catch (IOException | RuntimeException e) {
            Constants.LOG.warn("Failed to read hitbox bones of console {}", consoleId, e);
        }
        return boxes;
    }

    private static @Nullable Box boxOf(JsonObject bone, Map<String, JsonObject> bones) {
        if (!bone.has("cubes")) return null;
        JsonArray cubes = bone.getAsJsonArray("cubes");
        if (cubes.isEmpty()) return null;
        JsonObject cube = cubes.get(0).getAsJsonObject();
        double[] origin = numbers(cube, "origin");
        double[] size = numbers(cube, "size");
        if (origin == null || size == null) return null;

        double[] low = {Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE};
        double[] high = {-Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE};
        for (int corner = 0; corner < 8; corner++) {
            /* x is mirrored, as the model is drawn */
            double[] point = {
                    -(origin[0] + ((corner & 1) != 0 ? size[0] : 0.0)),
                    origin[1] + ((corner & 2) != 0 ? size[1] : 0.0),
                    origin[2] + ((corner & 4) != 0 ? size[2] : 0.0)};
            spin(point, numbers(cube, "pivot"), numbers(cube, "rotation"));
            JsonObject current = bone;
            int guard = 0;
            while (current != null && guard++ < 64) {
                spin(point, numbers(current, "pivot"), numbers(current, "rotation"));
                current = current.has("parent") ? bones.get(current.get("parent").getAsString()) : null;
            }
            for (int axis = 0; axis < 3; axis++) {
                low[axis] = Math.min(low[axis], point[axis]);
                high[axis] = Math.max(high[axis], point[axis]);
            }
        }
        Vec3 centre = new Vec3((low[0] + high[0]) / 2.0 / 16.0, (low[1] + high[1]) / 2.0 / 16.0 - Y_OFFSET,
                (low[2] + high[2]) / 2.0 / 16.0);
        return new Box(centre, (float) (size[0] / 16.0), (float) (size[1] / 16.0), (float) (size[2] / 16.0));
    }

    /* turns a point about a pivot the way GeckoLib does: x mirrored, x and y angles negated, x then y then z */
    private static void spin(double[] point, double @Nullable [] pivot, double @Nullable [] rotation) {
        if (rotation == null || (rotation[0] == 0.0 && rotation[1] == 0.0 && rotation[2] == 0.0)) return;
        double px = pivot == null ? 0.0 : -pivot[0];
        double py = pivot == null ? 0.0 : pivot[1];
        double pz = pivot == null ? 0.0 : pivot[2];
        double x = point[0] - px;
        double y = point[1] - py;
        double z = point[2] - pz;

        double a = -Math.toRadians(rotation[0]);
        double c = Math.cos(a);
        double s = Math.sin(a);
        double ny = y * c - z * s;
        double nz = y * s + z * c;
        y = ny;
        z = nz;

        a = -Math.toRadians(rotation[1]);
        c = Math.cos(a);
        s = Math.sin(a);
        double nx = x * c + z * s;
        nz = -x * s + z * c;
        x = nx;
        z = nz;

        a = Math.toRadians(rotation[2]);
        c = Math.cos(a);
        s = Math.sin(a);
        nx = x * c - y * s;
        ny = x * s + y * c;
        x = nx;
        y = ny;

        point[0] = x + px;
        point[1] = y + py;
        point[2] = z + pz;
    }

    private static double @Nullable [] numbers(JsonObject object, String key) {
        if (!object.has(key) || !object.get(key).isJsonArray()) return null;
        JsonArray array = object.getAsJsonArray(key);
        if (array.size() < 3) return null;
        return new double[]{array.get(0).getAsDouble(), array.get(1).getAsDouble(), array.get(2).getAsDouble()};
    }
}
