package com.noahtnt2009.gallifreyan_chronicles.mixin.client.render;

import com.noahtnt2009.gallifreyan_chronicles.client.tardis.RematAlpha;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/* how faded a player is while a tardis materialises around them */
@Mixin(LivingEntityRenderState.class)
public abstract class LivingEntityRenderStateRematMixin implements RematAlpha {
    @Unique
    private float gc$rematAlpha = 1.0f;

    @Override
    public float gc$rematAlpha() {
        return gc$rematAlpha;
    }

    @Override
    public void gc$setRematAlpha(float alpha) {
        gc$rematAlpha = alpha;
    }
}
