package com.noahtnt2009.gallifreyan_chronicles.client.camera;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class GCCameraShake {
    public static final double MAX_DISTANCE = 25.0;
    public static final double SHAKE_DURATION = 0.7;
    public static final double ATTACK_TIME = 0.06;
    public static final boolean ROTATION_FALLS_OFF_WITH_DISTANCE = false;

    public static final float MAX_PITCH_DEGREES = 5.0F;
    public static final float MAX_YAW_DEGREES = 5.0F;
    public static final float MAX_ROLL_DEGREES = 8.0F;

    private static final double SLOW_HZ_MIN = 3.5, SLOW_HZ_MAX = 5.5;
    private static final double FAST_HZ_MIN = 8.0, FAST_HZ_MAX = 12.0;
    private static final double SLOW_WEIGHT = 0.7, FAST_WEIGHT = 0.3;

    public static final double RUMBLE_RANGE = 25.0;
    public static final double RUMBLE_DELAY = 0.6;
    public static final double RUMBLE_FADE_IN = 1.5;
    public static final double RUMBLE_FADE_OUT = 1.2;
    public static final float RUMBLE_PITCH_DEGREES = 0.35F;
    public static final float RUMBLE_YAW_DEGREES = 0.25F;
    public static final float RUMBLE_ROLL_DEGREES = 0.6F;

    private static final double[] RUMBLE_PITCH_HZ = {7.1, 11.3, 17.9};
    private static final double[] RUMBLE_YAW_HZ = {6.3, 9.7, 15.1};
    private static final double[] RUMBLE_ROLL_HZ = {5.2, 8.9, 13.7};
    private static final double[] RUMBLE_WEIGHTS = {0.5, 0.3, 0.2};
    private static final double RUMBLE_SWELL_HZ = 0.4;

    public interface FlightSource {
        Vec3 position();

        boolean isInFlight();

        boolean isValid();
    }

    private static final class RumbleSource {
        final FlightSource source;
        double flightSeconds = 0.0;
        double level = 0.0;

        RumbleSource(FlightSource source) {
            this.source = source;
        }
    }

    private static final List<RumbleSource> RUMBLE_SOURCES = new ArrayList<>();
    private static long lastFrameNanos = 0L;

    public record Sample(float pitch, float yaw, float roll) {}

    private record Wave(double slowHz, double slowPhase, double fastHz, double fastPhase) {
        static Wave random(ThreadLocalRandom r) {
            return new Wave(
                    r.nextDouble(SLOW_HZ_MIN, SLOW_HZ_MAX), r.nextDouble(0, Math.TAU),
                    r.nextDouble(FAST_HZ_MIN, FAST_HZ_MAX), r.nextDouble(0, Math.TAU));
        }

        double at(double t) {
            return SLOW_WEIGHT * Math.sin(Math.TAU * slowHz * t + slowPhase)
                    + FAST_WEIGHT * Math.sin(Math.TAU * fastHz * t + fastPhase);
        }
    }

    private static boolean active = false;
    private static long startNanos = 0L;
    private static float strength = 1.0F;
    private static Wave pitchWave = Wave.random(ThreadLocalRandom.current());
    private static Wave yawWave = pitchWave;
    private static Wave rollWave = pitchWave;

    private GCCameraShake() {}

    public static void trigger(Vec3 origin, float strengthMultiplier) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        double distance = player.getEyePosition().distanceTo(origin);
        float attenuation = (float) Math.clamp(1.0 - distance / MAX_DISTANCE, 0.0, 1.0);
        if (attenuation <= 0.0F) return;

        ThreadLocalRandom random = ThreadLocalRandom.current();
        pitchWave = Wave.random(random);
        yawWave = Wave.random(random);
        rollWave = Wave.random(random);

        strength = strengthMultiplier * (ROTATION_FALLS_OFF_WITH_DISTANCE ? attenuation : 1.0F);
        startNanos = System.nanoTime();
        active = true;
    }

    public static void addFlightSource(FlightSource source) {
        RUMBLE_SOURCES.add(new RumbleSource(source));
    }

    public static @Nullable Sample current() {
        Sample drum = drumSample();
        Sample rumble = rumbleSample();
        if (drum == null) return rumble;
        if (rumble == null) return drum;
        return new Sample(drum.pitch() + rumble.pitch(), drum.yaw() + rumble.yaw(), drum.roll() + rumble.roll());
    }

    private static @Nullable Sample rumbleSample() {
        if (RUMBLE_SOURCES.isEmpty()) return null;

        long now = System.nanoTime();
        double dt = lastFrameNanos == 0L ? 0.0 : Math.min((now - lastFrameNanos) / 1.0E9, 0.1);
        lastFrameNanos = now;

        LocalPlayer player = Minecraft.getInstance().player;
        double strongest = 0.0;

        for (int i = RUMBLE_SOURCES.size() - 1; i >= 0; i--) {
            RumbleSource rumble = RUMBLE_SOURCES.get(i);
            if (!rumble.source.isValid()) {
                RUMBLE_SOURCES.remove(i);
                continue;
            }

            if (rumble.source.isInFlight()) {
                rumble.flightSeconds += dt;
            } else {
                rumble.flightSeconds = 0.0;
            }
            boolean rumbling = rumble.flightSeconds > RUMBLE_DELAY;
            double target = rumbling ? 1.0 : 0.0;
            double rate = rumbling ? 1.0 / RUMBLE_FADE_IN : 1.0 / RUMBLE_FADE_OUT;
            if (rumble.level < target) {
                rumble.level = Math.min(target, rumble.level + rate * dt);
            } else {
                rumble.level = Math.max(target, rumble.level - rate * dt);
            }

            if (rumble.level <= 0.0 || player == null) continue;

            double distance = player.getEyePosition().distanceTo(rumble.source.position());
            double attenuation = Math.clamp(1.0 - distance / RUMBLE_RANGE, 0.0, 1.0);
            double smoothLevel = rumble.level * rumble.level * (3.0 - 2.0 * rumble.level);
            strongest = Math.max(strongest, smoothLevel * attenuation);
        }

        if (strongest <= 0.0) return null;

        double t = now / 1.0E9;
        double swell = 0.875 + 0.125 * Math.sin(Math.TAU * RUMBLE_SWELL_HZ * t);
        double amount = strongest * swell;
        return new Sample(
                (float) (rumbleWave(RUMBLE_PITCH_HZ, t) * amount * RUMBLE_PITCH_DEGREES),
                (float) (rumbleWave(RUMBLE_YAW_HZ, t + 3.7) * amount * RUMBLE_YAW_DEGREES),
                (float) (rumbleWave(RUMBLE_ROLL_HZ, t + 8.3) * amount * RUMBLE_ROLL_DEGREES)
        );
    }

    private static double rumbleWave(double[] frequencies, double t) {
        double value = 0.0;
        for (int i = 0; i < frequencies.length; i++) {
            value += RUMBLE_WEIGHTS[i] * Math.sin(Math.TAU * frequencies[i] * t);
        }
        return value;
    }

    private static @Nullable Sample drumSample() {
        if (!active) return null;

        double t = (System.nanoTime() - startNanos) / 1.0E9;
        if (t >= SHAKE_DURATION) {
            active = false;
            return null;
        }

        double envelope = envelope(t) * strength;
        return new Sample(
                (float) (pitchWave.at(t) * envelope * MAX_PITCH_DEGREES),
                (float) (yawWave.at(t) * envelope * MAX_YAW_DEGREES),
                (float) (rollWave.at(t) * envelope * MAX_ROLL_DEGREES)
        );
    }

    private static double envelope(double t) {
        double a = Math.min(t / ATTACK_TIME, 1.0);
        double attack = a * a * (3.0 - 2.0 * a);
        double remaining = 1.0 - t / SHAKE_DURATION;
        return attack * remaining * remaining;
    }
}
