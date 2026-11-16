package uk.nbforge.gallifreyan_chronicles.client.model;

import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;
import uk.nbforge.gallifreyan_chronicles.client.renderer.block.InteriorDoorBlockRenderer;
import uk.nbforge.gallifreyan_chronicles.client.renderer.util.DoorRoundelTextures;
import uk.nbforge.gallifreyan_chronicles.client.util.DetailLevelUtil;
import uk.nbforge.gallifreyan_chronicles.core.block_entities.InteriorDoorBlockEntity;
import uk.nbforge.gallifreyan_chronicles.util.GCUtil;

public class InteriorDoorBlockModel extends GeoModel<InteriorDoorBlockEntity> {
    private static final String DEFAULT_VARIANT = "brachacki_doors";

    @Override
    public @NonNull Identifier getModelResource(@NonNull GeoRenderState renderState) {
        return GeoAssets.INTERIOR_DOOR.model(getVariantId(renderState));
    }

    @Override
    public @NonNull Identifier getTextureResource(@NonNull GeoRenderState renderState) {
        return textureFor(renderState);
    }

    /**
     * The texture this door is drawn with, its skin with the texture of the roundel block painted on if it has one.
     * Static so the renderer can ask for it directly when it picks the render type.
     */
    public static Identifier textureFor(GeoRenderState renderState) {
        Identifier base = baseTexture(renderState);
        String roundel = renderState.hasGeckolibData(InteriorDoorBlockRenderer.ROUNDEL)
                ? renderState.getGeckolibData(InteriorDoorBlockRenderer.ROUNDEL)
                : null;
        if (roundel == null || roundel.isEmpty() || !DetailLevelUtil.highDetail()) return base;
        return DoorRoundelTextures.get(GeoAssets.INTERIOR_DOOR.model(getVariantId(renderState)), base, roundel);
    }

    private static Identifier baseTexture(GeoRenderState renderState) {
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

    private static Identifier getVariantId(GeoRenderState renderState) {
        if (renderState.hasGeckolibData(InteriorDoorBlockRenderer.VARIANT)) {
            String id = renderState.getGeckolibData(InteriorDoorBlockRenderer.VARIANT);
            if (id != null) {
                return GCUtil.modIdentifier(id);
            }
        }
        return GCUtil.modIdentifier(DEFAULT_VARIANT);
    }
}
