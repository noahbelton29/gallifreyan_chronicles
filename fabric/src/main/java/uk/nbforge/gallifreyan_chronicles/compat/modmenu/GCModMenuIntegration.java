package uk.nbforge.gallifreyan_chronicles.compat.modmenu;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.minecraft.client.Minecraft;
import uk.nbforge.gallifreyan_chronicles.client.gui.screen.GCConfigScreen;

public class GCModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> new GCConfigScreen(parent, Minecraft.getInstance().options);
    }
}
