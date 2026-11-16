package uk.nbforge.gallifreyan_chronicles.compat.jade;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import snownee.jade.api.Accessor;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IWailaClientRegistration;
import uk.nbforge.gallifreyan_chronicles.core.GCBlocks;
import uk.nbforge.gallifreyan_chronicles.core.block.ConsoleBlock;
import uk.nbforge.gallifreyan_chronicles.core.block.InteriorDoorBlock;
import uk.nbforge.gallifreyan_chronicles.core.block.TardisBlock;
import uk.nbforge.gallifreyan_chronicles.core.entity.TardisControlEntity;

final class GCJadeClient {
    private static final String PORTAL_PACKAGE = "qouteall.imm_ptl.";

    private GCJadeClient() {
    }

    static void register(IWailaClientRegistration registration) {
        registration.addRayTraceCallback((hitResult, accessor, originalAccessor) -> {
            // Looking at a portal, show the info of the TARDIS exterior or interior door it belongs to
            if (accessor instanceof EntityAccessor entityAccessor && isPortal(entityAccessor.getEntity())) {
                Entity portal = entityAccessor.getEntity();

                BlockPos exterior = findNearby(portal, true);
                if (exterior != null) return blockAccessor(registration, exterior);

                BlockPos door = findNearby(portal, false);
                if (door != null) return blockAccessor(registration, door);

                return accessor;
            }

            // Only show the tooltip of a control while the crosshair is really on its hitbox
            if (accessor instanceof EntityAccessor entityAccessor
                    && entityAccessor.getEntity() instanceof TardisControlEntity control) {
                Player player = Minecraft.getInstance().player;
                if (player == null) return null;

                Vec3 eye = player.getEyePosition();
                Vec3 end = eye.add(player.getViewVector(1.0F).scale(player.entityInteractionRange()));
                if (control.getBoundingBox().clip(eye, end).isEmpty()) return null;
            }

            // Looking at the top half of the TARDIS, show the info of the exterior instead
            if (accessor instanceof BlockAccessor blockAccessor
                    && blockAccessor.getBlockState().is(GCBlocks.TARDIS_TOP)) {
                BlockPos base = blockAccessor.getPosition().below();
                if (!blockAccessor.getLevel().getBlockState(base).is(GCBlocks.TARDIS)) return null;
                return blockAccessor(registration, base);
            }

            if (accessor instanceof BlockAccessor doorAccessor
                    && doorAccessor.getBlockState().getBlock() instanceof InteriorDoorBlock
                    && !InteriorDoorBlock.isMaster(doorAccessor.getBlockState())) {
                BlockPos master = InteriorDoorBlock.masterBlockPos(doorAccessor.getBlockState(), doorAccessor.getPosition());
                return blockAccessor(registration, master);
            }

            return accessor;
        });
        registration.registerEntityIcon(ControlIconProvider.INSTANCE, TardisControlEntity.class);
        registration.registerEntityComponent(ControlIconProvider.INSTANCE, TardisControlEntity.class);
        registration.registerBlockIcon(TardisBlockIconProvider.INSTANCE, TardisBlock.class);
        registration.registerBlockIcon(TardisBlockIconProvider.INSTANCE, ConsoleBlock.class);
        registration.registerBlockComponent(TardisOwnerProvider.INSTANCE, TardisBlock.class);
        registration.registerBlockComponent(TardisExteriorInfoProvider.INSTANCE, TardisBlock.class);
        registration.registerBlockComponent(TardisConsoleInfoProvider.INSTANCE, ConsoleBlock.class);
        registration.registerBlockComponent(TardisInteriorDoorInfoProvider.INSTANCE, InteriorDoorBlock.class);
    }

    private static Accessor<?> blockAccessor(IWailaClientRegistration registration, BlockPos pos) {
        Level level = Minecraft.getInstance().level;
        Player player = Minecraft.getInstance().player;
        if (level == null || player == null) return null;

        BlockState state = level.getBlockState(pos);
        return registration.blockAccessor()
                .level(level)
                .player(player)
                .blockState(state)
                .blockEntity(() -> level.getBlockEntity(pos))
                .hit(new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false))
                .build();
    }

    private static boolean isPortal(Entity entity) {
        return entity.getClass().getName().startsWith(PORTAL_PACKAGE);
    }

    private static BlockPos findNearby(Entity portal, boolean exterior) {
        Level level = Minecraft.getInstance().level;
        if (level == null) return null;

        BlockPos centre = portal.blockPosition();
        BlockPos fallbackDoor = null;
        for (BlockPos pos : BlockPos.betweenClosed(centre.offset(-2, -2, -2), centre.offset(2, 2, 2))) {
            BlockState state = level.getBlockState(pos);
            if (exterior) {
                if (state.getBlock() instanceof TardisBlock) return pos.immutable();
            } else if (state.getBlock() instanceof InteriorDoorBlock door && door.isLinked()) {
                if (InteriorDoorBlock.isMaster(state)) return pos.immutable();
                if (fallbackDoor == null) fallbackDoor = pos.immutable();
            }
        }
        return fallbackDoor;
    }
}
