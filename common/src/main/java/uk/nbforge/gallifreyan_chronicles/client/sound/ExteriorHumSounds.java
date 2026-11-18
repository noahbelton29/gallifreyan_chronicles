package uk.nbforge.gallifreyan_chronicles.client.sound;

import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.world.phys.Vec3;
import uk.nbforge.gallifreyan_chronicles.client.lifecycle.ClientState;
import uk.nbforge.gallifreyan_chronicles.core.GCSounds;
import uk.nbforge.gallifreyan_chronicles.core.block_entity.TardisExteriorBlockEntity;

public final class ExteriorHumSounds {
    private static final int CHECK_INTERVAL_TICKS = 1;
    private static final double START_RANGE_SQR =
            (ExteriorHumSoundInstance.OPEN_RANGE + 2) * (ExteriorHumSoundInstance.OPEN_RANGE + 2);

    private static final Map<TardisExteriorBlockEntity, ExteriorHumSoundInstance> HUMS =
            ClientState.track(new WeakHashMap<>());

    private static final Map<TardisExteriorBlockEntity, Boolean> LAST_POWER = ClientState.track(new WeakHashMap<>());
    private static int ticks;

    private ExteriorHumSounds() {}

    private static void checkPowerChanges(Minecraft minecraft) {
        double maxRangeSqr = ExteriorHumSoundInstance.OPEN_RANGE * ExteriorHumSoundInstance.OPEN_RANGE;
        for (TardisExteriorBlockEntity exterior : HUMS.keySet()) {
            if (exterior.isRemoved() || exterior.getLevel() != SoundListener.level()) {
                LAST_POWER.remove(exterior);
                continue;
            }
            boolean powered = exterior.lamp().isPowered();
            Boolean last = LAST_POWER.put(exterior, powered);
            if (last == null || last == powered) continue;
            if (exterior.travel().isFullyInvisible()) continue;
            if (SoundListener.pos().distanceToSqr(Vec3.atCenterOf(exterior.getBlockPos())) > maxRangeSqr) continue;
            ExteriorPowerSoundInstance sound = new ExteriorPowerSoundInstance(
                    exterior, powered ? GCSounds.TARDIS_POWER_ON : GCSounds.TARDIS_POWER_OFF);
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
        Iterator<Map.Entry<TardisExteriorBlockEntity, ExteriorHumSoundInstance>> iterator =
                HUMS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<TardisExteriorBlockEntity, ExteriorHumSoundInstance> entry = iterator.next();
            TardisExteriorBlockEntity exterior = entry.getKey();
            ExteriorHumSoundInstance hum = entry.getValue();

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
            if (exterior.travel().isFullyInvisible()) {
                if (hum != null) {
                    hum.fadeOut();
                    entry.setValue(null);
                }
                continue;
            }
            if (hum != null && sounds.isActive(hum)) continue;
            if (!exterior.lamp().isPowered()) continue;
            if (SoundListener.pos().distanceToSqr(Vec3.atCenterOf(exterior.getBlockPos())) > START_RANGE_SQR) continue;

            ExteriorHumSoundInstance fresh = new ExteriorHumSoundInstance(exterior);
            sounds.play(fresh);
            entry.setValue(fresh);
        }
    }
}
