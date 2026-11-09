package com.noahtnt2009.gallifreyan_chronicles.mixin.client.compat;

import com.noahtnt2009.gallifreyan_chronicles.compat.soundphysics.SoundPhysicsCompat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "com.sonicether.soundphysics.SoundPhysics", remap = false)
public abstract class SoundPhysicsTardisEchoMixin {
    @Unique
    private static final ThreadLocal<Boolean> GC$APPLYING = ThreadLocal.withInitial(() -> Boolean.FALSE);

    @Shadow(remap = false)
    public static void setEnvironment(int sourceID, float sendGain0, float sendGain1, float sendGain2, float sendGain3,
                                      float sendCutoff0, float sendCutoff1, float sendCutoff2, float sendCutoff3,
                                      float directCutoff, float directGain) {
        throw new AssertionError();
    }

    @Inject(method = "setEnvironment", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private static void gc$tardisEcho(int sourceID, float sendGain0, float sendGain1, float sendGain2, float sendGain3,
                                      float sendCutoff0, float sendCutoff1, float sendCutoff2, float sendCutoff3,
                                      float directCutoff, float directGain, CallbackInfo ci) {
        if (GC$APPLYING.get()) return;
        if (sendGain0 == 0.0F && sendGain1 == 0.0F && sendGain2 == 0.0F && sendGain3 == 0.0F) return;

        if (!SoundPhysicsCompat.inTardisInterior()) return;
        SoundPhysicsCompat.markActive();
        float echo = SoundPhysicsCompat.tardisEcho();
        float hum = 1.0F - echo;

        float gain0 = SoundPhysicsCompat.damp(sendGain0, 0.0F, hum);
        float gain1 = SoundPhysicsCompat.damp(sendGain1, 0.0F, hum);
        float gain2 = SoundPhysicsCompat.damp(sendGain2, 0.0F, hum);
        float gain3 = SoundPhysicsCompat.damp(sendGain3, 0.0F, hum);

        GC$APPLYING.set(Boolean.TRUE);
        try {
            setEnvironment(sourceID,
                    gain0,
                    SoundPhysicsCompat.boost(gain1, 1.4F, 0.15F, echo),
                    SoundPhysicsCompat.boost(gain2, 1.8F, 0.35F, echo),
                    SoundPhysicsCompat.boost(gain3, 2.2F, 0.5F, echo),
                    sendCutoff0,
                    sendCutoff1,
                    SoundPhysicsCompat.boost(sendCutoff2, 1.0F, 0.25F, echo),
                    SoundPhysicsCompat.boost(sendCutoff3, 1.0F, 0.25F, echo),
                    directCutoff,
                    directGain);
        } finally {
            GC$APPLYING.set(Boolean.FALSE);
        }
        ci.cancel();
    }
}
