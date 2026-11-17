package uk.nbforge.gallifreyan_chronicles.event_subscriber;

import net.minecraft.world.InteractionResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import uk.nbforge.gallifreyan_chronicles.core.block.DoorRoundelInteraction;

public class DoorRoundelHandler {
    @SubscribeEvent
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        InteractionResult result = DoorRoundelInteraction.interact(
                event.getEntity(), event.getLevel(), event.getHand(), event.getHitVec());
        if (result != InteractionResult.PASS) {
            event.setCancellationResult(result);
            event.setCanceled(true);
        }
    }
}
