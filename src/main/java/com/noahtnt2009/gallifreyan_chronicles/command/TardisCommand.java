package com.noahtnt2009.gallifreyan_chronicles.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisExteriorBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.tardis.TardisComponent;
import com.noahtnt2009.gallifreyan_chronicles.tardis.TardisManager;
import com.noahtnt2009.gallifreyan_chronicles.tardis.exterior.TardisExterior;
import com.noahtnt2009.gallifreyan_chronicles.tardis.exterior.TardisExteriorRegistry;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

import java.util.Optional;
import java.util.UUID;

public class TardisCommand {
    public static int setExterior(CommandContext<CommandSourceStack> ctx) {
        String tardisIdStr = StringArgumentType.getString(ctx, "id");
        String exteriorId = StringArgumentType.getString(ctx, "exterior_id");

        UUID tardisId;
        try {
            tardisId = UUID.fromString(tardisIdStr);
        } catch (IllegalArgumentException e) {
            ctx.getSource().sendFailure(Component.literal("Invalid TARDIS ID: " + tardisIdStr));
            return 0;
        }

        if (!TardisExteriorRegistry.contains(exteriorId)) {
            ctx.getSource().sendFailure(Component.literal("Unknown exterior: " + exteriorId));
            return 0;
        }

        TardisManager manager = TardisManager.get(ctx.getSource().getServer());
        Optional<TardisComponent> opt = manager.get(tardisId);

        if (opt.isEmpty()) {
            ctx.getSource().sendFailure(Component.literal("No TARDIS found with ID: " + tardisIdStr));
            return 0;
        }

        TardisComponent component = opt.get();
        TardisExterior exterior = TardisExteriorRegistry.get(exteriorId);
        component.setExterior(exterior);
        manager.setDirty();

        // Update the block entity in the world (resets its GeckoLib animation cache and
        // door state so the new exterior's door animations resolve and play correctly)
        BlockPos pos = component.getBlockPos();
        if (pos != null) {
            for (ServerLevel level : ctx.getSource().getServer().getAllLevels()) {
                if (level.getBlockEntity(pos) instanceof TardisExteriorBlockEntity blockEntity) {
                    blockEntity.setExterior(exterior);
                    break;
                }
            }
        }

        ctx.getSource().sendSuccess(
                () -> Component.literal("Set exterior of " + tardisIdStr + " to " + exteriorId),
                true
        );
        return 1;
    }

    public static int getExterior(CommandContext<CommandSourceStack> ctx) {
        String tardisIdStr = StringArgumentType.getString(ctx, "id");

        UUID tardisId;
        try {
            tardisId = UUID.fromString(tardisIdStr);
        } catch (IllegalArgumentException e) {
            ctx.getSource().sendFailure(Component.literal("Invalid TARDIS ID: " + tardisIdStr));
            return 0;
        }

        TardisManager manager = TardisManager.get(ctx.getSource().getServer());
        Optional<TardisComponent> component = manager.get(tardisId);

        if (component.isEmpty()) {
            ctx.getSource().sendFailure(Component.literal("No TARDIS found with ID: " + tardisIdStr));
            return 0;
        }

        String exteriorId = component.get().getExterior().id();
        ctx.getSource().sendSuccess(
                () -> Component.literal("Exterior of " + tardisIdStr + ": " + exteriorId),
                false
        );
        return 1;
    }
}