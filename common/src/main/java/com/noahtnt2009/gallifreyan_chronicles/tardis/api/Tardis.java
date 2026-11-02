package com.noahtnt2009.gallifreyan_chronicles.tardis.api;

import com.noahtnt2009.gallifreyan_chronicles.init.GCItems;
import com.noahtnt2009.gallifreyan_chronicles.tardis.control.impl.FlightLeverControl;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.Prediction;
import net.minecraft.network.chat.Component;
import com.noahtnt2009.gallifreyan_chronicles.init.GCDataComponents;
import com.noahtnt2009.gallifreyan_chronicles.block.entity.InteriorDoorBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisConsoleBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisExteriorBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.init.GCTardisControls;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.component.DoorState;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.component.TardisComponent;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.system.ConsoleSyncSystem;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.system.DoorSyncSystem;
import com.noahtnt2009.gallifreyan_chronicles.tardis.flight.LandingTarget;
import com.noahtnt2009.gallifreyan_chronicles.tardis.flight.TardisDestinations;
import com.noahtnt2009.gallifreyan_chronicles.tardis.flight.TardisFlightState;
import com.noahtnt2009.gallifreyan_chronicles.tardis.interior.TardisDimensionManager;
import com.noahtnt2009.gallifreyan_chronicles.tardis.interior.TardisInterior;
import com.noahtnt2009.gallifreyan_chronicles.tardis.interior.TardisInteriorSystem;
import com.noahtnt2009.gallifreyan_chronicles.tardis.manager.TardisManager;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

    public TardisManager data() {
        return TardisManager.get(server);
    }

    public boolean exists() {
        return data().get(id).isPresent();
    }

    private TardisComponent record() {
        return data().get(id).orElseThrow(() -> new IllegalStateException("TARDIS " + id + " no longer exists"));
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

    public Optional<String> ownerName() {
        Optional<ServerPlayer> online = ownerPlayer();
        if (online.isPresent()) {
            String name = online.get().getGameProfile().name();
            data().updateOwnerName(id, name);
            return Optional.of(name);
        }
        String saved = record().ownerName();
        return saved.isEmpty() ? Optional.empty() : Optional.of(saved);
    }

    public Optional<BlockPos> exteriorPos() {
        return Optional.ofNullable(data().resolveExteriorBlockPos(id));
    }

    public ResourceKey<Level> exteriorDimension() {
        return data().getExteriorDimension(id);
    }

    public Optional<ServerLevel> exteriorLevel() {
        return Optional.ofNullable(server.getLevel(exteriorDimension()));
    }

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
        if (pos.isEmpty() || level.isEmpty() || !level.get().hasChunkAt(pos.get())) return Optional.empty();
        return level.get().getBlockEntity(pos.get()) instanceof TardisExteriorBlockEntity exterior
                ? Optional.of(exterior)
                : Optional.empty();
    }

    public Optional<BlockPos> consolePos() {
        return Optional.ofNullable(data().getConsoleBlockPos(id));
    }

    public List<TardisConsoleBlockEntity> consoles() {
        return ConsoleSyncSystem.consolesOf(id);
    }

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
        return exterior().map(TardisExteriorBlockEntity::areDoorsSealedForTravel).orElse(false);
    }

    public Optional<LandingTarget> landingTarget() {
        return Optional.ofNullable(data().getLandingTarget(id));
    }

    public Optional<LandingTarget> destination() {
        return Optional.ofNullable(TardisDestinations.current(server, id));
    }

    public boolean getControl(String controlId) {
        return console().map(console -> console.getControlState(controlId)).orElse(false);
    }

    public boolean setControl(String controlId, boolean on) {
        TardisConsoleBlockEntity console = console().orElse(null);
        if (console == null) return false;
        if (console.getControlState(controlId) != on) console.triggerControl(controlId, on, null);
        return true;
    }

    public Optional<Boolean> livePower() {
        TardisConsoleBlockEntity live = ConsoleSyncSystem.firstLiveOf(id);
        return live == null ? Optional.empty() : Optional.of(live.hasPower());
    }

    public boolean hasPower() {
        return console().map(TardisConsoleBlockEntity::hasPower).orElse(true);
    }

    public boolean toggleControl(String controlId) {
        return setControl(controlId, !getControl(controlId));
    }

    public void syncControl(String controlId, boolean on) {
        console().ifPresent(console -> console.reconcileControl(controlId, on));
    }

    public boolean openDoors() {
        if (isLocked() || areDoorsSealedForTravel()) return false;
        return doorState().isClosed() ? setControl(GCTardisControls.DOOR_CONTROL, true) : true;
    }

    public boolean closeDoors() {
        return doorState().isClosed() || setControl(GCTardisControls.DOOR_CONTROL, false);
    }

    public boolean lock() {
        if (isLocked()) return true;
        return areDoorsFullyClosed() && setControl(GCTardisControls.DOOR_LOCK_CONTROL, true);
    }

    public boolean unlock() {
        return !isLocked() || setControl(GCTardisControls.DOOR_LOCK_CONTROL, false);
    }

    public boolean takeOff() {
        if (!hasPower() || getControl(GCTardisControls.FLIGHT_LEVER)) return false;
        return console().map(FlightLeverControl::pull).orElse(false);
    }

    public boolean land() {
        if (!hasPower() || !getControl(GCTardisControls.FLIGHT_LEVER)) return false;
        return console().map(FlightLeverControl::pull).orElse(false);
    }

    public boolean setDestination(ServerLevel level, BlockPos pos, float yaw) {
        if (TardisApi.idOfLevel(level).isPresent()) return false;
        if (level.isOutsideBuildHeight(pos) || level.isOutsideBuildHeight(pos.above())) return false;
        data().setLandingTarget(id, new LandingTarget(level.dimension(), pos, yaw));
        return true;
    }

    public void clearDestination() {
        data().setLandingTarget(id, null);
    }

    public boolean isIsomorphicActive() {
        return console().map(c -> c.getControlState(GCTardisControls.ISOMORPHIC_CONTROL)).orElse(false);
    }

    public boolean canUseControls(Player player) {
        return !isIsomorphicActive() || isOwner(player) || isTrusted(player.getUUID());
    }

    public boolean isTrusted(UUID playerId) {
        return data().isTrusted(id, playerId);
    }

    public boolean trust(UUID playerId) {
        return data().setTrusted(id, playerId, true);
    }

    public boolean untrust(UUID playerId) {
        return data().setTrusted(id, playerId, false);
    }

    public boolean returnKeyTo(Player player) {
        TardisExteriorBlockEntity exterior = exterior().orElse(null);
        if (exterior == null) return false;
        if (!exterior.canRemoveKey(player)) {
            player.sendOverlayMessage(Component.translatable("message.gallifreyan_chronicles.not_your_key"));
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
