package com.noahtnt2009.gallifreyan_chronicles.block;

import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisConsoleBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.client.renderer.TardisConsoleBlockRenderer;
import com.noahtnt2009.gallifreyan_chronicles.client.tardis.TardisConsoleAssets;
import com.noahtnt2009.gallifreyan_chronicles.tardis.console.TardisConsoleRegistry;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class TardisConsoleBlockModel extends GeoModel<TardisConsoleBlockEntity> {
    @Override
    public @NonNull Identifier getModelResource(@NonNull GeoRenderState renderState) {
        return TardisConsoleAssets.model(getConsoleId(renderState));
    }

    @Override
    public @NonNull Identifier getTextureResource(@NonNull GeoRenderState renderState) {
        return TardisConsoleAssets.texture(getConsoleId(renderState));
    }

    @Override
    public @Nullable Identifier getAnimationResource(@NonNull TardisConsoleBlockEntity animatable) {
        return TardisConsoleAssets.animation(animatable.getConsole().id());
    }

    private String getConsoleId(GeoRenderState renderState) {
        if (renderState.hasGeckolibData(TardisConsoleBlockRenderer.CONSOLE_ID)) {
            String id = renderState.getGeckolibData(TardisConsoleBlockRenderer.CONSOLE_ID);
            if (TardisConsoleRegistry.contains(id)) {
                return id;
            }
        }
        return TardisConsoleRegistry.getDefault().id();
    }
}
