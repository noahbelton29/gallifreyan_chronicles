package uk.nbforge.gallifreyan_chronicles.compat.jade;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import uk.nbforge.gallifreyan_chronicles.core.block.ConsoleBlock;
import uk.nbforge.gallifreyan_chronicles.core.block.InteriorDoorBlock;
import uk.nbforge.gallifreyan_chronicles.core.block.TardisBlock;
import uk.nbforge.gallifreyan_chronicles.core.block_entities.TardisExteriorBlockEntity;
import uk.nbforge.gallifreyan_chronicles.compat.jade.ControlIconProvider;
import uk.nbforge.gallifreyan_chronicles.compat.jade.TardisBlockIconProvider;
import uk.nbforge.gallifreyan_chronicles.compat.jade.TardisConsoleInfoProvider;
import uk.nbforge.gallifreyan_chronicles.compat.jade.TardisExteriorInfoProvider;
import uk.nbforge.gallifreyan_chronicles.compat.jade.TardisOwnerDataProvider;
import uk.nbforge.gallifreyan_chronicles.compat.jade.TardisOwnerProvider;
import uk.nbforge.gallifreyan_chronicles.core.entity.TardisControlEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

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
            // hide immersive portals jade tooltips
            if (accessor instanceof EntityAccessor entityAccessor && isTardisPortal(entityAccessor.getEntity())) {
                return null;
            }

            // only show tardis control jade tooltips if directly looking at a control entity
            if (accessor instanceof EntityAccessor entityAccessor && entityAccessor.getEntity() instanceof TardisControlEntity control) {
                Player player = Minecraft.getInstance().player;
                if (player == null) return null;

                Vec3 eye = player.getEyePosition();
                Vec3 end = eye.add(player.getViewVector(1.0F).scale(player.entityInteractionRange()));

                boolean lookingAtIt = control.getBoundingBox().clip(eye, end).isPresent();
                if (!lookingAtIt) return null;
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

    private static boolean isTardisPortal(Entity entity) {
        if (!entity.getClass().getName().startsWith(PORTAL_PACKAGE)) return false;
        Level level = entity.level();
        BlockPos centre = entity.blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(centre.offset(-2, -2, -2), centre.offset(2, 2, 2))) {
            BlockState state = level.getBlockState(pos);
            if (state.getBlock() instanceof TardisBlock) return true;
            if (state.getBlock() instanceof InteriorDoorBlock door && door.isLinked()) return true;
        }
        return false;
    }
}
