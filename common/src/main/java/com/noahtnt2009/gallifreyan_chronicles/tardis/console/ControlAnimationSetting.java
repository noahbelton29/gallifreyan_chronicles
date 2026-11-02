package com.noahtnt2009.gallifreyan_chronicles.tardis.console;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public record ControlAnimationSetting(boolean enabled, Optional<String> active, Optional<String> inactive,
                                      Optional<Boolean> snapWhenInactive) {
    public static final ControlAnimationSetting DEFAULT =
            new ControlAnimationSetting(true, Optional.empty(), Optional.empty(), Optional.empty());
    public static final ControlAnimationSetting DISABLED =
            new ControlAnimationSetting(false, Optional.empty(), Optional.empty(), Optional.empty());

    private static final Codec<ControlAnimationSetting> OBJECT_CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.STRING.optionalFieldOf("active").forGetter(ControlAnimationSetting::active),
            Codec.STRING.optionalFieldOf("inactive").forGetter(ControlAnimationSetting::inactive),
            Codec.BOOL.optionalFieldOf("snap_when_inactive").forGetter(ControlAnimationSetting::snapWhenInactive)
    ).apply(inst, (active, inactive, snap) -> new ControlAnimationSetting(true, active, inactive, snap)));

    public static final Codec<ControlAnimationSetting> CODEC = Codec.either(Codec.BOOL, OBJECT_CODEC).xmap(
            either -> either.map(enabled -> enabled ? DEFAULT : DISABLED, setting -> setting),
            setting -> {
                if (!setting.enabled()) return Either.left(false);
                return setting.equals(DEFAULT) ? Either.left(true) : Either.right(setting);
            }
    );

    public static final StreamCodec<ByteBuf, ControlAnimationSetting> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, ControlAnimationSetting::enabled,
            ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8), ControlAnimationSetting::active,
            ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8), ControlAnimationSetting::inactive,
            ByteBufCodecs.optional(ByteBufCodecs.BOOL), ControlAnimationSetting::snapWhenInactive,
            ControlAnimationSetting::new
    );

    public @Nullable String animationFor(boolean activated, @Nullable String fallback) {
        if (!enabled) return null;
        return (activated ? active : inactive).orElse(fallback);
    }

    public boolean snapsWhenInactive(boolean fallback) {
        return snapWhenInactive.orElse(fallback);
    }
}
