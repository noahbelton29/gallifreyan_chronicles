package uk.nbforge.gallifreyan_chronicles.core.item;

import java.util.UUID;
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
import uk.nbforge.gallifreyan_chronicles.core.GCDataComponents;
import uk.nbforge.gallifreyan_chronicles.core.GCTardisControls;
import uk.nbforge.gallifreyan_chronicles.core.block_entity.TardisExteriorBlockEntity;
import uk.nbforge.gallifreyan_chronicles.core.item.tooltip.ItemInfoTooltip;
import uk.nbforge.gallifreyan_chronicles.tardis.Tardis;
import uk.nbforge.gallifreyan_chronicles.tardis.TardisDimensionUtil;
import uk.nbforge.gallifreyan_chronicles.tardis.flight.FlightTimings;
import uk.nbforge.gallifreyan_chronicles.tardis.flight.LandingTarget;
import uk.nbforge.gallifreyan_chronicles.tardis.flight.TardisTravel;
import uk.nbforge.gallifreyan_chronicles.util.SchedulerUtil;

public class StattenheimRemoteItem extends Item {
    private static final int SUMMON_COOLDOWN_TICKS = 400;

    public StattenheimRemoteItem(Properties properties) {
        super(properties.component(GCDataComponents.ITEM_INFO, ItemInfoTooltip.STATTENHEIM_REMOTE));
    }

    @Override
    public @NonNull InteractionResult useOn(UseOnContext context) {
        if (!(context.getLevel() instanceof ServerLevel serverLevel)) return InteractionResult.SUCCESS;
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;
        ItemStack stack = context.getItemInHand();

        if (player.isShiftKeyDown()) {
            return link(serverLevel, context.getClickedPos(), player, stack);
        }

        UUID tardisID = stack.get(GCDataComponents.TARDIS_ID);
        if (tardisID == null) {
            say(player, "stattenheim_not_linked", ChatFormatting.RED);
            return InteractionResult.FAIL;
        }
        Tardis tardis = Tardis.get(serverLevel.getServer(), tardisID).orElse(null);
        if (tardis == null) {
            say(player, "stattenheim_lost", ChatFormatting.RED);
            return InteractionResult.FAIL;
        }
        if (isStranger(tardis, player)) {
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
        tardis.data().setLandingTarget(landingTarget);

        if (tardis.getControlState(GCTardisControls.SPACE_TIME_THROTTLE)) {
            if (!tardis.land()) {
                say(player, "stattenheim_cannot_summon", ChatFormatting.RED);
                return InteractionResult.FAIL;
            }
            player.getCooldowns().addCooldown(stack, SUMMON_COOLDOWN_TICKS);
            say(player, "stattenheim_summoned", ChatFormatting.GREEN);
            return InteractionResult.SUCCESS;
        }

        tardis.closeDoors();

        boolean handbrakeWasOn = tardis.getControlState(GCTardisControls.TIME_ROTOR_HANDBRAKE);
        if (handbrakeWasOn) {
            tardis.setControlState(GCTardisControls.TIME_ROTOR_HANDBRAKE, false);
        }
        tardis.takeOff();
        if (!tardis.getControlState(GCTardisControls.SPACE_TIME_THROTTLE)) {
            if (handbrakeWasOn) tardis.setControlState(GCTardisControls.TIME_ROTOR_HANDBRAKE, true);
            say(player, "stattenheim_cannot_summon", ChatFormatting.RED);
            return InteractionResult.FAIL;
        }

        long flightTicks = FlightTimings.TAKEOFF_TICKS + TardisTravel.requiredTicks(tardis) + 5;
        SchedulerUtil.runLater(serverLevel.getServer(), (int) flightTicks, tardis::land);
        player.getCooldowns().addCooldown(stack, SUMMON_COOLDOWN_TICKS);
        say(player, "stattenheim_summoned", ChatFormatting.GREEN);
        return InteractionResult.SUCCESS;
    }

    private static InteractionResult link(ServerLevel level, BlockPos pos, Player player, ItemStack stack) {
        if (!(level.getBlockEntity(pos) instanceof TardisExteriorBlockEntity exterior)) return InteractionResult.PASS;
        UUID id = exterior.getTardisId();
        Tardis tardis = id == null ? null : Tardis.get(level.getServer(), id).orElse(null);
        if (tardis == null) {
            say(player, "stattenheim_cannot_link", ChatFormatting.RED);
            return InteractionResult.FAIL;
        }
        if (isStranger(tardis, player)) {
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
            player.sendOverlayMessage(
                    Component.translatable("overlay.gallifreyan_chronicles.stattenheim_linked_other", owner)
                            .withStyle(ChatFormatting.GREEN));
        }
        return InteractionResult.SUCCESS;
    }

    private static boolean isStranger(Tardis tardis, Player player) {
        return !tardis.isOwner(player) && !tardis.isTrusted(player.getUUID());
    }

    private static boolean isInsideATardis(ServerLevel level) {
        return Tardis.idOfLevel(level).isPresent() || TardisDimensionUtil.isInterior(level);
    }

    private static void say(Player player, String key, ChatFormatting colour) {
        player.sendOverlayMessage(
                Component.translatable("overlay.gallifreyan_chronicles." + key).withStyle(colour));
    }
}
