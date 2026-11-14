package com.noahtnt2009.gallifreyan_chronicles.init;

import com.noahtnt2009.gallifreyan_chronicles.effect.ChokingEffect;
import com.noahtnt2009.gallifreyan_chronicles.util.GCUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;

public final class GCEffects {
    public static final Holder<MobEffect> CHOKING = register("choking", new ChokingEffect());

    private GCEffects() {
    }

    private static Holder<MobEffect> register(String name, MobEffect effect) {
        return Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, GCUtils.modIdentifier(name), effect);
    }

    public static void registerEffects() {
    }
}
