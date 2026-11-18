package uk.nbforge.gallifreyan_chronicles.tardis;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Prediction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import uk.nbforge.gallifreyan_chronicles.core.GCDataComponents;
import uk.nbforge.gallifreyan_chronicles.core.GCItems;
import uk.nbforge.gallifreyan_chronicles.core.GCTardisControls;
import uk.nbforge.gallifreyan_chronicles.core.block_entities.InteriorDoorBlockEntity;
import uk.nbforge.gallifreyan_chronicles.core.block_entities.TardisConsoleBlockEntity;
import uk.nbforge.gallifreyan_chronicles.core.block_entities.TardisExteriorBlockEntity;
import uk.nbforge.gallifreyan_chronicles.tardis.control.impl.SpaceTimeThrottleControl;
import uk.nbforge.gallifreyan_chronicles.tardis.ecs.component.DoorState;
import uk.nbforge.gallifreyan_chronicles.tardis.ecs.component.TardisComponent;
import uk.nbforge.gallifreyan_chronicles.tardis.ecs.system.ConsoleSyncSystem;
import uk.nbforge.gallifreyan_chronicles.tardis.ecs.system.DoorSyncSystem;
import uk.nbforge.gallifreyan_chronicles.tardis.ecs.system.TardisInteriorSystem;
import uk.nbforge.gallifreyan_chronicles.tardis.flight.LandingTarget;
import uk.nbforge.gallifreyan_chronicles.tardis.flight.TardisDestinations;
import uk.nbforge.gallifreyan_chronicles.tardis.flight.TardisFlightState;
import uk.nbforge.gallifreyan_chronicles.util.MessageUtil;

public final class Tardis {
    private final MinecraftServer server;
    private final UUID id;

    Tardis(MinecraftServer server, UUID id) {
        this.server = server;
        this.id = id;
    }

    public UUID id() {
        return id;
    }

    public MinecraftServer server() {
        return server;
    }

    /** This TARDIS's saved data, throws if the TARDIS has been deleted. */
    public TardisData data() {
        TardisData data = TardisManager.get(server).getTardis(id);
        if (data == null) throw new IllegalStateException("TARDIS " + id + " no longer exists");
        return data;
    }

    public boolean exists() {
        return TardisManager.get(server).getTardis(id) != null;
    }

