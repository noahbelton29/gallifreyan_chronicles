package uk.nbforge.gallifreyan_chronicles.mixin.client.gameplay;

import net.minecraft.client.gui.Gui;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import uk.nbforge.gallifreyan_chronicles.Constants;
import uk.nbforge.gallifreyan_chronicles.config.GCConfig;

/**
 * hides the mod's action bar messages when the player turned them off. vanilla and other mods are left alone, only
 * translation keys in the mod's namespace get dropped
 */
@Mixin(Gui.class)
public abstract class OverlayMessageMixin {
    @Inject(method = "setOverlayMessage", at = @At("HEAD"), cancellable = true, require = 0)
    private void gc$hideOverlayMessages(Component message, boolean animate, CallbackInfo ci) {
        if (!(message.getContents() instanceof TranslatableContents contents)) return;
        String key = contents.getKey();
        boolean ours = key.startsWith("overlay." + Constants.MOD_ID + ".")
                || key.startsWith("message." + Constants.MOD_ID + ".");
        if (!ours) return;

        boolean flight = key.contains(".flight_");
        if (flight ? !GCConfig.showFlightMessages() : !GCConfig.showOverlayMessages()) {
            ci.cancel();
        }
    }
}
