package com.noahtnt2009.gallifreyan_chronicles.command;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisConsoleBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisExteriorBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.network.TardisConsoleSyncPayload;
import com.noahtnt2009.gallifreyan_chronicles.platform.Services;
import com.noahtnt2009.gallifreyan_chronicles.tardis.console.TardisConsole;
import com.noahtnt2009.gallifreyan_chronicles.tardis.console.TardisConsoleRegistry;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.component.TardisComponent;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.system.ConsoleLinkSystem;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.system.ExteriorLinkSystem;
import com.noahtnt2009.gallifreyan_chronicles.tardis.manager.TardisManager;
import com.noahtnt2009.gallifreyan_chronicles.tardis.exterior.TardisExterior;
import com.noahtnt2009.gallifreyan_chronicles.tardis.exterior.TardisExteriorRegistry;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public class TardisCommand {
    private static <T extends BlockEntity> Optional<T> findBlockEntity(
            CommandContext<CommandSourceStack> ctx,
            BlockPos pos,
            Class<T> entityClass
    ) {
        if (pos == null) return Optional.empty();

        for (ServerLevel level : ctx.getSource().getServer().getAllLevels()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (entityClass.isInstance(blockEntity)) {
                return Optional.of(entityClass.cast(blockEntity));
            }
        }

        return Optional.empty();
    }

    private static final double LOOK_DISTANCE = 8.0;

    private static Optional<BlockPos> getLookedAtBlockPos(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player;
        try {
            player = ctx.getSource().getPlayerOrException();
        } catch (com.mojang.brigadier.exceptions.CommandSyntaxException e) {
            return Optional.empty();
        }

        ServerLevel level = ctx.getSource().getLevel();

        Vec3 eyePos = player.getEyePosition();
        Vec3 lookVec = player.getViewVector(1.0F);
        Vec3 endPos = eyePos.add(lookVec.x * LOOK_DISTANCE, lookVec.y * LOOK_DISTANCE, lookVec.z * LOOK_DISTANCE);

        ClipContext clipContext = new ClipContext(
                eyePos,
                endPos,
                ClipContext.Block.OUTLINE,
                ClipContext.Fluid.NONE,
                player
        );

        BlockHitResult hit = level.clip(clipContext);
        if (hit.getType() != HitResult.Type.BLOCK) {
            return Optional.empty();
        }

        return Optional.of(hit.getBlockPos());
    }

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

        TardisExterior exterior = TardisExteriorRegistry.get(exteriorId);
        BlockPos pos = manager.resolveExteriorBlockPos(tardisId);
        Optional<TardisExteriorBlockEntity> blockEntity = findBlockEntity(ctx, pos, TardisExteriorBlockEntity.class);
        blockEntity.ifPresent(be -> be.setExterior(exterior));

        if (blockEntity.isEmpty()) {
            ctx.getSource().sendFailure(Component.translatable(
                    "command.gallifreyan_chronicles.no_exterior",
                    tardisIdStr
            ));
            return 0;
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

    public static int setConsole(CommandContext<CommandSourceStack> ctx) {
        String tardisIdStr = StringArgumentType.getString(ctx, "id");
        String consoleId = StringArgumentType.getString(ctx, "console_id");

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

        if (!TardisConsoleRegistry.contains(consoleId)) {
            ctx.getSource().sendFailure(Component.translatable(
                    "command.gallifreyan_chronicles.unknown_console",
                    consoleId
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

        TardisConsole console = TardisConsoleRegistry.get(consoleId);
        BlockPos pos = manager.getConsoleBlockPos(tardisId);
        Optional<TardisConsoleBlockEntity> blockEntity = findBlockEntity(ctx, pos, TardisConsoleBlockEntity.class);
        blockEntity.ifPresent(be -> {
            be.setConsole(console);
            ConsoleLinkSystem.spawnDefaultControls(be);
        });

        if (blockEntity.isEmpty()) {
            ctx.getSource().sendFailure(Component.translatable(
                    "command.gallifreyan_chronicles.no_console",
                    tardisIdStr
            ));
            return 0;
        }

        ServerPlayer sourcePlayer = ctx.getSource().getPlayer();
        if (sourcePlayer != null) {
            // setConsole only mutates the block entity's ECS state; it never sends a
            // TardisConsoleSyncPayload the way /reload does. Without this, the client's
            // GeckoLib model cache never gets told to reload, so a newly added console's
            // model/texture/animation won't render until the next /reload or a rejoin.
            Services.NETWORK.sendTardisConsoleSync(sourcePlayer, TardisConsoleSyncPayload.create());
        }

        ctx.getSource().sendSuccess(
                () -> Component.translatable(
                        "command.gallifreyan_chronicles.set_console",
                        tardisIdStr,
                        consoleId
                ),
                true
        );

        return 1;
    }

    public static int getConsole(CommandContext<CommandSourceStack> ctx) {
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

        BlockPos pos = manager.getConsoleBlockPos(tardisId);
        String exteriorId = findBlockEntity(ctx, pos, TardisConsoleBlockEntity.class)
                .map(be -> be.getConsole().id())
                .orElse("unknown");

        ctx.getSource().sendSuccess(
                () -> Component.translatable(
                        "command.gallifreyan_chronicles.get_console",
                        tardisIdStr,
                        exteriorId
                ),
                false
        );

        return 1;
    }

    public static int unlinkConsole(CommandContext<CommandSourceStack> ctx) {
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

        manager.unlinkConsole(tardisId);

        ctx.getSource().sendSuccess(
                () -> Component.translatable(
                        "command.gallifreyan_chronicles.unlink_console",
                        tardisIdStr
                ),
                true
        );

        return 1;
    }

    public static int setGlowing(CommandContext<CommandSourceStack> ctx) {
        String tardisIdStr = StringArgumentType.getString(ctx, "id");
        boolean glowing = BoolArgumentType.getBool(ctx, "glowing");

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

        BlockPos pos = manager.getBlockPos(tardisId);
        Optional<TardisExteriorBlockEntity> blockEntity = findBlockEntity(ctx, pos, TardisExteriorBlockEntity.class);
        blockEntity.ifPresent(be -> be.setGlowing(glowing));

        if (blockEntity.isEmpty()) {
            ctx.getSource().sendFailure(Component.translatable(
                    "command.gallifreyan_chronicles.no_tardis_id",
                    tardisIdStr
            ));
            return 0;
        }

        ctx.getSource().sendSuccess(
                () -> Component.translatable(
                        "command.gallifreyan_chronicles.set_glow",
                        tardisIdStr,
                        String.valueOf(glowing)
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

        BlockPos pos = manager.resolveExteriorBlockPos(tardisId);
        String exteriorId = findBlockEntity(ctx, pos, TardisExteriorBlockEntity.class)
                .map(be -> be.getExterior().id())
                .orElse("unknown");

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

    public static int linkExterior(CommandContext<CommandSourceStack> ctx) {
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

        Optional<BlockPos> lookedAt = getLookedAtBlockPos(ctx);
        if (lookedAt.isEmpty()) {
            ctx.getSource().sendFailure(Component.translatable(
                    "command.gallifreyan_chronicles.not_looking_at_exterior"
            ));
            return 0;
        }

        BlockPos pos = lookedAt.get();
        if (findBlockEntity(ctx, pos, TardisExteriorBlockEntity.class).isEmpty()) {
            ctx.getSource().sendFailure(Component.translatable(
                    "command.gallifreyan_chronicles.not_looking_at_exterior"
            ));
            return 0;
        }

        ServerLevel level = ctx.getSource().getLevel();
        ExteriorLinkSystem.link(level, tardisId, pos);

        ctx.getSource().sendSuccess(
                () -> Component.translatable(
                        "command.gallifreyan_chronicles.link_exterior",
                        tardisIdStr
                ),
                true
        );

        return 1;
    }

    public static int unlinkExterior(CommandContext<CommandSourceStack> ctx) {
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

        ServerLevel level = ctx.getSource().getLevel();
        ExteriorLinkSystem.unlink(level, tardisId);

        ctx.getSource().sendSuccess(
                () -> Component.translatable(
                        "command.gallifreyan_chronicles.unlink_exterior",
                        tardisIdStr
                ),
                true
        );

        return 1;
    }

    public static int linkConsole(CommandContext<CommandSourceStack> ctx) {
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

        Optional<BlockPos> lookedAt = getLookedAtBlockPos(ctx);
        if (lookedAt.isEmpty()) {
            ctx.getSource().sendFailure(Component.translatable(
                    "command.gallifreyan_chronicles.not_looking_at_console"
            ));
            return 0;
        }

        BlockPos pos = lookedAt.get();
        if (findBlockEntity(ctx, pos, TardisConsoleBlockEntity.class).isEmpty()) {
            ctx.getSource().sendFailure(Component.translatable(
                    "command.gallifreyan_chronicles.not_looking_at_console"
            ));
            return 0;
        }

        ServerLevel level = ctx.getSource().getLevel();
        ConsoleLinkSystem.link(level, tardisId, pos);

        ctx.getSource().sendSuccess(
                () -> Component.translatable(
                        "command.gallifreyan_chronicles.link_console",
                        tardisIdStr
                ),
                true
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

        ids.forEach(id -> {
            BlockPos pos = manager.resolveExteriorBlockPos(id);
            String exterior = findBlockEntity(ctx, pos, TardisExteriorBlockEntity.class)
                    .map(be -> be.getExterior().id())
                    .orElse("unknown");
            String posStr = pos != null
                    ? pos.getX() + " " + pos.getY() + " " + pos.getZ()
                    : "unplaced";

            ctx.getSource().sendSuccess(
                    () -> Component.translatable(
                            "command.gallifreyan_chronicles.list_tardis",
                            id.toString(),
                            exterior,
                            posStr
                    ),
                    false
            );
        });

        return 1;
    }

    public static int listControls(CommandContext<CommandSourceStack> ctx) {
        Optional<BlockPos> lookedAt = getLookedAtBlockPos(ctx);
        if (lookedAt.isEmpty()) {
            ctx.getSource().sendFailure(Component.translatable(
                    "command.gallifreyan_chronicles.not_looking_at_console"
            ));
            return 0;
        }

        Optional<TardisConsoleBlockEntity> console = findBlockEntity(ctx, lookedAt.get(), TardisConsoleBlockEntity.class);
        if (console.isEmpty()) {
            ctx.getSource().sendFailure(Component.translatable(
                    "command.gallifreyan_chronicles.not_looking_at_console"
            ));
            return 0;
        }

        var controls = console.get().getControlEntities();
        if (controls.isEmpty()) {
            ctx.getSource().sendSuccess(
                    () -> Component.literal("This console has no control entities."),
                    false
            );
            return 0;
        }

        ServerPlayer player;
        try {
            player = ctx.getSource().getPlayerOrException();
        } catch (com.mojang.brigadier.exceptions.CommandSyntaxException e) {
            ctx.getSource().sendFailure(Component.literal("This command must be run by a player."));
            return 0;
        }

        for (var control : controls) {
            control.printPosition(player);
        }

        return controls.size();
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
        BlockPos pos = manager.resolveExteriorBlockPos(tardisId);
        String posStr = pos != null
                ? pos.getX() + " " + pos.getY() + " " + pos.getZ()
                : "unplaced";
        String exteriorId = findBlockEntity(ctx, pos, TardisExteriorBlockEntity.class)
                .map(be -> be.getExterior().id())
                .orElse("unknown");

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
                        exteriorId
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
