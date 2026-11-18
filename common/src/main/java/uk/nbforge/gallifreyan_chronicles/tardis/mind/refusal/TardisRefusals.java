package uk.nbforge.gallifreyan_chronicles.tardis.mind.refusal;

import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import uk.nbforge.gallifreyan_chronicles.core.GCSounds;
import uk.nbforge.gallifreyan_chronicles.core.block_entity.TardisConsoleBlockEntity;
import uk.nbforge.gallifreyan_chronicles.core.sound.timed.TimedSounds;
import uk.nbforge.gallifreyan_chronicles.network.tardis.TardisDrumShakePayload;
import uk.nbforge.gallifreyan_chronicles.tardis.Tardis;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.MindMessages;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.TardisMinds;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.mood.TardisAlarm;
import uk.nbforge.gallifreyan_chronicles.util.PlayerMotionUtil;
import uk.nbforge.gallifreyan_chronicles.util.SchedulerUtil;
import uk.nbforge.gallifreyan_chronicles.util.SparkUtil;

public final class TardisRefusals {
    private static final int SNAP_BACK_TICKS = 8;

    private static final int WARNING_ANGER = 2;
    private static final int LASH_ANGER = 3;
    private static final int BURN_ANGER = 5;

    private static final double LASH_RANGE = 4.0;
    private static final float MAX_LASH_DAMAGE = 12.0f;

    private enum Force {
        GENTLE,
        AGITATED,
        ANGRY,
        VIOLENT
    }

    private TardisRefusals() {}

    /**
     * Some refusals only give a warning, others throw the lever back and may make the TARDIS attack players nearby.
     * Returns true if takeoff was refused.
     */
    public static boolean refuseTakeoff(
            ServerLevel level,
            TardisConsoleBlockEntity console,
            String leverId,
            BlockPos soundPos,
            Vec3 leverPos,
            Tardis tardis,
            @Nullable UUID pilot) {
        TardisMinds.Refusal refusal = TardisMinds.refusesTakeoff(tardis, pilot);
        String reason = TardisMinds.tooStressed(tardis, pilot) ? "stressed" : "distrust";

        switch (refusal) {
            case NONE -> {
                return false;
            }
            case COWED -> {
                play(level, soundPos, GCSounds.LEVER, 0.6f);
                tell(level, tardis, pilot, ChatFormatting.DARK_GRAY, "tardis_cowed");
                return false;
            }
            case BALK -> {
                snapLeverBack(level, console, leverId, soundPos, leverPos, Force.GENTLE);
                tell(level, tardis, pilot, ChatFormatting.GOLD, "tardis_balks." + reason);
            }
            case WORKED_UP -> {
                snapLeverBack(level, console, leverId, soundPos, leverPos, Force.AGITATED);
                tell(level, tardis, pilot, ChatFormatting.RED, "tardis_worked_up");
            }
            case REFUSE -> refuse(level, console, leverId, soundPos, leverPos, tardis, pilot, reason);
        }

        return true;
    }

    private static void refuse(
            ServerLevel level,
            TardisConsoleBlockEntity console,
            String leverId,
            BlockPos soundPos,
            Vec3 leverPos,
            Tardis tardis,
            @Nullable UUID pilot,
            String reason) {
        boolean aggressive = TardisAnger.level(tardis) > 0;
        if (!aggressive) {
            snapLeverBack(level, console, leverId, soundPos, leverPos, Force.ANGRY);
            tell(level, tardis, pilot, ChatFormatting.RED, "tardis_refuses." + reason);
            return;
        }

        int anger = TardisAnger.level(tardis);
        snapLeverBack(level, console, leverId, soundPos, leverPos, Force.VIOLENT);
        tell(
                level,
                tardis,
                pilot,
                anger >= LASH_ANGER ? ChatFormatting.DARK_RED : ChatFormatting.RED,
                angerMessage(anger, reason));
        soundAlarm(level, soundPos, tardis, anger);

        if (anger >= LASH_ANGER) {
            SchedulerUtil.runLater(level.getServer(), SNAP_BACK_TICKS, () -> lashOut(level, leverPos, anger));
        }
    }

