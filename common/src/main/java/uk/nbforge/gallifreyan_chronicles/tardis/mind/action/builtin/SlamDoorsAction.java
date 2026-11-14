package uk.nbforge.gallifreyan_chronicles.tardis.mind.action.builtin;

import uk.nbforge.gallifreyan_chronicles.Constants;
import uk.nbforge.gallifreyan_chronicles.core.GCSounds;
import uk.nbforge.gallifreyan_chronicles.network.tardis.TardisDrumShakePayload;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.action.MindAction;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.action.MindContext;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;

/* {"type": "slam_doors", "volume": 1.5, "shake": 0.6, "lock": true}
   it slams its doors shut with a bang and the room shakes. lock: locks them too */
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
            level.playSound(null, BlockPos.containing(pos), GCSounds.TARDIS_DOOR_CLOSE, SoundSource.BLOCKS, volume, 0.6F);
            TardisDrumShakePayload.broadcast(level, pos, shake);
        });
    }
}
