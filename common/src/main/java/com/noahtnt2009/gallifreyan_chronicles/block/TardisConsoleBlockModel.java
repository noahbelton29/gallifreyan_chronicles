package com.noahtnt2009.gallifreyan_chronicles.block;

import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisConsoleBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.client.renderer.TardisConsoleBlockRenderer;
import com.noahtnt2009.gallifreyan_chronicles.tardis.console.TardisConsole;
import com.noahtnt2009.gallifreyan_chronicles.tardis.console.TardisConsoleRegistry;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class TardisConsoleBlockModel extends GeoModel<TardisConsoleBlockEntity> {
    @Override
    public @NonNull Identifier getModelResource(@NonNull GeoRenderState renderState) {
        return getConsole(renderState).model();
    }

    @Override
    public @NonNull Identifier getTextureResource(@NonNull GeoRenderState renderState) {
        return getConsole(renderState).texture();
    }

    @Override
    public @Nullable Identifier getAnimationResource(@NonNull TardisConsoleBlockEntity animatable) {
        return animatable.getConsole().animation();
    }

    private TardisConsole getConsole(GeoRenderState renderState) {
        if (renderState.hasGeckolibData(TardisConsoleBlockRenderer.CONSOLE_ID)) {
            String id = renderState.getGeckolibData(TardisConsoleBlockRenderer.CONSOLE_ID);
            return TardisConsoleRegistry.get(id);
        }
        return TardisConsoleRegistry.getDefault();
    }
}
