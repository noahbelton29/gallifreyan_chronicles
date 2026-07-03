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

import java.util.Collection;
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
            ctx.getSource().sendFailure(Component.translatable(
                    "command.gallifreyan_chronicles.invalid_id",
                    tardisIdStr
            ));
            return 0;
        }

        if (!TardisExteriorRegistry.contains(exteriorId)) {
            ctx.getSource().sendFailure(Component.translatable(
                    "command.gallifreyan_chronicles.unknown_exterior",
                    exteriorId
            ));
            return 0;
        }

        TardisManager manager = TardisManager.get(ctx.getSource().getServer());
        Optional<TardisComponent> opt = manager.get(tardisId);

        if (opt.isEmpty()) {
            ctx.getSource().sendFailure(Component.translatable(
                    "command.gallifreyan_chronicles.no_tardis_id",
                    tardisIdStr
            ));
            return 0;
        }

        TardisComponent component = opt.get();
        TardisExterior exterior = TardisExteriorRegistry.get(exteriorId);
        component.setExterior(exterior);
        manager.setDirty();

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
                () -> Component.translatable(
                        "command.gallifreyan_chronicles.set_exterior",
                        tardisIdStr,
                        exteriorId
                ),
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
            ctx.getSource().sendFailure(Component.translatable(
                    "command.gallifreyan_chronicles.invalid_id",
                    tardisIdStr
            ));
            return 0;
        }

        TardisManager manager = TardisManager.get(ctx.getSource().getServer());
        Optional<TardisComponent> component = manager.get(tardisId);

        if (component.isEmpty()) {
            ctx.getSource().sendFailure(Component.translatable(
                    "command.gallifreyan_chronicles.no_tardis_id",
                    tardisIdStr
            ));
            return 0;
        }

        String exteriorId = component.get().getExterior().id();

        ctx.getSource().sendSuccess(
                () -> Component.translatable(
                        "command.gallifreyan_chronicles.get_exterior",
                        tardisIdStr,
                        exteriorId
                ),
                false
        );

        return 1;
    }

    public static int listTardis(CommandContext<CommandSourceStack> ctx) {
        TardisManager manager = TardisManager.get(ctx.getSource().getServer());
        Collection<UUID> ids = manager.getAllIds();

        if (ids.isEmpty()) {
            ctx.getSource().sendSuccess(
                    () -> Component.translatable(
                            "command.gallifreyan_chronicles.no_tardises"
                    ),
                    false
            );
            return 0;
        }

        ids.forEach(id -> manager.get(id).ifPresent(component -> {
            String exterior = component.getExterior().id();
            BlockPos pos = component.getBlockPos();
            String posStr = pos != null
                    ? pos.getX() + " " + pos.getY() + " " + pos.getZ()
                    : "unplaced";

            ctx.getSource().sendSuccess(
                    () -> Component.translatable(
                            "command.gallifreyan_chronicles.list_tardis",
                            id,
                            exterior,
                            posStr
                    ),
                    false
            );
        }));

        return 1;
    }

    public static int debugTardis(CommandContext<CommandSourceStack> ctx) {
        String tardisIdStr = StringArgumentType.getString(ctx, "id");

        UUID tardisId;
        try {
            tardisId = UUID.fromString(tardisIdStr);
        } catch (IllegalArgumentException e) {
            ctx.getSource().sendFailure(Component.translatable(
                    "command.gallifreyan_chronicles.invalid_id",
                    tardisIdStr
            ));
            return 0;
        }

        TardisManager manager = TardisManager.get(ctx.getSource().getServer());
        Optional<TardisComponent> opt = manager.get(tardisId);

        if (opt.isEmpty()) {
            ctx.getSource().sendFailure(Component.translatable(
                    "command.gallifreyan_chronicles.no_tardis_id",
                    tardisIdStr
            ));
            return 0;
        }

        TardisComponent component = opt.get();
        BlockPos pos = component.getBlockPos();
        String posStr = pos != null
                ? pos.getX() + " " + pos.getY() + " " + pos.getZ()
                : "unplaced";

        ctx.getSource().sendSuccess(
                () -> Component.translatable(
                        "command.gallifreyan_chronicles.debug",
                        tardisIdStr
                ),
                false
        );

        ctx.getSource().sendSuccess(
                () -> Component.translatable(
                        "command.gallifreyan_chronicles.owner",
                        component.getOwnerId() != null
                                ? component.getOwnerId().toString()
                                : "None"
                ),
                false
        );

        ctx.getSource().sendSuccess(
                () -> Component.translatable(
                        "command.gallifreyan_chronicles.exterior",
                        component.getExterior().id()
                ),
                false
        );

        ctx.getSource().sendSuccess(
                () -> Component.translatable(
                        "command.gallifreyan_chronicles.position",
                        posStr
                ),
                false
        );

        return 1;
    }
}