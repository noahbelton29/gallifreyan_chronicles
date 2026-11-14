package uk.nbforge.gallifreyan_chronicles.network.tardis;

import uk.nbforge.gallifreyan_chronicles.client.gui.hud.GCDangerAlarm;
import uk.nbforge.gallifreyan_chronicles.platform.Services;
import uk.nbforge.gallifreyan_chronicles.util.GCUtil;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.mood.TardisAlarm;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.jspecify.annotations.NonNull;

public record TardisAlarmPayload(int flashes, Identifier dimension) implements CustomPacketPayload {
    public static final int STOP = 0;
    public static final int FOREVER = -1;

    public static final Type<TardisAlarmPayload> TYPE =
            new Type<>(GCUtil.modIdentifier("tardis_alarm"));

    /* everyone inside, plus anyone outside near the doors so the portal glows red */
    public static void broadcast(ServerLevel level, int flashes) {
        TardisAlarmPayload payload = new TardisAlarmPayload(flashes, level.dimension().identifier());
        for (ServerPlayer player : level.players()) {
            Services.NETWORK.sendTardisAlarm(player, payload);
        }
        for (ServerPlayer player : TardisAlarm.playersOutside(level)) {
            Services.NETWORK.sendTardisAlarm(player, payload);
        }
    }

    public static void sendTo(ServerPlayer player, int flashes) {
        Services.NETWORK.sendTardisAlarm(player, new TardisAlarmPayload(flashes, player.level().dimension().identifier()));
    }

    public void apply() {
        GCDangerAlarm.start(flashes, ResourceKey.create(Registries.DIMENSION, dimension));
    }

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
