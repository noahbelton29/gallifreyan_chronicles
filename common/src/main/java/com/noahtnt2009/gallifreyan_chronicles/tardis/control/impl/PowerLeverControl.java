package com.noahtnt2009.gallifreyan_chronicles.tardis.control.impl;

import com.mojang.serialization.MapCodec;
import com.noahtnt2009.gallifreyan_chronicles.tardis.control.BaseControl;
import com.noahtnt2009.gallifreyan_chronicles.tardis.control.ControlAnimations;

public class PowerLeverControl extends BaseControl {
    public static final ControlAnimations DEFAULT_ANIMATIONS = new ControlAnimations("power_lever_down", "power_lever_up", false);
    public static final MapCodec<PowerLeverControl> CODEC = codec(DEFAULT_ANIMATIONS, PowerLeverControl::new);

    public PowerLeverControl() { this(DEFAULT_ANIMATIONS); }
    public PowerLeverControl(ControlAnimations animations) { super(animations); }

    @Override
    public MapCodec<PowerLeverControl> codec() { return CODEC; }
}
