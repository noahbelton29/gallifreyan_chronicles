package uk.nbforge.gallifreyan_chronicles.sky.data;

import uk.nbforge.gallifreyan_chronicles.core.data.IdentifiableData;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Optional;
import uk.nbforge.gallifreyan_chronicles.sky.shader.ShaderDimension;
import uk.nbforge.gallifreyan_chronicles.sky.shader.ShaderSky;

public record DimensionSky(String id, Identifier dimension, Optional<Integer> skyColor, boolean darkDisc,
                           Optional<SkyStars> stars, List<SkyBody> bodies,
                           Optional<ShaderDimension> shaderDimension, ShaderSky shaderSky,
                           Optional<OverlayFog> overlayFog)
        implements IdentifiableData<DimensionSky> {
    public static final String UNSET_ID = "__unset__";

    @Override
    public DimensionSky withId(String id) {
        return new DimensionSky(id, dimension, skyColor, darkDisc, stars, bodies, shaderDimension, shaderSky, overlayFog);
    }
}
