package uk.nbforge.gallifreyan_chronicles.core;

import uk.nbforge.gallifreyan_chronicles.core.effect.ChokingEffect;
import uk.nbforge.gallifreyan_chronicles.util.GCUtil;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;

public final class GCEffects {
    public static final Holder<MobEffect> CHOKING = register("choking", new ChokingEffect());

    private GCEffects() {
    }

    private static Holder<MobEffect> register(String name, MobEffect effect) {
        return Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, GCUtil.modIdentifier(name), effect);
    }

    public static void registerEffects() {
    }
}
