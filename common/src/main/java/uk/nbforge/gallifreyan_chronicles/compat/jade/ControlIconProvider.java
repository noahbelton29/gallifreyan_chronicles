package uk.nbforge.gallifreyan_chronicles.compat.jade;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.Element;
import snownee.jade.api.ui.JadeUI;
import uk.nbforge.gallifreyan_chronicles.core.GCBlocks;
import uk.nbforge.gallifreyan_chronicles.core.GCTardisControls;
import uk.nbforge.gallifreyan_chronicles.core.block_entities.TardisConsoleBlockEntity;
import uk.nbforge.gallifreyan_chronicles.core.dynamic_registries.TardisControlRegistry;
import uk.nbforge.gallifreyan_chronicles.core.entity.TardisControlEntity;
import uk.nbforge.gallifreyan_chronicles.tardis.flight.TardisDestinations;
import uk.nbforge.gallifreyan_chronicles.util.GCUtil;

public class ControlIconProvider implements IEntityComponentProvider {
    public static final ControlIconProvider INSTANCE = new ControlIconProvider();

    @Override
    public @Nullable Element getIcon(
            @NonNull EntityAccessor accessor, @NonNull IPluginConfig config, @Nullable Element currentIcon) {
        if (accessor.getEntity() instanceof TardisControlEntity control
                && control.getConsolePos() != null
                && accessor.getLevel().getBlockEntity(control.getConsolePos())
                        instanceof TardisConsoleBlockEntity console) {
            return JadeUI.item(TardisBlockIconProvider.consoleStack(console));
        }
        return JadeUI.item(new ItemStack(GCBlocks.TARDIS_CONSOLE));
    }

    @Override
    public void appendTooltip(
            @NonNull ITooltip tooltip, @NonNull EntityAccessor accessor, @NonNull IPluginConfig config) {
        if (accessor.getEntity() instanceof TardisControlEntity control
                && accessor.getLevel().getBlockEntity(control.getConsolePos())
                        instanceof TardisConsoleBlockEntity console
                && console.isPowerKnown()) {
            Component text = TardisControlRegistry.get(control.getControlId())
                    .describeState(console.getControlState(control.getControlId()));
            if (text != null) tooltip.add(text);
            appendDestination(tooltip, control.getControlId(), accessor.getServerData());
        }
    }

    /**
     * The destination controls, what they would set or show, read from the server data.
     */
    private static void appendDestination(ITooltip tooltip, String id, CompoundTag data) {
        switch (id) {
            case GCTardisControls.X_INCREMENT_CONTROL -> coord(tooltip, "target_x", data);
            case GCTardisControls.Y_INCREMENT_CONTROL -> coord(tooltip, "target_y", data);
            case GCTardisControls.Z_INCREMENT_CONTROL -> coord(tooltip, "target_z", data);
            case GCTardisControls.COORDINATE_STEP_CONTROL -> {
                if (!data.contains(ControlDataProvider.STEP)) return;
                int step = data.getIntOr(ControlDataProvider.STEP, 0);
                tooltip.add(line(
                        "step", Component.literal((step > 0 ? "+" : "") + step).withStyle(ChatFormatting.GOLD)));
            }
            case GCTardisControls.DIRECTIONAL_CONTROL -> {
                if (!data.contains(ControlDataProvider.YAW)) return;
                tooltip.add(line(
                        "facing",
                        TardisDestinations.facingName(data.getFloatOr(ControlDataProvider.YAW, 0F))
                                .copy()
                                .withStyle(ChatFormatting.WHITE)));
            }
            case GCTardisControls.DIMENSIONAL_CONTROL -> {
                if (!data.contains(ControlDataProvider.DIMENSION)) return;
                tooltip.add(line(
                        "dimension",
                        Component.literal(data.getStringOr(ControlDataProvider.DIMENSION, ""))
                                .withStyle(ChatFormatting.AQUA)
                                .append(Component.literal(
                                                " (" + data.getStringOr(ControlDataProvider.DIMENSION_INDEX, "") + ")")
                                        .withStyle(ChatFormatting.GRAY))));
            }
            default -> {}
        }
    }

    private static void coord(ITooltip tooltip, String label, CompoundTag data) {
        if (!data.contains(ControlDataProvider.COORD)) return;
        tooltip.add(line(
                label,
                Component.literal(String.valueOf(data.getIntOr(ControlDataProvider.COORD, 0)))
                        .withStyle(ChatFormatting.WHITE)));
    }

    private static Component line(String label, Component value) {
        return Component.translatable("tooltip.gallifreyan_chronicles.control_state.label." + label)
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(": ").withStyle(ChatFormatting.GRAY))
                .append(value);
    }

    @Override
    public @NonNull Identifier getUid() {
        return GCUtil.modIdentifier("tardis_control");
    }
}
