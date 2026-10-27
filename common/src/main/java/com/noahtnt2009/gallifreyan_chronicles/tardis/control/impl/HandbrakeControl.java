package com.noahtnt2009.gallifreyan_chronicles.tardis.control.impl;

import com.mojang.serialization.MapCodec;
import com.noahtnt2009.gallifreyan_chronicles.tardis.control.BaseControl;
import com.noahtnt2009.gallifreyan_chronicles.tardis.control.ControlAnimations;

public class HandbrakeControl extends BaseControl {
    public static final ControlAnimations DEFAULT_ANIMATIONS = new ControlAnimations("handbrake_down", "handbrake_up", false);
    public static final MapCodec<HandbrakeControl> CODEC = codec(DEFAULT_ANIMATIONS, HandbrakeControl::new);

    public HandbrakeControl() { this(DEFAULT_ANIMATIONS); }
    public HandbrakeControl(ControlAnimations animations) { super(animations); }

    @Override
    public MapCodec<HandbrakeControl> codec() { return CODEC; }
}
