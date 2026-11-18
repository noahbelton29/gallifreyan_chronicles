package uk.nbforge.gallifreyan_chronicles.tardis.console;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import uk.nbforge.gallifreyan_chronicles.Constants;
import uk.nbforge.gallifreyan_chronicles.codec.Vec3Codec;
import uk.nbforge.gallifreyan_chronicles.core.data_registry.TardisConsoleRegistry;
import uk.nbforge.gallifreyan_chronicles.core.entity.TardisControlEntity;
import uk.nbforge.gallifreyan_chronicles.loader.IdentifiableData;

public record TardisConsole(
        String id, List<ControlEntry> controls, String defaultVariant, List<GlowGroup> glowGroups, int sortOrder)
        implements IdentifiableData<TardisConsole> {
    public static final int NO_SORT_ORDER = Integer.MAX_VALUE;

    public static final Codec<TardisConsole> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                    Codec.STRING.optionalFieldOf("id", "").forGetter(TardisConsole::id),
                    ControlEntry.CODEC
                            .listOf()
                            .optionalFieldOf("controls", List.of())
                            .forGetter(TardisConsole::controls),
                    Codec.STRING.optionalFieldOf("default_variant", "").forGetter(TardisConsole::defaultVariant),
                    GlowGroup.CODEC
                            .listOf()
                            .optionalFieldOf("glow_groups", List.of())
                            .forGetter(TardisConsole::glowGroups),
                    Codec.INT
                            .optionalFieldOf("sort_order", TardisConsole.NO_SORT_ORDER)
                            .forGetter(TardisConsole::sortOrder))
            .apply(inst, TardisConsole::new));

    public static final StreamCodec<FriendlyByteBuf, TardisConsole> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            TardisConsole::id,
            ControlEntry.STREAM_CODEC.apply(ByteBufCodecs.list()),
            TardisConsole::controls,
            ByteBufCodecs.STRING_UTF8,
            TardisConsole::defaultVariant,
            GlowGroup.STREAM_CODEC.apply(ByteBufCodecs.list()),
            TardisConsole::glowGroups,
            ByteBufCodecs.INT,
            TardisConsole::sortOrder,
            TardisConsole::new);

    public record ControlEntry(
            String id,
            Vec3 offset,
            float width,
            float height,
            float depth,
            float cooldown,
            ControlAnimationSetting animation) {
        public static final float DEFAULT_COOLDOWN_SECONDS = 1.5f;
        public static final Vec3 FROM_HITBOX = new Vec3(Double.NaN, Double.NaN, Double.NaN);

        private static final Codec<Vec3> VEC3_CODEC = Vec3Codec.CODEC;

        private record Raw(
                String id,
                Optional<List<Double>> position,
                Optional<Vec3> offset,
                Optional<List<Float>> size,
                Optional<Float> width,
                Optional<Float> height,
                Optional<Float> depth,
                float cooldown,
                Optional<ControlAnimationSetting> animation,
                Optional<Boolean> animated) {
            private static final Codec<Raw> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                            Codec.STRING.fieldOf("id").forGetter(Raw::id),
                            Codec.DOUBLE.listOf().optionalFieldOf("position").forGetter(Raw::position),
                            VEC3_CODEC.optionalFieldOf("offset").forGetter(Raw::offset),
                            Codec.FLOAT.listOf().optionalFieldOf("size").forGetter(Raw::size),
                            Codec.FLOAT.optionalFieldOf("width").forGetter(Raw::width),
                            Codec.FLOAT.optionalFieldOf("height").forGetter(Raw::height),
                            Codec.FLOAT.optionalFieldOf("depth").forGetter(Raw::depth),
                            Codec.floatRange(0.0f, Float.MAX_VALUE)
                                    .optionalFieldOf("cooldown", TardisConsole.ControlEntry.DEFAULT_COOLDOWN_SECONDS)
                                    .forGetter(Raw::cooldown),
                            ControlAnimationSetting.CODEC
                                    .optionalFieldOf("animation")
                                    .forGetter(Raw::animation),
                            Codec.BOOL.optionalFieldOf("animated").forGetter(Raw::animated))
                    .apply(inst, Raw::new));

            private DataResult<TardisConsole.ControlEntry> toEntry() {
                Vec3 pos;
                if (position.isPresent()) {
                    List<Double> p = position.get();
                    if (p.size() != 3) return DataResult.error(() -> "Control '" + id + "' position needs 3 numbers");
                    pos = new Vec3(p.get(0), p.get(1), p.get(2));
                } else pos = offset.orElse(TardisConsole.ControlEntry.FROM_HITBOX);

                float w;
                float h;
                float d;
                boolean noSize = size.isEmpty() && width.isEmpty() && height.isEmpty() && depth.isEmpty();
                if (pos == TardisConsole.ControlEntry.FROM_HITBOX && noSize) {
                    w = Float.NaN;
                    h = Float.NaN;
                    d = Float.NaN;
                } else if (size.isPresent()) {
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

                ControlAnimationSetting anim = animation.orElseGet(() -> animated.isPresent() && !animated.get()
                        ? ControlAnimationSetting.DISABLED
                        : ControlAnimationSetting.DEFAULT);
                return DataResult.success(new TardisConsole.ControlEntry(id, pos, w, h, d, cooldown, anim));
            }

            private static Raw of(TardisConsole.ControlEntry entry) {
                return new Raw(
                        entry.id(),
                        Optional.of(List.of(entry.offset().x, entry.offset().y, entry.offset().z)),
                        Optional.empty(),
                        Optional.of(List.of(entry.width(), entry.height(), entry.depth())),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        entry.cooldown(),
                        entry.animation().equals(ControlAnimationSetting.DEFAULT)
                                ? Optional.empty()
                                : Optional.of(entry.animation()),
                        Optional.empty());
            }
        }

        public static final Codec<TardisConsole.ControlEntry> CODEC = Raw.CODEC.comapFlatMap(Raw::toEntry, Raw::of);

        public static final StreamCodec<FriendlyByteBuf, TardisConsole.ControlEntry> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.STRING_UTF8,
                        TardisConsole.ControlEntry::id,
                        ByteBufCodecs.DOUBLE,
                        e -> e.offset().x,
                        ByteBufCodecs.DOUBLE,
                        e -> e.offset().y,
                        ByteBufCodecs.DOUBLE,
                        e -> e.offset().z,
                        ByteBufCodecs.FLOAT,
                        TardisConsole.ControlEntry::width,
                        ByteBufCodecs.FLOAT,
                        TardisConsole.ControlEntry::height,
                        ByteBufCodecs.FLOAT,
                        TardisConsole.ControlEntry::depth,
                        ByteBufCodecs.FLOAT,
                        TardisConsole.ControlEntry::cooldown,
                        ControlAnimationSetting.STREAM_CODEC,
                        TardisConsole.ControlEntry::animation,
                        (id, x, y, z, width, height, depth, cooldown, animation) -> new TardisConsole.ControlEntry(
                                id, new Vec3(x, y, z), width, height, depth, cooldown, animation));
    }

    public TardisConsole(String id) {
        this(id, List.of(), "", List.of(), NO_SORT_ORDER);
    }

    public TardisConsole(String id, List<ControlEntry> controls) {
        this(id, controls, "", List.of(), NO_SORT_ORDER);
    }

    private static List<ControlEntry> resolveHitboxes(String consoleId, List<ControlEntry> entries) {
        if (entries.stream().noneMatch(e -> e.offset() == ControlEntry.FROM_HITBOX)) return entries;
        Map<String, ConsoleHitboxBones.Box> boxes = ConsoleHitboxBones.read(consoleId);
        List<ControlEntry> out = new ArrayList<>(entries.size());
        for (ControlEntry e : entries) {
            if (e.offset() != ControlEntry.FROM_HITBOX) {
                out.add(e);
                continue;
            }
            ConsoleHitboxBones.Box box = boxes.get(e.id());
            if (box == null) {
                Constants.LOG.warn(
                        "Console {} control '{}' has no position and no '{}{}' bone",
                        consoleId,
                        e.id(),
                        e.id(),
                        ConsoleHitboxBones.SUFFIX);
                continue;
            }
            boolean sized = !Float.isNaN(e.width());
            out.add(new ControlEntry(
                    e.id(),
                    box.center(),
                    sized ? e.width() : box.width(),
                    sized ? e.height() : box.height(),
                    sized ? e.depth() : box.depth(),
                    e.cooldown(),
                    e.animation()));
        }
        return List.copyOf(out);
    }

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
        return new TardisConsole(id, resolveHitboxes(id, controls), defaultVariant, glowGroups, sortOrder);
    }
}
