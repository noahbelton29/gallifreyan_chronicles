package uk.nbforge.gallifreyan_chronicles.addon;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import uk.nbforge.gallifreyan_chronicles.Constants;
import uk.nbforge.gallifreyan_chronicles.addon.api.AddonApi;
import uk.nbforge.gallifreyan_chronicles.addon.api.GCAddon;
import uk.nbforge.gallifreyan_chronicles.platform.Services;

final class ModAddonLoader {
    private static final Map<String, GCAddon> LOADED = new LinkedHashMap<>();
    private static final Map<String, String> FAILURES = new HashMap<>();

    private ModAddonLoader() {}

    static synchronized void load() {
        for (ModAddonEntry entry : Services.PLATFORM.findModAddons()) {
            AddonInfo info = AddonManager.infoFor(entry.modId());
            AddonProblem blocking = info == null ? null : AddonManager.blockingProblem(info);
            if (blocking != null) {
                Constants.LOG.warn("Not starting addon '{}': {}", entry.modId(), blocking.kind());
                FAILURES.put(entry.modId(), blocking.kind() + " " + blocking.detail());
                continue;
            }
            try {
                Class<?> type = Class.forName(entry.className(), true, ModAddonLoader.class.getClassLoader());
                if (!GCAddon.class.isAssignableFrom(type)) {
                    throw new IllegalStateException(entry.className() + " does not implement GCAddon");
                }
                GCAddon addon = (GCAddon) type.getDeclaredConstructor().newInstance();
                addon.onInitialize(new ModAddonContext(entry.modId()));
                LOADED.put(entry.modId(), addon);
                Constants.LOG.info("Started addon '{}' (addon API {})", entry.modId(), AddonApi.VERSION);
            } catch (Throwable t) {
                if (t instanceof VirtualMachineError error) throw error;
                Throwable cause = t instanceof java.lang.reflect.InvocationTargetException && t.getCause() != null
                        ? t.getCause()
                        : t;
                Constants.LOG.error("Addon '{}' failed to start", entry.modId(), cause);
                FAILURES.put(entry.modId(), cause.getClass().getSimpleName() + ": " + cause.getMessage());
            }
        }
        Runtime.getRuntime().addShutdownHook(new Thread(ModAddonLoader::shutdown, "GC addon shutdown"));
    }

    static synchronized boolean isRunning(String id) {
        return LOADED.containsKey(id);
    }

    static synchronized boolean wasFound(String id) {
        return LOADED.containsKey(id) || FAILURES.containsKey(id);
    }

    static synchronized String failure(String id) {
        return FAILURES.get(id);
    }

    private static synchronized void shutdown() {
        for (Map.Entry<String, GCAddon> entry : LOADED.entrySet()) {
            try {
                entry.getValue().onShutdown();
            } catch (Throwable t) {
                Constants.LOG.warn("Addon '{}' failed during shutdown", entry.getKey(), t);
            }
        }
    }
}
