package com.noahtnt2009.gallifreyan_chronicles.tardis.console;

import java.util.Optional;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.noahtnt2009.gallifreyan_chronicles.data.IdentifiableData;
import com.noahtnt2009.gallifreyan_chronicles.entity.TardisControlEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.List;

public record TardisConsole(String id, List<ControlEntry> controls, String defaultVariant,
                            List<GlowGroup> glowGroups) implements IdentifiableData<TardisConsole> {
    public record ControlEntry(String id, Vec3 offset, float width, float height, float depth, float cooldown,
                               ControlAnimationSetting animation) {
        public static final float DEFAULT_COOLDOWN_SECONDS = 1.5F;

        private static final Codec<Vec3> VEC3_CODEC = RecordCodecBuilder.create(inst -> inst.group(
                Codec.DOUBLE.fieldOf("x").forGetter(Vec3::x),
                Codec.DOUBLE.fieldOf("y").forGetter(Vec3::y),
                Codec.DOUBLE.fieldOf("z").forGetter(Vec3::z)
        ).apply(inst, Vec3::new));

        private record Raw(String id, Optional<List<Double>> position, Optional<Vec3> offset,
                           Optional<List<Float>> size, Optional<Float> width, Optional<Float> height,
                           Optional<Float> depth, float cooldown, Optional<ControlAnimationSetting> animation,
                           Optional<Boolean> animated) {
            private static final Codec<Raw> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                    Codec.STRING.fieldOf("id").forGetter(Raw::id),
                    Codec.DOUBLE.listOf().optionalFieldOf("position").forGetter(Raw::position),
                    VEC3_CODEC.optionalFieldOf("offset").forGetter(Raw::offset),
                    Codec.FLOAT.listOf().optionalFieldOf("size").forGetter(Raw::size),
                    Codec.FLOAT.optionalFieldOf("width").forGetter(Raw::width),
                    Codec.FLOAT.optionalFieldOf("height").forGetter(Raw::height),
                    Codec.FLOAT.optionalFieldOf("depth").forGetter(Raw::depth),
                    Codec.floatRange(0.0F, Float.MAX_VALUE).optionalFieldOf("cooldown", DEFAULT_COOLDOWN_SECONDS).forGetter(Raw::cooldown),
                    ControlAnimationSetting.CODEC.optionalFieldOf("animation").forGetter(Raw::animation),
                    Codec.BOOL.optionalFieldOf("animated").forGetter(Raw::animated)
            ).apply(inst, Raw::new));

            private DataResult<ControlEntry> toEntry() {
                Vec3 pos;
                if (position.isPresent()) {
                    List<Double> p = position.get();
                    if (p.size() != 3) return DataResult.error(() -> "Control '" + id + "' position needs 3 numbers");
                    pos = new Vec3(p.get(0), p.get(1), p.get(2));
                } else if (offset.isPresent()) {
                    pos = offset.get();
                } else {
                    return DataResult.error(() -> "Control '" + id + "' needs a position");
                }

                float w;
                float h;
                float d;
                if (size.isPresent()) {
                    List<Float> s = size.get();
                    if (s.isEmpty() || s.size() > 3) {
                        return DataResult.error(() -> "Control '" + id + "' size needs 1 to 3 numbers");
                    }
                    w = s.get(0);
                    h = s.size() > 1 ? s.get(1) : w;
                    d = s.size() > 2 ? s.get(2) : w;
                } else {
                    w = width.orElse(TardisControlEntity.DEFAULT_WIDTH);
                    h = height.orElse(TardisControlEntity.DEFAULT_HEIGHT);
                    d = depth.orElse(w);
                }

                ControlAnimationSetting anim = animation.orElseGet(() ->
                        animated.isPresent() && !animated.get()
                                ? ControlAnimationSetting.DISABLED
                                : ControlAnimationSetting.DEFAULT);
                return DataResult.success(new ControlEntry(id, pos, w, h, d, cooldown, anim));
            }

            private static Raw of(ControlEntry entry) {
                return new Raw(entry.id(),
                        Optional.of(List.of(entry.offset().x, entry.offset().y, entry.offset().z)),
                        Optional.empty(),
                        Optional.of(List.of(entry.width(), entry.height(), entry.depth())),
                        Optional.empty(), Optional.empty(), Optional.empty(),
                        entry.cooldown(),
                        entry.animation().equals(ControlAnimationSetting.DEFAULT)
                                ? Optional.empty()
                                : Optional.of(entry.animation()),
                        Optional.empty());
            }
        }

        public static final Codec<ControlEntry> CODEC = Raw.CODEC.comapFlatMap(Raw::toEntry, Raw::of);

        public static final StreamCodec<FriendlyByteBuf, ControlEntry> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8, ControlEntry::id,
                ByteBufCodecs.DOUBLE, e -> e.offset().x,
                ByteBufCodecs.DOUBLE, e -> e.offset().y,
                ByteBufCodecs.DOUBLE, e -> e.offset().z,
                ByteBufCodecs.FLOAT, ControlEntry::width,
                ByteBufCodecs.FLOAT, ControlEntry::height,
                ByteBufCodecs.FLOAT, ControlEntry::depth,
                ByteBufCodecs.FLOAT, ControlEntry::cooldown,
                ControlAnimationSetting.STREAM_CODEC, ControlEntry::animation,
                (id, x, y, z, width, height, depth, cooldown, animation) ->
                        new ControlEntry(id, new Vec3(x, y, z), width, height, depth, cooldown, animation)
        );
    }

    public TardisConsole(String id) {
        this(id, List.of(), "", List.of());
    }

    public TardisConsole(String id, List<ControlEntry> controls) {
        this(id, controls, "", List.of());
    }

    public static final Codec<TardisConsole> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.STRING.optionalFieldOf("id", "").forGetter(TardisConsole::id),
            ControlEntry.CODEC.listOf().optionalFieldOf("controls", List.of()).forGetter(TardisConsole::controls),
            Codec.STRING.optionalFieldOf("default_variant", "").forGetter(TardisConsole::defaultVariant),
            GlowGroup.CODEC.listOf().optionalFieldOf("glow_groups", List.of()).forGetter(TardisConsole::glowGroups)
    ).apply(inst, TardisConsole::new));

    public static final StreamCodec<FriendlyByteBuf, TardisConsole> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, TardisConsole::id,
            ControlEntry.STREAM_CODEC.apply(ByteBufCodecs.list()), TardisConsole::controls,
            ByteBufCodecs.STRING_UTF8, TardisConsole::defaultVariant,
            GlowGroup.STREAM_CODEC.apply(ByteBufCodecs.list()), TardisConsole::glowGroups,
            TardisConsole::new
    );

    public @Nullable ControlEntry controlEntry(String controlId) {
        for (ControlEntry entry : controls) {
            if (entry.id().equals(controlId)) return entry;
        }
        return null;
    }

    public List<TardisConsoleVariant> variants() {
        return TardisConsoleRegistry.variantsOf(id);
    }

    public List<String> variantIds() {
        return variants().stream().map(TardisConsoleVariant::localId).toList();
    }

    public boolean hasVariants() {
        return !variants().isEmpty();
    }

    public boolean hasVariant(String variantId) {
        return exactVariant(variantId) != null;
    }

    public String resolvedDefaultVariant() {
        if (!defaultVariant.isEmpty() && hasVariant(defaultVariant)) return defaultVariant;
        List<TardisConsoleVariant> all = variants();
        return all.isEmpty() ? "" : all.getFirst().localId();
    }

    private @Nullable TardisConsoleVariant exactVariant(String variantId) {
        TardisConsoleVariant variant = TardisConsoleRegistry.getVariant(variantId);
        return variant != null && variant.parent().equals(id) ? variant : null;
    }

    public @Nullable TardisConsoleVariant variant(String variantId) {
        TardisConsoleVariant variant = exactVariant(variantId);
        return variant != null ? variant : exactVariant(resolvedDefaultVariant());
    }

    public String textureIdFor(String variantId) {
        TardisConsoleVariant variant = variant(variantId);
        return variant != null ? variant.texture() : id;
    }

    @Override
    public TardisConsole withId(String id) {
        return new TardisConsole(id, controls, defaultVariant, glowGroups);
    }
}
