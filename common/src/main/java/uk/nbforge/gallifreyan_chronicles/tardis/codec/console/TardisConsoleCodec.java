package uk.nbforge.gallifreyan_chronicles.tardis.codec.console;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import uk.nbforge.gallifreyan_chronicles.tardis.console.GlowGroup;
import uk.nbforge.gallifreyan_chronicles.tardis.console.TardisConsole;

public final class TardisConsoleCodec {
    public static final Codec<TardisConsole> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.STRING.optionalFieldOf("id", "").forGetter(TardisConsole::id),
            ControlEntryCodec.CODEC.listOf().optionalFieldOf("controls", List.of()).forGetter(TardisConsole::controls),
            Codec.STRING.optionalFieldOf("default_variant", "").forGetter(TardisConsole::defaultVariant),
            GlowGroupCodec.CODEC.listOf().optionalFieldOf("glow_groups", List.of()).forGetter(TardisConsole::glowGroups)
    ).apply(inst, TardisConsole::new));

    public static final StreamCodec<FriendlyByteBuf, TardisConsole> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, TardisConsole::id,
            ControlEntryCodec.STREAM_CODEC.apply(ByteBufCodecs.list()), TardisConsole::controls,
            ByteBufCodecs.STRING_UTF8, TardisConsole::defaultVariant,
            GlowGroupCodec.STREAM_CODEC.apply(ByteBufCodecs.list()), TardisConsole::glowGroups,
            TardisConsole::new
    );

    private TardisConsoleCodec() {
    }
}
