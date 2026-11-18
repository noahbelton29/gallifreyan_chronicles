package uk.nbforge.gallifreyan_chronicles.tardis.exterior;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import uk.nbforge.gallifreyan_chronicles.client.GCClientHooks;
import uk.nbforge.gallifreyan_chronicles.core.block.TardisBlock;
import uk.nbforge.gallifreyan_chronicles.core.block_entity.TardisExteriorBlockEntity;
import uk.nbforge.gallifreyan_chronicles.tardis.exterior.effect.TakeoffWind;
import uk.nbforge.gallifreyan_chronicles.tardis.exterior.fade.CloakFade;
import uk.nbforge.gallifreyan_chronicles.tardis.exterior.fade.FadeType;
import uk.nbforge.gallifreyan_chronicles.tardis.flight.ExteriorFlight;
import uk.nbforge.gallifreyan_chronicles.tardis.flight.RematCatch;
import uk.nbforge.gallifreyan_chronicles.tardis.flight.TardisFlightState;
import uk.nbforge.gallifreyan_chronicles.util.FadingValue;
import uk.nbforge.gallifreyan_chronicles.util.GameClockUtil;

public final class ExteriorTravel {
    private static final float KEY_REVEAL_OPACITY = 0.2f;

    private final TardisExteriorBlockEntity exterior;
    private final ExteriorFlight flight = new ExteriorFlight();
    private final CloakFade cloak = new CloakFade();
    private final FadingValue keyReveal = new FadingValue(0.5f, 0.0f);

    public ExteriorTravel(TardisExteriorBlockEntity exterior) {
        this.exterior = exterior;
    }

    public TardisFlightState getFlightState() {
        Level level = exterior.getLevel();
        if (level != null && !level.isClientSide() && flight.expireState(GameClockUtil.now(level))) {
            exterior.setChanged();
        }
        return flight.state();
    }

    public void setFlightState(TardisFlightState state, long durationTicks) {
        Level level = exterior.getLevel();
        if (level == null || level.isClientSide()) return;
        flight.setState(state, GameClockUtil.now(level), durationTicks);
        exterior.setChanged();
    }

    public boolean isTravelling() {
        return getFlightState().isTravelling();
    }

    public void setInFlight(boolean inFlight) {
        Level level = exterior.getLevel();
        long now = level != null ? GameClockUtil.now(level) : 0L;

        this.flight.setInFlight(inFlight, now);

        if (level != null && !level.isClientSide()) {
            BlockState state = exterior.getBlockState();

            if (state.hasProperty(TardisBlock.IN_FLIGHT) && state.getValue(TardisBlock.IN_FLIGHT) != inFlight) {
                level.setBlock(
                        exterior.getBlockPos(), state.setValue(TardisBlock.IN_FLIGHT, inFlight), Block.UPDATE_ALL);
            }
        }

        exterior.setChanged();
    }

    private void setInFlight(boolean inFlight, long atTick) {
        Level level = exterior.getLevel();
        if (level == null || level.isClientSide()) return;
        if (flight.setInFlight(inFlight, atTick)) {
            exterior.sync();
            if (!inFlight) RematCatch.onRemat(exterior, atTick);
        }
    }

    public void vanishInstantly() {
        Level level = exterior.getLevel();
        if (level == null || level.isClientSide()) return;
        flight.vanish(GameClockUtil.now(level));
        BlockState state = exterior.getBlockState();
        if (state.hasProperty(TardisBlock.IN_FLIGHT) && !state.getValue(TardisBlock.IN_FLIGHT)) {
            level.setBlock(exterior.getBlockPos(), state.setValue(TardisBlock.IN_FLIGHT, true), Block.UPDATE_ALL);
        }
        exterior.sync();
    }

    public void scheduleLanding(int delayTicks) {
        Level level = exterior.getLevel();
        if (level == null || level.isClientSide()) return;
        if (flight.scheduleLanding(GameClockUtil.now(level), delayTicks)) exterior.setChanged();
    }

