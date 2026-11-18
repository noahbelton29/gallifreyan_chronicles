package uk.nbforge.gallifreyan_chronicles.client.sky;

import com.mojang.blaze3d.platform.NativeImage;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;
import uk.nbforge.gallifreyan_chronicles.Constants;
import uk.nbforge.gallifreyan_chronicles.util.GCUtil;

public final class SkyGlowTextures {
    private static final Map<Identifier, Identifier> CONVERTED = new HashMap<>();
    private static final Set<Identifier> FAILED = new HashSet<>();

    private SkyGlowTextures() {}

    public static AbstractTexture get(Identifier source) {
        Minecraft minecraft = Minecraft.getInstance();
        TextureManager textures = minecraft.getTextureManager();
        if (FAILED.contains(source)) return textures.getTexture(source);

        Identifier converted = CONVERTED.get(source);
        if (converted == null) {
            converted = GCUtil.modIdentifier("sky_glow/" + source.getNamespace() + "/" + source.getPath());
            try (InputStream in = minecraft.getResourceManager().open(source)) {
                NativeImage image = NativeImage.read(in);
                Identifier label = converted;
                try {
                    convert(image);
                    textures.register(converted, new DynamicTexture(() -> "GC sky glow " + label, image));
                } catch (RuntimeException e) {
                    image.close();
                    throw e;
                }
                CONVERTED.put(source, converted);
            } catch (Exception e) {
                Constants.LOG.warn("Could not prepare glowing sky texture {}", source, e);
                FAILED.add(source);
                return textures.getTexture(source);
            }
        }
        return textures.getTexture(converted);
    }

    /** Turns the image into a glow mask, the brightest colour channel becomes the alpha and the colour is scaled back up to full strength. */
    private static void convert(NativeImage image) {
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int argb = image.getPixel(x, y);
                float alpha = ((argb >>> 24) & 0xFF) / 255.0f;
                float red = ((argb >> 16) & 0xFF) / 255.0f * alpha;
                float green = ((argb >> 8) & 0xFF) / 255.0f * alpha;
                float blue = (argb & 0xFF) / 255.0f * alpha;
                float light = Math.max(red, Math.max(green, blue));
                if (light <= 0.0f) {
                    image.setPixel(x, y, 0);
                    continue;
                }
                int a = Math.round(light * 255.0f);
                int r = Math.round(red / light * 255.0f);
                int g = Math.round(green / light * 255.0f);
                int b = Math.round(blue / light * 255.0f);
                image.setPixel(x, y, (a << 24) | (r << 16) | (g << 8) | b);
            }
        }
    }

    public static void clear() {
        TextureManager textures = Minecraft.getInstance().getTextureManager();
        CONVERTED.values().forEach(textures::release);
        CONVERTED.clear();
        FAILED.clear();
    }
}
