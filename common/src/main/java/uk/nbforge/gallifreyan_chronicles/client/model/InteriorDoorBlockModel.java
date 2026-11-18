package uk.nbforge.gallifreyan_chronicles.client.model;

import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;
import uk.nbforge.gallifreyan_chronicles.client.renderer.block.InteriorDoorBlockRenderer;
import uk.nbforge.gallifreyan_chronicles.client.renderer.util.DoorRoundelTextures;
import uk.nbforge.gallifreyan_chronicles.client.util.DetailLevelUtil;
import uk.nbforge.gallifreyan_chronicles.core.block_entity.InteriorDoorBlockEntity;
import uk.nbforge.gallifreyan_chronicles.util.GCUtil;

public class InteriorDoorBlockModel extends GeoModel<InteriorDoorBlockEntity> {
    private static final String DEFAULT_VARIANT = "brachacki_doors";

    @Override
    public @NonNull Identifier getModelResource(@NonNull GeoRenderState renderState) {
        return getVariantId(renderState);
    }

    @Override
    public @NonNull Identifier getTextureResource(@NonNull GeoRenderState renderState) {
        return textureFor(renderState);
    }

    /**
     * the door skin with the roundel block texture painted on if it has one. static so the renderer can grab it
     * directly when it picks the render type
     */
    public static Identifier textureFor(GeoRenderState renderState) {
        Identifier base = baseTexture(renderState);
        String roundel = GeoAssets.read(renderState, InteriorDoorBlockRenderer.ROUNDEL);
        if (roundel == null || DetailLevelUtil.lowDetail()) return base;
        return DoorRoundelTextures.get(getVariantId(renderState), base, roundel);
    }

    private static Identifier baseTexture(GeoRenderState renderState) {
        String textureId = GeoAssets.read(renderState, InteriorDoorBlockRenderer.TEXTURE_ID);
        return textureId != null ? GeoAssets.texture(textureId) : GeoAssets.texture(getVariantId(renderState));
    }

    @Override
    public @NonNull Identifier getAnimationResource(InteriorDoorBlockEntity animatable) {
        return GeoAssets.INTERIOR_DOOR.animation(GCUtil.modIdentifier(animatable.getVariant()));
    }

    private static Identifier getVariantId(GeoRenderState renderState) {
        String id = GeoAssets.read(renderState, InteriorDoorBlockRenderer.VARIANT);
        return GCUtil.modIdentifier(id != null ? id : DEFAULT_VARIANT);
    }
}
