package uk.nbforge.gallifreyan_chronicles.client.sound;

import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.world.phys.Vec3;
import uk.nbforge.gallifreyan_chronicles.client.lifecycle.ClientState;
import uk.nbforge.gallifreyan_chronicles.client.util.DetailLevelUtil;
import uk.nbforge.gallifreyan_chronicles.core.block_entity.TardisConsoleBlockEntity;
import uk.nbforge.gallifreyan_chronicles.tardis.TardisDimensionUtil;

public final class ConsoleHumSounds {
    private static final int CHECK_INTERVAL_TICKS = 1;
    private static final double START_RANGE_SQR =
            TardisConsoleHumSoundInstance.HEARING_RANGE * TardisConsoleHumSoundInstance.HEARING_RANGE;

    private static final Map<TardisConsoleBlockEntity, TardisConsoleHumSoundInstance> HUMS =
            ClientState.track(new WeakHashMap<>());

    private static int ticks;

    private ConsoleHumSounds() {}

    public static void track(TardisConsoleBlockEntity console) {
        HUMS.putIfAbsent(console, null);
    }

    public static void tick() {
        if (++ticks % CHECK_INTERVAL_TICKS != 0) return;

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) return;

        SoundManager sounds = minecraft.getSoundManager();
        Iterator<Map.Entry<TardisConsoleBlockEntity, TardisConsoleHumSoundInstance>> iterator =
                HUMS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<TardisConsoleBlockEntity, TardisConsoleHumSoundInstance> entry = iterator.next();
            TardisConsoleBlockEntity console = entry.getKey();
            TardisConsoleHumSoundInstance hum = entry.getValue();

            if (console.isRemoved()) {
                if (hum != null) sounds.stop(hum);
                iterator.remove();
                continue;
            }

            if (console.getLevel() != SoundListener.level()) {
                if (hum != null) {
                    sounds.stop(hum);
                    entry.setValue(null);
                }
                continue;
            }

            if (!TardisDimensionUtil.isInterior(console.getLevel())) {
                if (hum != null) {
                    sounds.stop(hum);
                    entry.setValue(null);
                }
                continue;
            }

            if (hum != null && sounds.isActive(hum)) continue;
            if (DetailLevelUtil.lowDetail()) continue;
            if (SoundListener.pos().distanceToSqr(Vec3.atCenterOf(console.getBlockPos())) > START_RANGE_SQR) continue;

            TardisConsoleHumSoundInstance fresh = new TardisConsoleHumSoundInstance(
                    console.getBlockPos(),
                    () -> !console.isRemoved()
                            && console.getLevel() != null
                            && console.getLevel().isClientSide(),
                    console::hasPower,
                    console::isPowerKnown);
            sounds.play(fresh);
            entry.setValue(fresh);
        }
    }
}
