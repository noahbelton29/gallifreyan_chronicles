package com.noahtnt2009.gallifreyan_chronicles.compat.jade;

import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisConsoleBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.entity.TardisControlEntity;
import com.noahtnt2009.gallifreyan_chronicles.init.GCBlocks;
import com.noahtnt2009.gallifreyan_chronicles.util.GCUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.JadeUI;

import snownee.jade.api.ui.Element;

public class ControlIconProvider implements IEntityComponentProvider {
    public static final ControlIconProvider INSTANCE = new ControlIconProvider();

    @Override
    public @Nullable Element getIcon(@NonNull EntityAccessor accessor, @NonNull IPluginConfig config, @Nullable Element currentIcon) {
        if (accessor.getEntity() instanceof TardisControlEntity control
                && control.getConsolePos() != null
                && accessor.getLevel().getBlockEntity(control.getConsolePos()) instanceof TardisConsoleBlockEntity console) {
            return JadeUI.item(TardisBlockIconProvider.consoleStack(console));
        }
        return JadeUI.item(new ItemStack(GCBlocks.TARDIS_CONSOLE));
    }

    @Override
    public void appendTooltip(@NonNull ITooltip tooltip, @NonNull EntityAccessor accessor, @NonNull IPluginConfig config) {
        // name comes from the entity, icon from getIcon
    }

    @Override
    public @NonNull Identifier getUid() {
        return GCUtils.modIdentifier("tardis_control");
    }
}