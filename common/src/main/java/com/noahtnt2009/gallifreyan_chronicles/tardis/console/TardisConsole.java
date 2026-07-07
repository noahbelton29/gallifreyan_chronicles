package com.noahtnt2009.gallifreyan_chronicles.tardis.console;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.noahtnt2009.gallifreyan_chronicles.data.IdentifiableData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record TardisConsole(String id) implements IdentifiableData<TardisConsole> {
    public static final Codec<TardisConsole> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.STRING.fieldOf("id").forGetter(TardisConsole::id)
    ).apply(inst, TardisConsole::new));

    public static final StreamCodec<FriendlyByteBuf, TardisConsole> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, TardisConsole::id,
            TardisConsole::new
    );

    @Override
    public TardisConsole withId(String id) {
        return new TardisConsole(id);
    }
}