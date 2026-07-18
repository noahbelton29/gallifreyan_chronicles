package com.noahtnt2009.gallifreyan_chronicles.tardis.console;

import com.noahtnt2009.gallifreyan_chronicles.data.GCJsonDataRegistry;
import com.noahtnt2009.gallifreyan_chronicles.util.GCUtils;

import java.util.Collection;

public class TardisConsoleRegistry {
    public static final String DEFAULT_ID = GCUtils.ofNamespace("brachacki_console");

    private static final GCJsonDataRegistry<TardisConsole> REGISTRY = new GCJsonDataRegistry<>();
    private static TardisConsole defaultConsole;

    private TardisConsoleRegistry() {
    }

    public static void clear() {
        REGISTRY.clear();
        defaultConsole = null;
    }

    public static TardisConsole register(TardisConsole console) {
        return REGISTRY.register(console);
    }

    public static void setDefault(TardisConsole console) {
        defaultConsole = console;
    }

    public static TardisConsole get(String id) {
        TardisConsole result = REGISTRY.getById(id);
        if (result != null) return result;
        if (defaultConsole != null) return defaultConsole;
        return REGISTRY.getAll().iterator().next();
    }

    public static boolean contains(String id) {
        return REGISTRY.contains(id);
    }
    public static Collection<TardisConsole> getAll() {
        return REGISTRY.getAll();
    }
    public static TardisConsole getDefault() {
        return defaultConsole;
    }
    public static int size() {
        return REGISTRY.size();
    }
}
