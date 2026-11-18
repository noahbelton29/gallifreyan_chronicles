package uk.nbforge.gallifreyan_chronicles.tardis.control.impl;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import uk.nbforge.gallifreyan_chronicles.core.block_entity.TardisConsoleBlockEntity;
import uk.nbforge.gallifreyan_chronicles.tardis.control.BaseControl;
import uk.nbforge.gallifreyan_chronicles.tardis.control.ControlAnimations;
import uk.nbforge.gallifreyan_chronicles.tardis.flight.TardisFlight;

public class TimeRotorHandbrakeControl extends BaseControl {
    public static final ControlAnimations DEFAULT_ANIMATIONS =
            new ControlAnimations("time_rotor_handbrake_on", "time_rotor_handbrake_off", false);

    public TimeRotorHandbrakeControl(ControlAnimations animations) {
        super(animations);
    }

    @Override
    public void onStateChanged(TardisConsoleBlockEntity console, boolean activated) {
        if (!activated || TardisFlight.abortTakeoff(console)) return;
        TardisFlight.crashLand(console);
    }

    @Override
    public Component describeState(boolean activated) {
        return activated
                ? state("handbrake", "engaged", ChatFormatting.GREEN)
                : state("handbrake", "released", ChatFormatting.RED);
    }
}
