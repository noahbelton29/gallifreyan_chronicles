package com.noahtnt2009.gallifreyan_chronicles.tardis.door;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.noahtnt2009.gallifreyan_chronicles.data.IdentifiableData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record TardisDoorVariant(String id, String parent, String texture, boolean isDefault, boolean interior,
                                String exterior,
                                String openSound, String closeSound, boolean openAllOnly, int priority,
                                String block, String particle)
        implements IdentifiableData<TardisDoorVariant> {

    public static final int NO_PRIORITY = Integer.MAX_VALUE;

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
            Codec.INT.optionalFieldOf("priority", NO_PRIORITY).forGetter(TardisDoorVariant::priority),
            Codec.STRING.optionalFieldOf("block", "").forGetter(TardisDoorVariant::block),
            Codec.STRING.optionalFieldOf("particle", "").forGetter(TardisDoorVariant::particle)
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
                    ByteBufCodecs.STRING_UTF8.decode(buf)
            )
    );

    public String localId() {
        int colon = id.indexOf(':');
        return colon < 0 ? id : id.substring(colon + 1);
    }

    @Override
    public TardisDoorVariant withId(String id) {
        return new TardisDoorVariant(id, parent, texture, isDefault, interior, exterior, openSound, closeSound, openAllOnly, priority, block, particle);
    }
}
