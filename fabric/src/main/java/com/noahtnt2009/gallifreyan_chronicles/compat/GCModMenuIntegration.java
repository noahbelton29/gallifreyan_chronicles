package com.noahtnt2009.gallifreyan_chronicles.compat;

import com.noahtnt2009.gallifreyan_chronicles.client.screen.GCConfigScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class GCModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return GCConfigScreen::new;
    }
}
