package com.noahtnt2009.gallifreyan_chronicles.tardis.console;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.noahtnt2009.gallifreyan_chronicles.data.IdentifiableData;
import com.noahtnt2009.gallifreyan_chronicles.tardis.exterior.TardisExterior;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

public record TardisConsole(String id, Identifier model, Identifier texture, Identifier animation)
        implements IdentifiableData<TardisConsole> {

    public static final Codec<TardisConsole> CODEC = RecordCodecBuilder.create(tardisConsoleInstance
            -> tardisConsoleInstance.group(
                    Identifier.CODEC.fieldOf("model").forGetter(TardisConsole::model),
                    Identifier.CODEC.fieldOf("texture").forGetter(TardisConsole::texture),
                    Identifier.CODEC.fieldOf("animation").forGetter(TardisConsole::animation)
    ).apply(tardisConsoleInstance, (model, texture, animation) ->
            new TardisConsole("__unset__", model, texture, animation)
    ));

    public static final StreamCodec<FriendlyByteBuf, TardisConsole> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, TardisConsole::id,
            Identifier.STREAM_CODEC, TardisConsole::model,
            Identifier.STREAM_CODEC, TardisConsole::texture,
            Identifier.STREAM_CODEC, TardisConsole::animation,
            TardisConsole::new
    );

    @Override
    public TardisConsole withId(String id) {
        return new TardisConsole(id, model, texture, animation);
    }
}
