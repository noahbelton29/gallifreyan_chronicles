package uk.nbforge.gallifreyan_chronicles.tardis.codec.console;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import uk.nbforge.gallifreyan_chronicles.tardis.console.ControlAnimationSetting;

public final class ControlAnimationSettingCodec {
    private static final Codec<ControlAnimationSetting> OBJECT_CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.STRING.optionalFieldOf("active").forGetter(ControlAnimationSetting::active),
            Codec.STRING.optionalFieldOf("inactive").forGetter(ControlAnimationSetting::inactive),
            Codec.BOOL.optionalFieldOf("snap_when_inactive").forGetter(ControlAnimationSetting::snapWhenInactive)
    ).apply(inst, (active, inactive, snap) -> new ControlAnimationSetting(true, active, inactive, snap)));

    public static final Codec<ControlAnimationSetting> CODEC = Codec.either(Codec.BOOL, OBJECT_CODEC).xmap(
            either -> either.map(enabled -> enabled ? ControlAnimationSetting.DEFAULT : ControlAnimationSetting.DISABLED, setting -> setting),
            setting -> {
                if (!setting.enabled()) return Either.left(false);
                return setting.equals(ControlAnimationSetting.DEFAULT) ? Either.left(true) : Either.right(setting);
            }
    );

    public static final StreamCodec<ByteBuf, ControlAnimationSetting> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, ControlAnimationSetting::enabled,
            ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8), ControlAnimationSetting::active,
            ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8), ControlAnimationSetting::inactive,
            ByteBufCodecs.optional(ByteBufCodecs.BOOL), ControlAnimationSetting::snapWhenInactive,
            ControlAnimationSetting::new
    );

    private ControlAnimationSettingCodec() {
    }
}
