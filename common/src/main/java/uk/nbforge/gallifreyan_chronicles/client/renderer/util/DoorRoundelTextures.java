package uk.nbforge.gallifreyan_chronicles.client.renderer.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import uk.nbforge.gallifreyan_chronicles.Constants;
import uk.nbforge.gallifreyan_chronicles.core.block.DoorRoundelInteraction;
import uk.nbforge.gallifreyan_chronicles.util.GCUtil;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public final class DoorRoundelTextures {
    private static final int BLOCK_PIXELS = 16;
    private static final int MIN_LEAF_WIDTH = 5;
    private static final int MIN_LEAF_HEIGHT = 20;

    private static final Map<String, Identifier> CACHE = new HashMap<>();
    private static final Map<String, Long> LAST_FAILURE = new HashMap<>();
    private static final long RETRY_MS = 5000L;

    private DoorRoundelTextures() {
    }

    /**
     * the texture to draw this door with: base with the roundel painted on, or base itself if it can't be built
     */
    public static Identifier get(Identifier model, Identifier baseTexture, String roundelBlockId) {
        String key = model + "|" + baseTexture + "|" + roundelBlockId;
        Identifier cached = CACHE.get(key);
        if (cached != null) return cached;

        /* a failed build isn't remembered for good, so fixing the cause needs no restart, but it isn't retried
           every frame either */
        long now = System.currentTimeMillis();
        Long lastFailure = LAST_FAILURE.get(key);
        if (lastFailure != null && now - lastFailure < RETRY_MS) return baseTexture;

        Identifier built = build(model, baseTexture, roundelBlockId, key);
        if (built.equals(baseTexture)) {
            LAST_FAILURE.put(key, now);
            return built;
        }
        CACHE.put(key, built);
        return built;
    }

    private record Leaf(int width, int height, int depth, int u, int v) {
    }

    private static Identifier build(Identifier model, Identifier baseTexture, String roundelBlockId, String key) {
        ResourceManager resources = Minecraft.getInstance().getResourceManager();
        Identifier block = Identifier.tryParse(roundelBlockId);
        if (block == null) return baseTexture;
        Identifier geo = model.withPrefix("geckolib/models/").withSuffix(".json");

        Identifier roundelId = block.withPrefix("textures/block/").withSuffix(".png");
        Identifier wallId = wallTexture(resources, block);

        try (InputStream baseIn = resources.open(baseTexture);
             InputStream roundelIn = resources.open(roundelId);
             InputStream geoIn = resources.open(geo);
             NativeImage roundel = NativeImage.read(roundelIn)) {
            NativeImage image = NativeImage.read(baseIn);
            List<Leaf> leaves = leaves(JsonParser.parseReader(new InputStreamReader(geoIn, StandardCharsets.UTF_8)).getAsJsonObject());
            if (leaves.isEmpty()) {
                Constants.LOG.warn("No door leaves found in model {}, so {} can't be painted on it", model, roundelBlockId);
                image.close();
                return baseTexture;
            }

            NativeImage wall = null;
            if (wallId != null) {
                try (InputStream wallIn = resources.open(wallId)) {
                    wall = NativeImage.read(wallIn);
                } catch (Exception e) {
                    Constants.LOG.warn("Could not read wall block texture {} for door style {}", wallId, roundelBlockId, e);
                }
            }

            paint(image, roundel, wall, leaves);
            if (wall != null) wall.close();
            Constants.LOG.info("Built door texture for {} (wall block {}) on {}, {} leaves painted", roundelBlockId,
                    wallId, baseTexture, leaves.size());
            dump(image, block);

            Identifier id = GCUtil.modIdentifier("dynamic/door_roundel/"
                    + Integer.toHexString(key.hashCode()) + "_" + sanitize(block.getPath()));
            Minecraft.getInstance().getTextureManager().register(id, new DynamicTexture(() -> "GC door roundel " + id, image));
            return id;
        } catch (Exception e) {
            Constants.LOG.warn("Could not build a door texture from {} on {}", roundelBlockId, baseTexture, e);
            return baseTexture;
        }
    }

    /* the plain block this roundel block is the roundel version of: brachacki_roundel_block_4 -> brachacki_wall_block_4,
       oak_half_roundel_block -> oak planks, gray_concrete_roundel_block -> gray concrete */
    private static Identifier wallTexture(ResourceManager resources, Identifier roundelBlock) {
        for (Identifier candidate : DoorRoundelInteraction.wallBlockCandidates(roundelBlock)) {
            Identifier texture = candidate.withPrefix("textures/block/").withSuffix(".png");
            if (resources.getResource(texture).isPresent()) return texture;
        }
        return null;
    }

    /* the big, plain cubes: the door leaves. a half turn about the vertical axis doesn't change which faces they are */
    private static List<Leaf> leaves(JsonObject geo) {
        List<Leaf> result = new ArrayList<>();
        JsonArray geometries = geo.getAsJsonArray("minecraft:geometry");
        if (geometries == null || geometries.isEmpty()) return result;
        JsonArray bones = geometries.get(0).getAsJsonObject().getAsJsonArray("bones");
        if (bones == null) return result;

        for (JsonElement boneElement : bones) {
            JsonArray cubes = boneElement.getAsJsonObject().getAsJsonArray("cubes");
            if (cubes == null) continue;
            for (JsonElement cubeElement : cubes) {
                JsonObject cube = cubeElement.getAsJsonObject();
                if (!cube.has("uv") || !cube.get("uv").isJsonArray()) continue;
                if (cube.has("rotation")) {
                    JsonArray rotation = cube.getAsJsonArray("rotation");
                    boolean flat = Math.abs(rotation.get(0).getAsFloat()) < 0.01F && Math.abs(rotation.get(2).getAsFloat()) < 0.01F;
                    float yaw = Math.abs(rotation.get(1).getAsFloat()) % 360F;
                    if (!flat || (yaw > 0.01F && Math.abs(yaw - 180F) > 0.01F)) continue;
                }
                JsonArray size = cube.getAsJsonArray("size");
                JsonArray uv = cube.getAsJsonArray("uv");
                int width = Math.round(size.get(0).getAsFloat());
                int height = Math.round(size.get(1).getAsFloat());
                int depth = Math.round(size.get(2).getAsFloat());
                if (width < MIN_LEAF_WIDTH || height < MIN_LEAF_HEIGHT) continue;
                result.add(new Leaf(width, height, depth, uv.get(0).getAsInt(), uv.get(1).getAsInt()));
            }
        }
        return result;
    }

    /* box uv layout, left to right on the row below the top and bottom faces:
       west (d wide), north (w wide), east (d wide), south (w wide), each h tall. top and bottom (w wide, d tall) sit
       on the row above, at the north and east columns. */
    private static void paint(NativeImage image, NativeImage roundel, NativeImage wall, List<Leaf> leaves) {
        for (Leaf leaf : leaves) {
            int w = leaf.width();
            int h = leaf.height();
            int d = leaf.depth();
            int u = leaf.u();
            int v = leaf.v();

            fill(image, roundel, u + d, v + d, w, h);
            fill(image, roundel, u + 2 * d + w, v + d, w, h);
            if (wall != null) {
                fill(image, wall, u, v + d, d, h);
                fill(image, wall, u + d + w, v + d, d, h);
                fill(image, wall, u + d, v, w, d);
                fill(image, wall, u + d + w, v, w, d);
            }
        }
    }

    /* copies the top left of the block texture into a rectangle of the atlas, repeating it down for tall faces, so a
       face that is half a block wide shows the left half of the block and one that is half a block tall shows
       the top half */
    private static void fill(NativeImage image, NativeImage tile, int x, int y, int width, int height) {
        int tileW = tile.getWidth();
        int tileH = tile.getHeight();
        for (int j = 0; j < height; j++) {
            int sourceY = (j % BLOCK_PIXELS) * tileH / BLOCK_PIXELS;
            for (int i = 0; i < width; i++) {
                int sourceX = (i % BLOCK_PIXELS) * tileW / BLOCK_PIXELS;
                set(image, x + i, y + j, tile.getPixel(sourceX, sourceY));
            }
        }
    }

    private static void set(NativeImage image, int x, int y, int pixel) {
        if (x >= 0 && y >= 0 && x < image.getWidth() && y < image.getHeight()) image.setPixel(x, y, pixel);
    }

    /* writes the finished texture next to the game files so it can be looked at: <game dir>/gc_debug/ */
    private static void dump(NativeImage image, Identifier block) {
        try {
            Path folder = Minecraft.getInstance().gameDirectory.toPath().resolve("gc_debug");
            Files.createDirectories(folder);
            image.writeToFile(folder.resolve("door_" + sanitize(block.getPath()) + ".png"));
        } catch (Exception ignored) {
            /* only a debugging aid */
        }
    }

    private static String sanitize(String text) {
        return text.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_.-]", "_");
    }
}
