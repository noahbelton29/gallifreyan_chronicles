package uk.nbforge.gallifreyan_chronicles.compat.flashback;

import uk.nbforge.gallifreyan_chronicles.client.sound.SoundListener;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;
import qouteall.imm_ptl.core.ClientWorldLoader;

/* see SnapshotOutsideWorldMixin. remembers the last world that isn't a tardis,
   and takes snapshots from there while you're inside one */
final class FlashbackSnapshotRoot {
    private static @Nullable ResourceKey<Level> lastOutside;

    private FlashbackSnapshotRoot() {
    }

    static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) {
                lastOutside = null;
                return;
            }
            if (!SoundListener.isTardisLevel(client.player.level())) lastOutside = client.player.level().dimension();
        });
    }

    static void write(Runnable original) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || !SoundListener.isTardisLevel(mc.player.level()) || lastOutside == null) {
            original.run();
            return;
        }
        ClientLevel outside = ClientWorldLoader.getOptionalWorld(lastOutside);
        if (outside == null) {
            original.run();
            return;
        }
        ClientWorldLoader.withSwitchedWorld(outside, original);
    }
}
