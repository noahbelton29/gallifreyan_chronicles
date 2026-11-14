package com.noahtnt2009.gallifreyan_chronicles.command;

import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ClickEvent;
import com.noahtnt2009.gallifreyan_chronicles.tardis.interior.TardisInteriorSystem;
import com.noahtnt2009.gallifreyan_chronicles.tardis.interior.TardisDimensionManager;
import com.noahtnt2009.gallifreyan_chronicles.tardis.api.TardisApi;
import com.noahtnt2009.gallifreyan_chronicles.block.TardisTopBlock;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisConsoleBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisExteriorBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.entity.TardisControlEntity;
import com.noahtnt2009.gallifreyan_chronicles.tardis.api.Tardis;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.system.TardisLandingSystem;
import com.noahtnt2009.gallifreyan_chronicles.tardis.flight.TardisDamage;
import com.noahtnt2009.gallifreyan_chronicles.tardis.exterior.TardisExterior;
import com.noahtnt2009.gallifreyan_chronicles.tardis.exterior.TardisExteriorRegistry;
import com.noahtnt2009.gallifreyan_chronicles.tardis.manager.TardisDeletion;
import com.noahtnt2009.gallifreyan_chronicles.tardis.manager.TardisDeletionMode;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.noahtnt2009.gallifreyan_chronicles.command.CommandUtil.*;
import static net.minecraft.commands.Commands.literal;

public final class TardisCommands {
    private static final int SUMMON_DISTANCE = 5;

    private TardisCommands() {
    }

    public static LiteralArgumentBuilder<CommandSourceStack> list() {
        return literal("list").executes(TardisCommands::list);
    }

    public static LiteralArgumentBuilder<CommandSourceStack> deletion() {
        return literal("deletion")
                .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                .then(literal("on").executes(ctx -> setDeletionMode(ctx, true)))
                .then(literal("off").executes(ctx -> setDeletionMode(ctx, false)));
    }

    public static LiteralArgumentBuilder<CommandSourceStack> info() {
        return literal("info").executes(TardisCommands::info);
    }

    public static LiteralArgumentBuilder<CommandSourceStack> summon() {
        return literal("summon").executes(TardisCommands::summon);
    }

    public static LiteralArgumentBuilder<CommandSourceStack> repair() {
        return literal("repair").executes(TardisCommands::repair);
    }

    public static LiteralArgumentBuilder<CommandSourceStack> tp() {
        return literal("tp")
                .then(literal("exterior").executes(TardisCommands::tpExterior))
                .then(literal("interior").executes(TardisCommands::tpInterior));
    }

    public static LiteralArgumentBuilder<CommandSourceStack> id() {
        return literal("id").executes(TardisCommands::id);
    }

    private static int tpExterior(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return withTardis(ctx, (tardis, idText) -> {
            ServerPlayer player = playerOrFail(ctx);
            if (player == null) return 0;
            if (tardis.exteriorPos().isEmpty()) return fail(ctx, "no_exterior", idText);

            TardisInteriorSystem.exitInterior(ctx.getSource().getServer(), player, tardis.id());
            return success(ctx, false, "tp_exterior", idText);
        });
    }

    private static int tpInterior(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return withTardis(ctx, (tardis, idText) -> {
            ServerPlayer player = playerOrFail(ctx);
            if (player == null) return 0;

            /* loads the interior first so you go straight in */
            TardisDimensionManager.getOrCreate(ctx.getSource().getServer(), tardis.id());
            TardisDimensionManager.requestEntry(ctx.getSource().getServer(), player, tardis.id());
            return success(ctx, false, "tp_interior", idText);
        });
    }

    /* the id of the tardis you're looking at, or the one you're inside. click it to copy */
    private static int id(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = playerOrFail(ctx);
        if (player == null) return 0;

        ServerLevel level = ctx.getSource().getLevel();
        Optional<Tardis> tardis = lookedAtBlock(ctx)
                .flatMap(pos -> tardisAt(level, pos))
                .or(() -> TardisApi.ofLevel(level));
        if (tardis.isEmpty()) return fail(ctx, "no_tardis_here");

        String id = tardis.get().id().toString();
        Component copy = Component.literal(id).withStyle(style -> style
                .withColor(ChatFormatting.GREEN)
                .withUnderlined(true)
                .withClickEvent(new ClickEvent.CopyToClipboard(id))
                .withHoverEvent(new HoverEvent.ShowText(text("click_to_copy"))));
        send(ctx, text("tardis_id", copy), false);
        return 1;
    }

    private static Optional<Tardis> tardisAt(ServerLevel level, BlockPos pos) {
        BlockPos exteriorPos = level.getBlockState(pos).getBlock() instanceof TardisTopBlock ? pos.below() : pos;
        if (level.getBlockEntity(exteriorPos) instanceof TardisExteriorBlockEntity exterior) return TardisApi.of(exterior);
        if (level.getBlockEntity(pos) instanceof TardisConsoleBlockEntity console) return TardisApi.of(console);
        return Optional.empty();
    }

