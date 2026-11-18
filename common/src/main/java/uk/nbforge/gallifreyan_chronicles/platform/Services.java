package uk.nbforge.gallifreyan_chronicles.platform;

import java.util.ServiceLoader;
import uk.nbforge.gallifreyan_chronicles.platform.services.INetworkHelper;
import uk.nbforge.gallifreyan_chronicles.platform.services.IPlatformHelper;
import uk.nbforge.gallifreyan_chronicles.util.GCLog;

public class Services {
    public static final IPlatformHelper PLATFORM = load(IPlatformHelper.class);
    public static final INetworkHelper NETWORK = load(INetworkHelper.class);

    public static <T> T load(Class<T> clazz) {
        final T loadedService = ServiceLoader.load(clazz, Services.class.getClassLoader())
                .findFirst()
                .orElseThrow(() -> new NullPointerException("Failed to load service for " + clazz.getName()));
        GCLog.CORE.debug("Loaded {} for service {}", loadedService, clazz);
        return loadedService;
    }
}
