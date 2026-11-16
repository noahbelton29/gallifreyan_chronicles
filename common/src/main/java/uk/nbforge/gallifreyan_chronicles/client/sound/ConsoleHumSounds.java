package uk.nbforge.gallifreyan_chronicles.client.sound;

import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.world.phys.Vec3;
import uk.nbforge.gallifreyan_chronicles.client.util.DetailLevelUtil;
import uk.nbforge.gallifreyan_chronicles.core.block_entities.TardisConsoleBlockEntity;
import uk.nbforge.gallifreyan_chronicles.util.TardisDimensionUtil;

public final class ConsoleHumSounds {
    // Checked every tick (was throttled to every 10 ticks): the interior hum stopping on
    // one poll and the exterior hum starting on the next, independent, poll left a gap of
    // up to half a second of true silence when walking out through the door, which stood
    // out once the exterior hum stopped masking it with its own fade-in
    private static final int CHECK_INTERVAL_TICKS = 1;
    private static final double START_RANGE_SQR =
            TardisConsoleHumSoundInstance.HEARING_RANGE * TardisConsoleHumSoundInstance.HEARING_RANGE;

    private static final Map<TardisConsoleBlockEntity, TardisConsoleHumSoundInstance> HUMS = new WeakHashMap<>();
    private static int ticks;

    private ConsoleHumSounds() {}

    private static boolean isTardisLevel(TardisConsoleBlockEntity console) {
        return TardisDimensionUtil.isInterior(console.getLevel());
    }

    public static void track(TardisConsoleBlockEntity console) {
        HUMS.putIfAbsent(console, null);
    }

    /** Every few ticks, start a hum for consoles near you and stop it for consoles that are removed, in another world or outside a TARDIS. */
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

            // A console placed outside a TARDIS does not hum
            if (!isTardisLevel(console)) {
                if (hum != null) {
                    sounds.stop(hum);
                    entry.setValue(null);
                }
                continue;
            }

            if (hum != null && sounds.isActive(hum)) continue;
            if (!DetailLevelUtil.highDetail()) continue;
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
