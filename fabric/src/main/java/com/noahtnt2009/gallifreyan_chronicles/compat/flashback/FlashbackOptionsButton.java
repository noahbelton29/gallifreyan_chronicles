package com.noahtnt2009.gallifreyan_chronicles.compat.flashback;

import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.client.gui.title.ReplayViewerHook;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;

import java.lang.reflect.Constructor;
import java.nio.file.Path;

/* moves flashback's replay button from the title screen into our options menu */
public final class FlashbackOptionsButton {
    private static final String PACKAGE = "com.moulberry.flashback";
    private static final String SELECT_SCREEN = PACKAGE + ".screen.select_replay.SelectReplayScreen";

    /* the screen that is open, so flashback's screen can return to it */
    private static volatile Screen lastScreen;

    private FlashbackOptionsButton() {
    }

    public static void register() {
        /* replaymod's hook wins if both are installed */
        if (ReplayViewerHook.opener == null) {
            ReplayViewerHook.opener = FlashbackOptionsButton::open;
        }
        FlashbackChunks.registerClient();
        ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
            lastScreen = screen;
            if (screen instanceof TitleScreen) hideTitleButton(screen);
        });
    }

    /* hide any widget that is a flashback class */
    private static void hideTitleButton(Screen screen) {
        for (Object child : screen.children()) {
            if (child instanceof AbstractWidget widget && child.getClass().getName().startsWith(PACKAGE)) {
                widget.visible = false;
                widget.active = false;
            }
        }
    }

    /* opens flashback's replay list. reflection, so a renamed class only logs a warning */
    private static void open() {
        Minecraft minecraft = Minecraft.getInstance();
        Screen current = lastScreen;
        try {
            Class<?> type = Class.forName(SELECT_SCREEN);
            for (Constructor<?> constructor : type.getConstructors()) {
                Class<?>[] params = constructor.getParameterTypes();
                if (params.length == 1 && params[0] == Screen.class) {
                    minecraft.gui.setScreen((Screen) constructor.newInstance(current));
                    return;
                }
                if (params.length == 2 && params[0] == Screen.class && params[1] == Path.class) {
                    Path folder = (Path) Class.forName(PACKAGE + ".Flashback").getMethod("getReplayFolder").invoke(null);
                    minecraft.gui.setScreen((Screen) constructor.newInstance(current, folder));
                    return;
                }
            }
            Constants.LOG.warn("Flashback: no constructor of {} that I know how to call", SELECT_SCREEN);
        } catch (ReflectiveOperationException | RuntimeException e) {
            Constants.LOG.warn("Flashback: could not open the replay list", e);
        }
    }
}
