package uk.nbforge.gallifreyan_chronicles.tardis;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Prediction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jspecify.annotations.Nullable;
import uk.nbforge.gallifreyan_chronicles.Constants;
import uk.nbforge.gallifreyan_chronicles.core.GCDataComponents;
import uk.nbforge.gallifreyan_chronicles.core.GCItems;
import uk.nbforge.gallifreyan_chronicles.core.GCTardisControls;
import uk.nbforge.gallifreyan_chronicles.core.block_entity.InteriorDoorBlockEntity;
import uk.nbforge.gallifreyan_chronicles.core.block_entity.TardisConsoleBlockEntity;
import uk.nbforge.gallifreyan_chronicles.core.block_entity.TardisExteriorBlockEntity;
import uk.nbforge.gallifreyan_chronicles.tardis.console.ConsoleSync;
import uk.nbforge.gallifreyan_chronicles.tardis.door.DoorState;
import uk.nbforge.gallifreyan_chronicles.tardis.door.DoorSync;
import uk.nbforge.gallifreyan_chronicles.tardis.flight.LandingTarget;
import uk.nbforge.gallifreyan_chronicles.tardis.flight.TardisDestinations;
import uk.nbforge.gallifreyan_chronicles.tardis.flight.TardisFlight;
import uk.nbforge.gallifreyan_chronicles.tardis.flight.TardisFlightState;
import uk.nbforge.gallifreyan_chronicles.tardis.interior.TardisInterior;
import uk.nbforge.gallifreyan_chronicles.tardis.interior.TardisInteriors;
import uk.nbforge.gallifreyan_chronicles.util.MessageUtil;

public final class Tardis {
    private static final String INTERIOR_PREFIX = TardisDimensionUtil.INTERIOR_PREFIX;

    private final MinecraftServer server;
    private final UUID id;

    private Tardis(MinecraftServer server, UUID id) {
        this.server = server;
        this.id = id;
    }

    public static Optional<Tardis> get(MinecraftServer server, @Nullable UUID tardisId) {
        if (tardisId == null || TardisManager.get(server).getTardis(tardisId) == null) return Optional.empty();
        return Optional.of(new Tardis(server, tardisId));
    }

    /** The TARDIS whose interior this is, empty on the client and outside interiors. */
    public static Optional<Tardis> ofLevel(Level level) {
        if (!(level instanceof ServerLevel serverLevel)) return Optional.empty();
        return idOfLevel(level).flatMap(id -> get(serverLevel.getServer(), id));
    }

    /** The linked TARDIS, or the one whose interior the block is in. Empty on the client. */
    public static <T extends BlockEntity & TardisLinkable> Optional<Tardis> of(T blockEntity) {
        if (!(blockEntity.getLevel() instanceof ServerLevel level)) return Optional.empty();
        UUID id =
                blockEntity.getLinkedTardisId().orElseGet(() -> idOfLevel(level).orElse(null));
        return get(level.getServer(), id);
    }

    public static List<Tardis> all(MinecraftServer server) {
        return TardisManager.get(server).getAllIds().stream()
                .map(id -> new Tardis(server, id))
                .toList();
    }

    @SuppressWarnings("unused")
    public static List<Tardis> ownedBy(MinecraftServer server, UUID ownerId) {
        return TardisManager.get(server).getByOwner(ownerId).stream()
                .map(data -> new Tardis(server, data.id()))
                .toList();
    }

