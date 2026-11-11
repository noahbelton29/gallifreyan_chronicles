package com.noahtnt2009.gallifreyan_chronicles.util;

import com.noahtnt2009.gallifreyan_chronicles.Constants;
import net.minecraft.resources.Identifier;
import org.joml.Vector3f;

public class GCUtils {
    public static Vector3f rgb(int color) {
        return new Vector3f(
                ((color >> 16) & 0xFF) / 255.0F,
                ((color >> 8) & 0xFF) / 255.0F,
                (color & 0xFF) / 255.0F
        );
    }
    
    public static Identifier modIdentifier(String name) {
        return Identifier.fromNamespaceAndPath(Constants.MOD_ID, name);
    }

    public static String localPath(String id) {
        int colon = id.indexOf(':');
        return colon < 0 ? id : id.substring(colon + 1);
    }

    public static String withModIdPrefix(String name) {
        return String.format(Constants.MOD_ID.concat(": ".concat(name)));
    }
}
