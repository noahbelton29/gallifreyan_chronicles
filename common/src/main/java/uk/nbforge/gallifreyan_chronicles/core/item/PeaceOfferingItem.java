package uk.nbforge.gallifreyan_chronicles.core.item;

import uk.nbforge.gallifreyan_chronicles.tardis.Tardis;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.offering.TardisPeaceOffering;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

/* a treat to make peace with a tardis. use it on the console or the box */
public class PeaceOfferingItem extends Item {
    private static final int USE_COOLDOWN_TICKS = 20;

    public PeaceOfferingItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NonNull InteractionResult useOn(@NonNull UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        Optional<Tardis> tardis = MalletItem.tardisAt(level, context.getClickedPos());
        if (player == null || tardis.isEmpty()) return InteractionResult.PASS;
        if (!(level instanceof ServerLevel)) return InteractionResult.SUCCESS;

        ServerLevel serverLevel = (ServerLevel) level;
        boolean accepted = TardisPeaceOffering.offer(tardis.get(), serverLevel, context.getClickedPos(), player);
        player.getCooldowns().addCooldown(context.getItemInHand(), USE_COOLDOWN_TICKS);
        if (accepted && !player.isCreative()) context.getItemInHand().shrink(1);
        return InteractionResult.SUCCESS;
    }
}
