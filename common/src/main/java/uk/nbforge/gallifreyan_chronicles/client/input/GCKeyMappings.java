package uk.nbforge.gallifreyan_chronicles.client.input;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;
import uk.nbforge.gallifreyan_chronicles.config.GCConfig;

/**
 * The key mappings are built here but registered by each loader because Fabric and NeoForge register them and their category differently.
 */
public final class GCKeyMappings {
    private static @Nullable KeyMapping showControlNames;

    private GCKeyMappings() {}

    public static KeyMapping createShowControlNames(KeyMapping.Category category) {
        showControlNames =
                new KeyMapping("key.gallifreyan_chronicles.show_control_names", InputConstants.KEY_N, category);
        return showControlNames;
    }

    public static boolean controlNamesOn() {
        return GCConfig.showControlNames();
    }

    public static void tick() {
        if (showControlNames == null) return;
        Minecraft minecraft = Minecraft.getInstance();
        while (showControlNames.consumeClick()) {
            boolean on = !GCConfig.showControlNames();
            GCConfig.setShowControlNames(on);
            GCConfig.saveSoon();
            if (minecraft.player != null) {
                minecraft.player.sendOverlayMessage(Component.translatable(
                        on
                                ? "message.gallifreyan_chronicles.control_names_on"
                                : "message.gallifreyan_chronicles.control_names_off"));
            }
        }
    }
}
