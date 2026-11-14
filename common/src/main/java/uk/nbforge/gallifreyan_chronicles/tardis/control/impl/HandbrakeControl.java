package uk.nbforge.gallifreyan_chronicles.tardis.control.impl;

import uk.nbforge.gallifreyan_chronicles.core.block_entities.TardisConsoleBlockEntity;
import uk.nbforge.gallifreyan_chronicles.tardis.control.BaseControl;
import uk.nbforge.gallifreyan_chronicles.tardis.control.ControlAnimations;

public class HandbrakeControl extends BaseControl {
    public static final ControlAnimations DEFAULT_ANIMATIONS = new ControlAnimations("handbrake_down", "handbrake_up", false);

    public HandbrakeControl() {
        this(DEFAULT_ANIMATIONS);
    }

    public HandbrakeControl(ControlAnimations animations) {
        super(animations);
    }

    @Override
    public void onStateChanged(TardisConsoleBlockEntity console, boolean activated) {
        if (activated && !FlightLeverControl.abortTakeoff(console)) FlightLeverControl.crashLand(console);
    }
}
