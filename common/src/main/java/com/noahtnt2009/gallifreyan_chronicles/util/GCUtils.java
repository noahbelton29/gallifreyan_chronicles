package com.noahtnt2009.gallifreyan_chronicles.util;

import com.noahtnt2009.gallifreyan_chronicles.Constants;
import net.minecraft.resources.Identifier;

public class GCUtils {
    public static Identifier of(String name) {
        return Identifier.fromNamespaceAndPath(Constants.MOD_ID, name);
    }

    public static String ofNamespace(String name) {
        return String.format(Constants.MOD_ID + ":" + name);
    }
}
