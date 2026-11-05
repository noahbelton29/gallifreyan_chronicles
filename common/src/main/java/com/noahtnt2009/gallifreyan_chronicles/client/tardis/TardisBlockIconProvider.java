package com.noahtnt2009.gallifreyan_chronicles.client.tardis;

import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisConsoleBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisExteriorBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.init.GCBlocks;
import com.noahtnt2009.gallifreyan_chronicles.init.GCDataComponents;
import com.noahtnt2009.gallifreyan_chronicles.tardis.console.TardisConsole;
import com.noahtnt2009.gallifreyan_chronicles.tardis.interior.TardisInteriorOptions;
import com.noahtnt2009.gallifreyan_chronicles.util.GCUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.Element;
import snownee.jade.api.ui.JadeUI;

public class TardisBlockIconProvider implements IBlockComponentProvider {
    public static final TardisBlockIconProvider INSTANCE = new TardisBlockIconProvider();

    @Override
    public @Nullable Element getIcon(@NonNull BlockAccessor accessor, @NonNull IPluginConfig config, @Nullable Element currentIcon) {
        if (accessor.getBlockEntity() instanceof TardisConsoleBlockEntity console) {
            return JadeUI.item(consoleStack(console));
        }
        if (accessor.getBlockEntity() instanceof TardisExteriorBlockEntity exterior) {
            return JadeUI.item(exteriorStack(exterior));
        }
        return currentIcon;
    }

    public static ItemStack consoleStack(TardisConsoleBlockEntity console) {
        ItemStack stack = new ItemStack(GCBlocks.TARDIS_CONSOLE);
        TardisConsole model = console.getConsole();
        if (model == null) return stack;
        stack.set(GCDataComponents.CONSOLE_ID, model.id());
        String variantId = console.getVariant();
        if (variantId != null && !variantId.isEmpty() && model.hasVariant(variantId)) {
            stack.set(GCDataComponents.CONSOLE_VARIANT, variantId.substring(variantId.indexOf(':') + 1));
        }
        return stack;
    }

    private static ItemStack exteriorStack(TardisExteriorBlockEntity exterior) {
        ItemStack stack = new ItemStack(GCBlocks.TARDIS);
        String exteriorId = exterior.getExterior().id();
        for (TardisInteriorOptions.Option option : TardisInteriorOptions.client()) {
            if (exteriorId.equals(option.exteriorId())) {
                stack.set(GCDataComponents.TARDIS_INTERIOR, option.interiorId());
                break;
            }
        }
        return stack;
    }

    @Override
    public void appendTooltip(@NonNull ITooltip tooltip, @NonNull BlockAccessor accessor, @NonNull IPluginConfig config) {
    }

    @Override
    public @NonNull Identifier getUid() {
        return GCUtils.modIdentifier("tardis_block_icon");
    }
}
