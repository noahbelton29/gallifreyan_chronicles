package com.noahtnt2009.gallifreyan_chronicles.command;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.noahtnt2009.gallifreyan_chronicles.block.TardisBlock;
import com.noahtnt2009.gallifreyan_chronicles.block.entity.InteriorDoorBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisConsoleBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisExteriorBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.init.GCBlocks;
import com.noahtnt2009.gallifreyan_chronicles.network.TardisConsoleSyncPayload;
import com.noahtnt2009.gallifreyan_chronicles.platform.Services;
import com.noahtnt2009.gallifreyan_chronicles.tardis.console.TardisConsole;
import com.noahtnt2009.gallifreyan_chronicles.tardis.console.TardisConsoleRegistry;
import com.noahtnt2009.gallifreyan_chronicles.entity.TardisControlEntity;
import com.noahtnt2009.gallifreyan_chronicles.tardis.control.TardisControlRegistry;
import com.noahtnt2009.gallifreyan_chronicles.tardis.door.TardisDoorRegistry;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.component.TardisComponent;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.component.TardisSubsystem;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.system.ConsoleLinkSystem;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.system.DoorSyncSystem;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.system.ExteriorLinkSystem;
import com.noahtnt2009.gallifreyan_chronicles.tardis.interior.TardisDimensionManager;
import com.noahtnt2009.gallifreyan_chronicles.tardis.interior.TardisInteriorRegistry;
import com.noahtnt2009.gallifreyan_chronicles.tardis.interior.TardisInteriorSystem;
import com.noahtnt2009.gallifreyan_chronicles.tardis.manager.TardisManager;
import com.noahtnt2009.gallifreyan_chronicles.tardis.exterior.TardisExterior;
import com.noahtnt2009.gallifreyan_chronicles.tardis.exterior.TardisExteriorRegistry;
import com.noahtnt2009.gallifreyan_chronicles.tardis.exterior.fade.FadeType;
import com.noahtnt2009.gallifreyan_chronicles.tardis.exterior.fade.FadeTypes;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

