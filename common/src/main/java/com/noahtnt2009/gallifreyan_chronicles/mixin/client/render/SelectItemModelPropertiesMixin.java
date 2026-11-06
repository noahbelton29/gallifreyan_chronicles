package com.noahtnt2009.gallifreyan_chronicles.mixin.client.render;

import com.noahtnt2009.gallifreyan_chronicles.client.model.ConsoleVariantSelectProperty;
import com.noahtnt2009.gallifreyan_chronicles.client.model.TardisExteriorSelectProperty;
import com.noahtnt2009.gallifreyan_chronicles.util.GCUtils;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SelectItemModelProperties.class)
public abstract class SelectItemModelPropertiesMixin {
    @Inject(method = "bootstrap", at = @At("TAIL"))
    private static void gc$registerItemProperties(CallbackInfo ci) {
        SelectItemModelProperties.ID_MAPPER.put(GCUtils.modIdentifier("tardis_exterior"), TardisExteriorSelectProperty.TYPE);
        SelectItemModelProperties.ID_MAPPER.put(GCUtils.modIdentifier("console_variant"), ConsoleVariantSelectProperty.TYPE);
    }
}
