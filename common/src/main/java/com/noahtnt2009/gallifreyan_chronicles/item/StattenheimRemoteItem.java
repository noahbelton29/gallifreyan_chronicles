package com.noahtnt2009.gallifreyan_chronicles.item;

import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisConsoleBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisExteriorBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.init.GCDataComponents;
import com.noahtnt2009.gallifreyan_chronicles.init.GCTardisControls;
import com.noahtnt2009.gallifreyan_chronicles.tardis.api.Tardis;
import com.noahtnt2009.gallifreyan_chronicles.tardis.api.TardisApi;
import com.noahtnt2009.gallifreyan_chronicles.tardis.flight.FlightTimings;
import com.noahtnt2009.gallifreyan_chronicles.tardis.flight.LandingTarget;
import com.noahtnt2009.gallifreyan_chronicles.tardis.flight.TardisTravel;
import com.noahtnt2009.gallifreyan_chronicles.util.GCScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

import java.util.List;
import java.util.UUID;

public class StattenheimRemoteItem extends Item {
    public StattenheimRemoteItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!(context.getLevel() instanceof ServerLevel serverLevel)) return InteractionResult.SUCCESS;
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;
        ItemStack stack = context.getItemInHand();

        // link the stattenheim remote
        if (player.isShiftKeyDown()) {
            if (!(serverLevel.getBlockEntity(context.getClickedPos()) instanceof TardisExteriorBlockEntity exterior))
                return InteractionResult.PASS;
            UUID id = exterior.getTardisId();
            if (id == null) return InteractionResult.FAIL;

            stack.set(GCDataComponents.TARDIS_ID, id);
            return InteractionResult.SUCCESS;
        }

        // summon the TARDIS
        UUID tardisID = stack.get(GCDataComponents.TARDIS_ID);
        if (tardisID == null) return InteractionResult.FAIL;
        Tardis tardis = TardisApi.get(serverLevel.getServer(), tardisID).orElse(null);
        if (tardis == null) return InteractionResult.FAIL;

        BlockPos target = context.getClickedPos().relative(context.getClickedFace());
        float yaw = Mth.wrapDegrees(player.getYRot() + 180);

        LandingTarget landingTarget = new LandingTarget(serverLevel.dimension(), target, yaw);
        tardis.data().setLandingTarget(tardisID, landingTarget);

        if (tardis.getControl(GCTardisControls.HANDBRAKE)) {
            tardis.setControl(GCTardisControls.HANDBRAKE, false);
        }
        if (!tardis.takeOff()) return InteractionResult.FAIL;

        long flightTicks = FlightTimings.TAKEOFF_TICKS + TardisTravel.requiredTicks(tardis) + 5;
        GCScheduler.runLater(serverLevel.getServer(), (int) flightTicks, tardis::land);
        return InteractionResult.SUCCESS;
    }
}
