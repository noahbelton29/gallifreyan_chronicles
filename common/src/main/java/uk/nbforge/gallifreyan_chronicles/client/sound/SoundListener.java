package uk.nbforge.gallifreyan_chronicles.client.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import uk.nbforge.gallifreyan_chronicles.tardis.TardisDimensionUtil;

/** In a Flashback replay the camera is the listener and once it flies through a door it hears the TARDIS world instead of the overworld, the replay side sets the override. */
public final class SoundListener {
    public interface Override {
        @Nullable
        Level level();

        Vec3 pos();
    }

    private static @Nullable Override override;

    private SoundListener() {}

    public static void setOverride(Override value) {
        override = value;
    }

    public static boolean inReplay() {
        return override != null && override.level() != null;
    }

    public static @Nullable Level level() {
        if (override != null) {
            Level level = override.level();
            if (level != null) return level;
        }
        Minecraft mc = Minecraft.getInstance();
        return mc.player != null ? mc.player.level() : mc.level;
    }

    public static Vec3 pos() {
        if (inReplay()) return override.pos();
        Minecraft mc = Minecraft.getInstance();
        return mc.player != null ? mc.player.position() : Vec3.ZERO;
    }

    public static boolean isTardisLevel(@Nullable Level level) {
        return TardisDimensionUtil.isInterior(level);
    }
}
