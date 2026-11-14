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
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import uk.nbforge.gallifreyan_chronicles.core.GCBlocks;
import uk.nbforge.gallifreyan_chronicles.core.block.ConsoleBlock;
import uk.nbforge.gallifreyan_chronicles.core.block.InteriorDoorBlock;
import uk.nbforge.gallifreyan_chronicles.core.block.TardisBlock;
import uk.nbforge.gallifreyan_chronicles.core.block_entities.TardisExteriorBlockEntity;
import uk.nbforge.gallifreyan_chronicles.core.entity.TardisControlEntity;

@WailaPlugin
public class GCJadePlugin implements IWailaPlugin {
    private static final String PORTAL_PACKAGE = "qouteall.imm_ptl.";

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(TardisOwnerDataProvider.INSTANCE, TardisExteriorBlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.addRayTraceCallback((hitResult, accessor, originalAccessor) -> {
            // looking at a portal: near a tardis exterior show the exterior's info, near a linked door show nothing
            if (accessor instanceof EntityAccessor entityAccessor && isPortal(entityAccessor.getEntity())) {
                Entity portal = entityAccessor.getEntity();
                BlockPos exterior = findExterior(portal);
                if (exterior != null) {
                    return blockAccessor(registration, accessor, exterior);
                }
                if (isNearLinkedDoor(portal)) return null;
                return accessor;
            }

            // only show a control's tooltip while the crosshair is really on its hitbox
            if (accessor instanceof EntityAccessor entityAccessor
                    && entityAccessor.getEntity() instanceof TardisControlEntity control) {
                Player player = Minecraft.getInstance().player;
                if (player == null) return null;

                Vec3 eye = player.getEyePosition();
                Vec3 end = eye.add(player.getViewVector(1.0F).scale(player.entityInteractionRange()));
                if (control.getBoundingBox().clip(eye, end).isEmpty()) return null;
            }

            // looking at the top half of the tardis: show the exterior's info instead
            if (accessor instanceof BlockAccessor blockAccessor
                    && blockAccessor.getBlockState().is(GCBlocks.TARDIS_TOP)) {
                BlockPos base = blockAccessor.getPosition().below();
                if (!blockAccessor.getLevel().getBlockState(base).is(GCBlocks.TARDIS)) return null;
                return blockAccessor(registration, accessor, base);
            }

            return accessor;
        });
        registration.registerEntityIcon(ControlIconProvider.INSTANCE, TardisControlEntity.class);
        registration.registerBlockIcon(TardisBlockIconProvider.INSTANCE, TardisBlock.class);
        registration.registerBlockIcon(TardisBlockIconProvider.INSTANCE, ConsoleBlock.class);
        registration.registerBlockComponent(TardisOwnerProvider.INSTANCE, TardisBlock.class);
        registration.registerBlockComponent(TardisExteriorInfoProvider.INSTANCE, TardisBlock.class);
        registration.registerBlockComponent(TardisConsoleInfoProvider.INSTANCE, ConsoleBlock.class);
    }

    // builds a jade block accessor for the tardis block at base, so all the normal block providers run on it
    private static Accessor<?> blockAccessor(IWailaClientRegistration registration, Accessor<?> from, BlockPos base) {
        Level level = from.getLevel();
        BlockState state = level.getBlockState(base);
        return registration.blockAccessor()
                .level(level)
                .player(from.getPlayer())
                .blockState(state)
                .blockEntity(() -> level.getBlockEntity(base))
                .hit(new BlockHitResult(Vec3.atCenterOf(base), Direction.UP, base, false))
                .build();
    }

    private static boolean isPortal(Entity entity) {
        return entity.getClass().getName().startsWith(PORTAL_PACKAGE);
    }

    private static BlockPos findExterior(Entity portal) {
        Level level = portal.level();
        BlockPos centre = portal.blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(centre.offset(-2, -2, -2), centre.offset(2, 2, 2))) {
            if (level.getBlockState(pos).getBlock() instanceof TardisBlock) return pos.immutable();
        }
        return null;
    }

    private static boolean isNearLinkedDoor(Entity portal) {
        Level level = portal.level();
        BlockPos centre = portal.blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(centre.offset(-2, -2, -2), centre.offset(2, 2, 2))) {
            if (level.getBlockState(pos).getBlock() instanceof InteriorDoorBlock door && door.isLinked()) return true;
        }
        return false;
    }
}