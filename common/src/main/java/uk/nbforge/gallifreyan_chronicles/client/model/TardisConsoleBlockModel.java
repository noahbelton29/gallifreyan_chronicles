package uk.nbforge.gallifreyan_chronicles.client.model;

import uk.nbforge.gallifreyan_chronicles.client.model.GeoAssets;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import uk.nbforge.gallifreyan_chronicles.core.block_entities.TardisConsoleBlockEntity;
import uk.nbforge.gallifreyan_chronicles.client.renderer.block.TardisConsoleBlockRenderer;
import uk.nbforge.gallifreyan_chronicles.core.dynamic_registries.TardisConsoleRegistry;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

public class TardisConsoleBlockModel extends GeoModel<TardisConsoleBlockEntity> {
    @Override
    public @NonNull Identifier getModelResource(@NonNull GeoRenderState renderState) {
        return GeoAssets.CONSOLE.model(getConsoleId(renderState));
    }

    @Override
    public @NonNull Identifier getTextureResource(@NonNull GeoRenderState renderState) {
        return GeoAssets.CONSOLE.texture(getTextureId(renderState));
    }

    @Override
    public @NonNull Identifier getAnimationResource(TardisConsoleBlockEntity animatable) {
        return GeoAssets.CONSOLE.animation(animatable.getConsole().id());
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

    private String getTextureId(GeoRenderState renderState) {
        if (renderState.hasGeckolibData(TardisConsoleBlockRenderer.CONSOLE_TEXTURE_ID)) {
            String textureId = renderState.getGeckolibData(TardisConsoleBlockRenderer.CONSOLE_TEXTURE_ID);
            if (textureId != null && !textureId.isEmpty()) {
                return textureId;
            }
        }
        return getConsoleId(renderState);
    }
}