    private static int repair(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return withTardis(ctx, (tardis, idText) -> {
            if (!TardisDamage.isCrashed(tardis)) return fail(ctx, "not_crashed", idText);
            TardisDamage.repair(tardis);
            return success(ctx, true, "repaired", idText);
        });
    }

    public static LiteralArgumentBuilder<CommandSourceStack> remove() {
        return literal("remove").executes(TardisCommands::remove);
    }

    public static LiteralArgumentBuilder<CommandSourceStack> debug() {
        return literal("debug")
                .then(literal("exteriors").executes(TardisCommands::listExteriors))
                .then(literal("controls").executes(TardisCommands::listControls))
                .then(literal("tardis")
                        .then(tardisIdArgument().executes(TardisCommands::info)));
    }

    private static int list(CommandContext<CommandSourceStack> ctx) {
        Collection<UUID> ids = manager(ctx).getAllIds();
        if (ids.isEmpty()) {
            success(ctx, false, "no_tardises");
            return 0;
        }

        for (UUID id : ids) {
            BlockPos pos = manager(ctx).resolveExteriorBlockPos(id);
            success(ctx, false, "list_tardis", id.toString(), exteriorIdAt(ctx, pos), describe(pos));
        }
        return 1;
    }

    private static int info(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return withTardis(ctx, (tardis, idText) -> {
            BlockPos pos = manager(ctx).resolveExteriorBlockPos(tardis.id());
            String owner = tardis.ownerId() != null ? tardis.ownerId().toString() : "None";

            success(ctx, false, "debug", idText);
            success(ctx, false, "owner", owner);
            success(ctx, false, "exterior", exteriorIdAt(ctx, pos));
            return success(ctx, false, "position", describe(pos));
        });
    }

    private static String exteriorIdAt(CommandContext<CommandSourceStack> ctx, BlockPos pos) {
        return findBlockEntity(ctx, pos, TardisExteriorBlockEntity.class)
                .map(be -> be.getExterior().id())
                .orElse("unknown");
    }

    private static int setDeletionMode(CommandContext<CommandSourceStack> ctx, boolean enabled) {
        ServerPlayer player = playerOrFail(ctx);
        if (player == null) return 0;

        TardisDeletionMode.setEnabled(player.getUUID(), enabled);
        String key = enabled ? "deletion_mode_on" : "deletion_mode_off";
        send(ctx, text(key).withStyle(enabled ? ChatFormatting.RED : ChatFormatting.GREEN), false);
        return 1;
    }

    private static int remove(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return withTardisId(ctx, (id, idText) -> {
            if (!TardisDeletion.removeCompletely(ctx.getSource().getServer(), id)) {
                return fail(ctx, "no_tardis_id", idText);
            }
            return success(ctx, true, "remove_tardis", idText);
        });
    }

    private static int summon(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return withTardis(ctx, (tardis, idText) -> {
            ServerPlayer player = playerOrFail(ctx);
            if (player == null) return 0;
            return moveInFrontOf(ctx, tardis, player, idText);
        });
    }

    private static int moveInFrontOf(CommandContext<CommandSourceStack> ctx, Tardis tardis, ServerPlayer player,
                                     String idText) {
        ServerLevel oldLevel = tardis.exteriorLevel().orElse(null);
        BlockPos oldPos = tardis.exteriorPos().orElse(null);
        if (oldLevel == null || oldPos == null || tardis.exterior().isEmpty()) return fail(ctx, "no_exterior", idText);

        ServerLevel level = ctx.getSource().getLevel();
        BlockPos pos = player.blockPosition().relative(player.getDirection(), SUMMON_DISTANCE);
        boolean samePlace = level == oldLevel && pos.equals(oldPos);
        if (!samePlace && !level.getBlockState(pos).canBeReplaced()) return fail(ctx, "summon_blocked");

        if (TardisLandingSystem.moveExterior(tardis, level, pos) == null) return fail(ctx, "no_exterior", idText);
        return success(ctx, true, "summon_tardis", idText, pos.toShortString());
    }

    private static int listExteriors(CommandContext<CommandSourceStack> ctx) {
        String ids = TardisExteriorRegistry.getAll().stream()
                .map(TardisExterior::id)
                .collect(Collectors.joining(", "));
        return success(ctx, false, "list_exterior", ids);
    }

    private static int listControls(CommandContext<CommandSourceStack> ctx) {
        Optional<TardisConsoleBlockEntity> console = lookedAtBlock(ctx)
                .flatMap(pos -> findBlockEntity(ctx, pos, TardisConsoleBlockEntity.class));
        if (console.isEmpty()) return fail(ctx, "not_looking_at_console");

        List<TardisControlEntity> controls = console.get().getControlEntities();
        if (controls.isEmpty()) {
            success(ctx, false, "no_control_entities");
            return 0;
        }

        ServerPlayer player = playerOrFail(ctx);
        if (player == null) return 0;

        controls.forEach(control -> control.printPosition(player));
        return controls.size();
    }
}
