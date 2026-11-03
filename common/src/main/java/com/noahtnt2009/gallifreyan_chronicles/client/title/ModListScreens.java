package com.noahtnt2009.gallifreyan_chronicles.client.title;

import net.minecraft.client.gui.screens.Screen;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Constructor;

public final class ModListScreens {
    private static final String[] CANDIDATES = {
            "com.terraformersmc.modmenu.gui.ModsScreen",
            "net.neoforged.neoforge.client.gui.ModListScreen"
    };

    private static boolean resolved;
    private static @Nullable Constructor<?> constructor;

    private ModListScreens() {
    }

    private static void resolve() {
        if (resolved) return;
        resolved = true;
        for (String name : CANDIDATES) {
            try {
                constructor = Class.forName(name).getConstructor(Screen.class);
                return;
            } catch (ReflectiveOperationException | LinkageError ignored) {
            }
        }
    }

    public static boolean available() {
        resolve();
        return constructor != null;
    }

    public static @Nullable Screen create(Screen parent) {
        resolve();
        if (constructor == null) return null;
        try {
            return (Screen) constructor.newInstance(parent);
        } catch (ReflectiveOperationException | RuntimeException e) {
            return null;
        }
    }
}
