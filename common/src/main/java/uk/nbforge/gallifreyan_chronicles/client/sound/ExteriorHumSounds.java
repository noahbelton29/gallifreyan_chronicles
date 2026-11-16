package uk.nbforge.gallifreyan_chronicles.client.sound;

import uk.nbforge.gallifreyan_chronicles.core.block_entities.TardisExteriorBlockEntity;
import uk.nbforge.gallifreyan_chronicles.core.GCSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.world.phys.Vec3;

import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;

/** Starts and stops the outside hum for each TARDIS exterior near the player. */
public final class ExteriorHumSounds {
    // Checked every tick (was throttled to every 10 ticks): the interior hum stopping on
    // one poll and the exterior hum starting on the next, independent, poll left a gap of
    // up to half a second of true silence when walking out through the door, which stood
    // out once the exterior hum stopped masking it with its own fade-in
    private static final int CHECK_INTERVAL_TICKS = 1;
    private static final double START_RANGE_SQR = (ExteriorHumSoundInstance.OPEN_RANGE + 2) * (ExteriorHumSoundInstance.OPEN_RANGE + 2);

    private static final Map<TardisExteriorBlockEntity, ExteriorHumSoundInstance> HUMS = new WeakHashMap<>();
    // The last power state seen for each exterior, to play the power sound when it flips
    private static final Map<TardisExteriorBlockEntity, Boolean> LAST_POWER = new WeakHashMap<>();
    private static int ticks;

    private ExteriorHumSounds() {
    }

    /** Every tick, did the power flip on an exterior near us, then play the power sound there. */
    private static void checkPowerChanges(Minecraft minecraft) {
        double maxRangeSqr = ExteriorHumSoundInstance.OPEN_RANGE * ExteriorHumSoundInstance.OPEN_RANGE;
        for (TardisExteriorBlockEntity exterior : HUMS.keySet()) {
            if (exterior.isRemoved() || exterior.getLevel() != SoundListener.level()) {
                LAST_POWER.remove(exterior);
                continue;
            }
            boolean powered = exterior.isPowered();
            Boolean last = LAST_POWER.put(exterior, powered);
            if (last == null || last == powered) continue;
            // Still tracked above so a flip missed while gone does not fire the click once it is back,
            // but there is nothing there for the click itself to come from while fully dematerialised
            if (exterior.isFullyInvisible()) continue;
            if (SoundListener.pos().distanceToSqr(Vec3.atCenterOf(exterior.getBlockPos())) > maxRangeSqr) continue;
            ExteriorPowerSoundInstance sound = new ExteriorPowerSoundInstance(exterior,
                    powered ? GCSounds.TARDIS_POWER_ON : GCSounds.TARDIS_POWER_OFF);
            minecraft.getSoundManager().play(sound);
            sound.applyStartMuffle();
        }
    }

    public static void track(TardisExteriorBlockEntity exterior) {
        HUMS.putIfAbsent(exterior, null);
    }

    /** Every few ticks, start the hum of exteriors that are powered and near you and fade out the ones that are gone or in another world. */
    public static void tick() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) return;

        checkPowerChanges(minecraft);
        if (++ticks % CHECK_INTERVAL_TICKS != 0) return;

        SoundManager sounds = minecraft.getSoundManager();
        Iterator<Map.Entry<TardisExteriorBlockEntity, ExteriorHumSoundInstance>> iterator = HUMS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<TardisExteriorBlockEntity, ExteriorHumSoundInstance> entry = iterator.next();
            TardisExteriorBlockEntity exterior = entry.getKey();
            ExteriorHumSoundInstance hum = entry.getValue();

            // The hum fades itself out, cutting it off made a pop
            if (exterior.isRemoved()) {
                if (hum != null) hum.fadeOut();
                iterator.remove();
                continue;
            }
            if (exterior.getLevel() != SoundListener.level()) {
                if (hum != null) {
                    hum.fadeOut();
                    entry.setValue(null);
                }
                continue;
            }
            // Fully dematerialised means genuinely gone, not just mid fade, so the hum should not be
            // heard from an empty patch of air; it comes back on its own once any part of it rematerialises
            if (exterior.isFullyInvisible()) {
                if (hum != null) {
                    hum.fadeOut();
                    entry.setValue(null);
                }
                continue;
            }
            if (hum != null && sounds.isActive(hum)) continue;
            if (!exterior.isPowered()) continue;
            if (SoundListener.pos().distanceToSqr(Vec3.atCenterOf(exterior.getBlockPos())) > START_RANGE_SQR) continue;

            ExteriorHumSoundInstance fresh = new ExteriorHumSoundInstance(exterior);
            sounds.play(fresh);
            entry.setValue(fresh);
        }
    }
}
