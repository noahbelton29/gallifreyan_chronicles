package uk.nbforge.gallifreyan_chronicles.tardis.exterior;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;
import uk.nbforge.gallifreyan_chronicles.core.block.TardisBlock;
import uk.nbforge.gallifreyan_chronicles.core.block_entity.TardisExteriorBlockEntity;
import uk.nbforge.gallifreyan_chronicles.tardis.Tardis;
import uk.nbforge.gallifreyan_chronicles.tardis.exterior.fade.PulseFadeType;
import uk.nbforge.gallifreyan_chronicles.tardis.flight.TardisFlightState;
import uk.nbforge.gallifreyan_chronicles.util.GameClockUtil;
import uk.nbforge.gallifreyan_chronicles.util.SavedValue;

public final class ExteriorLamp {
    private static final float LAMP_FLICKER_TICKS = 25f;
    private static final long ORB_TAKEOFF_DELAY_TICKS = 40L;

    private final TardisExteriorBlockEntity exterior;
    private final SavedValue<Glow> glow = new SavedValue<>("glow", Glow.CODEC, Glow::fresh);
    private boolean powered = true;
    private @Nullable TardisFlightState orbState;
    private long orbTransitionStart;
    private long orbStart = -1L;
    private float orbEndCycle = -1f;
    private boolean orbDone;

    public ExteriorLamp(TardisExteriorBlockEntity exterior) {
        this.exterior = exterior;
    }

    public boolean isPowered() {
        return powered;
    }

    public boolean isGlowing() {
        return glow.get().glowing();
    }

    public void setGlowing(boolean glowing) {
        glow.set(new Glow(glowing, glowing));
        exterior.sync();
        applyGlowToBlockState(glowing);
        exterior.sync();
    }

    public float lampBrightness(float partialTick) {
        Level level = exterior.getLevel();
        if (!isGlowing()) return 0f;
        if (isSteady(partialTick) || level == null) return 1f;
        float cycles = (level.getGameTime() + partialTick) / LAMP_FLICKER_TICKS;
        if (exterior.travel().getFadeType() instanceof PulseFadeType) {
            return 0.5f + 0.5f * (float) Math.cos(cycles * Math.PI);
        }
        return ((int) cycles) % 2 == 0 ? 1f : 0f;
    }

    private boolean isSteady(float partialTick) {
        ExteriorTravel travel = exterior.travel();
        if (!travel.getFlightState().isTransitioning()) return true;
        float opacity = travel.getOpacity(partialTick);
        boolean gone = travel.getFlightState() == TardisFlightState.TAKING_OFF && opacity <= 0.001f;
        boolean landed = travel.getFlightState() == TardisFlightState.LANDING && opacity >= 0.999f;
        return gone || landed;
    }

    public float lampOrb(float partialTick) {
        Level level = exterior.getLevel();
        ExteriorTravel travel = exterior.travel();
        if (level == null || !isGlowing() || !(travel.getFadeType() instanceof PulseFadeType)) return 0f;
        long now = GameClockUtil.now(level);
        TardisFlightState state = travel.getFlightState();

        if (state.isTransitioning() && state != orbState) {
            orbState = state;
            orbTransitionStart = now;
            orbStart = -1L;
            orbEndCycle = -1f;
            orbDone = false;
        }
        if (orbDone) return 0f;

        if (orbStart < 0L) {
            if (!state.isTransitioning()) return 0f;
            boolean ready = state == TardisFlightState.TAKING_OFF
                    ? now - orbTransitionStart >= ORB_TAKEOFF_DELAY_TICKS
                    : travel.getOpacity(partialTick) > 0.001f;
            if (!ready) return 0f;
            orbStart = now;
        }

        float cycles = (now - orbStart + partialTick) / LAMP_FLICKER_TICKS;
        if (orbEndCycle < 0f && travel.fadeFinished(now)) {
            float extra = orbState == TardisFlightState.TAKING_OFF ? 2f : 0f;
            float end = (float) Math.ceil(cycles + extra);
            if (((int) end) % 2 != 0) end += 1f;
            orbEndCycle = end;
        }
        if (orbEndCycle >= 0f && cycles >= orbEndCycle) {
            orbDone = true;
            return 0f;
        }
        return 0.5f - 0.5f * (float) Math.cos(cycles * Math.PI);
    }

    private void applyGlowToBlockState(boolean glowing) {
        Level level = exterior.getLevel();
        if (level == null || level.isClientSide()) return;
        BlockState current = exterior.getBlockState();
        if (current.getValue(TardisBlock.GLOWING) != glowing) {
            level.setBlock(exterior.getBlockPos(), current.setValue(TardisBlock.GLOWING, glowing), 3);
        }
    }

    public void updateLight(Level level) {
        BlockState current = exterior.getBlockState();
        if (!current.hasProperty(TardisBlock.LIGHT)) return;
        int target = isGlowing()
                ? Math.round(TardisBlock.MAX_LIGHT * exterior.travel().getOpacity(0.0f))
                : 0;
        target = Math.clamp(target, 0, TardisBlock.MAX_LIGHT);
        if (current.getValue(TardisBlock.LIGHT) != target) {
            level.setBlock(exterior.getBlockPos(), current.setValue(TardisBlock.LIGHT, target), 3);
        }
    }

    public void cutPower() {
        Level level = exterior.getLevel();
        if (level == null || level.isClientSide()) return;
        powered = false;
        refreshPowerAndGlow();
        exterior.setChanged();
    }

    private boolean tickGlow(boolean powered) {
        Glow current = glow.get();
        boolean shouldGlow = powered && current.lampOn();
        if (current.glowing() == shouldGlow) return false;
        glow.set(new Glow(shouldGlow, current.lampOn()));
        exterior.sync();
        return true;
    }

    public void refreshPowerAndGlow() {
        boolean wasPowered = powered;
        powered = Tardis.of(exterior).flatMap(Tardis::livePower).orElse(powered);
        if (tickGlow(powered)) {
            applyGlowToBlockState(isGlowing());
            exterior.sync();
        } else if (powered != wasPowered) {
            exterior.sync();
        }
    }

    public void copyFrom(ExteriorLamp other) {
        glow.copyFrom(other.glow);
        powered = other.powered;
    }

    public void save(ValueOutput output) {
        glow.save(output);
        output.putBoolean("Powered", powered);
    }

    public void load(ValueInput input) {
        glow.load(input);
        powered = input.getBooleanOr("Powered", true);
    }

    // lampOn = switched on, glowing = actually lit (needs power too)
    private record Glow(boolean glowing, boolean lampOn) {
        static final Codec<Glow> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                        Codec.BOOL.fieldOf("glowing").forGetter(Glow::glowing),
                        Codec.BOOL.optionalFieldOf("lamp_on", true).forGetter(Glow::lampOn))
                .apply(inst, Glow::new));

        static Glow fresh() {
            return new Glow(false, true);
        }
    }
}