public class TardisCommand {
    private static <T extends BlockEntity> Optional<T> findBlockEntity(
            CommandContext<CommandSourceStack> ctx,
            BlockPos pos,
            Class<T> entityClass
    ) {
        if (pos == null) return Optional.empty();

        ServerLevel sourceLevel = ctx.getSource().getLevel();
        BlockEntity local = sourceLevel.getBlockEntity(pos);
        if (entityClass.isInstance(local)) {
            return Optional.of(entityClass.cast(local));
        }

        for (ServerLevel level : ctx.getSource().getServer().getAllLevels()) {
            if (level == sourceLevel) continue;
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

    public static CompletableFuture<Suggestions> suggestInteriors(
            CommandContext<CommandSourceStack> ctx,
            com.mojang.brigadier.suggestion.SuggestionsBuilder builder
    ) {
        TardisInteriorRegistry.getAll().forEach(interior -> builder.suggest(interior.id()));
        return builder.buildFuture();
    }

    public static int getInterior(CommandContext<CommandSourceStack> ctx) {
        String tardisIdStr = StringArgumentType.getString(ctx, "id");
        UUID tardisId;
        try {
            tardisId = UUID.fromString(tardisIdStr);
        } catch (IllegalArgumentException e) {
            ctx.getSource().sendFailure(Component.translatable("command.gallifreyan_chronicles.invalid_id", tardisIdStr));
            return 0;
        }

        var server = ctx.getSource().getServer();
        if (TardisManager.get(server).get(tardisId).isEmpty()) {
            ctx.getSource().sendFailure(Component.translatable("command.gallifreyan_chronicles.no_tardis_id", tardisIdStr));
            return 0;
        }

        String interiorId = TardisInteriorSystem.getInteriorDefinition(server, tardisId).id();
        ctx.getSource().sendSuccess(
                () -> Component.translatable("command.gallifreyan_chronicles.get_interior", tardisIdStr, interiorId),
                false
        );
        return 1;
    }

    public static int setInterior(CommandContext<CommandSourceStack> ctx) {
        String tardisIdStr = StringArgumentType.getString(ctx, "id");
        String interiorId = StringArgumentType.getString(ctx, "interior_id");

        UUID tardisId;
        try {
            tardisId = UUID.fromString(tardisIdStr);
        } catch (IllegalArgumentException e) {
            ctx.getSource().sendFailure(Component.translatable("command.gallifreyan_chronicles.invalid_id", tardisIdStr));
            return 0;
        }

        var server = ctx.getSource().getServer();
        if (TardisManager.get(server).get(tardisId).isEmpty()) {
            ctx.getSource().sendFailure(Component.translatable("command.gallifreyan_chronicles.no_tardis_id", tardisIdStr));
            return 0;
        }

        switch (TardisInteriorSystem.changeInterior(server, tardisId, interiorId)) {
            case UNKNOWN_INTERIOR -> {
                ctx.getSource().sendFailure(Component.translatable("command.gallifreyan_chronicles.unknown_interior", interiorId));
                return 0;
            }
            case MISSING_TEMPLATE -> {
                ctx.getSource().sendFailure(Component.translatable("command.gallifreyan_chronicles.missing_interior_template", interiorId));
                return 0;
            }
            case UNCHANGED -> {
                ctx.getSource().sendFailure(Component.translatable("command.gallifreyan_chronicles.interior_unchanged", tardisIdStr, interiorId));
                return 0;
            }
            case CHANGED -> {
                ctx.getSource().sendSuccess(
                        () -> Component.translatable("command.gallifreyan_chronicles.set_interior", tardisIdStr, interiorId),
                        true
                );
                return 1;
            }
        }
        return 0;
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

    public static CompletableFuture<com.mojang.brigadier.suggestion.Suggestions> suggestConsoleVariants(
            CommandContext<CommandSourceStack> ctx,
            com.mojang.brigadier.suggestion.SuggestionsBuilder builder
    ) {
        String tardisIdStr;
        try {
            tardisIdStr = StringArgumentType.getString(ctx, "id");
        } catch (IllegalArgumentException e) {
            return builder.buildFuture();
        }

        UUID tardisId;
        try {
            tardisId = UUID.fromString(tardisIdStr);
        } catch (IllegalArgumentException e) {
            return builder.buildFuture();
        }

        TardisManager manager = TardisManager.get(ctx.getSource().getServer());
        BlockPos pos = manager.getConsoleBlockPos(tardisId);
        findBlockEntity(ctx, pos, TardisConsoleBlockEntity.class)
                .map(TardisConsoleBlockEntity::getConsole)
                .ifPresent(console -> console.variantIds().forEach(builder::suggest));

        return builder.buildFuture();
    }

    public static int setConsoleVariant(CommandContext<CommandSourceStack> ctx) {
        String tardisIdStr = StringArgumentType.getString(ctx, "id");
        String variantId = StringArgumentType.getString(ctx, "variant_id");

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

        BlockPos pos = manager.getConsoleBlockPos(tardisId);
        Optional<TardisConsoleBlockEntity> blockEntity = findBlockEntity(ctx, pos, TardisConsoleBlockEntity.class);

        if (blockEntity.isEmpty()) {
            ctx.getSource().sendFailure(Component.translatable(
                    "command.gallifreyan_chronicles.no_console",
                    tardisIdStr
            ));
            return 0;
        }

        TardisConsoleBlockEntity be = blockEntity.get();
        TardisConsole console = be.getConsole();
        if (!console.hasVariant(variantId)) {
            ctx.getSource().sendFailure(Component.translatable(
                    "command.gallifreyan_chronicles.unknown_console_variant",
                    variantId,
                    console.id()
            ));
            return 0;
        }

        be.setVariant(variantId);

        ServerPlayer sourcePlayer = ctx.getSource().getPlayer();
        if (sourcePlayer != null) {
            Services.NETWORK.sendTardisConsoleSync(sourcePlayer, TardisConsoleSyncPayload.create());
        }

        ctx.getSource().sendSuccess(
                () -> Component.translatable(
                        "command.gallifreyan_chronicles.set_console_variant",
                        tardisIdStr,
                        variantId
                ),
                true
        );

        return 1;
    }

    public static CompletableFuture<Suggestions> suggestDoorVariants(
            CommandContext<CommandSourceStack> ctx,
            SuggestionsBuilder builder
    ) {
        try {
            UUID tardisId = UUID.fromString(StringArgumentType.getString(ctx, "id"));
            DoorSyncSystem.doorsOf(tardisId).stream().findFirst()
                    .ifPresent(door -> TardisDoorRegistry.variantIds(door.getVariant()).forEach(builder::suggest));
        } catch (IllegalArgumentException ignored) {
        }
        return builder.buildFuture();
    }

    public static int setDoorVariant(CommandContext<CommandSourceStack> ctx) {
        String tardisIdStr = StringArgumentType.getString(ctx, "id");
        String variantId = StringArgumentType.getString(ctx, "variant_id");

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

        List<InteriorDoorBlockEntity> doors = DoorSyncSystem.doorsOf(tardisId);
        if (doors.isEmpty()) {
            ctx.getSource().sendFailure(Component.translatable(
                    "command.gallifreyan_chronicles.no_interior_door",
                    tardisIdStr
            ));
            return 0;
        }

        int changed = 0;
        for (InteriorDoorBlockEntity door : doors) {
            if (!TardisDoorRegistry.hasVariant(door.getVariant(), variantId)) {
                ctx.getSource().sendFailure(Component.translatable(
                        "command.gallifreyan_chronicles.unknown_door_variant",
                        variantId,
                        door.getVariant()
                ));
                continue;
            }
            door.setDoorVariant(variantId);
            changed++;
        }
        if (changed == 0) return 0;

        int count = changed;
        ctx.getSource().sendSuccess(
                () -> Component.translatable(
                        "command.gallifreyan_chronicles.set_door_variant",
                        tardisIdStr,
                        variantId,
                        count
                ),
                true
        );
        return count;
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

        BlockPos pos = manager.getRegisteredBlockPos(tardisId);
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

    public static CompletableFuture<Suggestions> suggestFadeTypes(
            CommandContext<CommandSourceStack> ctx,
            SuggestionsBuilder builder
    ) {
        for (String id : FadeTypes.suggestionIds()) {
            builder.suggest(id.contains(":") ? "\"" + id + "\"" : id);
        }
        return builder.buildFuture();
    }

    public static int getFadeType(CommandContext<CommandSourceStack> ctx) {
        return withFadeExterior(ctx, (tardisIdStr, be) -> {
            ctx.getSource().sendSuccess(
                    () -> Component.translatable(
                            "command.gallifreyan_chronicles.get_fade_type",
                            tardisIdStr,
                            be.getFadeType().id()
                    ),
                    false
            );
            return 1;
        });
    }

    public static int setFadeType(CommandContext<CommandSourceStack> ctx) {
        String typeId = StringArgumentType.getString(ctx, "fade_type");
        if (!FadeTypes.contains(typeId)) {
            ctx.getSource().sendFailure(Component.translatable(
                    "command.gallifreyan_chronicles.unknown_fade_type",
                    typeId,
                    String.join(", ", FadeTypes.suggestionIds())
            ));
            return 0;
        }
        FadeType type = FadeTypes.get(typeId);

        return withFadeExterior(ctx, (tardisIdStr, be) -> {
            be.setFadeType(type);
            ctx.getSource().sendSuccess(
                    () -> Component.translatable(
                            "command.gallifreyan_chronicles.set_fade_type",
                            tardisIdStr,
                            type.id()
                    ),
                    true
            );
            return 1;
        });
    }

    private static int withFadeExterior(
            CommandContext<CommandSourceStack> ctx,
            BiFunction<String, TardisExteriorBlockEntity, Integer> action
    ) {
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
        if (manager.get(tardisId).isEmpty()) {
            ctx.getSource().sendFailure(Component.translatable(
                    "command.gallifreyan_chronicles.no_tardis_id",
                    tardisIdStr
            ));
            return 0;
        }

        BlockPos pos = manager.getRegisteredBlockPos(tardisId);
        Optional<TardisExteriorBlockEntity> blockEntity = findBlockEntity(ctx, pos, TardisExteriorBlockEntity.class);
        if (blockEntity.isEmpty()) {
            ctx.getSource().sendFailure(Component.translatable(
                    "command.gallifreyan_chronicles.no_exterior",
                    tardisIdStr
            ));
            return 0;
        }

        return action.apply(tardisIdStr, blockEntity.get());
    }

    private static Optional<TardisConsoleBlockEntity> loadConsole(CommandContext<CommandSourceStack> ctx,
                                                                  UUID tardisId) {
        MinecraftServer server = ctx.getSource().getServer();
        BlockPos pos = TardisManager.get(server).getConsoleBlockPos(tardisId);
        if (pos == null) return Optional.empty();

        ServerLevel interior = TardisDimensionManager.get(server, tardisId);
        if (interior != null) {
            interior.getChunkAt(pos);
            if (interior.getBlockEntity(pos) instanceof TardisConsoleBlockEntity console) {
                return Optional.of(console);
            }
        }
        return findBlockEntity(ctx, pos, TardisConsoleBlockEntity.class);
    }

    public static CompletableFuture<Suggestions> suggestControls(
            CommandContext<CommandSourceStack> ctx,
            SuggestionsBuilder builder
    ) {
        try {
            UUID tardisId = UUID.fromString(StringArgumentType.getString(ctx, "id"));
            BlockPos pos = TardisManager.get(ctx.getSource().getServer()).getConsoleBlockPos(tardisId);
            Optional<TardisConsoleBlockEntity> console = findBlockEntity(ctx, pos, TardisConsoleBlockEntity.class);
            if (console.isPresent()) {
                console.get().getControlEntities().forEach(control -> builder.suggest(control.getControlId()));
                return builder.buildFuture();
            }
        } catch (IllegalArgumentException ignored) {
        }
        TardisControlRegistry.allIds().forEach(builder::suggest);
        return builder.buildFuture();
    }

    public static int setControl(CommandContext<CommandSourceStack> ctx, boolean on) {
        String tardisIdStr = StringArgumentType.getString(ctx, "id");
        String controlId = StringArgumentType.getString(ctx, "control");

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

        if (TardisManager.get(ctx.getSource().getServer()).get(tardisId).isEmpty()) {
            ctx.getSource().sendFailure(Component.translatable(
                    "command.gallifreyan_chronicles.no_tardis_id",
                    tardisIdStr
            ));
            return 0;
        }

        if (!TardisControlRegistry.allIds().contains(controlId)) {
            ctx.getSource().sendFailure(Component.translatable(
                    "command.gallifreyan_chronicles.unknown_control",
                    controlId
            ));
            return 0;
        }

        Optional<TardisConsoleBlockEntity> console = loadConsole(ctx, tardisId);
        if (console.isEmpty()) {
            ctx.getSource().sendFailure(Component.translatable(
                    "command.gallifreyan_chronicles.no_console",
                    tardisIdStr
            ));
            return 0;
        }

        TardisControlEntity entity = null;
        for (TardisControlEntity candidate : console.get().getControlEntities()) {
            if (candidate.getControlId().equals(controlId)) {
                entity = candidate;
                break;
            }
        }
        if (entity == null) {
            ctx.getSource().sendFailure(Component.translatable(
                    "command.gallifreyan_chronicles.control_not_on_console",
                    controlId,
                    tardisIdStr
            ));
            return 0;
        }

        String stateName = on ? "on" : "off";
        if (console.get().getControlState(controlId) == on) {
            ctx.getSource().sendFailure(Component.translatable(
                    "command.gallifreyan_chronicles.control_already",
                    controlId,
                    stateName
            ));
            return 0;
        }

        InteractionResult result = entity.activate(ctx.getSource().getPlayer(), InteractionHand.MAIN_HAND);
        if (result != InteractionResult.SUCCESS) {
            ctx.getSource().sendFailure(Component.translatable(
                    "command.gallifreyan_chronicles.control_refused",
                    controlId,
                    stateName
            ));
            return 0;
        }

        ctx.getSource().sendSuccess(
                () -> Component.translatable(
                        "command.gallifreyan_chronicles.set_control",
                        controlId,
                        tardisIdStr,
                        stateName
                ),
                true
        );
        return 1;
    }

    public static CompletableFuture<Suggestions> suggestSubsystems(
            CommandContext<CommandSourceStack> ctx,
            SuggestionsBuilder builder
    ) {
        for (TardisSubsystem subsystem : TardisSubsystem.values()) {
            builder.suggest(subsystem.id());
        }
        return builder.buildFuture();
    }

    private static Optional<TardisExteriorBlockEntity> resolveExteriorForSubsystem(
            CommandContext<CommandSourceStack> ctx, String tardisIdStr) {
        UUID tardisId;
        try {
            tardisId = UUID.fromString(tardisIdStr);
        } catch (IllegalArgumentException e) {
            ctx.getSource().sendFailure(Component.translatable(
                    "command.gallifreyan_chronicles.invalid_id",
                    tardisIdStr
            ));
            return Optional.empty();
        }

        TardisManager manager = TardisManager.get(ctx.getSource().getServer());
        if (manager.get(tardisId).isEmpty()) {
            ctx.getSource().sendFailure(Component.translatable(
                    "command.gallifreyan_chronicles.no_tardis_id",
                    tardisIdStr
            ));
            return Optional.empty();
        }

        BlockPos pos = manager.resolveExteriorBlockPos(tardisId);
        Optional<TardisExteriorBlockEntity> blockEntity = findBlockEntity(ctx, pos, TardisExteriorBlockEntity.class);
        if (blockEntity.isEmpty()) {
            ctx.getSource().sendFailure(Component.translatable(
                    "command.gallifreyan_chronicles.no_exterior",
                    tardisIdStr
            ));
        }
        return blockEntity;
    }

    private static Optional<TardisSubsystem> parseSubsystem(CommandContext<CommandSourceStack> ctx) {
        String id = StringArgumentType.getString(ctx, "subsystem");
        Optional<TardisSubsystem> subsystem = TardisSubsystem.byId(id);
        if (subsystem.isEmpty()) {
            ctx.getSource().sendFailure(Component.translatable(
                    "command.gallifreyan_chronicles.unknown_subsystem",
                    id
            ));
        }
        return subsystem;
    }

    public static int setSubsystemHealth(CommandContext<CommandSourceStack> ctx) {
        String tardisIdStr = StringArgumentType.getString(ctx, "id");
        float health = FloatArgumentType.getFloat(ctx, "health");

        Optional<TardisSubsystem> subsystem = parseSubsystem(ctx);
        if (subsystem.isEmpty()) return 0;

        Optional<TardisExteriorBlockEntity> blockEntity = resolveExteriorForSubsystem(ctx, tardisIdStr);
        if (blockEntity.isEmpty()) return 0;

        blockEntity.get().setSubsystemHealth(subsystem.get(), health);

        ctx.getSource().sendSuccess(
                () -> Component.translatable(
                        "command.gallifreyan_chronicles.set_subsystem_health",
                        subsystem.get().id(),
                        tardisIdStr,
                        String.valueOf(health)
                ),
                true
        );

        return 1;
    }

    public static int getSubsystemHealth(CommandContext<CommandSourceStack> ctx) {
        String tardisIdStr = StringArgumentType.getString(ctx, "id");

        Optional<TardisSubsystem> subsystem = parseSubsystem(ctx);
        if (subsystem.isEmpty()) return 0;

        Optional<TardisExteriorBlockEntity> blockEntity = resolveExteriorForSubsystem(ctx, tardisIdStr);
        if (blockEntity.isEmpty()) return 0;

        float health = blockEntity.get().getSubsystemHealth(subsystem.get());

        ctx.getSource().sendSuccess(
                () -> Component.translatable(
                        "command.gallifreyan_chronicles.get_subsystem_health",
                        subsystem.get().id(),
                        tardisIdStr,
                        String.valueOf(health)
                ),
                false
        );

        return Math.round(health);
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
                    () -> Component.translatable(
                            "command.gallifreyan_chronicles.no_control_entities"
                    ),
                    false
            );
            return 0;
        }

        ServerPlayer player;
        try {
            player = ctx.getSource().getPlayerOrException();
        } catch (com.mojang.brigadier.exceptions.CommandSyntaxException e) {
            ctx.getSource().sendFailure(Component.translatable(
                    "command.gallifreyan_chronicles.player_only"
            ));
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
                        component.ownerId() != null
                                ? component.ownerId().toString()
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

    private static final int SUMMON_DISTANCE = 5;

    // debug only: moves the exterior in front of the player, any dimension
    public static int summonTardis(CommandContext<CommandSourceStack> ctx) {
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

        ServerPlayer player;
        try {
            player = ctx.getSource().getPlayerOrException();
        } catch (com.mojang.brigadier.exceptions.CommandSyntaxException e) {
            ctx.getSource().sendFailure(Component.translatable("command.gallifreyan_chronicles.player_only"));
            return 0;
        }

        MinecraftServer server = ctx.getSource().getServer();
        TardisManager manager = TardisManager.get(server);
        if (manager.get(tardisId).isEmpty()) {
            ctx.getSource().sendFailure(Component.translatable(
                    "command.gallifreyan_chronicles.no_tardis_id",
                    tardisIdStr
            ));
            return 0;
        }

        ServerLevel oldLevel = server.getLevel(manager.getExteriorDimension(tardisId));
        BlockPos oldPos = manager.resolveExteriorBlockPos(tardisId);
        if (oldLevel == null || oldPos == null
                || !(oldLevel.getBlockEntity(oldPos) instanceof TardisExteriorBlockEntity oldExterior)) {
            ctx.getSource().sendFailure(Component.translatable(
                    "command.gallifreyan_chronicles.no_exterior",
                    tardisIdStr
            ));
            return 0;
        }

        ServerLevel targetLevel = ctx.getSource().getLevel();
        BlockPos targetPos = player.blockPosition().relative(player.getDirection(), SUMMON_DISTANCE);
        boolean samePlace = targetLevel == oldLevel && targetPos.equals(oldPos);
        if (!samePlace && !targetLevel.getBlockState(targetPos).canBeReplaced()) {
            ctx.getSource().sendFailure(Component.translatable("command.gallifreyan_chronicles.summon_blocked"));
            return 0;
        }

        boolean glowing = oldExterior.getBlockState().getValue(TardisBlock.GLOWING);

        oldLevel.removeBlock(oldPos, false);
        if (oldLevel.getBlockState(oldPos.above()).is(GCBlocks.TARDIS_TOP)) {
            oldLevel.removeBlock(oldPos.above(), false);
        }

        targetLevel.setBlock(
                targetPos,
                GCBlocks.TARDIS.defaultBlockState().setValue(TardisBlock.GLOWING, glowing),
                Block.UPDATE_ALL
        );
        if (!(targetLevel.getBlockEntity(targetPos) instanceof TardisExteriorBlockEntity newExterior)) {
            ctx.getSource().sendFailure(Component.translatable(
                    "command.gallifreyan_chronicles.no_exterior",
                    tardisIdStr
            ));
            return 0;
        }
        newExterior.copyStateFrom(oldExterior);
        newExterior.spawnKeyEntity();

        manager.setRegisteredBlockPos(tardisId, targetPos);
        manager.setExteriorDimension(tardisId, targetLevel.dimension());
        if (manager.getExteriorBlockPos(tardisId) != null) {
            manager.setExteriorBlockPos(tardisId, targetPos);
        }

        ctx.getSource().sendSuccess(
                () -> Component.translatable(
                        "command.gallifreyan_chronicles.summon_tardis",
                        tardisIdStr,
                        targetPos.toShortString()
                ),
                true
        );
        return 1;
    }
}