    /** A console with no TARDIS never blocks anyone. */
    public static <T extends BlockEntity & TardisLinkable> boolean cannotUseControls(T console, Player player) {
        return of(console).map(tardis -> tardis.cannotUseControls(player)).orElse(false);
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

    private static void give(Player player, ItemStack stack) {
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false, Prediction.SERVER_ONLY);
        }
    }

    public UUID id() {
        return id;
    }

    public MinecraftServer server() {
        return server;
    }

    /** throws if the tardis got deleted */
    public TardisData data() {
        TardisData data = TardisManager.get(server).getTardis(id);
        if (data == null) throw new IllegalStateException("TARDIS " + id + " no longer exists");
        return data;
    }

    public boolean exists() {
        return TardisManager.get(server).getTardis(id) != null;
    }

    private TardisIdentity record() {
        return data().component();
    }

    public UUID ownerId() {
        return record().ownerId();
    }

    public boolean isOwner(UUID playerId) {
        return ownerId().equals(playerId);
    }

    public boolean isOwner(Player player) {
        return isOwner(player.getUUID());
    }

    public Optional<ServerPlayer> ownerPlayer() {
        return Optional.ofNullable(server.getPlayerList().getPlayer(ownerId()));
    }

    /**
     * The name of the owner, live from the player if they are online and saved for later, otherwise the last saved name.
     */
    public Optional<String> ownerName() {
        Optional<ServerPlayer> online = ownerPlayer();
        if (online.isPresent()) {
            String name = online.get().getGameProfile().name();
            data().updateOwnerName(name);
            return Optional.of(name);
        }
        String saved = record().ownerName();
        return saved.isEmpty() ? Optional.empty() : Optional.of(saved);
    }

    public Optional<BlockPos> exteriorPos() {
        return Optional.ofNullable(data().resolveExteriorBlockPos());
    }

    public ResourceKey<Level> exteriorDimension() {
        return data().getExteriorDimension();
    }

    public Optional<ServerLevel> exteriorLevel() {
        return Optional.ofNullable(server.getLevel(exteriorDimension()));
    }

    /** loads the chunk if it has to */
    public Optional<TardisExteriorBlockEntity> exterior() {
        Optional<BlockPos> pos = exteriorPos();
        Optional<ServerLevel> level = exteriorLevel();
        if (pos.isEmpty() || level.isEmpty()) return Optional.empty();
        level.get().getChunkAt(pos.get());
        return level.get().getBlockEntity(pos.get()) instanceof TardisExteriorBlockEntity exterior
                ? Optional.of(exterior)
                : Optional.empty();
    }

    public Optional<TardisExteriorBlockEntity> exteriorIfLoaded() {
        Optional<BlockPos> pos = exteriorPos();
        Optional<ServerLevel> level = exteriorLevel();
        if (pos.isEmpty() || level.isEmpty() || !level.get().isLoaded(pos.get())) return Optional.empty();
        return level.get().getBlockEntity(pos.get()) instanceof TardisExteriorBlockEntity exterior
                ? Optional.of(exterior)
                : Optional.empty();
    }

    public Optional<BlockPos> consolePos() {
        return Optional.ofNullable(data().getConsoleBlockPos());
    }

    public List<TardisConsoleBlockEntity> consoles() {
        return ConsoleSync.consolesOf(id);
    }

    /**
     * The console block entity, a live one first and if there is none it loads the chunk at the saved position.
     */
    public Optional<TardisConsoleBlockEntity> console() {
        TardisConsoleBlockEntity live = ConsoleSync.firstLiveOf(id);
        if (live != null) return Optional.of(live);

        Optional<ServerLevel> interior = interiorLevel();
        Optional<BlockPos> pos = consolePos();
        if (interior.isEmpty() || pos.isEmpty()) return Optional.empty();
        interior.get().getChunkAt(pos.get());
        return interior.get().getBlockEntity(pos.get()) instanceof TardisConsoleBlockEntity console
                ? Optional.of(console)
                : Optional.empty();
    }

    public Optional<ServerLevel> interiorLevel() {
        return Optional.ofNullable(TardisDimensionManager.get(this));
    }

    public TardisInterior interior() {
        return TardisInteriors.getInteriorDefinition(this);
    }

    public List<InteriorDoorBlockEntity> interiorDoors() {
        return DoorSync.doorsOf(id);
    }

    public Optional<InteriorDoorBlockEntity> primaryInteriorDoor() {
        return Optional.ofNullable(DoorSync.primaryDoor(id));
    }

    public boolean isLocked() {
        return exterior().map(exterior -> exterior.lock().isLocked()).orElse(false);
    }

    public boolean isTravelling() {
        return exterior().map(exterior -> exterior.travel().isTravelling()).orElse(false);
    }

    public TardisFlightState flightState() {
        return exterior().map(exterior -> exterior.travel().getFlightState()).orElse(TardisFlightState.IDLE);
    }

    public DoorState doorState() {
        return exterior().map(exterior -> exterior.doors().getState()).orElse(DoorState.CLOSED);
    }

    public boolean areDoorsFullyClosed() {
        return exterior().map(exterior -> exterior.doors().areFullyClosed()).orElse(true);
    }

    public boolean areDoorsSealedForTravel() {
        return exterior().map(exterior -> exterior.doors().areSealedForTravel()).orElse(false);
    }

    public Optional<LandingTarget> landingTarget() {
        return Optional.ofNullable(data().getLandingTarget());
    }

    public Optional<LandingTarget> destination() {
        return Optional.ofNullable(TardisDestinations.current(this));
    }

    public boolean getControlState(String controlId) {
        return console().map(console -> console.getControlState(controlId)).orElse(false);
    }

    public boolean setControlState(String controlId, boolean on) {
        TardisConsoleBlockEntity console = console().orElse(null);
        if (console == null) return false;
        if (console.getControlState(controlId) != on) console.triggerControl(controlId, on, null);
        return true;
    }

    public Optional<Boolean> livePower() {
        TardisConsoleBlockEntity live = ConsoleSync.firstLiveOf(id);
        return live == null ? Optional.empty() : Optional.of(live.hasPower());
    }

    public boolean hasPower() {
        return console().map(TardisConsoleBlockEntity::hasPower).orElse(false);
    }

    public boolean toggleControl(String controlId) {
        return setControlState(controlId, !getControlState(controlId));
    }

    public void syncControl(String controlId, boolean on) {
        console().ifPresent(console -> console.reconcileControl(controlId, on));
    }

    /** false if locked or sealed for travel, already open counts as success */
    public boolean openDoors() {
        if (isLocked() || areDoorsSealedForTravel()) return false;
        return !doorState().isClosed() || setControlState(GCTardisControls.DOOR_CONTROL, true);
    }

    public boolean closeDoors() {
        return doorState().isClosed() || setControlState(GCTardisControls.DOOR_CONTROL, false);
    }

    /** true if already locked, false if the doors arent fully shut */
    public boolean lock() {
        if (isLocked()) return true;
        return areDoorsFullyClosed() && setControlState(GCTardisControls.DOOR_LOCK_CONTROL, true);
    }

    public boolean unlock() {
        return !isLocked() || setControlState(GCTardisControls.DOOR_LOCK_CONTROL, false);
    }

    /** false if theres no power or the throttle is already pulled */
    public boolean takeOff() {
        if (!hasPower() || getControlState(GCTardisControls.SPACE_TIME_THROTTLE)) return false;
        return console().map(TardisFlight::pullThrottle).orElse(false);
    }

    /** false if theres no power or the throttle isnt pulled */
    public boolean land() {
        if (!hasPower() || !getControlState(GCTardisControls.SPACE_TIME_THROTTLE)) return false;
        return console().map(TardisFlight::pullThrottle).orElse(false);
    }

    /** false inside a tardis interior or outside build height */
    public boolean setDestination(ServerLevel level, BlockPos pos, float yaw) {
        if (Tardis.idOfLevel(level).isPresent()) return false;
        if (level.isOutsideBuildHeight(pos) || level.isOutsideBuildHeight(pos.above())) return false;
        data().setLandingTarget(new LandingTarget(level.dimension(), pos, yaw));
        return true;
    }

    public void clearDestination() {
        data().setLandingTarget(null);
    }

    public boolean isIsomorphicActive() {
        return console()
                .map(c -> c.getControlState(GCTardisControls.ISOMORPHIC_CONTROL))
                .orElse(false);
    }

    public boolean cannotUseControls(Player player) {
        return isIsomorphicActive() && !isOwner(player) && !isTrusted(player.getUUID());
    }

    public boolean isTrusted(UUID playerId) {
        return data().isTrusted(playerId);
    }

    public boolean trust(UUID playerId) {
        return data().setTrusted(playerId, true);
    }

    public boolean untrust(UUID playerId) {
        return data().setTrusted(playerId, false);
    }

    /** false if theres no exterior or the key isnt theirs to take */
    public boolean returnKeyTo(Player player) {
        TardisExteriorBlockEntity exterior = exterior().orElse(null);
        if (exterior == null) return false;
        if (!exterior.lock().canRemoveKey(player)) {
            MessageUtil.overlay(player, "not_your_key");
            return false;
        }
        ItemStack returned = exterior.lock().clearHeldKey();
        returned.set(GCDataComponents.KEY_INSERTED, false);
        give(player, returned);
        return true;
    }

    public ItemStack createKey() {
        ItemStack key = new ItemStack(GCItems.TARDIS_KEY);
        key.set(GCDataComponents.TARDIS_ID, id);
        key.set(GCDataComponents.KEY_INSERTED, false);
        return key;
    }

    public void giveKeyTo(Player player) {
        give(player, createKey());
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Tardis tardis && tardis.id.equals(id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Tardis[" + id + "]";
    }
}
