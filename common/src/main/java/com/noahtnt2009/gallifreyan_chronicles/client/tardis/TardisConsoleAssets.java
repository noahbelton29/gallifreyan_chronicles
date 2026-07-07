package com.noahtnt2009.gallifreyan_chronicles.client.tardis;

import net.minecraft.resources.Identifier;

public final class TardisConsoleAssets {
    private static final String TEXTURE_FOLDER = "textures/block_entity/";
    private static final String ANIMATION_FOLDER = "console/";

    private TardisConsoleAssets() {
    }

    public static Identifier model(Identifier id) {
        return id;
    }

    public static Identifier model(String id) {
        return model(Identifier.parse(id));
    }

    public static Identifier texture(Identifier id) {
        return id.withPrefix(TEXTURE_FOLDER).withSuffix(".png");
    }

    public static Identifier texture(String id) {
        return texture(Identifier.parse(id));
    }

    public static Identifier animation(Identifier id) {
        return id.withPrefix(ANIMATION_FOLDER);
    }

    public static Identifier animation(String id) {
        return animation(Identifier.parse(id));
    }
}