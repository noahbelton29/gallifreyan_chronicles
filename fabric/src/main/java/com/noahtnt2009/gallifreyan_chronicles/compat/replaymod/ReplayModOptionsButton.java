package com.noahtnt2009.gallifreyan_chronicles.compat.replaymod;

import com.noahtnt2009.gallifreyan_chronicles.client.gui.title.ReplayViewerHook;
import com.replaymod.replay.ReplayModReplay;
import com.replaymod.replay.gui.screen.GuiReplayViewer;

public final class ReplayModOptionsButton {
    private ReplayModOptionsButton() {
    }

    public static void register() {
        ReplayViewerHook.opener = () -> new GuiReplayViewer(ReplayModReplay.instance).display();
    }
}
