package uk.nbforge.gallifreyan_chronicles.mixin.client.compat;

import uk.nbforge.gallifreyan_chronicles.client.sound.SoundListener;
import uk.nbforge.gallifreyan_chronicles.core.GCSounds;
import uk.nbforge.gallifreyan_chronicles.core.dynamic_registries.TardisDoorRegistry;
import uk.nbforge.gallifreyan_chronicles.tardis.door.TardisDoorVariant;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/* immersive portals also carries sounds through portals. the interior door sounds
   reach 10 blocks, so outside you heard them up to 10 blocks away. the main door's
   sound is already played outside by us within the hum's range
   (InteriorDoorBlockEntity.playThroughExterior), so don't carry door sounds across */
@Pseudo
@Mixin(targets = "qouteall.imm_ptl.core.teleportation.CrossPortalSound", remap = false)
public abstract class PortalDoorSoundMixin {
    @Inject(method = "createCrossPortalSound", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private static void gc$noDoorSoundAcross(ClientLevel level, SoundEvent sound, SoundSource source, Vec3 pos,
                                             float volume, float pitch, long seed, CallbackInfoReturnable<SimpleSoundInstance> cir) {
        if (SoundListener.isTardisLevel(level) && isDoorSound(sound.location())) cir.setReturnValue(null);
    }

    private static boolean isDoorSound(Identifier id) {
        if (id.equals(GCSounds.TARDIS_DOOR_OPEN.location()) || id.equals(GCSounds.TARDIS_DOOR_CLOSE.location()))
            return true;
        String name = id.toString();
        for (TardisDoorVariant variant : TardisDoorRegistry.getAll()) {
            if (name.equals(variant.openSound()) || name.equals(variant.closeSound())) return true;
        }
        return false;
    }
}
