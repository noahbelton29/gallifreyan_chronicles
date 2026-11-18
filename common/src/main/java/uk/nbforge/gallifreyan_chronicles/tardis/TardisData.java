package uk.nbforge.gallifreyan_chronicles.tardis;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;
import uk.nbforge.gallifreyan_chronicles.tardis.flight.LandingTarget;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.TardisMind;

/** every setter marks the save as changed */
public final class TardisData {
    private final Set<UUID> trusted = new HashSet<>();
    private @Nullable TardisManager manager;
    private TardisIdentity component;
    private @Nullable BlockPos registeredBlockPos;
    private @Nullable BlockPos consoleBlockPos;
    private @Nullable BlockPos exteriorBlockPos;
    private @Nullable BlockPos interiorOrigin;
    private @Nullable Identifier exteriorDimension;
    private @Nullable String interiorId;
    private @Nullable LandingTarget landingTarget;
    private int coordIncrementIndex;
    private boolean coordIncrementNegative;
    private @Nullable TardisMind mind;
    private @Nullable Long flightStart;
    private boolean crashed;
    private @Nullable Long flightDurationOverride;

    public TardisData(TardisIdentity component) {
        this.component = component;
    }

    void attach(TardisManager manager) {
        this.manager = manager;
    }

    private void changed() {
        if (manager != null) manager.setDirty();
    }

    public TardisIdentity component() {
        return component;
    }

    public UUID id() {
        return component.tardisId();
    }

    public void setOwner(UUID ownerId, String ownerName) {
        component = new TardisIdentity(id(), ownerId, ownerName);
        changed();
    }

    public void updateOwnerName(String ownerName) {
        if (!component.ownerName().equals(ownerName)) setOwner(component.ownerId(), ownerName);
    }

    public boolean isTrusted(UUID playerId) {
        return trusted.contains(playerId);
    }

    public Set<UUID> trustedPlayers() {
        return Set.copyOf(trusted);
    }

    /** true only if it actually changed something */
    public boolean setTrusted(UUID playerId, boolean isTrusted) {
        boolean didChange = isTrusted ? trusted.add(playerId) : trusted.remove(playerId);
        if (didChange) changed();
        return didChange;
    }

    public boolean isCrashed() {
        return crashed;
    }

    public void setCrashed(boolean crashed) {
        if (this.crashed == crashed) return;
        this.crashed = crashed;
        changed();
    }

    public @Nullable Long getFlightStart() {
        return flightStart;
    }

    public void setFlightStart(@Nullable Long flightStart) {
        this.flightStart = flightStart;
        changed();
    }

    /**
     * ticks it has to spend in flight before it can land, overrides the normal distance formula. null means use the
     * default
     */
    public @Nullable Long getFlightDurationOverride() {
        return flightDurationOverride;
    }

    public void setFlightDurationOverride(@Nullable Long ticks) {
        this.flightDurationOverride = ticks;
        changed();
    }

    public @Nullable LandingTarget getLandingTarget() {
        return landingTarget;
    }

    public void setLandingTarget(@Nullable LandingTarget landingTarget) {
        this.landingTarget = landingTarget;
        changed();
    }

    /** index into TardisDestinations INCREMENTS, the step the console's x, y and z controls move the destination by */
    public int getCoordIncrementIndex() {
        return coordIncrementIndex;
    }

    public void setCoordIncrementIndex(int index) {
        this.coordIncrementIndex = index;
        changed();
    }

    public boolean isCoordIncrementNegative() {
        return coordIncrementNegative;
    }

    public void setCoordIncrementNegative(boolean negative) {
        this.coordIncrementNegative = negative;
        changed();
    }

    /** rolled and saved the first time its asked for */
    public TardisMind mind() {
        if (mind == null) {
            mind = TardisMind.roll(id());
            changed();
        }
        return mind;
    }

    public @Nullable TardisMind savedMind() {
        return mind;
    }

    public void setMind(TardisMind mind) {
        this.mind = mind;
        changed();
    }

    public @Nullable BlockPos getRegisteredBlockPos() {
        return registeredBlockPos;
    }

    public void setRegisteredBlockPos(@Nullable BlockPos pos) {
        this.registeredBlockPos = pos;
        changed();
    }

    public @Nullable BlockPos getConsoleBlockPos() {
        return consoleBlockPos;
    }

    public void setConsoleBlockPos(@Nullable BlockPos pos) {
        this.consoleBlockPos = pos;
        changed();
    }

    public @Nullable BlockPos getExteriorBlockPos() {
        return exteriorBlockPos;
    }

    public void setExteriorBlockPos(@Nullable BlockPos pos) {
        this.exteriorBlockPos = pos;
        changed();
    }

    public @Nullable BlockPos resolveExteriorBlockPos() {
        return exteriorBlockPos != null ? exteriorBlockPos : registeredBlockPos;
    }

    public ResourceKey<Level> getExteriorDimension() {
        return exteriorDimension == null
                ? Level.OVERWORLD
                : ResourceKey.create(Registries.DIMENSION, exteriorDimension);
    }

    public void setExteriorDimension(@Nullable ResourceKey<Level> dimension) {
        this.exteriorDimension = dimension == null ? null : dimension.identifier();
        changed();
    }

    public @Nullable Identifier getExteriorDimensionId() {
        return exteriorDimension;
    }

    public @Nullable String getInteriorId() {
        return interiorId;
    }

    public void setInteriorId(@Nullable String interiorId) {
        this.interiorId = interiorId;
        changed();
    }

    public @Nullable BlockPos getInteriorOrigin() {
        return interiorOrigin;
    }

    public void setInteriorOrigin(@Nullable BlockPos origin) {
        this.interiorOrigin = origin;
        changed();
    }
}
