package uk.nbforge.gallifreyan_chronicles.tardis.mind.action.builtin;

import uk.nbforge.gallifreyan_chronicles.Constants;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.action.MindAction;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.action.MindContext;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

/* {"type": "sound", "sound": "minecraft:block.anvil.land", "volume": 1, "pitch": 1} at the tardis */
public record SoundAction(SoundEvent sound, float volume, float pitch) implements MindAction {
    public static final String TYPE = Constants.MOD_ID + ":sound";

    @Override
    public String type() {
        return TYPE;
    }

    @Override
    public void run(MindContext context) {
        context.here((level, pos) -> level.playSound(null, BlockPos.containing(pos), sound, SoundSource.BLOCKS, volume, pitch));
    }
}
