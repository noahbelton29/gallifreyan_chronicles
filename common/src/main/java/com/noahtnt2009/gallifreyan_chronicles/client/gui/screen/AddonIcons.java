package com.noahtnt2009.gallifreyan_chronicles.client.gui.screen;

import com.mojang.blaze3d.platform.NativeImage;
import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.addon.AddonInfo;
import com.noahtnt2009.gallifreyan_chronicles.util.GCUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

final class AddonIcons {
    record Icon(Identifier texture, int width, int height) {
    }

    private static final Icon PLACEHOLDER = new Icon(Identifier.withDefaultNamespace("textures/misc/unknown_pack.png"), 64, 64);
    private static final Map<String, Icon> CACHE = new HashMap<>();

    private AddonIcons() {
    }

    static Icon get(AddonInfo info) {
        if (info.icon() == null) return PLACEHOLDER;
        String key = info.id() + "/" + Arrays.hashCode(info.icon());
        Icon cached = CACHE.get(key);
        if (cached != null) return cached;

        Icon icon = PLACEHOLDER;
        try {
            NativeImage image = NativeImage.read(info.icon());
            Identifier id = GCUtils.modIdentifier("addon_icon/" + key.replaceAll("[^a-z0-9/._-]", "_"));
            Minecraft.getInstance().getTextureManager().register(id, new DynamicTexture(() -> "GC addon icon " + id, image));
            icon = new Icon(id, image.getWidth(), image.getHeight());
        } catch (Exception e) {
            Constants.LOG.warn("Addon '{}' has an icon that could not be read", info.id(), e);
        }
        CACHE.put(key, icon);
        return icon;
    }
}
