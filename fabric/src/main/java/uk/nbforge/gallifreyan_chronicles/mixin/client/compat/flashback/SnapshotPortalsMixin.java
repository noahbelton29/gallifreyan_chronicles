package uk.nbforge.gallifreyan_chronicles.mixin.client.compat.flashback;

import uk.nbforge.gallifreyan_chronicles.compat.flashback.FlashbackChunks;

import java.util.function.Consumer;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/* flashback calls this empty method while writing a snapshot, so mods can add
   packets. we add the portals. see FlashbackPortalSnapshot */
@Pseudo
@Mixin(targets = "com.moulberry.flashback.record.Recorder", remap = false)
public abstract class SnapshotPortalsMixin {
    @Inject(method = "writeCustomSnapshot", at = @At("HEAD"), remap = false, require = 0)
    private void gc$addPortals(Consumer<Packet<? super ClientGamePacketListener>> out, CallbackInfo ci) {
        FlashbackChunks.writePortalSnapshot(out);
    }
}
