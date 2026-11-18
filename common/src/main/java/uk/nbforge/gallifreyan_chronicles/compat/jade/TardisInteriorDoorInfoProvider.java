package uk.nbforge.gallifreyan_chronicles.compat.jade;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.config.IPluginConfig;
import uk.nbforge.gallifreyan_chronicles.util.GCUtil;

public class TardisInteriorDoorInfoProvider implements IBlockComponentProvider {
    public static final TardisInteriorDoorInfoProvider INSTANCE = new TardisInteriorDoorInfoProvider();

    @Override
    public void appendTooltip(
            @NonNull ITooltip iTooltip, BlockAccessor blockAccessor, @NonNull IPluginConfig iPluginConfig) {
        var data = blockAccessor.getServerData();
        renameDoor(iTooltip, data);
        if (!data.contains(TardisInteriorDoorDataProvider.KEY)) return;

        boolean locked = data.getBooleanOr(TardisInteriorDoorDataProvider.KEY, false);
        Component state = locked
                ? Component.translatable("tooltip.gallifreyan_chronicles.tardis.locked")
                        .withStyle(ChatFormatting.RED)
                : Component.translatable("tooltip.gallifreyan_chronicles.tardis.unlocked")
                        .withStyle(ChatFormatting.GREEN);

        iTooltip.add(Component.translatable("tooltip.gallifreyan_chronicles.tardis.door")
                .append(Component.translatable("tooltip.gallifreyan_chronicles.label_separator"))
                .append(state));
    }

    private static void renameDoor(ITooltip tooltip, net.minecraft.nbt.CompoundTag data) {
        String role = data.getStringOr(TardisInteriorDoorDataProvider.ROLE_KEY, "");
        if (role.isEmpty()) return;

        Component name = Component.translatable(
                "tooltip.gallifreyan_chronicles.interior_door." + role.toLowerCase(java.util.Locale.ROOT));
        Identifier style = Identifier.tryParse(data.getStringOr(TardisInteriorDoorDataProvider.STYLE_KEY, ""));
        if (style != null && BuiltInRegistries.BLOCK.containsKey(style)) {
            name = Component.translatable(
                    "tooltip.gallifreyan_chronicles.interior_door.styled",
                    name,
                    BuiltInRegistries.BLOCK.getValue(style).getName());
        }
        tooltip.replace(JadeIds.CORE_OBJECT_NAME, name);
    }

    @Override
    public @NonNull Identifier getUid() {
        return GCUtil.modIdentifier("interior_door_lock");
    }
}
