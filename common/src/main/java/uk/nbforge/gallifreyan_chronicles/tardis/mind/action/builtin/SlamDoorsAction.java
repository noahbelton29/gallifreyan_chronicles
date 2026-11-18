package uk.nbforge.gallifreyan_chronicles.tardis.mind.action.builtin;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import uk.nbforge.gallifreyan_chronicles.Constants;
import uk.nbforge.gallifreyan_chronicles.core.GCSounds;
import uk.nbforge.gallifreyan_chronicles.network.tardis.TardisDrumShakePayload;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.action.MindAction;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.action.MindContext;

public record SlamDoorsAction(float volume, float shake, boolean lock) implements MindAction {
    public static final String TYPE = Constants.MOD_ID + ":slam_doors";

    @Override
    public String type() {
        return TYPE;
    }

    @Override
    public void run(MindContext context) {
        if (lock) LockDoorsAction.closeAndLock(context.tardis);
        else context.tardis.closeDoors();
        context.everywhere((level, pos) -> {
            level.playSound(
                    null, BlockPos.containing(pos), GCSounds.TARDIS_DOOR_CLOSE, SoundSource.BLOCKS, volume, 0.6f);
            TardisDrumShakePayload.broadcast(level, pos, shake);
        });
    }
}
