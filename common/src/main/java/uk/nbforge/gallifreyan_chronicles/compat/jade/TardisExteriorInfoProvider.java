package uk.nbforge.gallifreyan_chronicles.compat.jade;

import uk.nbforge.gallifreyan_chronicles.core.block_entities.TardisExteriorBlockEntity;
import uk.nbforge.gallifreyan_chronicles.core.item.tooltip.SelectionTooltip;
import uk.nbforge.gallifreyan_chronicles.tardis.TardisExterior;
import uk.nbforge.gallifreyan_chronicles.util.GCUtil;
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
        return GCUtil.modIdentifier("tardis_exterior_info");
    }
}
