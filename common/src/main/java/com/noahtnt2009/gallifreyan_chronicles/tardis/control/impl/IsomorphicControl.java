package com.noahtnt2009.gallifreyan_chronicles.tardis.control.impl;

import com.mojang.serialization.MapCodec;
import com.noahtnt2009.gallifreyan_chronicles.tardis.control.BaseControl;
import com.noahtnt2009.gallifreyan_chronicles.tardis.control.ControlAnimations;
import com.noahtnt2009.gallifreyan_chronicles.tardis.control.TardisControl;

public class IsomorphicControl extends BaseControl {
    public static final ControlAnimations DEFAULT_ANIMATIONS = new ControlAnimations("isomorphic_control_on", "isomorphic_control_off", false);
    public static final MapCodec<IsomorphicControl> CODEC = codec(DEFAULT_ANIMATIONS, IsomorphicControl::new);
    public IsomorphicControl() { this(DEFAULT_ANIMATIONS); }
    public IsomorphicControl(ControlAnimations animations) { super(animations); }

    @Override
    public MapCodec<? extends TardisControl> codec() {
        return CODEC;
    }
}