    public void abortTakeoff(int fadeInTicks) {
        Level level = exterior.getLevel();
        if (level == null || level.isClientSide()) return;
        flight.cancelLanding();
        flight.setQuickFadeIn(fadeInTicks);
        setInFlight(false);
        setFlightState(TardisFlightState.LANDING, fadeInTicks);
        exterior.sync();
    }

    public void cancelLanding() {
        if (flight.cancelLanding()) exterior.setChanged();
    }

    public void tickLanding(Level level) {
        long dueTick = flight.consumeDueLanding(GameClockUtil.now(level));
        if (dueTick < 0L) return;
        setInFlight(false, dueTick);
        exterior.setChanged();
    }

    public FadeType getFadeType() {
        return flight.fadeType();
    }

    public void setFadeType(FadeType fadeType) {
        Level level = exterior.getLevel();
        boolean serverSide = level != null && !level.isClientSide();
        long now = serverSide ? GameClockUtil.now(level) : 0L;
        if (flight.setFadeType(fadeType, now, serverSide)) exterior.sync();
    }

    public void refreshFadeType() {
        Level level = exterior.getLevel();
        if (level != null && flight.refreshFadeType(GameClockUtil.now(level))) exterior.sync();
    }

    public boolean fadeFinished(long now) {
        return now - flight.fadeStartTick() >= flight.duration();
    }

    public boolean isCloaked() {
        return cloak.isCloaked();
    }

    public void setCloaked(boolean cloaked) {
        Level level = exterior.getLevel();
        if (level == null || level.isClientSide()) return;
        if (cloak.set(cloaked, GameClockUtil.now(level))) exterior.sync();
    }

    private float cloakOpacity(float partialTick) {
        Level level = exterior.getLevel();
        if (level == null) return cloak.restingOpacity();
        return cloak.opacity(GameClockUtil.now(level), partialTick);
    }

    public boolean isShadowActive() {
        Level level = exterior.getLevel();
        if (level == null) return true;
        return !flight.inFlight() && flight.opacity(GameClockUtil.now(level), 0.0f) >= 1.0f;
    }

    public boolean isFullyInvisible() {
        Level level = exterior.getLevel();
        if (level == null) return false;
        return flight.isFullyInvisible(GameClockUtil.now(level));
    }

    // holding the key shows a cloaked TARDIS a little (client only)
    public float getOpacity(float partialTick) {
        float flight = flightOpacity(partialTick);
        float base = Math.min(flight, cloakOpacity(partialTick));
        Level level = exterior.getLevel();
        if (level == null || !level.isClientSide()) return base;
        float revealed = Math.min(flight, updateKeyReveal() * KEY_REVEAL_OPACITY);
        return Math.max(base, revealed);
    }

    private float updateKeyReveal() {
        boolean target = cloak.isCloaked() && GCClientHooks.isHoldingKeyFor(exterior.getTardisId());
        return keyReveal.update(target ? 1.0f : 0.0f);
    }

    private float flightOpacity(float partialTick) {
        Level level = exterior.getLevel();
        if (level == null) return flight.restingOpacity();
        return flight.opacity(GameClockUtil.now(level), partialTick);
    }

    public void tickTakeoffWind(ServerLevel serverLevel) {
        if (cloak.isCloaked()) return;
        TakeoffWind.tick(
                serverLevel,
                exterior.getBlockPos(),
                flight.inFlight(),
                isFullyInvisible(),
                flight.fadeStartTick(),
                flight.duration());
    }

    public void copyFrom(ExteriorTravel other) {
        flight.copyFrom(other.flight);
        cloak.copyFrom(other.cloak);
    }

    public void save(ValueOutput output) {
        flight.save(output);
        cloak.save(output);
    }

    public void load(ValueInput input) {
        flight.load(input);
        cloak.load(input);
    }
}