    private static void tell(
            ServerLevel level, Tardis tardis, @Nullable UUID pilot, ChatFormatting colour, String key) {
        ServerPlayer who = pilot != null
                ? level.getServer().getPlayerList().getPlayer(pilot)
                : tardis.ownerPlayer().orElse(null);

        if (who != null) MindMessages.say(who, colour, key);
    }

    private static String angerMessage(int anger, String reason) {
        if (anger >= BURN_ANGER) return "tardis_furious";
        if (anger >= LASH_ANGER) return "tardis_lashes_out";
        if (anger == WARNING_ANGER) return "tardis_warning";
        return "tardis_refuses." + reason;
    }

    private static void soundAlarm(ServerLevel level, BlockPos soundPos, Tardis tardis, int anger) {
        if (anger >= BURN_ANGER) {
            TardisAlarm.untilCalm(tardis, level, soundPos);
        } else if (anger >= LASH_ANGER) {
            TardisAlarm.flash(level, soundPos, anger);
        } else if (anger == WARNING_ANGER) {
            TardisAlarm.flash(level, soundPos, 1);
        }
    }

    private static void snapLeverBack(
            ServerLevel level,
            TardisConsoleBlockEntity console,
            String leverId,
            BlockPos soundPos,
            Vec3 leverPos,
            Force force) {
        play(level, soundPos, GCSounds.SLIDER, 0.9f);
        console.setControlActivated(leverId, true);

        SchedulerUtil.runLater(level.getServer(), SNAP_BACK_TICKS, () -> {
            if (console.isRemoved()) return;

            console.setControlActivated(leverId, false);
            play(level, soundPos, GCSounds.LEVER, 0.8f - force.ordinal() * 0.1f);
            snapBackEffects(level, soundPos, leverPos, force);
        });
    }

    private static void snapBackEffects(ServerLevel level, BlockPos soundPos, Vec3 leverPos, Force force) {
        int strength = force.ordinal();
        double x = leverPos.x;
        double y = leverPos.y + 0.1;
        double z = leverPos.z;

        level.sendParticles(ParticleTypes.SMOKE, x, y, z, 5 + strength * 10, 0.12, 0.08, 0.12, 0.02);

        if (force.compareTo(Force.AGITATED) >= 0) {
            play(level, soundPos, GCSounds.TARDIS_STOP_DEMAT, 1.2f - strength * 0.13f);
            level.sendParticles(ParticleTypes.LARGE_SMOKE, x, y, z, strength * 3, 0.08, 0.05, 0.08, 0.01);
            SparkUtil.small(level, leverPos.add(0, 0.1, 0), strength * 4);
            TardisDrumShakePayload.broadcast(level, leverPos, strength * 0.3f);
        }

        if (force == Force.VIOLENT) {
            SparkUtil.big(level, leverPos.add(0, 0.1, 0));
        }
    }

    private static void lashOut(ServerLevel level, Vec3 leverPos, int anger) {
        int strength = anger - LASH_ANGER + 1;

        float damage = Math.min(2.0f * strength, MAX_LASH_DAMAGE);
        double push = 0.5 + 0.2 * strength;

        for (ServerPlayer player : level.players()) {
            if (player.isSpectator() || player.distanceToSqr(leverPos) > LASH_RANGE * LASH_RANGE) continue;

            player.hurtServer(level, level.damageSources().lightningBolt(), damage);
            throwBack(player, leverPos, push, 0.35 + 0.05 * strength);

            if (anger >= BURN_ANGER) {
                player.igniteForSeconds(2 + (anger - BURN_ANGER));
            }

            SparkUtil.big(level, player.position().add(0, 1, 0));
        }
    }

    private static void throwBack(ServerPlayer player, Vec3 from, double push, double lift) {
        Vec3 away = player.position().subtract(from).multiply(1, 0, 1);

        away = away.lengthSqr() < 1.0E-4 ? new Vec3(0, 0, 1) : away.normalize();

        PlayerMotionUtil.set(player, new Vec3(away.x * push, lift, away.z * push));
    }

    private static void play(ServerLevel level, BlockPos pos, SoundEvent sound, float pitch) {
        TimedSounds.play(level, pos, sound, SoundSource.BLOCKS, 1.0f, pitch);
    }
}
