package com.noahtnt2009.gallifreyan_chronicles.client.tardis;

import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisConsoleBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.item.SelectionTooltip;
import com.noahtnt2009.gallifreyan_chronicles.tardis.console.TardisConsole;
import com.noahtnt2009.gallifreyan_chronicles.tardis.console.TardisConsoleRegistry;
import com.noahtnt2009.gallifreyan_chronicles.tardis.console.TardisConsoleVariant;
import com.noahtnt2009.gallifreyan_chronicles.util.GCUtils;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public class TardisConsoleInfoProvider implements IBlockComponentProvider {
    public static final TardisConsoleInfoProvider INSTANCE = new TardisConsoleInfoProvider();

    @Override
    public void appendTooltip(@NonNull ITooltip tooltip, @NonNull BlockAccessor accessor, @NonNull IPluginConfig config) {
        if (!(accessor.getBlockEntity() instanceof TardisConsoleBlockEntity console)) return;
        TardisConsole model = console.getConsole();
        if (model == null) return;

        tooltip.add(SelectionTooltip.line("tooltip.gallifreyan_chronicles.console.model",
                SelectionTooltip.highlight(SelectionTooltip.displayName("tardis_console", model.id()))));

        String variantId = console.getVariant();
        if (variantId == null || variantId.isEmpty() || model.variants().size() < 2) return;
        TardisConsoleVariant variant = TardisConsoleRegistry.getVariant(variantId.substring(variantId.indexOf(':') + 1));
        String fullId = variant != null ? variant.id() : variantId.contains(":") ? variantId : Constants.MOD_ID + ":" + variantId;
        tooltip.add(SelectionTooltip.line("tooltip.gallifreyan_chronicles.console.variant",
                SelectionTooltip.highlight(SelectionTooltip.displayName("tardis_console_variant", fullId))));
    }

    @Override
    public @NonNull Identifier getUid() {
        return GCUtils.modIdentifier("tardis_console_info");
    }
}
