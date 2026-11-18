package uk.nbforge.gallifreyan_chronicles.sky.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringRepresentable;
import uk.nbforge.gallifreyan_chronicles.codec.HexColorCodec;
import uk.nbforge.gallifreyan_chronicles.core.data_registry.CelestialBodyRegistry;

public record SkyBody(
        Optional<Identifier> body,
        Optional<Identifier> texture,
        float yaw,
        float pitch,
        float roll,
        Optional<Float> size,
        Optional<Integer> color,
        Optional<Blend> blend,
        Optional<Boolean> fadesInRain,
        Optional<SkyOrbit> orbit,
        Visibility visibility) {
    public static final float DEFAULT_SIZE = 20.0f;

    public static final Codec<SkyBody.Blend> BLEND = StringRepresentable.fromEnum(SkyBody.Blend::values);

    public static final Codec<SkyBody.Visibility> VISIBILITY = StringRepresentable.fromEnum(SkyBody.Visibility::values);

    public static final Codec<SkyBody> CODEC = RecordCodecBuilder.<SkyBody>create(inst -> inst.group(
                            Identifier.CODEC.optionalFieldOf("body").forGetter(SkyBody::body),
                            Identifier.CODEC.optionalFieldOf("texture").forGetter(SkyBody::texture),
                            Codec.FLOAT.optionalFieldOf("yaw", 0.0f).forGetter(SkyBody::yaw),
                            Codec.floatRange(-90.0f, 90.0f)
                                    .optionalFieldOf("pitch", 0.0f)
                                    .forGetter(SkyBody::pitch),
                            Codec.FLOAT.optionalFieldOf("roll", 0.0f).forGetter(SkyBody::roll),
                            Codec.floatRange(0.0f, 200.0f)
                                    .optionalFieldOf("size")
                                    .forGetter(SkyBody::size),
                            HexColorCodec.CODEC.optionalFieldOf("color").forGetter(SkyBody::color),
                            BLEND.optionalFieldOf("blend").forGetter(SkyBody::blend),
                            Codec.BOOL.optionalFieldOf("fades_in_rain").forGetter(SkyBody::fadesInRain),
                            SkyCodec.ORBIT.optionalFieldOf("orbit").forGetter(SkyBody::orbit),
                            VISIBILITY
                                    .optionalFieldOf("visibility", SkyBody.Visibility.ALWAYS)
                                    .forGetter(SkyBody::visibility))
                    .apply(inst, SkyBody::new))
            .validate(SkyBody::validate);

    public static DataResult<SkyBody> validate(SkyBody body) {
        return body.body().isPresent() || body.texture().isPresent()
                ? DataResult.success(body)
                : DataResult.error(() -> "Sky body needs either a \"body\" or a \"texture\"");
    }

    public static SkyBody of(Identifier body) {
        return new SkyBody(
                Optional.of(body),
                Optional.empty(),
                0.0f,
                0.0f,
                0.0f,
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Visibility.ALWAYS);
    }

    public SkyBody at(float yaw, float pitch) {
        return new SkyBody(body, texture, yaw, pitch, roll, size, color, blend, fadesInRain, orbit, visibility);
    }

    public SkyBody orbiting(SkyOrbit orbit) {
        return new SkyBody(
                body, texture, yaw, pitch, roll, size, color, blend, fadesInRain, Optional.of(orbit), visibility);
    }

    public SkyBody sized(float size) {
        return new SkyBody(
                body, texture, yaw, pitch, roll, Optional.of(size), color, blend, fadesInRain, orbit, visibility);
    }

    public SkyBody colored(int color) {
        return new SkyBody(
                body, texture, yaw, pitch, roll, size, Optional.of(color), blend, fadesInRain, orbit, visibility);
    }

    public SkyBody fadingInRain(boolean fades) {
        return new SkyBody(body, texture, yaw, pitch, roll, size, color, blend, Optional.of(fades), orbit, visibility);
    }

    public SkyBody visibleDuring(Visibility visibility) {
        return new SkyBody(body, texture, yaw, pitch, roll, size, color, blend, fadesInRain, orbit, visibility);
    }

    public Optional<ResolvedSkyBody> resolve() {
        CelestialBody base = body.map(CelestialBodyRegistry::get).orElse(null);
        if (body.isPresent() && base == null) return Optional.empty();

        Identifier resolvedTexture = texture.orElseGet(() -> base.texture());
        float resolvedSize = size.orElse(base != null ? base.size() : DEFAULT_SIZE);
        int resolvedColor = color.orElse(base != null ? base.color() : 0xFFFFFFFF);
        Blend resolvedBlend = blend.orElse(base != null ? base.blend() : Blend.NORMAL);
        boolean resolvedFades = fadesInRain.orElse(base == null || base.fadesInRain());
        return Optional.of(new ResolvedSkyBody(
                resolvedTexture,
                yaw,
                pitch,
                roll,
                resolvedSize,
                resolvedColor,
                resolvedBlend,
                resolvedFades,
                orbit,
                visibility));
    }

    public enum Blend implements StringRepresentable {
        NORMAL("normal"),
        ADDITIVE("additive");

        private final String name;

        Blend(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    public enum Visibility implements StringRepresentable {
        ALWAYS("always"),
        DAY("day"),
        NIGHT("night");

        private final String name;

        Visibility(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }
}
