package com.noahtnt2009.gallifreyan_chronicles.tardis.mind.mood;

import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.TardisMindSystem;

import net.minecraft.server.MinecraftServer;

import com.noahtnt2009.gallifreyan_chronicles.tardis.api.TardisApi;

import com.noahtnt2009.gallifreyan_chronicles.init.GCSounds;
import com.noahtnt2009.gallifreyan_chronicles.network.TardisAlarmPayload;
import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisExteriorBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.tardis.api.Tardis;
import com.noahtnt2009.gallifreyan_chronicles.util.GCScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class TardisAlarm {
    public static final int FLASH_TICKS = 30;
    private static final float CALM_STRESS = 30f;
    private static final int LOUD_LAYERS = 3;
    private static final float INSIDE_VOLUME = 1.0F;
    private static final float OUTSIDE_VOLUME = 0.6F;
    private static final double LEAK_RANGE = 32.0;

    private static final Set<UUID> RUNNING = new HashSet<>();
    private static final Set<UUID> UNTIL_REPAIRED = new HashSet<>();

    private TardisAlarm() {
    }

    public static void reset() {
        RUNNING.clear();
        UNTIL_REPAIRED.clear();
    }

    public static void untilRepaired(Tardis tardis) {
        if (UNTIL_REPAIRED.add(tardis.id())) {
            repairTick(tardis);
        }
    }

    public static void resumeCrashAlarms(MinecraftServer server) {
        for (Tardis tardis : TardisApi.all(server)) {
            if (tardis.data().isCrashed(tardis.id())) untilRepaired(tardis);
        }
    }

    private static void repairTick(Tardis tardis) {
        if (!UNTIL_REPAIRED.contains(tardis.id())) return;
        ServerLevel level = tardis.interiorLevel().orElse(null);

        boolean repaired = !tardis.exists() || !tardis.data().isCrashed(tardis.id());
        if (repaired) {
            UNTIL_REPAIRED.remove(tardis.id());
            if (level != null) TardisAlarmPayload.broadcast(level, TardisAlarmPayload.STOP);
            return;
        }

        if (level != null) {
            TardisAlarmPayload.broadcast(level, TardisAlarmPayload.FOREVER);
            bell(level);
        }
        GCScheduler.runLater(tardis.server(), FLASH_TICKS, () -> repairTick(tardis));
    }

    public static void flash(ServerLevel level, BlockPos bellPos, int times) {
        flashWithBell(level, times, () -> bell(level));
    }

    public static void flashLoud(ServerLevel level, int times) {
        flashWithBell(level, times, () -> loudBell(level));
    }

    private static void flashWithBell(ServerLevel level, int times, Runnable ring) {
        TardisAlarmPayload.broadcast(level, times);
        for (int i = 0; i < times; i++) {
            GCScheduler.runLater(level.getServer(), i * FLASH_TICKS, ring);
        }
    }

    public static void untilCalm(Tardis tardis, ServerLevel level, BlockPos bellPos) {
        if (!RUNNING.add(tardis.id())) return;
        TardisAlarmPayload.broadcast(level, TardisAlarmPayload.FOREVER);
        tick(tardis, level, bellPos);
    }

    private static void tick(Tardis tardis, ServerLevel level, BlockPos bellPos) {
        if (!RUNNING.contains(tardis.id())) return;

        boolean calmedDown = !tardis.exists() || TardisMindSystem.stress(tardis) < CALM_STRESS;
        if (calmedDown) {
            RUNNING.remove(tardis.id());
            TardisAlarmPayload.broadcast(level, TardisAlarmPayload.STOP);
            return;
        }

        TardisAlarmPayload.broadcast(level, TardisAlarmPayload.FOREVER);
        bell(level);
        GCScheduler.runLater(level.getServer(), FLASH_TICKS, () -> tick(tardis, level, bellPos));
    }

    private static void loudBell(ServerLevel level) {
        for (int i = 0; i < LOUD_LAYERS; i++) {
            bell(level);
        }
    }

    /* same loudness everywhere inside, and it leaks out through the doors */
    private static void bell(ServerLevel level) {
        for (ServerPlayer player : level.players()) {
            ringAt(player, INSIDE_VOLUME);
        }
        for (ServerPlayer player : playersOutside(level)) {
            ringAt(player, OUTSIDE_VOLUME);
        }
    }

    private static void ringAt(ServerPlayer player, float volume) {
        Holder<SoundEvent> sound = BuiltInRegistries.SOUND_EVENT.wrapAsHolder(GCSounds.TARDIS_CLOISTER_BELL);
        player.connection.send(new ClientboundSoundPacket(sound, SoundSource.BLOCKS,
                player.getX(), player.getEyeY(), player.getZ(), volume, 1.0F, player.getRandom().nextLong()));
    }

    /* players standing near this interior's tardis outside */
    public static List<ServerPlayer> playersOutside(ServerLevel interior) {
        Tardis tardis = TardisApi.ofLevel(interior).orElse(null);
        if (tardis == null) return List.of();
        TardisExteriorBlockEntity exterior = tardis.exterior().orElse(null);
        if (exterior == null || !(exterior.getLevel() instanceof ServerLevel outside)) return List.of();
        Vec3 doors = Vec3.atCenterOf(exterior.getBlockPos());
        List<ServerPlayer> near = new ArrayList<>();
        for (ServerPlayer player : outside.players()) {
            if (player.distanceToSqr(doors) <= LEAK_RANGE * LEAK_RANGE) near.add(player);
        }
        return near;
    }
}
