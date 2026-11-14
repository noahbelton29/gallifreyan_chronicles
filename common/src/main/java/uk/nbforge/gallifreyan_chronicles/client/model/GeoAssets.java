package uk.nbforge.gallifreyan_chronicles.client.model;

import net.minecraft.resources.Identifier;

public final class GeoAssets {
    public static final GeoAssets CONSOLE = new GeoAssets("console/");
    public static final GeoAssets EXTERIOR = new GeoAssets("tardis_door/");
    public static final GeoAssets INTERIOR_DOOR = new GeoAssets("interior_doors/");

    private static final String TEXTURE_FOLDER = "textures/block_entity/";

    private final String animationFolder;

    private GeoAssets(String animationFolder) {
        this.animationFolder = animationFolder;
    }

    public Identifier model(Identifier id) {
        return id;
    }

    public Identifier model(String id) {
        return model(Identifier.parse(id));
    }

    public Identifier texture(Identifier id) {
        return id.withPrefix(TEXTURE_FOLDER).withSuffix(".png");
    }

    public Identifier texture(String id) {
        return texture(Identifier.parse(id));
    }

    public Identifier animation(Identifier id) {
        return id.withPrefix(animationFolder);
    }

    public Identifier animation(String id) {
        return animation(Identifier.parse(id));
    }
}
