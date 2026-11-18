package uk.nbforge.gallifreyan_chronicles.tardis.door;

import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import uk.nbforge.gallifreyan_chronicles.compat.portal.TardisPortalBridge;
import uk.nbforge.gallifreyan_chronicles.core.block.InteriorDoorBlock;
import uk.nbforge.gallifreyan_chronicles.core.block_entity.InteriorDoorBlockEntity;
import uk.nbforge.gallifreyan_chronicles.tardis.Tardis;
import uk.nbforge.gallifreyan_chronicles.tardis.interior.TardisInteriors;

public final class InteriorDoorExitZone {
    private static final int CHECK_INTERVAL_TICKS = 4;
    private static final double NEAR = -0.5;
    private static final double FAR = 0.5;
    private static final double HALF_WIDTH = 1.0;
    private static final double BOTTOM = -0.5;

    private final InteriorDoorBlockEntity door;

    public InteriorDoorExitZone(InteriorDoorBlockEntity door) {
        this.door = door;
    }

    public void tick(ServerLevel serverLevel) {
        UUID tardisId = door.getTardisId();
        if (tardisId == null || !door.isLinked()) return;
        if (serverLevel.getGameTime() % CHECK_INTERVAL_TICKS != 0) return;
        if (door.getDoorState().isClosed()) return;
        if (TardisPortalBridge.isActive(tardisId)) return;

        MinecraftServer server = serverLevel.getServer();
        Tardis tardis = Tardis.get(server, tardisId).orElse(null);
        if (tardis == null || tardis.isTravelling()) return;
        if (door.isPrimary()
                && tardis.exterior().map(e -> e.doors().getState().isClosed()).orElse(true)) return;

        for (ServerPlayer player : List.copyOf(serverLevel.players())) {
            if (player.isSpectator()) continue;
            boolean inZone = contains(player);
            if (TardisInteriors.isArriving(player)) {
                // just came in through this door, wait until they've stepped away before it can send them back out
                if (!inZone && (door.isFlush() || frontDepth(player) > FAR)) {
                    TardisInteriors.clearArriving(player);
                }
                continue;
            }
            if (inZone) TardisInteriors.exitInterior(tardis, player);
        }
    }

    private double frontDepth(ServerPlayer player) {
        BlockState state = door.getBlockState();
        Direction facing = state.getValue(InteriorDoorBlock.FACING);
        Direction right = InteriorDoorBlock.rightDirectionOf(facing, InteriorDoorBlock.isFlipped(state));
        BlockPos bottom = InteriorDoorBlock.isFlipped(state)
                ? door.getBlockPos().below(InteriorDoorBlock.HEIGHT - 1)
                : door.getBlockPos();
        Vec3 centre = Vec3.atBottomCenterOf(bottom).add(right.getStepX() * 0.5, 0.0, right.getStepZ() * 0.5);
        return (player.getX() - centre.x) * facing.getStepX() + (player.getZ() - centre.z) * facing.getStepZ();
    }

    private boolean contains(ServerPlayer player) {
        BlockPos pos = door.getBlockPos();
        BlockState state = door.getBlockState();
        Direction facing = state.getValue(InteriorDoorBlock.FACING);

        if (InteriorDoorBlock.isFlush(state)) {
            Direction right = InteriorDoorBlock.rightDirectionOf(facing, false);
            Vec3 centre = Vec3.atBottomCenterOf(pos)
                    .add(right.getStepX() * 0.5 + facing.getStepX(), 0.0, right.getStepZ() * 0.5 + facing.getStepZ());
            double dx = player.getX() - centre.x;
            double dz = player.getZ() - centre.z;
            double lateral = dx * right.getStepX() + dz * right.getStepZ();
            double along = dx * facing.getStepX() + dz * facing.getStepZ();
            double eye = player.getEyeY() - pos.getY();
            return Math.abs(lateral) <= HALF_WIDTH && Math.abs(along) <= 1.0 && eye >= 0.3 && eye <= 1.5;
        }

        boolean flipped = InteriorDoorBlock.isFlipped(state);
        Direction right = InteriorDoorBlock.rightDirectionOf(facing, flipped);
        BlockPos bottom = flipped ? pos.below(InteriorDoorBlock.HEIGHT - 1) : pos;
        Vec3 centre = Vec3.atBottomCenterOf(bottom).add(right.getStepX() * 0.5, 0.0, right.getStepZ() * 0.5);
        double dx = player.getX() - centre.x;
        double dz = player.getZ() - centre.z;
        double dy = player.getY() - centre.y;
        double depth = dx * facing.getStepX() + dz * facing.getStepZ();
        double lateral = dx * right.getStepX() + dz * right.getStepZ();
        return depth >= NEAR
                && depth <= FAR
                && Math.abs(lateral) <= HALF_WIDTH
                && dy >= BOTTOM
                && dy <= InteriorDoorBlock.HEIGHT;
    }
}
