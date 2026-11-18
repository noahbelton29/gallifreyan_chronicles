package uk.nbforge.gallifreyan_chronicles.tardis.exterior;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import uk.nbforge.gallifreyan_chronicles.Constants;
import uk.nbforge.gallifreyan_chronicles.loader.IdentifiableData;

public record TardisExterior(String id, String waypointStyle) implements IdentifiableData<TardisExterior> {
    public static final String DEFAULT_WAYPOINT_STYLE = "tardis";
    public static final Vec3 DEFAULT_KEY_OFFSET = new Vec3(0.0, -0.925, 1.0);
    public static final String KEY_BONE_NAME = "key_to_render";

    private static final Map<String, Vec3> KEY_OFFSETS = new ConcurrentHashMap<>();

    public static final Codec<TardisExterior> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                    Codec.STRING.optionalFieldOf("id", "").forGetter(TardisExterior::id),
                    Codec.STRING
                            .optionalFieldOf("waypoint_style", TardisExterior.DEFAULT_WAYPOINT_STYLE)
                            .forGetter(TardisExterior::waypointStyle))
            .apply(inst, TardisExterior::new));

    public static final StreamCodec<FriendlyByteBuf, TardisExterior> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            TardisExterior::id,
            ByteBufCodecs.STRING_UTF8,
            TardisExterior::waypointStyle,
            TardisExterior::new);

    public Vec3 keyOffset() {
        return KEY_OFFSETS.computeIfAbsent(id, TardisExterior::readKeyBoneOffset);
    }

    /**
     * pivot is in sixteenths of a block and x is flipped
     * falls back to the default offset if the model or bone is missing
     */
    private static Vec3 readKeyBoneOffset(String exteriorId) {
        Identifier parsed = Identifier.tryParse(exteriorId);
        if (parsed == null) return DEFAULT_KEY_OFFSET;
        String resource = "/assets/" + parsed.getNamespace() + "/geckolib/models/" + parsed.getPath() + ".json";
        try (InputStream in = TardisExterior.class.getResourceAsStream(resource)) {
            if (in == null) {
                Constants.LOG.warn("No model {} found to read the key bone of exterior {}", resource, exteriorId);
                return DEFAULT_KEY_OFFSET;
            }
            JsonObject root = JsonParser.parseString(new String(in.readAllBytes(), StandardCharsets.UTF_8))
                    .getAsJsonObject();
            JsonArray geometries = root.getAsJsonArray("minecraft:geometry");
            if (geometries != null) {
                for (JsonElement geometry : geometries) {
                    JsonArray bones = geometry.getAsJsonObject().getAsJsonArray("bones");
                    if (bones == null) continue;
                    for (JsonElement boneElement : bones) {
                        JsonObject bone = boneElement.getAsJsonObject();
                        if (!bone.has("name")
                                || !KEY_BONE_NAME.equals(bone.get("name").getAsString())) continue;
                        JsonArray pivot = bone.getAsJsonArray("pivot");
                        if (pivot == null || pivot.size() < 3) continue;
                        return new Vec3(
                                -pivot.get(0).getAsDouble() / 16.0,
                                pivot.get(1).getAsDouble() / 16.0 - 0.5,
                                pivot.get(2).getAsDouble() / 16.0);
                    }
                }
            }
            Constants.LOG.warn("Model {} has no '{}' bone, using the default key offset", resource, KEY_BONE_NAME);
        } catch (IOException | RuntimeException e) {
            Constants.LOG.warn("Failed to read the key bone of exterior {}", exteriorId, e);
        }
        return DEFAULT_KEY_OFFSET;
    }

    public static float modelRotationDegrees(float yawDegrees) {
        return 180.0f - yawDegrees;
    }

    public Vec3 worldKeyOffset(float yawDegrees) {
        Vec3 keyOffset = keyOffset();
        double radians = Math.toRadians(modelRotationDegrees(yawDegrees));
        double sin = Math.sin(radians);
        double cos = Math.cos(radians);

        double x = keyOffset.x() * cos + keyOffset.z() * sin;
        double z = -keyOffset.x() * sin + keyOffset.z() * cos;

        return new Vec3(x, keyOffset.y(), z);
    }

    @Override
    public TardisExterior withId(String id) {
        return new TardisExterior(id, waypointStyle);
    }
}
