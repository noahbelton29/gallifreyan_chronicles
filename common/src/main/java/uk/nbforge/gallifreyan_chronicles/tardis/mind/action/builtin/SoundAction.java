package uk.nbforge.gallifreyan_chronicles.tardis.mind.action.builtin;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import uk.nbforge.gallifreyan_chronicles.Constants;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.action.MindAction;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.action.MindContext;

public record SoundAction(SoundEvent sound, float volume, float pitch) implements MindAction {
    public static final String TYPE = Constants.MOD_ID + ":sound";

    @Override
    public String type() {
        return TYPE;
    }

    @Override
    public void run(MindContext context) {
        context.here((level, pos) ->
                level.playSound(null, BlockPos.containing(pos), sound, SoundSource.BLOCKS, volume, pitch));
    }
}
