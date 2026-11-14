package uk.nbforge.gallifreyan_chronicles.client.model;

import uk.nbforge.gallifreyan_chronicles.client.model.GeoAssets;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import uk.nbforge.gallifreyan_chronicles.core.block_entities.InteriorDoorBlockEntity;
import uk.nbforge.gallifreyan_chronicles.client.renderer.block.InteriorDoorBlockRenderer;
import uk.nbforge.gallifreyan_chronicles.util.GCUtil;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

public class InteriorDoorBlockModel extends GeoModel<InteriorDoorBlockEntity> {
    private static final String DEFAULT_VARIANT = "brachacki_doors";

    @Override
    public @NonNull Identifier getModelResource(@NonNull GeoRenderState renderState) {
        return GeoAssets.INTERIOR_DOOR.model(getVariantId(renderState));
    }

    @Override
    public @NonNull Identifier getTextureResource(@NonNull GeoRenderState renderState) {
        if (renderState.hasGeckolibData(InteriorDoorBlockRenderer.TEXTURE_ID)) {
            String textureId = renderState.getGeckolibData(InteriorDoorBlockRenderer.TEXTURE_ID);
            if (textureId != null && !textureId.isEmpty()) {
                return GeoAssets.INTERIOR_DOOR.texture(textureId);
            }
        }
        return GeoAssets.INTERIOR_DOOR.texture(getVariantId(renderState));
    }

    @Override
    public @NonNull Identifier getAnimationResource(InteriorDoorBlockEntity animatable) {
        return GeoAssets.INTERIOR_DOOR.animation(GCUtil.modIdentifier(animatable.getVariant()));
    }

    private Identifier getVariantId(GeoRenderState renderState) {
        if (renderState.hasGeckolibData(InteriorDoorBlockRenderer.VARIANT)) {
            String id = renderState.getGeckolibData(InteriorDoorBlockRenderer.VARIANT);
            if (id != null) {
                return GCUtil.modIdentifier(id);
            }
        }
        return GCUtil.modIdentifier(DEFAULT_VARIANT);
    }
}
