package com.noahtnt2009.gallifreyan_chronicles.compat;

import com.noahtnt2009.gallifreyan_chronicles.block.ConsoleBlock;
import com.noahtnt2009.gallifreyan_chronicles.block.TardisBlock;
import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisExteriorBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.client.tardis.ControlIconProvider;
import com.noahtnt2009.gallifreyan_chronicles.client.tardis.TardisConsoleInfoProvider;
import com.noahtnt2009.gallifreyan_chronicles.client.tardis.TardisExteriorInfoProvider;
import com.noahtnt2009.gallifreyan_chronicles.client.tardis.TardisOwnerDataProvider;
import com.noahtnt2009.gallifreyan_chronicles.client.tardis.TardisOwnerProvider;
import com.noahtnt2009.gallifreyan_chronicles.entity.TardisControlEntity;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class GCJadePlugin implements IWailaPlugin {
    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(TardisOwnerDataProvider.INSTANCE, TardisExteriorBlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerEntityIcon(ControlIconProvider.INSTANCE, TardisControlEntity.class);
        registration.registerBlockComponent(TardisOwnerProvider.INSTANCE, TardisBlock.class);
        registration.registerBlockComponent(TardisExteriorInfoProvider.INSTANCE, TardisBlock.class);
        registration.registerBlockComponent(TardisConsoleInfoProvider.INSTANCE, ConsoleBlock.class);
    }
}