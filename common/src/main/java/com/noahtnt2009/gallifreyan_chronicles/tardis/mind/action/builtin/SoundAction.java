package com.noahtnt2009.gallifreyan_chronicles.tardis.mind.action.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.action.MindAction;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.action.MindContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

/* {"type": "sound", "sound": "minecraft:block.anvil.land", "volume": 1, "pitch": 1} at the tardis */
public record SoundAction(SoundEvent sound, float volume, float pitch) implements MindAction {
    public static final String TYPE = Constants.MOD_ID + ":sound";
    public static final MapCodec<SoundAction> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            BuiltInRegistries.SOUND_EVENT.byNameCodec().fieldOf("sound").forGetter(SoundAction::sound),
            Codec.FLOAT.optionalFieldOf("volume", 1f).forGetter(SoundAction::volume),
            Codec.FLOAT.optionalFieldOf("pitch", 1f).forGetter(SoundAction::pitch)
    ).apply(inst, SoundAction::new));

    @Override
    public String type() {
        return TYPE;
    }

    @Override
    public void run(MindContext context) {
        context.here((level, pos) -> level.playSound(null, BlockPos.containing(pos), sound, SoundSource.BLOCKS, volume, pitch));
    }
}
