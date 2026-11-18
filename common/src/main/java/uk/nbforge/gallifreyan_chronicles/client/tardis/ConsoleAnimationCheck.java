package uk.nbforge.gallifreyan_chronicles.client.tardis;

import com.geckolib.cache.GeckoLibResources;
import com.geckolib.cache.animation.BakedAnimations;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import uk.nbforge.gallifreyan_chronicles.client.model.GeoAssets;
import uk.nbforge.gallifreyan_chronicles.util.GCLog;

public final class ConsoleAnimationCheck {
    private static final Map<String, Boolean> RESULTS = new ConcurrentHashMap<>();
    private static final Set<String> WARNED = ConcurrentHashMap.newKeySet();

    private ConsoleAnimationCheck() {}

    public static boolean missing(String consoleId, String controlId, String animation) {
        String key = consoleId + "|" + animation;
        Boolean cached = RESULTS.get(key);
        if (cached != null) {
            return !cached;
        }

        BakedAnimations animations =
                GeckoLibResources.getBakedAnimations().cache().get(GeoAssets.CONSOLE.animation(consoleId));
        if (animations == null) {
            return false;
        }

        boolean found = animations.animations().containsKey(animation);
        RESULTS.put(key, found);
        if (!found && WARNED.add(key)) {
            GCLog.CLIENT.warn(
                    "Console '{}' has no animation '{}' for control '{}'; set \"animated\": false on that control or add the animation",
                    consoleId,
                    animation,
                    controlId);
        }
        return !found;
    }

    public static void clear() {
        RESULTS.clear();
        WARNED.clear();
    }
}
