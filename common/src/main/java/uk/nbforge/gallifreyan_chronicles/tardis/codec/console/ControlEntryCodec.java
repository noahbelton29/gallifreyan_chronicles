package uk.nbforge.gallifreyan_chronicles.tardis.codec.console;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;
import uk.nbforge.gallifreyan_chronicles.codec.Vec3Codec;
import uk.nbforge.gallifreyan_chronicles.tardis.codec.console.ControlAnimationSettingCodec;
import uk.nbforge.gallifreyan_chronicles.core.entity.TardisControlEntity;
import uk.nbforge.gallifreyan_chronicles.tardis.console.ControlAnimationSetting;
import uk.nbforge.gallifreyan_chronicles.tardis.console.TardisConsole;

public final class ControlEntryCodec {
    private static final Codec<Vec3> VEC3_CODEC = Vec3Codec.CODEC;

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
                Codec.floatRange(0.0F, Float.MAX_VALUE).optionalFieldOf("cooldown", TardisConsole.ControlEntry.DEFAULT_COOLDOWN_SECONDS).forGetter(Raw::cooldown),
                ControlAnimationSettingCodec.CODEC.optionalFieldOf("animation").forGetter(Raw::animation),
                Codec.BOOL.optionalFieldOf("animated").forGetter(Raw::animated)
        ).apply(inst, Raw::new));

        private DataResult<TardisConsole.ControlEntry> toEntry() {
            Vec3 pos;
            if (position.isPresent()) {
                List<Double> p = position.get();
                if (p.size() != 3) return DataResult.error(() -> "Control '" + id + "' position needs 3 numbers");
                pos = new Vec3(p.get(0), p.get(1), p.get(2));
            } else if (offset.isPresent()) {
                pos = offset.get();
            } else {
                /* no position: it comes from the model's <id>_hitbox bone once the console id is known */
                pos = TardisConsole.ControlEntry.FROM_HITBOX;
            }

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

            ControlAnimationSetting anim = animation.orElseGet(() ->
                    animated.isPresent() && !animated.get()
                            ? ControlAnimationSetting.DISABLED
                            : ControlAnimationSetting.DEFAULT);
            return DataResult.success(new TardisConsole.ControlEntry(id, pos, w, h, d, cooldown, anim));
        }

        private static Raw of(TardisConsole.ControlEntry entry) {
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

    public static final Codec<TardisConsole.ControlEntry> CODEC = Raw.CODEC.comapFlatMap(Raw::toEntry, Raw::of);

    public static final StreamCodec<FriendlyByteBuf, TardisConsole.ControlEntry> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, TardisConsole.ControlEntry::id,
            ByteBufCodecs.DOUBLE, e -> e.offset().x,
            ByteBufCodecs.DOUBLE, e -> e.offset().y,
            ByteBufCodecs.DOUBLE, e -> e.offset().z,
            ByteBufCodecs.FLOAT, TardisConsole.ControlEntry::width,
            ByteBufCodecs.FLOAT, TardisConsole.ControlEntry::height,
            ByteBufCodecs.FLOAT, TardisConsole.ControlEntry::depth,
            ByteBufCodecs.FLOAT, TardisConsole.ControlEntry::cooldown,
            ControlAnimationSettingCodec.STREAM_CODEC, TardisConsole.ControlEntry::animation,
            (id, x, y, z, width, height, depth, cooldown, animation) ->
                    new TardisConsole.ControlEntry(id, new Vec3(x, y, z), width, height, depth, cooldown, animation)
    );

    private ControlEntryCodec() {
    }
}
