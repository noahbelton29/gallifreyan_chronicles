package uk.nbforge.gallifreyan_chronicles.compat.jade;

import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import uk.nbforge.gallifreyan_chronicles.client.compat.jade.GCJadeClient;
import uk.nbforge.gallifreyan_chronicles.core.block_entity.InteriorDoorBlockEntity;
import uk.nbforge.gallifreyan_chronicles.core.block_entity.TardisExteriorBlockEntity;
import uk.nbforge.gallifreyan_chronicles.core.entity.TardisControlEntity;

@WailaPlugin
public class GCJadePlugin implements IWailaPlugin {
    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(TardisOwnerDataProvider.INSTANCE, TardisExteriorBlockEntity.class);
        registration.registerBlockDataProvider(TardisInteriorDoorDataProvider.INSTANCE, InteriorDoorBlockEntity.class);
        registration.registerEntityDataProvider(ControlDataProvider.INSTANCE, TardisControlEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        GCJadeClient.register(registration);
    }
}
