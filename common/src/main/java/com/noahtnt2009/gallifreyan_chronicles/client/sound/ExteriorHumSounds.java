package com.noahtnt2009.gallifreyan_chronicles.client.sound;

import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisExteriorBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.init.GCSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.world.phys.Vec3;

import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;

/* starts and stops the outside hum for each tardis exterior near the player */
public final class ExteriorHumSounds {
    private static final int CHECK_INTERVAL_TICKS = 10;
    private static final double START_RANGE_SQR = (ExteriorHumSoundInstance.OPEN_RANGE + 2) * (ExteriorHumSoundInstance.OPEN_RANGE + 2);

    private static final Map<TardisExteriorBlockEntity, ExteriorHumSoundInstance> HUMS = new WeakHashMap<>();
    /* last power state we saw for each exterior, to play the power on/off sound when it flips */
    private static final Map<TardisExteriorBlockEntity, Boolean> LAST_POWER = new WeakHashMap<>();
    private static int ticks;

    private ExteriorHumSounds() {
    }

    /* every tick: did the power flip on an exterior near us? play the power sound there */
    private static void checkPowerChanges(Minecraft minecraft) {
        double maxRangeSqr = ExteriorHumSoundInstance.OPEN_RANGE * ExteriorHumSoundInstance.OPEN_RANGE;
        for (TardisExteriorBlockEntity exterior : HUMS.keySet()) {
            if (exterior.isRemoved() || exterior.getLevel() != minecraft.level) {
                LAST_POWER.remove(exterior);
                continue;
            }
            boolean powered = exterior.isPowered();
            Boolean last = LAST_POWER.put(exterior, powered);
            if (last == null || last == powered) continue;
            if (minecraft.player.position().distanceToSqr(Vec3.atCenterOf(exterior.getBlockPos())) > maxRangeSqr) continue;
            ExteriorPowerSoundInstance sound = new ExteriorPowerSoundInstance(exterior,
                    powered ? GCSounds.TARDIS_POWER_ON : GCSounds.TARDIS_POWER_OFF);
            minecraft.getSoundManager().play(sound);
            sound.applyStartMuffle();
        }
    }

    public static void track(TardisExteriorBlockEntity exterior) {
        HUMS.putIfAbsent(exterior, null);
    }

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

            /* the hum fades itself out, cutting it off made a pop */
            if (exterior.isRemoved()) {
                if (hum != null) hum.fadeOut();
                iterator.remove();
                continue;
            }
            if (exterior.getLevel() != minecraft.level) {
                if (hum != null) {
                    hum.fadeOut();
                    entry.setValue(null);
                }
                continue;
            }
            if (hum != null && sounds.isActive(hum)) continue;
            if (!exterior.isPowered()) continue;
            if (minecraft.player.position().distanceToSqr(Vec3.atCenterOf(exterior.getBlockPos())) > START_RANGE_SQR) continue;

            ExteriorHumSoundInstance fresh = new ExteriorHumSoundInstance(exterior);
            sounds.play(fresh);
            entry.setValue(fresh);
        }
    }
}
