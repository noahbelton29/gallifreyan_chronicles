package uk.nbforge.gallifreyan_chronicles.tardis.codec.console;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import uk.nbforge.gallifreyan_chronicles.tardis.console.GlowGroup;

public final class GlowGroupCodec {
    public static final Codec<GlowGroup> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.STRING.fieldOf("name").forGetter(GlowGroup::name),
            Codec.STRING.optionalFieldOf("prefix").forGetter(group -> Optional.of(group.prefix())),
            Codec.intRange(1, 256).optionalFieldOf("frames", 1).forGetter(GlowGroup::frames),
            Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("hold_ticks", GlowGroup.DEFAULT_HOLD_TICKS).forGetter(GlowGroup::holdTicks),
            Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("fade_ticks", GlowGroup.DEFAULT_FADE_TICKS).forGetter(GlowGroup::fadeTicks)
    ).apply(inst, (name, prefix, frames, hold, fade) ->
            new GlowGroup(name, prefix.orElse(GlowGroup.defaultPrefix(name)), frames, hold, fade)));

    public static final StreamCodec<FriendlyByteBuf, GlowGroup> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, GlowGroup::name,
            ByteBufCodecs.STRING_UTF8, GlowGroup::prefix,
            ByteBufCodecs.VAR_INT, GlowGroup::frames,
            ByteBufCodecs.VAR_INT, GlowGroup::holdTicks,
            ByteBufCodecs.VAR_INT, GlowGroup::fadeTicks,
            GlowGroup::new
    );

    private GlowGroupCodec() {
    }
}
