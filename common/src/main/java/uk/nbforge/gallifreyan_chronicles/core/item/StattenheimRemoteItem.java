package uk.nbforge.gallifreyan_chronicles.core.item;

import uk.nbforge.gallifreyan_chronicles.core.block_entities.TardisExteriorBlockEntity;
import uk.nbforge.gallifreyan_chronicles.core.GCDataComponents;
import uk.nbforge.gallifreyan_chronicles.core.GCTardisControls;
import uk.nbforge.gallifreyan_chronicles.tardis.Tardis;
import uk.nbforge.gallifreyan_chronicles.tardis.TardisApi;
import uk.nbforge.gallifreyan_chronicles.tardis.flight.FlightTimings;
import uk.nbforge.gallifreyan_chronicles.tardis.flight.LandingTarget;
import uk.nbforge.gallifreyan_chronicles.tardis.flight.TardisTravel;
import uk.nbforge.gallifreyan_chronicles.util.GCSchedulerUtil;
import uk.nbforge.gallifreyan_chronicles.util.TardisDimensionUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import org.jspecify.annotations.NonNull;

import java.util.UUID;

public class StattenheimRemoteItem extends Item {
    private static final int SUMMON_COOLDOWN_TICKS = 400;

    public StattenheimRemoteItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NonNull InteractionResult useOn(UseOnContext context) {
        if (!(context.getLevel() instanceof ServerLevel serverLevel)) return InteractionResult.SUCCESS;
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;
        ItemStack stack = context.getItemInHand();

        // link the stattenheim remote
        if (player.isShiftKeyDown()) {
            return link(serverLevel, context.getClickedPos(), player, stack);
        }

        // summon the TARDIS
        UUID tardisID = stack.get(GCDataComponents.TARDIS_ID);
        if (tardisID == null) {
            say(player, "stattenheim_not_linked", ChatFormatting.RED);
            return InteractionResult.FAIL;
        }
        Tardis tardis = TardisApi.get(serverLevel.getServer(), tardisID).orElse(null);
        if (tardis == null) {
            say(player, "stattenheim_lost", ChatFormatting.RED);
            return InteractionResult.FAIL;
        }
        if (!mayUse(tardis, player)) {
            say(player, "stattenheim_not_yours", ChatFormatting.RED);
            return InteractionResult.FAIL;
        }
        if (isInsideATardis(serverLevel)) {
            say(player, "stattenheim_inside_tardis", ChatFormatting.RED);
            return InteractionResult.FAIL;
        }
        if (player.getCooldowns().isOnCooldown(stack)) {
            say(player, "stattenheim_recharging", ChatFormatting.GOLD);
            return InteractionResult.FAIL;
        }

        BlockPos target = context.getClickedPos().relative(context.getClickedFace());
        float yaw = Mth.wrapDegrees(player.getYRot() + 180);

        LandingTarget landingTarget = new LandingTarget(serverLevel.dimension(), target, yaw);
        tardis.data().setLandingTarget(tardisID, landingTarget);

        boolean handbrakeWasOn = tardis.getControl(GCTardisControls.HANDBRAKE);
        if (handbrakeWasOn) {
            tardis.setControl(GCTardisControls.HANDBRAKE, false);
        }
        if (!tardis.takeOff()) {
            if (handbrakeWasOn) tardis.setControl(GCTardisControls.HANDBRAKE, true);
            say(player, "stattenheim_cannot_summon", ChatFormatting.RED);
            return InteractionResult.FAIL;
        }

        long flightTicks = FlightTimings.TAKEOFF_TICKS + TardisTravel.requiredTicks(tardis) + 5;
        GCSchedulerUtil.runLater(serverLevel.getServer(), (int) flightTicks, tardis::land);
        player.getCooldowns().addCooldown(stack, SUMMON_COOLDOWN_TICKS);
        say(player, "stattenheim_summoned", ChatFormatting.GREEN);
        return InteractionResult.SUCCESS;
    }

    private static InteractionResult link(ServerLevel level, BlockPos pos, Player player, ItemStack stack) {
        if (!(level.getBlockEntity(pos) instanceof TardisExteriorBlockEntity exterior)) return InteractionResult.PASS;
        UUID id = exterior.getTardisId();
        Tardis tardis = id == null ? null : TardisApi.get(level.getServer(), id).orElse(null);
        if (tardis == null) {
            say(player, "stattenheim_cannot_link", ChatFormatting.RED);
            return InteractionResult.FAIL;
        }
        if (!mayUse(tardis, player)) {
            say(player, "stattenheim_not_yours", ChatFormatting.RED);
            return InteractionResult.FAIL;
        }
        if (id.equals(stack.get(GCDataComponents.TARDIS_ID))) {
            say(player, "stattenheim_already_linked", ChatFormatting.GOLD);
            return InteractionResult.SUCCESS;
        }

        stack.set(GCDataComponents.TARDIS_ID, id);
        if (tardis.isOwner(player)) {
            say(player, "stattenheim_linked", ChatFormatting.GREEN);
        } else {
            String owner = tardis.ownerName().orElse("?");
            player.sendOverlayMessage(Component.translatable("overlay.gallifreyan_chronicles.stattenheim_linked_other", owner)
                    .withStyle(ChatFormatting.GREEN));
        }
        return InteractionResult.SUCCESS;
    }

    /* the owner and anyone they've trusted. a remote linked to a stranger's TARDIS does nothing */
    private static boolean mayUse(Tardis tardis, Player player) {
        return tardis.isOwner(player) || tardis.isTrusted(player.getUUID());
    }

    /* a TARDIS can't land inside another TARDIS's interior */
    private static boolean isInsideATardis(ServerLevel level) {
        return TardisApi.idOfLevel(level).isPresent() || TardisDimensionUtil.isInterior(level);
    }

    private static void say(Player player, String key, ChatFormatting colour) {
        player.sendOverlayMessage(Component.translatable("overlay.gallifreyan_chronicles." + key).withStyle(colour));
    }
}
