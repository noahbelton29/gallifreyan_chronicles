package uk.nbforge.gallifreyan_chronicles.tardis;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jspecify.annotations.Nullable;
import uk.nbforge.gallifreyan_chronicles.Constants;
import uk.nbforge.gallifreyan_chronicles.tardis.ecs.TardisLinkable;
import uk.nbforge.gallifreyan_chronicles.util.TardisDimensionUtil;

public final class TardisApi {
    // The start of the name of every interior world, what follows it is the TARDIS id
    private static final String INTERIOR_PREFIX = TardisDimensionUtil.INTERIOR_PREFIX;

    public TardisApi() {}

    /**
     * Finds a TARDIS by its id, empty if the id is null or no TARDIS has it.
     */
    public static Optional<Tardis> get(MinecraftServer server, @Nullable UUID tardisId) {
        if (tardisId == null) return Optional.empty();
        return Optional.ofNullable(TardisManager.get(server).getTardis(tardisId))
                .map(data -> new Tardis(server, data.id()));
    }

    /**
     * The TARDIS whose interior world this is, empty on the client and in any world that is not a TARDIS interior.
     */
    public static Optional<Tardis> ofLevel(Level level) {
        if (!(level instanceof ServerLevel serverLevel)) return Optional.empty();
        return idOfLevel(level).flatMap(id -> get(serverLevel.getServer(), id));
    }

    /**
     * The TARDIS a block entity belongs to, it uses the TARDIS the block is linked to and if there is none, the TARDIS whose interior it is standing in.
     * Empty on the client.
     */
    public static <T extends BlockEntity & TardisLinkable> Optional<Tardis> of(T blockEntity) {
        if (!(blockEntity.getLevel() instanceof ServerLevel level)) return Optional.empty();
        UUID id =
                blockEntity.getLinkedTardisId().orElseGet(() -> idOfLevel(level).orElse(null));
        return get(level.getServer(), id);
    }

    /**
     * True when this player is not allowed to use the controls of the TARDIS the console belongs to.
     * A console with no TARDIS never blocks anyone.
     */
    public static <T extends BlockEntity & TardisLinkable> boolean cannotUseControls(T console, Player player) {
        return of(console).map(tardis -> !tardis.canUseControls(player)).orElse(false);
    }

    /**
     * Every TARDIS owned by this player.
     */
    @SuppressWarnings("unused")
    public static List<Tardis> ownedBy(MinecraftServer server, UUID ownerId) {
        return TardisManager.get(server).getByOwner(ownerId).stream()
                .map(data -> new Tardis(server, data.id()))
                .toList();
    }

    /**
     * Every TARDIS that exists on the server.
     */
    public static List<Tardis> all(MinecraftServer server) {
        return TardisManager.get(server).getAllIds().stream()
                .map(id -> new Tardis(server, id))
                .toList();
    }

    /**
     * Gets the TARDIS id out of the name of an interior world.
     * Interior worlds are named after the mod, a prefix and the id, so this returns empty for any other world or a name with a bad id.
     */
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