    private TardisComponent record() {
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

    /**
     * The exterior block entity, loading its chunk if it has to, empty if there is none.
     */
    public Optional<TardisExteriorBlockEntity> exterior() {
        Optional<BlockPos> pos = exteriorPos();
        Optional<ServerLevel> level = exteriorLevel();
        if (pos.isEmpty() || level.isEmpty()) return Optional.empty();
        level.get().getChunkAt(pos.get());
        return level.get().getBlockEntity(pos.get()) instanceof TardisExteriorBlockEntity exterior
                ? Optional.of(exterior)
                : Optional.empty();
    }

    /**
     * Same as exterior but never loads a chunk, empty if the chunk is not loaded.
     */
    public Optional<TardisExteriorBlockEntity> exteriorIfLoaded() {
        Optional<BlockPos> pos = exteriorPos();
        Optional<ServerLevel> level = exteriorLevel();
        if (pos.isEmpty() || level.isEmpty() || !level.get().hasChunkAt(pos.get())) return Optional.empty();
        return level.get().getBlockEntity(pos.get()) instanceof TardisExteriorBlockEntity exterior
                ? Optional.of(exterior)
                : Optional.empty();
    }

    public Optional<BlockPos> consolePos() {
        return Optional.ofNullable(data().getConsoleBlockPos());
    }

    public List<TardisConsoleBlockEntity> consoles() {
        return ConsoleSyncSystem.consolesOf(id);
    }

    /**
     * The console block entity, a live one first and if there is none it loads the chunk at the saved position.
     */
    public Optional<TardisConsoleBlockEntity> console() {
        TardisConsoleBlockEntity live = ConsoleSyncSystem.firstLiveOf(id);
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
        return Optional.ofNullable(TardisDimensionManager.get(server, id));
    }

    public TardisInterior interior() {
        return TardisInteriorSystem.getInteriorDefinition(server, id);
    }

    public List<InteriorDoorBlockEntity> interiorDoors() {
        return DoorSyncSystem.doorsOf(id);
    }

    public boolean isLocked() {
        return exterior().map(TardisExteriorBlockEntity::isLocked).orElse(false);
    }

    public boolean isTravelling() {
        return exterior().map(TardisExteriorBlockEntity::isTravelling).orElse(false);
    }

    public TardisFlightState flightState() {
        return exterior().map(TardisExteriorBlockEntity::getFlightState).orElse(TardisFlightState.IDLE);
    }

    public DoorState doorState() {
        return exterior().map(TardisExteriorBlockEntity::getDoorState).orElse(DoorState.CLOSED);
    }

    public boolean areDoorsFullyClosed() {
        return exterior().map(TardisExteriorBlockEntity::areDoorsFullyClosed).orElse(true);
    }

    public boolean areDoorsSealedForTravel() {
        return exterior()
                .map(TardisExteriorBlockEntity::areDoorsSealedForTravel)
                .orElse(false);
    }

    public Optional<LandingTarget> landingTarget() {
        return Optional.ofNullable(data().getLandingTarget());
    }

    public Optional<LandingTarget> destination() {
        return Optional.ofNullable(TardisDestinations.current(server, id));
    }

    public boolean getControlState(String controlId) {
        return console().map(console -> console.getControlState(controlId)).orElse(false);
    }

    /**
     * Puts a control on or off on the console, false if there is no console.
     */
    public boolean setControlState(String controlId, boolean on) {
        TardisConsoleBlockEntity console = console().orElse(null);
        if (console == null) return false;
        if (console.getControlState(controlId) != on) console.triggerControl(controlId, on, null);
        return true;
    }

    /**
     * Whether the console has power right now, empty if no console is loaded.
     */
    public Optional<Boolean> livePower() {
        TardisConsoleBlockEntity live = ConsoleSyncSystem.firstLiveOf(id);
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

    /**
     * Opens the doors, false if they are locked or sealed for travel, doors that are already open count as success.
     */
    public boolean openDoors() {
        if (isLocked() || areDoorsSealedForTravel()) return false;
        return !doorState().isClosed() || setControlState(GCTardisControls.DOOR_CONTROL, true);
    }

    public boolean closeDoors() {
        return doorState().isClosed() || setControlState(GCTardisControls.DOOR_CONTROL, false);
    }

    /**
     * Locks the doors, true if already locked and false if the doors are not fully shut.
     */
    public boolean lock() {
        if (isLocked()) return true;
        return areDoorsFullyClosed() && setControlState(GCTardisControls.DOOR_LOCK_CONTROL, true);
    }

    public boolean unlock() {
        return !isLocked() || setControlState(GCTardisControls.DOOR_LOCK_CONTROL, false);
    }

    /**
     * Pulls the throttle to take off, false if there is no power or the throttle is already pulled.
     */
    public boolean takeOff() {
        if (!hasPower() || getControlState(GCTardisControls.SPACE_TIME_THROTTLE)) return false;
        return console().map(SpaceTimeThrottleControl::pull).orElse(false);
    }

    /**
     * Pulls the throttle back to land, false if there is no power or the throttle is not pulled.
     */
    public boolean land() {
        if (!hasPower() || !getControlState(GCTardisControls.SPACE_TIME_THROTTLE)) return false;
        return console().map(SpaceTimeThrottleControl::pull).orElse(false);
    }

    /**
     * Sets where it lands next, false inside a TARDIS interior or outside the build height.
     */
    public boolean setDestination(ServerLevel level, BlockPos pos, float yaw) {
        if (TardisApi.idOfLevel(level).isPresent()) return false;
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

    public boolean canUseControls(Player player) {
        return !isIsomorphicActive() || isOwner(player) || isTrusted(player.getUUID());
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

    /**
     * Gives the key back to the player, false if there is no exterior or the key is not theirs to take.
     */
    public boolean returnKeyTo(Player player) {
        TardisExteriorBlockEntity exterior = exterior().orElse(null);
        if (exterior == null) return false;
        if (!exterior.canRemoveKey(player)) {
            MessageUtil.overlay(player, "not_your_key");
            return false;
        }
        ItemStack returned = exterior.clearHeldKey();
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

    private static void give(Player player, ItemStack stack) {
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false, Prediction.SERVER_ONLY);
        }
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
