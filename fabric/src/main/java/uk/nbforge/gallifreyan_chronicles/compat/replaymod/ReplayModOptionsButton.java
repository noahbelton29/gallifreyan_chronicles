package uk.nbforge.gallifreyan_chronicles.compat.replaymod;

import com.replaymod.replay.ReplayModReplay;
import com.replaymod.replay.gui.screen.GuiReplayViewer;
import uk.nbforge.gallifreyan_chronicles.client.gui.title.ReplayViewerHook;

public final class ReplayModOptionsButton {
    private ReplayModOptionsButton() {}

    public static void register() {
        ReplayViewerHook.opener = () -> new GuiReplayViewer(ReplayModReplay.instance).display();
    }
}
