package uk.nbforge.gallifreyan_chronicles.mixin.client.compat.replaymod;

import uk.nbforge.gallifreyan_chronicles.compat.replaymod.ReplayModSoundGate;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.class)
public abstract class InteriorSoundMixin {
    @Inject(method = {"playSeededSound*", "playLocalSound*", "playPlayerSound"}, at = @At("HEAD"), require = 0)
    private void gc$muteInteriorInReplay(CallbackInfo ci) {
        if (FabricLoader.getInstance().isModLoaded("replaymod")) {
            ReplayModSoundGate.shouldMute((Level) (Object) this);
        }
    }
}
