package com.noahtnt2009.gallifreyan_chronicles.client.sound;

import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisConsoleBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.world.phys.Vec3;

import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;

public final class ConsoleHumSounds {
    private static final int CHECK_INTERVAL_TICKS = 10;
    private static final double START_RANGE_SQR = TardisConsoleHumSoundInstance.HEARING_RANGE * TardisConsoleHumSoundInstance.HEARING_RANGE;

    private static final Map<TardisConsoleBlockEntity, TardisConsoleHumSoundInstance> HUMS = new WeakHashMap<>();
    private static int ticks;

    private ConsoleHumSounds() {
    }

    private static boolean isTardisLevel(TardisConsoleBlockEntity console) {
        if (console.getLevel() == null) return false;
        var id = console.getLevel().dimension().identifier();
        return id.getNamespace().equals(Constants.MOD_ID) && id.getPath().startsWith("tardis/");
    }

    public static void track(TardisConsoleBlockEntity console) {
        HUMS.putIfAbsent(console, null);
    }

    public static void tick() {
        if (++ticks % CHECK_INTERVAL_TICKS != 0) return;

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) return;

        SoundManager sounds = minecraft.getSoundManager();
        Iterator<Map.Entry<TardisConsoleBlockEntity, TardisConsoleHumSoundInstance>> iterator = HUMS.entrySet().iterator();
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

            /* a console placed outside a tardis doesn't hum */
            if (!isTardisLevel(console)) {
                if (hum != null) {
                    sounds.stop(hum);
                    entry.setValue(null);
                }
                continue;
            }

            if (hum != null && sounds.isActive(hum)) continue;
            if (SoundListener.pos().distanceToSqr(Vec3.atCenterOf(console.getBlockPos())) > START_RANGE_SQR) continue;

            TardisConsoleHumSoundInstance fresh = new TardisConsoleHumSoundInstance(console.getBlockPos(),
                    () -> !console.isRemoved() && console.getLevel() != null && console.getLevel().isClientSide(),
                    console::hasPower,
                    console::isPowerKnown);
            sounds.play(fresh);
            entry.setValue(fresh);
        }
    }
}
