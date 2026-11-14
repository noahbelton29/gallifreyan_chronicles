package uk.nbforge.gallifreyan_chronicles.tardis.flight.crash;

import uk.nbforge.gallifreyan_chronicles.core.GCEffects;
import net.minecraft.world.effect.MobEffectInstance;

import java.util.Map;
import java.util.HashMap;

import uk.nbforge.gallifreyan_chronicles.util.MessageUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerPlayer;
import uk.nbforge.gallifreyan_chronicles.core.block_entities.InteriorDoorBlockEntity;
import uk.nbforge.gallifreyan_chronicles.core.block_entities.TardisExteriorBlockEntity;
import uk.nbforge.gallifreyan_chronicles.tardis.Tardis;
import uk.nbforge.gallifreyan_chronicles.util.GCSchedulerUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/* a crashed tardis keeps billowing smoke out of its console until it's repaired.
   open the doors and the smoke gets pulled through the room, out the doorway and out of the box */
public final class CrashSmoke {
    private static final int PUFF_TICKS = 5;

    /* pouring out of the console */
    private static final int CONSOLE_PUFFS = 12;
    private static final int CONSOLE_COLUMN = 4;

    /* hanging in the room */
    private static final double ROOM_SPREAD = 8.0;
    private static final double ROOM_HEIGHT = 3.0;
    private static final int ROOM_PUFFS = 40;

    /* drawn towards the doors when they're open */
    private static final int DRAFT_PUFFS = 30;
    private static final double DRAFT_SPEED = 0.15;
    private static final int DOORWAY_PUFFS = 15;
    private static final int OUTSIDE_PUFFS = 25;
    private static final double OUTSIDE_SPEED = 0.2;

    /* breathing it in. about 10 seconds before you start choking, and it gets worse the longer you stay.
       open doors let some out so it builds slower */
    private static final int SAFE_TICKS = 200;
    private static final int WARN_TICKS = 100;
    /* choking goes up a level this often */
    private static final int WORSE_EVERY_TICKS = 80;
    private static final int MAX_LEVEL = 4;
    /* how long choking lasts after you get out */
    private static final int LINGER_TICKS = 100;

    private static final Set<UUID> RUNNING = new HashSet<>();
    /* how long each player has been breathing it, per tardis */
    private static final Map<UUID, Map<UUID, Integer>> BREATHED = new HashMap<>();

    private CrashSmoke() {
    }

    public static void reset() {
        RUNNING.clear();
        BREATHED.clear();
    }

    public static void start(Tardis tardis) {
        if (RUNNING.add(tardis.id())) puff(tardis);
    }

    private static void puff(Tardis tardis) {
        boolean repaired = !tardis.exists() || !TardisDamage.isCrashed(tardis);
        if (repaired) {
            RUNNING.remove(tardis.id());
            BREATHED.remove(tardis.id());
            return;
        }

        ServerLevel interior = tardis.interiorLevel().orElse(null);
        BlockPos console = tardis.consolePos().orElse(null);
        if (interior != null && console != null) {
            Vec3 centre = Vec3.atCenterOf(console);
            pourFromConsole(interior, centre);

            InteriorDoorBlockEntity door = tardis.doorState().isClosed() ? null : doorIn(tardis, interior);
            if (door == null) hangInRoom(interior, centre);
            else draftToDoor(interior, centre, door);
            choke(tardis, interior, door != null);
        }
        if (!tardis.doorState().isClosed()) pourOutOfBox(tardis);

        GCSchedulerUtil.runLater(tardis.server(), PUFF_TICKS, () -> puff(tardis));
    }

    /* thick smoke rising out of the console */
    private static void pourFromConsole(ServerLevel level, Vec3 console) {
        level.sendParticles(ParticleTypes.LARGE_SMOKE, console.x, console.y + 1.0, console.z, CONSOLE_PUFFS,
                0.6, 0.3, 0.6, 0.06);
        level.sendParticles(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE, console.x, console.y + 1.0, console.z, CONSOLE_COLUMN,
                0.4, 0.2, 0.4, 0.01);
    }

