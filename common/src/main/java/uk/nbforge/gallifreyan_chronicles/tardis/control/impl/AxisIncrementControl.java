package uk.nbforge.gallifreyan_chronicles.tardis.control.impl;

import com.mojang.serialization.MapCodec;
import java.util.Map;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;
import uk.nbforge.gallifreyan_chronicles.tardis.Tardis;
import uk.nbforge.gallifreyan_chronicles.tardis.control.Control;
import uk.nbforge.gallifreyan_chronicles.tardis.control.ControlAnimations;
import uk.nbforge.gallifreyan_chronicles.tardis.control.TardisControl;
import uk.nbforge.gallifreyan_chronicles.tardis.flight.LandingTarget;
import uk.nbforge.gallifreyan_chronicles.tardis.flight.TardisDestinations;
import uk.nbforge.gallifreyan_chronicles.tardis.flight.TardisDestinations.Axis;

public class AxisIncrementControl extends DestinationControl {
    private static final Map<Axis, MapCodec<AxisIncrementControl>> CODECS =
            Map.of(Axis.X, codecFor(Axis.X), Axis.Y, codecFor(Axis.Y), Axis.Z, codecFor(Axis.Z));

    private final Axis axis;

    public AxisIncrementControl(Axis axis, ControlAnimations animations) {
        super(animations);
        this.axis = axis;
    }

    public static ControlAnimations defaultAnimations(Axis axis) {
        return ControlAnimations.single(axis.name().toLowerCase() + "_increment");
    }

    public static MapCodec<AxisIncrementControl> codec(Axis axis) {
        return CODECS.get(axis);
    }

    private static MapCodec<AxisIncrementControl> codecFor(Axis axis) {
        return Control.codec(defaultAnimations(axis), animations -> new AxisIncrementControl(axis, animations));
    }

    // each axis is its own control type, so the codec comes from the axis rather than the class
    @Override
    public MapCodec<? extends TardisControl> codec() {
        return codec(axis);
    }

    @Override
    protected boolean apply(Tardis tardis, @Nullable Player player) {
        LandingTarget target = TardisDestinations.step(tardis, axis);
        if (target == null) return false;
        TardisDestinations.showDestination(player, axis, target);
        return true;
    }
}
