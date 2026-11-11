package com.noahtnt2009.gallifreyan_chronicles.client.tardis;

import com.geckolib.cache.GeckoLibResources;
import com.geckolib.cache.animation.BakedAnimations;
import com.noahtnt2009.gallifreyan_chronicles.Constants;
import net.minecraft.resources.Identifier;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class ConsoleAnimationCheck {
    private static final Map<String, Boolean> RESULTS = new ConcurrentHashMap<>();
    private static final Set<String> WARNED = ConcurrentHashMap.newKeySet();

    private ConsoleAnimationCheck() {
    }

    public static boolean exists(String consoleId, String controlId, String animation) {
        String key = consoleId + "|" + animation;
        Boolean cached = RESULTS.get(key);
        if (cached != null) {
            return cached;
        }

        BakedAnimations animations = GeckoLibResources.getBakedAnimations().cache()
                .get(GeoAssets.CONSOLE.animation(consoleId));
        if (animations == null) {
            return true;
        }

        boolean found = animations.animations().containsKey(animation);
        RESULTS.put(key, found);
        if (!found && WARNED.add(key)) {
            Constants.LOG.warn("Console '{}' has no animation '{}' for control '{}'; set \"animated\": false on that control or add the animation",
                    consoleId, animation, controlId);
        }
        return found;
    }

    public static void clear() {
        RESULTS.clear();
        WARNED.clear();
    }
}
