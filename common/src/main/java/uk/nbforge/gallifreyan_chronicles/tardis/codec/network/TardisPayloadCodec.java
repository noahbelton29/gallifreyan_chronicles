package uk.nbforge.gallifreyan_chronicles.tardis.codec.network;

import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import uk.nbforge.gallifreyan_chronicles.network.tardis.RematCatchPayload;
import uk.nbforge.gallifreyan_chronicles.network.tardis.TardisAlarmPayload;
import uk.nbforge.gallifreyan_chronicles.network.tardis.TardisConsoleAnimationPayload;
import uk.nbforge.gallifreyan_chronicles.network.tardis.TardisConsoleRotorStatePayload;
import uk.nbforge.gallifreyan_chronicles.network.tardis.TardisConsoleSyncPayload;
import uk.nbforge.gallifreyan_chronicles.network.tardis.TardisCrashRumblePayload;
import uk.nbforge.gallifreyan_chronicles.network.tardis.TardisDrumShakePayload;
import uk.nbforge.gallifreyan_chronicles.network.tardis.TardisExteriorSyncPayload;
import uk.nbforge.gallifreyan_chronicles.network.tardis.TardisFrightPayload;
import uk.nbforge.gallifreyan_chronicles.network.tardis.TardisInteriorSyncPayload;
import uk.nbforge.gallifreyan_chronicles.tardis.codec.console.TardisConsoleCodec;
import uk.nbforge.gallifreyan_chronicles.tardis.codec.console.TardisConsoleVariantCodec;
import uk.nbforge.gallifreyan_chronicles.tardis.codec.door.TardisDoorVariantCodec;
import uk.nbforge.gallifreyan_chronicles.tardis.codec.exterior.TardisExteriorCodec;
import uk.nbforge.gallifreyan_chronicles.tardis.codec.interior.TardisInteriorOptionCodec;

public final class TardisPayloadCodec {
    public static final StreamCodec<RegistryFriendlyByteBuf, RematCatchPayload> REMAT_CATCH = StreamCodec.of(
            (buf, p) -> {
                UUIDUtil.STREAM_CODEC.encode(buf, p.player());
                ByteBufCodecs.STRING_UTF8.encode(buf, p.fadeType());
                buf.writeVarInt(p.elapsedTicks());
                buf.writeVarInt(p.durationTicks());
                buf.writeBoolean(p.self());
                buf.writeBlockPos(p.boxMin());
                buf.writeBlockPos(p.boxMax());
                buf.writeVarInt(p.floorY());
            },
            buf -> new RematCatchPayload(UUIDUtil.STREAM_CODEC.decode(buf), ByteBufCodecs.STRING_UTF8.decode(buf),
                    buf.readVarInt(), buf.readVarInt(), buf.readBoolean(), buf.readBlockPos(), buf.readBlockPos(),
                    buf.readVarInt())
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, TardisAlarmPayload> TARDIS_ALARM = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            TardisAlarmPayload::flashes,
            Identifier.STREAM_CODEC,
            TardisAlarmPayload::dimension,
            TardisAlarmPayload::new
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, TardisConsoleAnimationPayload> TARDIS_CONSOLE_ANIMATION = StreamCodec.composite(
            Identifier.STREAM_CODEC,
            TardisConsoleAnimationPayload::dimension,
            BlockPos.STREAM_CODEC,
            TardisConsoleAnimationPayload::pos,
            ByteBufCodecs.STRING_UTF8,
            TardisConsoleAnimationPayload::animationName,
            ByteBufCodecs.STRING_UTF8,
            TardisConsoleAnimationPayload::controllerName,
            TardisConsoleAnimationPayload::new
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, TardisConsoleRotorStatePayload> TARDIS_CONSOLE_ROTOR_STATE = StreamCodec.composite(
            Identifier.STREAM_CODEC,
            TardisConsoleRotorStatePayload::dimension,
            BlockPos.STREAM_CODEC,
            TardisConsoleRotorStatePayload::pos,
            ByteBufCodecs.STRING_UTF8,
            TardisConsoleRotorStatePayload::rotorState,
            TardisConsoleRotorStatePayload::new
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, TardisConsoleSyncPayload> TARDIS_CONSOLE_SYNC = StreamCodec.composite(
            TardisConsoleCodec.STREAM_CODEC.apply(ByteBufCodecs.list()),
            TardisConsoleSyncPayload::consoles,
            TardisConsoleVariantCodec.STREAM_CODEC.apply(ByteBufCodecs.list()),
            TardisConsoleSyncPayload::variants,
            ByteBufCodecs.STRING_UTF8,
            TardisConsoleSyncPayload::defaultId,
            TardisDoorVariantCodec.STREAM_CODEC.apply(ByteBufCodecs.list()),
            TardisConsoleSyncPayload::doorVariants,
            TardisConsoleSyncPayload::new
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, TardisCrashRumblePayload> TARDIS_CRASH_RUMBLE = StreamCodec.composite(
            ByteBufCodecs.DOUBLE,
            TardisCrashRumblePayload::x,
            ByteBufCodecs.DOUBLE,
            TardisCrashRumblePayload::y,
            ByteBufCodecs.DOUBLE,
            TardisCrashRumblePayload::z,
            ByteBufCodecs.VAR_INT,
            TardisCrashRumblePayload::ticks,
            ByteBufCodecs.BOOL,
            TardisCrashRumblePayload::stress,
            ByteBufCodecs.FLOAT,
            TardisCrashRumblePayload::strength,
            TardisCrashRumblePayload::new
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, TardisDrumShakePayload> TARDIS_DRUM_SHAKE = StreamCodec.composite(
            ByteBufCodecs.DOUBLE,
            TardisDrumShakePayload::x,
            ByteBufCodecs.DOUBLE,
            TardisDrumShakePayload::y,
            ByteBufCodecs.DOUBLE,
            TardisDrumShakePayload::z,
            ByteBufCodecs.FLOAT,
            TardisDrumShakePayload::strength,
            TardisDrumShakePayload::new
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, TardisExteriorSyncPayload> TARDIS_EXTERIOR_SYNC = StreamCodec.composite(
            TardisExteriorCodec.STREAM_CODEC.apply(ByteBufCodecs.list()),
            TardisExteriorSyncPayload::exteriors,
            ByteBufCodecs.STRING_UTF8,
            TardisExteriorSyncPayload::defaultId,
            TardisExteriorSyncPayload::new
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, TardisFrightPayload> TARDIS_FRIGHT = StreamCodec.composite(
            ByteBufCodecs.FLOAT,
            TardisFrightPayload::severity,
            ByteBufCodecs.VAR_INT,
            TardisFrightPayload::ticks,
            Identifier.STREAM_CODEC,
            TardisFrightPayload::dimension,
            TardisFrightPayload::new
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, TardisInteriorSyncPayload> TARDIS_INTERIOR_SYNC = StreamCodec.composite(
            TardisInteriorOptionCodec.STREAM_CODEC.apply(ByteBufCodecs.list()),
            TardisInteriorSyncPayload::options,
            ByteBufCodecs.STRING_UTF8,
            TardisInteriorSyncPayload::defaultId,
            TardisInteriorSyncPayload::new
    );

    private TardisPayloadCodec() {
    }
}
