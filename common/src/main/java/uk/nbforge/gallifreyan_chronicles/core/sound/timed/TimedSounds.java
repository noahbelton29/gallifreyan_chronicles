package uk.nbforge.gallifreyan_chronicles.core.sound.timed;

import uk.nbforge.gallifreyan_chronicles.network.ResumeSoundPayload;
import uk.nbforge.gallifreyan_chronicles.platform.Services;
import uk.nbforge.gallifreyan_chronicles.core.sound.timed.TimedSoundData.Session;
import uk.nbforge.gallifreyan_chronicles.util.GCSchedulerUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class TimedSounds {
    private static final int MIN_TRACKED_TICKS = 40;

    private TimedSounds() {
    }

    private static long now(MinecraftServer server) {
        return server.overworld().getGameTime();
    }

    public static void play(ServerLevel level, BlockPos pos, SoundEvent sound, SoundSource source, float volume, float pitch) {
        start(level, pos, sound, source, volume, pitch, false, Optional.empty(), 1.0F);
    }

    public static void playThenLoop(ServerLevel level, BlockPos pos, SoundEvent first, SoundEvent then,
                                    SoundSource source, float volume, float pitch, float thenVolume) {
        start(level, pos, first, source, volume, pitch, false, Optional.of(then.location()), thenVolume);
    }

    private static void start(ServerLevel level, BlockPos pos, SoundEvent sound, SoundSource source, float volume,
                              float pitch, boolean loop, Optional<Identifier> next, float nextVolume) {
        level.playSound(null, pos, sound, source, volume, pitch);

        double seconds = SoundDurations.seconds(sound.location());
        int durationTicks = (int) Math.round(seconds * 20.0 / Math.max(pitch, 0.01F));
        boolean chained = loop || next.isPresent();
        if (durationTicks < (chained ? 1 : MIN_TRACKED_TICKS)) return;

        MinecraftServer server = level.getServer();
        Session session = new Session(level.dimension(), sound.location(), Vec3.atCenterOf(pos), volume, pitch,
                now(server), durationTicks, loop, next, nextVolume);
        TimedSoundData data = TimedSoundData.get(server);
        data.sessions().removeIf(old -> now(server) - old.startTick() >= old.durationTicks() && !old.loop() && old.next().isEmpty());
        data.add(session);
        scheduleEnd(server, session);
    }

    private static void scheduleEnd(MinecraftServer server, Session session) {
        long elapsed = Math.max(0, now(server) - session.startTick());
        long remaining = session.loop()
                ? session.durationTicks() - (elapsed % session.durationTicks())
                : session.durationTicks() - elapsed;
        GCSchedulerUtil.runLater(server, (int) Math.max(1, remaining), () -> onEnd(server, session));
    }

    private static void onEnd(MinecraftServer server, Session session) {
        TimedSoundData data = TimedSoundData.get(server);
        if (!data.remove(session)) return;

        ServerLevel level = server.getLevel(session.dimension());
        if (level == null) return;

        BlockPos pos = BlockPos.containing(session.pos());
        if (session.next().isPresent()) {
            SoundEvent next = BuiltInRegistries.SOUND_EVENT.getOptional(session.next().get()).orElse(null);
            if (next != null) start(level, pos, next, SoundSource.BLOCKS, session.nextVolume(), session.pitch(), true, Optional.empty(), 1.0F);
        } else if (session.loop()) {
            SoundEvent again = BuiltInRegistries.SOUND_EVENT.getOptional(session.sound()).orElse(null);
            if (again != null) start(level, pos, again, SoundSource.BLOCKS, session.volume(), session.pitch(), true, Optional.empty(), 1.0F);
        }
    }

    public static void stop(ServerLevel level, SoundEvent sound) {
        TimedSoundData data = TimedSoundData.get(level.getServer());
        for (Session session : new ArrayList<>(data.sessions())) {
            if (!session.dimension().equals(level.dimension())) continue;
            if (session.sound().equals(sound.location())) {
                data.remove(session);
            } else if (session.next().isPresent() && session.next().get().equals(sound.location())) {
                data.replace(session, new Session(session.dimension(), session.sound(), session.pos(), session.volume(),
                        session.pitch(), session.startTick(), session.durationTicks(), session.loop(), Optional.empty(), session.nextVolume()));
            }
        }
    }

    public static void onServerStart(MinecraftServer server) {
        for (Session session : new ArrayList<>(TimedSoundData.get(server).sessions())) {
            scheduleEnd(server, session);
        }
    }

    public static void resume(ServerPlayer player) {
        ServerLevel level = player.level();
        MinecraftServer server = level.getServer();
        long now = now(server);
        List<Session> sessions = new ArrayList<>(TimedSoundData.get(server).sessions());

        for (Session session : sessions) {
            if (!session.dimension().equals(level.dimension())) continue;

            long elapsed = now - session.startTick();
            if (elapsed < 0) continue;
            if (session.loop()) elapsed %= session.durationTicks();
            else if (elapsed >= session.durationTicks()) continue;

            SoundEvent event = BuiltInRegistries.SOUND_EVENT.getOptional(session.sound()).orElse(null);
            double range = event == null ? 16.0 : event.getRange(session.volume());
            if (player.distanceToSqr(session.pos()) > range * range) continue;

            float offsetSeconds = elapsed / 20.0F * session.pitch();
            Services.NETWORK.sendResumeSound(player, new ResumeSoundPayload(
                    session.sound(), session.pos().x, session.pos().y, session.pos().z,
                    session.volume(), session.pitch(), offsetSeconds));
        }
    }
}
