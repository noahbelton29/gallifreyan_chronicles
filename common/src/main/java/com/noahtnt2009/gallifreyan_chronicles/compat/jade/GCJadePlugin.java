package com.noahtnt2009.gallifreyan_chronicles.compat.jade;

import com.noahtnt2009.gallifreyan_chronicles.block.ConsoleBlock;
import com.noahtnt2009.gallifreyan_chronicles.block.InteriorDoorBlock;
import com.noahtnt2009.gallifreyan_chronicles.block.TardisBlock;
import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisExteriorBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.compat.jade.ControlIconProvider;
import com.noahtnt2009.gallifreyan_chronicles.compat.jade.TardisBlockIconProvider;
import com.noahtnt2009.gallifreyan_chronicles.compat.jade.TardisConsoleInfoProvider;
import com.noahtnt2009.gallifreyan_chronicles.compat.jade.TardisExteriorInfoProvider;
import com.noahtnt2009.gallifreyan_chronicles.compat.jade.TardisOwnerDataProvider;
import com.noahtnt2009.gallifreyan_chronicles.compat.jade.TardisOwnerProvider;
import com.noahtnt2009.gallifreyan_chronicles.entity.TardisControlEntity;
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
        registration.addRayTraceCallback((hitResult, accessor, originalAccessor) ->
                accessor instanceof EntityAccessor entityAccessor && isTardisPortal(entityAccessor.getEntity())
                        ? null
                        : accessor);
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
