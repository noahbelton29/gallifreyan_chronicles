package com.noahtnt2009.gallifreyan_chronicles.client.tardis;

import com.noahtnt2009.gallifreyan_chronicles.util.GCUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public class TardisOwnerProvider implements IBlockComponentProvider {
    public static final TardisOwnerProvider INSTANCE = new TardisOwnerProvider();

    @Override
    public void appendTooltip(@NonNull ITooltip tooltip, @NonNull BlockAccessor accessor, @NonNull IPluginConfig config) {
        String name = accessor.getServerData().getStringOr(TardisOwnerDataProvider.KEY, "");

        Component value = name.isEmpty()
                ? Component.translatable("tooltip.gallifreyan_chronicles.tardis.owner_unknown").withStyle(ChatFormatting.DARK_GRAY)
                : Component.literal(name).withStyle(ChatFormatting.AQUA);

        tooltip.add(Component.translatable("tooltip.gallifreyan_chronicles.tardis.owner")
                .append(Component.translatable("tooltip.gallifreyan_chronicles.label_separator"))
                .append(value));
    }

    @Override
    public @NonNull Identifier getUid() {
        return GCUtils.modIdentifier("tardis_owner");
    }
}
