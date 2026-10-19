package com.noahtnt2009.gallifreyan_chronicles.tardis.console;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record GlowGroup(String name, String prefix, int frames, int holdTicks, int fadeTicks) {
    public static final int DEFAULT_HOLD_TICKS = 30;
    public static final int DEFAULT_FADE_TICKS = 8;
    private static final float FADE_POWER = 2.5f;

    public static final Codec<GlowGroup> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.STRING.fieldOf("name").forGetter(GlowGroup::name),
            Codec.STRING.optionalFieldOf("prefix").forGetter(group -> java.util.Optional.of(group.prefix)),
            Codec.intRange(1, 256).optionalFieldOf("frames", 1).forGetter(GlowGroup::frames),
            Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("hold_ticks", DEFAULT_HOLD_TICKS).forGetter(GlowGroup::holdTicks),
            Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("fade_ticks", DEFAULT_FADE_TICKS).forGetter(GlowGroup::fadeTicks)
    ).apply(inst, (name, prefix, frames, hold, fade) ->
            new GlowGroup(name, prefix.orElse(defaultPrefix(name)), frames, hold, fade)));

    public static final StreamCodec<FriendlyByteBuf, GlowGroup> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, GlowGroup::name,
            ByteBufCodecs.STRING_UTF8, GlowGroup::prefix,
            ByteBufCodecs.VAR_INT, GlowGroup::frames,
            ByteBufCodecs.VAR_INT, GlowGroup::holdTicks,
            ByteBufCodecs.VAR_INT, GlowGroup::fadeTicks,
            GlowGroup::new
    );

    public static String defaultPrefix(String name) {
        return "glowmask_" + name + "_";
    }

    public float weight(float time, int frame) {
        if (frames <= 1) return 1f;

        float cycleTicks = holdTicks + fadeTicks;
        int active = (int) Math.floor(time / cycleTicks) % frames;
        float local = time % cycleTicks;

        if (local < holdTicks) {
            return frame == active ? 1f : 0f;
        }

        float fade = Math.clamp((local - holdTicks) / fadeTicks, 0f, 1f);

        if (frame == active) return (float) Math.pow(1f - fade, FADE_POWER);
        if (frame == (active + 1) % frames) return (float) Math.pow(fade, FADE_POWER);
        return 0f;
    }
}
