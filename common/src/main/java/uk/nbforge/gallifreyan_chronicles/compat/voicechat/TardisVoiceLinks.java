package uk.nbforge.gallifreyan_chronicles.compat.voicechat;

import uk.nbforge.gallifreyan_chronicles.core.block_entities.InteriorDoorBlockEntity;
import uk.nbforge.gallifreyan_chronicles.core.block_entities.TardisExteriorBlockEntity;
import uk.nbforge.gallifreyan_chronicles.tardis.Tardis;
import uk.nbforge.gallifreyan_chronicles.tardis.TardisApi;
import uk.nbforge.gallifreyan_chronicles.tardis.ecs.component.DoorState;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class TardisVoiceLinks {
    private static final int REFRESH_TICKS = 5;

    public record Link(UUID tardisId,
                       ResourceKey<Level> exteriorDimension, double exteriorX, double exteriorY, double exteriorZ,
                       ResourceKey<Level> interiorDimension, double doorX, double doorY, double doorZ,
                       DoorState doorState) {
    }

    public record PlayerPos(UUID id, ResourceKey<Level> dimension, double x, double y, double z) {
    }

    private static volatile boolean active;
    private static volatile List<Link> links = List.of();
    private static volatile List<PlayerPos> players = List.of();

    private TardisVoiceLinks() {
    }

    public static void setActive(boolean value) {
        active = value;
        if (!value) {
            links = List.of();
            players = List.of();
        }
    }

    public static List<Link> links() {
        return links;
    }

    public static List<PlayerPos> players() {
        return players;
    }

    public static void tick(MinecraftServer server) {
        if (!active || server.getTickCount() % REFRESH_TICKS != 0) return;

        List<PlayerPos> playerSnapshot = new ArrayList<>();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            playerSnapshot.add(new PlayerPos(player.getUUID(), player.level().dimension(),
                    player.getX(), player.getY(), player.getZ()));
        }
        players = List.copyOf(playerSnapshot);

        List<Link> linkSnapshot = new ArrayList<>();
        for (Tardis tardis : TardisApi.all(server)) {
            Optional<TardisExteriorBlockEntity> exterior = tardis.exteriorIfLoaded();
            if (exterior.isEmpty()) continue;
            InteriorDoorBlockEntity door = pickDoor(tardis.interiorDoors());
            if (door == null || !(door.getLevel() instanceof ServerLevel interiorLevel)) continue;

            TardisExteriorBlockEntity ext = exterior.get();
            BlockPos exteriorPos = ext.getBlockPos();
            BlockPos doorPos = door.getBlockPos();
            linkSnapshot.add(new Link(tardis.id(),
                    ext.getLevel().dimension(), exteriorPos.getX() + 0.5, exteriorPos.getY() + 1.0, exteriorPos.getZ() + 0.5,
                    interiorLevel.dimension(), doorPos.getX() + 0.5, doorPos.getY() + 1.0, doorPos.getZ() + 0.5,
                    ext.getDoorState()));
        }
        links = List.copyOf(linkSnapshot);
    }

    private static InteriorDoorBlockEntity pickDoor(List<InteriorDoorBlockEntity> doors) {
        InteriorDoorBlockEntity first = null;
        for (InteriorDoorBlockEntity door : doors) {
            if (door.isPrimary()) continue;
            if (first == null) first = door;
        }
        return first != null ? first : (doors.isEmpty() ? null : doors.get(0));
    }
}
