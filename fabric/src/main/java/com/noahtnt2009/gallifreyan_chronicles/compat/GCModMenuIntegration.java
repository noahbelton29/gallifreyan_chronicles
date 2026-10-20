package com.noahtnt2009.gallifreyan_chronicles.compat;

import com.noahtnt2009.gallifreyan_chronicles.client.screen.GCConfigScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

/** Adds the "Configure" button for this mod in Mod Menu. */
public class GCModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return GCConfigScreen::new;
    }
}
