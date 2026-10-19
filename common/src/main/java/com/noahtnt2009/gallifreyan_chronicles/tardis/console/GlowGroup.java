package com.noahtnt2009.gallifreyan_chronicles.tardis.console;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * One independently animated glowmask group of a console (lights, buttons, rotor, ...),
 * declared in the console's data json under {@code glow_groups}.
 *
 * <p>For a variant texture {@code .../first_doctors_console.png} and a group with
 * prefix {@code glowmask_lights_}, frame {@code n} is loaded from
 * {@code .../first_doctors_console_glowmask_lights_n.png}.
 *
 * <p>Frames are cycled one at a time: each frame is held at full brightness for
 * {@code holdTicks}, then crossfades into the next over {@code fadeTicks}.
 * A group with a single frame is a steady (non-animated) glow.
 *
 * @param name      identifier for the group (logging / debugging, and the default prefix)
 * @param prefix    texture suffix before the frame index; defaults to {@code glowmask_<name>_}
 * @param frames    number of frame textures ({@code prefix0 .. prefix(frames-1)})
 * @param holdTicks ticks each frame stays at full brightness
 * @param fadeTicks ticks spent crossfading into the next frame
 */
public record GlowGroup(String name, String prefix, int frames, int holdTicks, int fadeTicks) {
    public static final int DEFAULT_HOLD_TICKS = 30;
    public static final int DEFAULT_FADE_TICKS = 8;

    /** Shapes the crossfade; higher = the outgoing frame drops faster / the incoming one lingers dim. */
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

    /**
     * Brightness (0..1) of the given frame at the given time.
     *
     * @param time  time in ticks, including partial tick (usually game time + partialTick)
     * @param frame frame index in {@code [0, frames)}
     */
    public float weight(float time, int frame) {
        if (frames <= 1) return 1f;

        float cycleTicks = holdTicks + fadeTicks;
        int active = (int) Math.floor(time / cycleTicks) % frames;
        float local = time % cycleTicks;

        // Holding the active frame at full brightness.
        if (local < holdTicks) {
            return frame == active ? 1f : 0f;
        }

        // Crossfading: the active frame fades out while the next one fades in.
        float fade = Math.clamp((local - holdTicks) / fadeTicks, 0f, 1f);

        if (frame == active) return (float) Math.pow(1f - fade, FADE_POWER);
        if (frame == (active + 1) % frames) return (float) Math.pow(fade, FADE_POWER);
        return 0f;
    }
}
