package com.noahtnt2009.gallifreyan_chronicles.tardis.api;

import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.TardisLinkable;
import com.noahtnt2009.gallifreyan_chronicles.tardis.manager.TardisManager;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class TardisApi {
    private static final String INTERIOR_PREFIX = "tardis/";

    private TardisApi() {
    }

    public static Optional<Tardis> get(MinecraftServer server, @Nullable UUID tardisId) {
        if (tardisId == null) return Optional.empty();
        return TardisManager.get(server).get(tardisId).map(record -> new Tardis(server, record.tardisId()));
    }

    public static Optional<Tardis> ofLevel(Level level) {
        if (!(level instanceof ServerLevel serverLevel)) return Optional.empty();
        return idOfLevel(level).flatMap(id -> get(serverLevel.getServer(), id));
    }

    public static <T extends BlockEntity & TardisLinkable> Optional<Tardis> of(T blockEntity) {
        if (!(blockEntity.getLevel() instanceof ServerLevel level)) return Optional.empty();
        UUID id = blockEntity.getLinkedTardisId().orElseGet(() -> idOfLevel(level).orElse(null));
        return get(level.getServer(), id);
    }

    public static <T extends BlockEntity & TardisLinkable> boolean canUseControls(T console, Player player) {
        return of(console).map(tardis -> tardis.canUseControls(player)).orElse(true);
    }

    public static List<Tardis> ownedBy(MinecraftServer server, UUID ownerId) {
        return TardisManager.get(server).getByOwner(ownerId).stream()
                .map(record -> new Tardis(server, record.tardisId()))
                .toList();
    }

    public static List<Tardis> all(MinecraftServer server) {
        return TardisManager.get(server).getAllIds().stream()
                .map(id -> new Tardis(server, id))
                .toList();
    }

    public static Optional<UUID> idOfLevel(Level level) {
        Identifier dimension = level.dimension().identifier();
        if (!dimension.getNamespace().equals(Constants.MOD_ID)) return Optional.empty();
        String path = dimension.getPath();
        if (!path.startsWith(INTERIOR_PREFIX)) return Optional.empty();
        try {
            return Optional.of(UUID.fromString(path.substring(INTERIOR_PREFIX.length())));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
