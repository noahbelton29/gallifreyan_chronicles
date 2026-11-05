package com.noahtnt2009.gallifreyan_chronicles.compat.jade;

import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisExteriorBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.item.SelectionTooltip;
import com.noahtnt2009.gallifreyan_chronicles.tardis.exterior.TardisExterior;
import com.noahtnt2009.gallifreyan_chronicles.util.GCUtils;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public class TardisExteriorInfoProvider implements IBlockComponentProvider {
    public static final TardisExteriorInfoProvider INSTANCE = new TardisExteriorInfoProvider();

    @Override
    public void appendTooltip(@NonNull ITooltip tooltip, @NonNull BlockAccessor accessor, @NonNull IPluginConfig config) {
        if (!(accessor.getBlockEntity() instanceof TardisExteriorBlockEntity tardis)) return;
        TardisExterior exterior = tardis.getExterior();
        if (exterior == null) return;

        tooltip.add(SelectionTooltip.line("tooltip.gallifreyan_chronicles.tardis.exterior",
                SelectionTooltip.highlight(SelectionTooltip.displayName("tardis_exterior", exterior.id()))));
    }

    @Override
    public @NonNull Identifier getUid() {
        return GCUtils.modIdentifier("tardis_exterior_info");
    }
}
