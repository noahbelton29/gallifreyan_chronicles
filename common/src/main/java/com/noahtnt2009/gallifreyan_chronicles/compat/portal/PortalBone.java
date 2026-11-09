package com.noahtnt2009.gallifreyan_chronicles.compat.portal;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.noahtnt2009.gallifreyan_chronicles.Constants;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/* where the portal goes, read from the "portal" bone of a geo model. blocks, blockbench space */
public record PortalBone(double x, double bottom, double width, double height, double front, double back) {
    public static final String BONE_NAME = "portal";

    private static final Map<String, Optional<PortalBone>> CACHE = new ConcurrentHashMap<>();

    public double centerY() {
        return bottom + height / 2.0;
    }

    public static @Nullable PortalBone of(@Nullable String modelId) {
        if (modelId == null || modelId.isEmpty()) return null;
        return CACHE.computeIfAbsent(modelId, PortalBone::read).orElse(null);
    }

    public static void clearCache() {
        CACHE.clear();
    }

    private static Optional<PortalBone> read(String modelId) {
        Identifier id = modelId.contains(":")
                ? Identifier.tryParse(modelId)
                : Identifier.tryParse(Constants.MOD_ID + ":" + modelId);
        if (id == null) return Optional.empty();
        String resource = "/assets/" + id.getNamespace() + "/geckolib/models/" + id.getPath() + ".json";
        try (InputStream in = PortalBone.class.getResourceAsStream(resource)) {
            if (in == null) return Optional.empty();
            JsonObject root = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
            JsonArray geometries = root.getAsJsonArray("minecraft:geometry");
            if (geometries == null) return Optional.empty();
            for (JsonElement geometry : geometries) {
                JsonArray bones = geometry.getAsJsonObject().getAsJsonArray("bones");
                if (bones == null) continue;
                Map<String, JsonObject> byName = new HashMap<>();
                for (JsonElement bone : bones) {
                    JsonObject obj = bone.getAsJsonObject();
                    if (obj.has("name")) byName.put(obj.get("name").getAsString(), obj);
                }
                JsonObject portal = byName.get(BONE_NAME);
                if (portal != null) return Optional.ofNullable(fromBone(portal, byName, resource));
            }
        } catch (IOException | RuntimeException e) {
            Constants.LOG.warn("Failed to read the portal bone of {}", resource, e);
        }
        return Optional.empty();
    }

    private static @Nullable PortalBone fromBone(JsonObject bone, Map<String, JsonObject> bones, String resource) {
        JsonArray cubes = bone.getAsJsonArray("cubes");
        if (cubes == null || cubes.isEmpty()) {
            Constants.LOG.warn("Portal bone in {} has no cube", resource);
            return null;
        }
        double[] min = {Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE};
        double[] max = {-Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE};
        for (JsonElement cubeElement : cubes) {
            JsonObject cube = cubeElement.getAsJsonObject();
            double[] o = vec(cube.getAsJsonArray("origin"));
            double[] s = vec(cube.getAsJsonArray("size"));
            for (int i = 0; i < 8; i++) {
                double[] p = {o[0] + ((i & 1) != 0 ? s[0] : 0), o[1] + ((i & 2) != 0 ? s[1] : 0),
                        o[2] + ((i & 4) != 0 ? s[2] : 0)};
                /* cube spin, then every bone up the chain */
                p = turn(p, cube);
                JsonObject at = bone;
                while (at != null) {
                    p = turn(p, at);
                    at = at.has("parent") ? bones.get(at.get("parent").getAsString()) : null;
                }
                for (int a = 0; a < 3; a++) {
                    min[a] = Math.min(min[a], p[a]);
                    max[a] = Math.max(max[a], p[a]);
                }
            }
        }
        /* geo json has x flipped vs blockbench */
        double x = -(min[0] + max[0]) / 2.0;
        return new PortalBone(x / 16.0, min[1] / 16.0, (max[0] - min[0]) / 16.0, (max[1] - min[1]) / 16.0,
                min[2] / 16.0, max[2] / 16.0);
    }

    /* y spin only, x/z spins on the portal are ignored */
    private static double[] turn(double[] p, JsonObject part) {
        JsonArray rotation = part.getAsJsonArray("rotation");
        if (rotation == null || rotation.size() < 3) return p;
        double degrees = rotation.get(1).getAsDouble();
        if (degrees == 0.0) return p;
        double[] pivot = part.has("pivot") ? vec(part.getAsJsonArray("pivot")) : new double[3];
        double r = Math.toRadians(degrees);
        double sin = Math.sin(r);
        double cos = Math.cos(r);
        double dx = p[0] - pivot[0];
        double dz = p[2] - pivot[2];
        return new double[]{pivot[0] + dx * cos + dz * sin, p[1], pivot[2] - dx * sin + dz * cos};
    }

    private static double[] vec(@Nullable JsonArray array) {
        if (array == null || array.size() < 3) return new double[3];
        return new double[]{array.get(0).getAsDouble(), array.get(1).getAsDouble(), array.get(2).getAsDouble()};
    }
}
