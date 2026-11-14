package uk.nbforge.gallifreyan_chronicles.compat.replaymod;

import uk.nbforge.gallifreyan_chronicles.util.TardisDimensionUtil;
import uk.nbforge.gallifreyan_chronicles.Constants;
import com.replaymod.replay.ReplayModReplay;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public final class ReplayModSoundGate {
    private ReplayModSoundGate() {
    }

    public static boolean shouldMute(Level soundLevel) {
        if (soundLevel == null || ReplayModReplay.instance == null || ReplayModReplay.instance.getReplayHandler() == null) {
            return false;
        }
        if (!TardisDimensionUtil.isInterior(soundLevel)) {
            return false;
        }
        Entity viewer = Minecraft.getInstance().getCameraEntity();
        return viewer == null || viewer.level() != soundLevel;
    }
}