    /* doors shut: it just fills the room */
    private static void hangInRoom(ServerLevel level, Vec3 console) {
        level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, console.x, console.y + 1.5, console.z, ROOM_PUFFS,
                ROOM_SPREAD, ROOM_HEIGHT, ROOM_SPREAD, 0.005);
        level.sendParticles(ParticleTypes.LARGE_SMOKE, console.x, console.y + 1.5, console.z, ROOM_PUFFS / 3,
                ROOM_SPREAD, ROOM_HEIGHT, ROOM_SPREAD, 0.01);
    }

    /* doors open: smoke all over the room drifts towards the doorway and out through it */
    private static void draftToDoor(ServerLevel level, Vec3 console, InteriorDoorBlockEntity door) {
        RandomSource random = level.getRandom();
        Vec3 doorway = Vec3.atCenterOf(door.getBlockPos()).add(0, 0.5, 0);

        for (int i = 0; i < DRAFT_PUFFS; i++) {
            Vec3 from = console.add((random.nextDouble() - 0.5) * ROOM_SPREAD * 2,
                    1 + random.nextDouble() * ROOM_HEIGHT,
                    (random.nextDouble() - 0.5) * ROOM_SPREAD * 2);
            Vec3 towards = doorway.subtract(from).normalize();
            send(level, random, from, towards, DRAFT_SPEED);
        }

        /* thick stream right at the doorway going through it */
        Direction facing = door.getFacing();
        Vec3 through = new Vec3(-facing.getStepX(), 0.05, -facing.getStepZ());
        Vec3 inFront = doorway.add(facing.getStepX() * 1.2, 0, facing.getStepZ() * 1.2);
        for (int i = 0; i < DOORWAY_PUFFS; i++) {
            Vec3 from = inFront.add((random.nextDouble() - 0.5) * 0.8, (random.nextDouble() - 0.5) * 1.6,
                    (random.nextDouble() - 0.5) * 0.8);
            send(level, random, from, through, DRAFT_SPEED);
        }
    }

    /* smoke billowing out of the box */
    private static void pourOutOfBox(Tardis tardis) {
        TardisExteriorBlockEntity exterior = tardis.exteriorIfLoaded().orElse(null);
        if (exterior == null || !(exterior.getLevel() instanceof ServerLevel outside)) return;

        RandomSource random = outside.getRandom();
        double yaw = Math.toRadians(exterior.getYaw());
        Vec3 outwards = new Vec3(-Math.sin(yaw), 0.15, Math.cos(yaw));
        Vec3 doors = Vec3.atBottomCenterOf(exterior.getBlockPos()).add(outwards.x * 0.6, 1.2, outwards.z * 0.6);
        for (int i = 0; i < OUTSIDE_PUFFS; i++) {
            Vec3 from = doors.add((random.nextDouble() - 0.5) * 0.9, (random.nextDouble() - 0.5) * 1.6,
                    (random.nextDouble() - 0.5) * 0.9);
            send(outside, random, from, outwards, OUTSIDE_SPEED * (0.6 + random.nextDouble() * 0.6));
        }
        outside.sendParticles(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE, doors.x, doors.y + 1.5, doors.z, 2, 0.4, 0.3, 0.4, 0.01);
    }

    private static void choke(Tardis tardis, ServerLevel interior, boolean doorsOpen) {
        Map<UUID, Integer> breathed = BREATHED.computeIfAbsent(tardis.id(), id -> new HashMap<>());
        int breath = doorsOpen ? PUFF_TICKS / 2 : PUFF_TICKS;
        for (ServerPlayer player : interior.players()) {
            if (player.isCreative() || player.isSpectator()) continue;
            int before = breathed.getOrDefault(player.getUUID(), 0);
            int now = before + breath;
            breathed.put(player.getUUID(), now);

            if (before < WARN_TICKS && now >= WARN_TICKS) {
                MessageUtil.overlay(player, ChatFormatting.GRAY, "smoke_choking");
            }
            if (now < SAFE_TICKS) continue;
            int level = Math.min((now - SAFE_TICKS) / WORSE_EVERY_TICKS, MAX_LEVEL);
            MobEffectInstance current = player.getEffect(GCEffects.CHOKING);
            /* only top it up when it gets worse or is about to run out, so its timer keeps ticking */
            boolean worse = current == null || current.getAmplifier() < level;
            if (worse || current.getDuration() < LINGER_TICKS / 2) {
                player.addEffect(new MobEffectInstance(GCEffects.CHOKING, LINGER_TICKS, level));
            }
        }
        /* leaving lets you catch your breath */
        breathed.keySet().removeIf(id -> interior.getPlayerByUUID(id) == null);
    }

    private static @Nullable InteriorDoorBlockEntity doorIn(Tardis tardis, ServerLevel interior) {
        for (InteriorDoorBlockEntity door : tardis.interiorDoors()) {
            if (door.getLevel() == interior) return door;
        }
        return null;
    }

    /* count 0 makes the particle move in the given direction */
    private static void send(ServerLevel level, RandomSource random, Vec3 from, Vec3 direction, double speed) {
        SimpleParticleType type = random.nextInt(3) == 0 ? ParticleTypes.LARGE_SMOKE : ParticleTypes.CAMPFIRE_COSY_SMOKE;
        level.sendParticles(type, from.x, from.y, from.z, 0, direction.x, direction.y, direction.z, speed);
    }
}
