package uk.nbforge.gallifreyan_chronicles.client;

import uk.nbforge.gallifreyan_chronicles.util.GameClockUtil;
import uk.nbforge.gallifreyan_chronicles.client.tardis.RematCatchClient;
import org.jspecify.annotations.Nullable;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.Registries;
import uk.nbforge.gallifreyan_chronicles.compat.portal.TardisPortalBridge;
import uk.nbforge.gallifreyan_chronicles.config.GCConfig;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import uk.nbforge.gallifreyan_chronicles.client.sound.OffsetSoundInstance;
import uk.nbforge.gallifreyan_chronicles.client.sound.SoundResumeStatus;
import uk.nbforge.gallifreyan_chronicles.network.ResumeSoundPayload;
import uk.nbforge.gallifreyan_chronicles.client.sound.SoundListener;
import uk.nbforge.gallifreyan_chronicles.core.block_entities.TardisConsoleBlockEntity;
import uk.nbforge.gallifreyan_chronicles.client.camera.GCCameraShake;
import uk.nbforge.gallifreyan_chronicles.client.gui.hud.GCPowerDarkness;
import uk.nbforge.gallifreyan_chronicles.client.protection.build.BuildExpiry;
import uk.nbforge.gallifreyan_chronicles.core.block_entities.TardisExteriorBlockEntity;
import uk.nbforge.gallifreyan_chronicles.client.sound.ConsoleHumSounds;
import uk.nbforge.gallifreyan_chronicles.client.sound.ExteriorHumSounds;
import uk.nbforge.gallifreyan_chronicles.client.sound.GCMusic;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class GCClientHooks {
    private GCClientHooks() {}

    static {
        /* fades use the time of the world you're standing in, the one the server keeps right */
        GameClockUtil.setClient(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            return minecraft.player != null ? minecraft.player.level().getGameTime() : -1L;
        });
    }

    public static void reloadResourcePacks() {
        Minecraft mc = Minecraft.getInstance();
        mc.execute(mc::reloadResourcePacks);
    }

    private static @Nullable Level clientLevel(Identifier dimension) {
        Level current = Minecraft.getInstance().level;
        if (current != null && current.dimension().identifier().equals(dimension)) return current;
        return TardisPortalBridge.clientLevel(ResourceKey.create(Registries.DIMENSION, dimension));
    }

    public static void applyConsoleRotorState(Identifier dimension, BlockPos pos, String rotorState) {
        Level level = clientLevel(dimension);
        if (level == null) return;
        if (level.getBlockEntity(pos) instanceof TardisConsoleBlockEntity console) {
            console.applyRotorStateClient(rotorState);
        }
    }

    public static void applyConsoleAnimation(Identifier dimension, BlockPos pos, String animationName, String controllerName) {
        Level level = clientLevel(dimension);
        if (level == null) return;
        if (level.getBlockEntity(pos) instanceof TardisConsoleBlockEntity console) {
            console.triggerAnimationClient(animationName, controllerName);
        }
    }

    public static void playResumedSound(ResumeSoundPayload payload) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) return;
        /* these restart flight loops for whoever just joined. a replay plays them again
           on every timeline jump, and they don't say which world they're in */
        if (SoundListener.inReplay()) return;
        SoundEvent event = BuiltInRegistries.SOUND_EVENT.getOptional(payload.sound())
                .orElseGet(() -> SoundEvent.createVariableRangeEvent(payload.sound()));
        SoundResumeStatus.warnIfEngineUnhooked();
        minecraft.getSoundManager().play(new OffsetSoundInstance(
                event, payload.volume(), payload.pitch(), payload.x(), payload.y(), payload.z(), payload.offsetSeconds()));
    }

    public static void triggerCameraShake(Vec3 origin, float strength) {
        /* in a replay, only shake when the camera is inside the tardis */
        if (SoundListener.inReplay() && !SoundListener.isTardisLevel(SoundListener.level())) return;
        GCCameraShake.trigger(origin, strength);
    }

    public static void clientTick() {
        Minecraft minecraft = Minecraft.getInstance();
        GCConfig.flushPending(false);
        BuildExpiry.tick(minecraft);
        ConsoleHumSounds.tick();
        RematCatchClient.tick();
        ExteriorHumSounds.tick();
        GCMusic.tick(minecraft);
    }

    public static void trackExteriorHum(TardisExteriorBlockEntity be) {
        ExteriorHumSounds.track(be);
    }

    public static void startConsoleClientEffects(TardisConsoleBlockEntity be) {
        BlockPos pos = be.getBlockPos();
        GCPowerDarkness.track(be);

        ConsoleHumSounds.track(be);

        Vec3 center = Vec3.atCenterOf(pos);
        GCCameraShake.addFlightSource(new GCCameraShake.FlightSource() {
            @Override
            public Vec3 position() {
                return center;
            }

            @Override
            public boolean isInFlight() {
                Minecraft minecraft = Minecraft.getInstance();
                return be.isInFlight() && minecraft.player != null && be.getLevel() == minecraft.player.level();
            }

            @Override
            public boolean isValid() {
                return !be.isRemoved() && be.getLevel() != null;
            }
        });
    }
}
