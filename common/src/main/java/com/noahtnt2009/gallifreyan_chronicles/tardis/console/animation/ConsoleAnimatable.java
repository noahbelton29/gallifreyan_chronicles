package com.noahtnt2009.gallifreyan_chronicles.tardis.console.animation;

import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

public interface ConsoleAnimatable {
    void triggerAnimation(String controllerName, String animationName);

    default void triggerAnimation(String controllerName, String animationName, @Nullable Player triggeringPlayer) {
        triggerAnimation(controllerName, animationName);
    }

    void applyAnimationLocally(String controllerName, String animationName);
}
