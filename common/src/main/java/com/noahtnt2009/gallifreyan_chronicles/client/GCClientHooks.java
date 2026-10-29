package com.noahtnt2009.gallifreyan_chronicles.client;

import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisConsoleBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.client.camera.GCCameraShake;
import com.noahtnt2009.gallifreyan_chronicles.client.sound.TardisConsoleHumSoundInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class GCClientHooks {
    private GCClientHooks() {}

    public static void reloadResourcePacks() {
        Minecraft mc = Minecraft.getInstance();
        mc.execute(mc::reloadResourcePacks);
    }

    public static void applyConsoleRotorState(BlockPos pos, String rotorState) {
        Level level = Minecraft.getInstance().level;
        if (level == null) return;
        if (level.getBlockEntity(pos) instanceof TardisConsoleBlockEntity console) {
            console.applyRotorStateClient(rotorState);
        }
    }

    public static void applyConsoleAnimation(BlockPos pos, String animationName, String controllerName) {
        Level level = Minecraft.getInstance().level;
        if (level == null) return;
        if (level.getBlockEntity(pos) instanceof TardisConsoleBlockEntity console) {
            console.triggerAnimationClient(animationName, controllerName);
        }
    }

    public static void triggerCameraShake(Vec3 origin, float strength) {
        GCCameraShake.trigger(origin, strength);
    }

    public static void startConsoleClientEffects(TardisConsoleBlockEntity be) {
        BlockPos pos = be.getBlockPos();

        Minecraft.getInstance().execute(() -> Minecraft.getInstance().getSoundManager().play(
                new TardisConsoleHumSoundInstance(pos,
                        () -> !be.isRemoved() && be.getLevel() != null && be.getLevel().isClientSide())
        ));

        Vec3 center = Vec3.atCenterOf(pos);
        GCCameraShake.addFlightSource(new GCCameraShake.FlightSource() {
            @Override
            public Vec3 position() {
                return center;
            }

            @Override
            public boolean isInFlight() {
                return be.isInFlight();
            }

            @Override
            public boolean isValid() {
                return !be.isRemoved() && be.getLevel() != null && be.getLevel() == Minecraft.getInstance().level;
            }
        });
    }
}
