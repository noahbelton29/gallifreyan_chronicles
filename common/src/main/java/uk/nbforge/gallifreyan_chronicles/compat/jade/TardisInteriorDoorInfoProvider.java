package uk.nbforge.gallifreyan_chronicles.compat.jade;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import uk.nbforge.gallifreyan_chronicles.util.GCUtil;

public class TardisInteriorDoorInfoProvider implements IBlockComponentProvider {
    public static final TardisInteriorDoorInfoProvider INSTANCE = new TardisInteriorDoorInfoProvider();

    @Override
    public void appendTooltip(@NonNull ITooltip iTooltip, BlockAccessor blockAccessor, @NonNull IPluginConfig iPluginConfig) {
        var data = blockAccessor.getServerData();
        if (!data.contains(TardisInteriorDoorDataProvider.KEY)) return;

        boolean locked = data.getBooleanOr(TardisInteriorDoorDataProvider.KEY, false);
        Component state = locked
                ? Component.translatable("tooltip.gallifreyan_chronicles.tardis.locked").withStyle(ChatFormatting.RED)
                : Component.translatable("tooltip.gallifreyan_chronicles.tardis.unlocked").withStyle(ChatFormatting.GREEN);

        iTooltip.add(Component.translatable("tooltip.gallifreyan_chronicles.tardis.door")
                .append(Component.translatable("tooltip.gallifreyan_chronicles.label_separator"))
                .append(state));
    }

    @Override
    public @NonNull Identifier getUid() {
        return GCUtil.modIdentifier("interior_door_lock");
    }
}
