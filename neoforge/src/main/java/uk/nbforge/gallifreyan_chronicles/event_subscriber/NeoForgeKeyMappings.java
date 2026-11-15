package uk.nbforge.gallifreyan_chronicles.event_subscriber;

import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import uk.nbforge.gallifreyan_chronicles.Constants;
import uk.nbforge.gallifreyan_chronicles.client.input.GCKeyMappings;
import uk.nbforge.gallifreyan_chronicles.util.GCUtil;

@EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT)
public class NeoForgeKeyMappings {

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        KeyMapping.Category category = new KeyMapping.Category(GCUtil.modIdentifier("controls"));
        event.registerCategory(category);
        event.register(GCKeyMappings.createShowControlNames(category));
    }
}
