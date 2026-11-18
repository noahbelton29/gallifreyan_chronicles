package uk.nbforge.gallifreyan_chronicles.client.model;

import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public enum GeoAssets {
    CONSOLE("console/"),
    EXTERIOR("tardis_door/"),
    INTERIOR_DOOR("interior_doors/");

    private final String animationFolder;

    GeoAssets(String animationFolder) {
        this.animationFolder = animationFolder;
    }

    public Identifier animation(Identifier id) {
        return id.withPrefix(animationFolder);
    }

    public Identifier animation(String id) {
        return animation(Identifier.parse(id));
    }

    public static Identifier model(String id) {
        return Identifier.parse(id);
    }

    public static Identifier texture(Identifier id) {
        return id.withPrefix("textures/block_entity/").withSuffix(".png");
    }

    public static Identifier texture(String id) {
        return texture(Identifier.parse(id));
    }

    public static @Nullable String read(GeoRenderState renderState, DataTicket<String> ticket) {
        if (!renderState.hasGeckolibData(ticket)) return null;
        String value = renderState.getGeckolibData(ticket);
        return value == null || value.isEmpty() ? null : value;
    }
}
