package uk.nbforge.gallifreyan_chronicles.tardis.control.impl;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;
import uk.nbforge.gallifreyan_chronicles.core.block_entity.TardisConsoleBlockEntity;
import uk.nbforge.gallifreyan_chronicles.core.entity.TardisControlEntity;
import uk.nbforge.gallifreyan_chronicles.tardis.control.BaseControl;
import uk.nbforge.gallifreyan_chronicles.tardis.control.ControlAnimations;
import uk.nbforge.gallifreyan_chronicles.tardis.control.ControlContext;
import uk.nbforge.gallifreyan_chronicles.tardis.flight.TardisFlight;

public class SpaceTimeThrottleControl extends BaseControl {
    public static final ControlAnimations DEFAULT_ANIMATIONS =
            new ControlAnimations("space_time_throttle_on", "space_time_throttle_off", false);

    public SpaceTimeThrottleControl(ControlAnimations animations) {
        super(animations);
    }

    @Override
    protected InteractionResult use(ControlContext ctx, TardisControlEntity entity, @Nullable Player player) {
        boolean pulled = TardisFlight.leverPulled(
                ctx.console(), ctx.level(), entity.getControlId(), entity.blockPosition(), entity.position(), player);
        return pulled ? InteractionResult.SUCCESS : InteractionResult.FAIL;
    }

    // runs on the client too, the rotor animations follow the lever there
    @Override
    public void onStateChanged(TardisConsoleBlockEntity console, boolean activated) {
        TardisFlight.throttleChanged(console, activated);
    }

    @Override
    public Component describeState(boolean activated) {
        return activated
                ? state("throttle", "engaged", ChatFormatting.GREEN)
                : state("throttle", "disengaged", ChatFormatting.RED);
    }
}
