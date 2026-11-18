package uk.nbforge.gallifreyan_chronicles.client.model;

import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;
import uk.nbforge.gallifreyan_chronicles.client.renderer.block.TardisConsoleBlockRenderer;
import uk.nbforge.gallifreyan_chronicles.core.block_entity.TardisConsoleBlockEntity;
import uk.nbforge.gallifreyan_chronicles.core.data_registry.TardisConsoleRegistry;

public class TardisConsoleBlockModel extends GeoModel<TardisConsoleBlockEntity> {
    @Override
    public @NonNull Identifier getModelResource(@NonNull GeoRenderState renderState) {
        return GeoAssets.model(consoleId(renderState));
    }

    @Override
    public @NonNull Identifier getTextureResource(@NonNull GeoRenderState renderState) {
        String texture = GeoAssets.read(renderState, TardisConsoleBlockRenderer.CONSOLE_TEXTURE_ID);
        return GeoAssets.texture(texture != null ? texture : consoleId(renderState));
    }

    @Override
    public @NonNull Identifier getAnimationResource(TardisConsoleBlockEntity animatable) {
        return GeoAssets.CONSOLE.animation(animatable.getConsole().id());
    }

    private static String consoleId(GeoRenderState renderState) {
        String id = GeoAssets.read(renderState, TardisConsoleBlockRenderer.CONSOLE_ID);
        return id != null && TardisConsoleRegistry.contains(id)
                ? id
                : TardisConsoleRegistry.getDefault().id();
    }
}
