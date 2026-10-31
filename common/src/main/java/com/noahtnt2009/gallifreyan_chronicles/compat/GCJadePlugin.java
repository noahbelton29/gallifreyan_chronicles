package com.noahtnt2009.gallifreyan_chronicles.compat;

import com.noahtnt2009.gallifreyan_chronicles.client.tardis.ControlIconProvider;
import com.noahtnt2009.gallifreyan_chronicles.entity.TardisControlEntity;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class GCJadePlugin implements IWailaPlugin {
    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerEntityIcon(ControlIconProvider.INSTANCE, TardisControlEntity.class);
    }
}