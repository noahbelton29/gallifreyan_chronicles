package uk.nbforge.gallifreyan_chronicles.tardis.codec.door;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import uk.nbforge.gallifreyan_chronicles.tardis.door.TardisDoorVariant;

public final class TardisDoorVariantCodec {
    public static final Codec<TardisDoorVariant> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.STRING.optionalFieldOf("id", "").forGetter(TardisDoorVariant::id),
            Codec.STRING.fieldOf("parent").forGetter(TardisDoorVariant::parent),
            Codec.STRING.fieldOf("texture").forGetter(TardisDoorVariant::texture),
            Codec.BOOL.optionalFieldOf("default", false).forGetter(TardisDoorVariant::isDefault),
            Codec.BOOL.optionalFieldOf("interior", false).forGetter(TardisDoorVariant::interior),
            Codec.STRING.optionalFieldOf("exterior", "").forGetter(TardisDoorVariant::exterior),
            Codec.STRING.optionalFieldOf("open_sound", "").forGetter(TardisDoorVariant::openSound),
            Codec.STRING.optionalFieldOf("close_sound", "").forGetter(TardisDoorVariant::closeSound),
            Codec.BOOL.optionalFieldOf("open_all_only", false).forGetter(TardisDoorVariant::openAllOnly),
            Codec.INT.optionalFieldOf("priority", TardisDoorVariant.NO_PRIORITY).forGetter(TardisDoorVariant::priority),
            Codec.STRING.optionalFieldOf("block", "").forGetter(TardisDoorVariant::block),
            Codec.STRING.optionalFieldOf("particle", "").forGetter(TardisDoorVariant::particle),
            Codec.FLOAT.optionalFieldOf("cooldown_seconds", TardisDoorVariant.DEFAULT_COOLDOWN_SECONDS).forGetter(TardisDoorVariant::cooldownSeconds)
    ).apply(inst, TardisDoorVariant::new));

    public static final StreamCodec<FriendlyByteBuf, TardisDoorVariant> STREAM_CODEC = StreamCodec.of(
            (buf, v) -> {
                ByteBufCodecs.STRING_UTF8.encode(buf, v.id());
                ByteBufCodecs.STRING_UTF8.encode(buf, v.parent());
                ByteBufCodecs.STRING_UTF8.encode(buf, v.texture());
                ByteBufCodecs.BOOL.encode(buf, v.isDefault());
                ByteBufCodecs.BOOL.encode(buf, v.interior());
                ByteBufCodecs.STRING_UTF8.encode(buf, v.exterior());
                ByteBufCodecs.STRING_UTF8.encode(buf, v.openSound());
                ByteBufCodecs.STRING_UTF8.encode(buf, v.closeSound());
                ByteBufCodecs.BOOL.encode(buf, v.openAllOnly());
                ByteBufCodecs.INT.encode(buf, v.priority());
                ByteBufCodecs.STRING_UTF8.encode(buf, v.block());
                ByteBufCodecs.STRING_UTF8.encode(buf, v.particle());
                ByteBufCodecs.FLOAT.encode(buf, v.cooldownSeconds());
            },
            buf -> new TardisDoorVariant(
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ByteBufCodecs.BOOL.decode(buf),
                    ByteBufCodecs.BOOL.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ByteBufCodecs.BOOL.decode(buf),
                    ByteBufCodecs.INT.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ByteBufCodecs.FLOAT.decode(buf)
            )
    );

    private TardisDoorVariantCodec() {
    }
}
