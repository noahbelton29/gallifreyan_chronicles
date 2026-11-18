package uk.nbforge.gallifreyan_chronicles.command;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;
import static uk.nbforge.gallifreyan_chronicles.command.CommandUtil.*;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import org.jspecify.annotations.Nullable;
import uk.nbforge.gallifreyan_chronicles.Constants;
import uk.nbforge.gallifreyan_chronicles.tardis.Tardis;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.TardisMind;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.TardisMinds;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.TardisPersonality;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.TardisTrait;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.data.TardisPersonalities;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.data.TardisTraits;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.disability.TardisDisability;

public final class MindCommands {
    private MindCommands() {}

    public static LiteralArgumentBuilder<CommandSourceStack> mind() {
        return literal("mind")
                .then(literal("inspect").executes(MindCommands::inspect))
                .then(literal("reroll").executes(MindCommands::reroll))
                .then(literal("undiscover").executes(MindCommands::undiscover))
                .then(literal("forgive")
                        .executes(MindCommands::forgive)
                        .then(argument("player", EntityArgument.player()).executes(MindCommands::forgive)));
    }

    public static LiteralArgumentBuilder<CommandSourceStack> mindData() {
        return literal("mind")
                .then(literal("betrayed")
                        .executes(MindCommands::getBetrayed)
                        .then(argument("betrayed", BoolArgumentType.bool()).executes(MindCommands::setBetrayed))
                        .then(argument("player", EntityArgument.player())
                                .executes(MindCommands::getBetrayed)
                                .then(argument("betrayed", BoolArgumentType.bool())
                                        .executes(MindCommands::setBetrayed))))
                .then(literal("personality")
                        .executes(MindCommands::getPersonality)
                        .then(argument("personality", StringArgumentType.greedyString())
                                .suggests(MindCommands::suggestPersonalities)
                                .executes(MindCommands::setPersonality)))
                .then(literal("traits")
                        .executes(MindCommands::getTraits)
                        .then(literal("add")
                                .then(argument("trait", StringArgumentType.greedyString())
                                        .suggests(MindCommands::suggestTraits)
                                        .executes(MindCommands::addTrait)))
                        .then(literal("remove")
                                .then(argument("trait", StringArgumentType.greedyString())
                                        .suggests(MindCommands::suggestTraits)
                                        .executes(MindCommands::removeTrait)))
                        .then(literal("clear").executes(MindCommands::clearTraits)))
                .then(literal("disabilities")
                        .executes(MindCommands::getDisabilities)
                        .then(literal("add")
                                .then(argument("disability", StringArgumentType.word())
                                        .suggests(MindCommands::suggestDisabilities)
                                        .executes(ctx -> changeDisability(ctx, true))))
                        .then(literal("remove")
                                .then(argument("disability", StringArgumentType.word())
                                        .suggests(MindCommands::suggestDisabilities)
                                        .executes(ctx -> changeDisability(ctx, false))))
                        .then(literal("clear").executes(MindCommands::clearDisabilities)))
                .then(value(MindValue.STRESS))
                .then(value(MindValue.TRUST))
                .then(value(MindValue.FEAR))
                .then(value(MindValue.SKILL))
                .then(value(MindValue.TRAUMA))
                .then(value(MindValue.TOTAL_TRAUMA))
                .then(value(MindValue.HITS))
                .then(literal("dump").executes(MindCommands::dump));
    }

    private static int dump(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return withTardis(ctx, (tardis, idText) -> {
            send(ctx, dataHeader("TARDIS mind dump", tardis.id()), false);

            TardisMind mind = TardisMinds.mind(tardis);
            sendLine(ctx, dataLine("personality", shortId(mind.personality().id())));
            sendLine(ctx, dataLine("traits", traitList(mind)));
            sendLine(ctx, dataLine("disabilities", disabilityList(mind)));
            sendLine(
                    ctx,
                    dataLine(
                            "betrayed",
                            String.valueOf(TardisMinds.bond(tardis, null).betrayed())));
            for (MindValue which : MindValue.values()) {
                sendLine(ctx, dataLine(which.name, oneDecimal(which.get(tardis, null))));
            }
            return 1;
        });
    }

    private enum MindValue {
        STRESS("stress"),
        TRUST("trust"),
        FEAR("fear"),
        SKILL("skill"),
        TRAUMA("trauma"),
        TOTAL_TRAUMA("total_trauma"),
        HITS("hits");

        private final String name;

        MindValue(String name) {
            this.name = name;
        }

        /**
         * Trust and fear are per player, null means the owner.
         */
        private boolean perPlayer() {
            return this == TRUST || this == FEAR || this == TRAUMA || this == HITS;
        }

        private float get(Tardis tardis, @Nullable UUID player) {
            return switch (this) {
                case STRESS -> TardisMinds.stress(tardis);
                case TRUST -> TardisMinds.trust(tardis, player);
                case FEAR -> TardisMinds.fear(tardis, player);
                case SKILL -> TardisMinds.skill(tardis) * 100f;
                case TRAUMA -> TardisMinds.traumaFrom(tardis, player);
                case TOTAL_TRAUMA -> TardisMinds.trauma(tardis);
                case HITS -> TardisMinds.bond(tardis, player).hits();
            };
        }

        private void set(Tardis tardis, @Nullable UUID player, float value) {
            switch (this) {
                case STRESS -> TardisMinds.setStress(tardis, value);
                case TRUST -> TardisMinds.setTrust(tardis, player, value);
                case FEAR -> {
                    float maxTrauma = value / TardisMind.TRAUMA_FEAR_SHARE;
                    if (TardisMinds.traumaFrom(tardis, player) > maxTrauma) {
                        TardisMinds.setTraumaFrom(tardis, player, maxTrauma);
                    }
                    TardisMinds.setFear(tardis, player, value);
                }
                case SKILL -> TardisMinds.setSkill(tardis, value / 100f);
                case TRAUMA -> TardisMinds.setTraumaFrom(tardis, player, value);
                case TOTAL_TRAUMA -> TardisMinds.setTrauma(tardis, value);
                case HITS -> TardisMinds.setHits(tardis, player, Math.round(value));
            }
        }
    }

    /**
     * Bare = get for the owner, a trailing float = set for the owner; a per-player value also takes an optional player before either.
     */
    private static LiteralArgumentBuilder<CommandSourceStack> value(MindValue which) {
        LiteralArgumentBuilder<CommandSourceStack> node = literal(which.name)
                .executes(ctx -> getValue(ctx, which))
                .then(argument("value", FloatArgumentType.floatArg(0f, 100f)).executes(ctx -> setValue(ctx, which)));
        if (which.perPlayer()) {
            node.then(argument("player", EntityArgument.player())
                    .executes(ctx -> getValue(ctx, which))
                    .then(argument("value", FloatArgumentType.floatArg(0f, 100f))
                            .executes(ctx -> setValue(ctx, which))));
        }
        return node;
    }

    private static CompletableFuture<Suggestions> suggestPersonalities(
            CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        for (TardisPersonality personality : TardisPersonalities.all()) {
            builder.suggest(shortId(personality.id()));
        }
        return builder.buildFuture();
    }

    private static CompletableFuture<Suggestions> suggestTraits(
            CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        for (TardisTrait trait : TardisTraits.all()) {
            builder.suggest(shortId(trait.id()));
        }
        return builder.buildFuture();
    }

    private static int inspect(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return withTardis(ctx, (tardis, idText) -> {
            TardisMind mind = TardisMinds.mind(tardis);
            long now = TardisMinds.now(tardis.server());
            Map<String, String> lines = new LinkedHashMap<>();

            lines.put("personality", shortId(mind.personality().id()));
            lines.put("traits", traitList(mind));
            lines.put("disabilities", disabilityList(mind));
            lines.put("skill", oneDecimal(mind.skill() * 100f));
            lines.put("stress", oneDecimal(TardisMinds.stress(tardis)));
            lines.put("trauma (total)", oneDecimal(TardisMinds.trauma(tardis)));
            lines.put(
                    "ptsd recovery",
                    mind.has(TardisDisability.PTSD) ? oneDecimal(mind.recovery() * 100f) + "%" : "no ptsd");
            lines.put("willing to fly", String.valueOf(!TardisMinds.unwilling(tardis)));
            lines.put("discovered", mind.discovered().isEmpty() ? "nothing" : String.join(", ", mind.discovered()));

            success(ctx, false, "mind_header", idText);
            lines.forEach((key, value) -> success(ctx, false, "data_line", key, value));

            if (mind.bonds().isEmpty()) return success(ctx, false, "mind_no_bonds");
            mind.bonds()
                    .forEach((player, bond) -> success(
                            ctx,
                            false,
                            "mind_bond",
                            playerName(tardis, player) + (tardis.isOwner(player) ? " (owner)" : ""),
                            oneDecimal(bond.trust()),
                            oneDecimal(TardisMinds.fear(tardis, player)),
                            TardisMinds.fearLevel(tardis, player).name().toLowerCase(Locale.ROOT),
                            oneDecimal(bond.traumaAt(now)),
                            bond.hits(),
                            bond.betrayed()));
            return 1;
        });
    }

    private static String playerName(Tardis tardis, UUID player) {
        ServerPlayer online = tardis.server().getPlayerList().getPlayer(player);
        return online != null ? online.getName().getString() : player.toString();
    }

    private static int reroll(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return withTardis(ctx, (tardis, idText) -> {
            TardisMinds.reroll(tardis);
            return inspect(ctx);
        });
    }

    private static int getBetrayed(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        UUID player = playerArg(ctx);
        return withTardis(
                ctx,
                (tardis, idText) -> reply(
                        ctx,
                        tardis,
                        "betrayed",
                        String.valueOf(TardisMinds.bond(tardis, player).betrayed()),
                        false));
    }

    private static int setBetrayed(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        boolean betrayed = BoolArgumentType.getBool(ctx, "betrayed");
        UUID player = playerArg(ctx);
        return withTardis(ctx, (tardis, idText) -> {
            TardisMinds.setBetrayed(tardis, player, betrayed);
            return reply(ctx, tardis, "betrayed", String.valueOf(betrayed), true);
        });
    }

    private static int forgive(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        UUID player = playerArg(ctx);
        return withTardis(ctx, (tardis, idText) -> {
            TardisMinds.forgive(tardis, player);
            return reply(ctx, tardis, "fear, trauma, hits and betrayal", "cleared", true);
        });
    }

    private static int undiscover(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return withTardis(ctx, (tardis, idText) -> {
            TardisMinds.setDiscovered(tardis, List.of());
            return reply(ctx, tardis, "discovered", "none", true);
        });
    }

    private static @Nullable UUID playerArg(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        try {
            return EntityArgument.getPlayer(ctx, "player").getUUID();
        } catch (IllegalArgumentException missing) {
            return null;
        }
    }

    private static int getValue(CommandContext<CommandSourceStack> ctx, MindValue which) throws CommandSyntaxException {
        UUID player = playerArg(ctx);
        return withTardis(
                ctx, (tardis, idText) -> reply(ctx, tardis, which.name, oneDecimal(which.get(tardis, player)), false));
    }

    private static int setValue(CommandContext<CommandSourceStack> ctx, MindValue which) throws CommandSyntaxException {
        float value = FloatArgumentType.getFloat(ctx, "value");
        UUID player = playerArg(ctx);
        return withTardis(ctx, (tardis, idText) -> {
            which.set(tardis, player, value);
            return reply(ctx, tardis, which.name, String.valueOf(value), true);
        });
    }

    private static int getPersonality(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return withTardis(
                ctx,
                (tardis, idText) -> reply(
                        ctx,
                        tardis,
                        "personality",
                        shortId(TardisMinds.mind(tardis).personality().id()),
                        false));
    }

    private static int setPersonality(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        String id = StringArgumentType.getString(ctx, "personality");
        return withTardis(ctx, (tardis, idText) -> {
            Optional<TardisPersonality> personality = TardisPersonalities.byId(id);
            if (personality.isEmpty()) return fail(ctx, "unknown_personality", id);

            TardisMinds.setPersonality(tardis, personality.get());
            return reply(ctx, tardis, "personality", id, true);
        });
    }

    private static int getTraits(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return withTardis(
                ctx, (tardis, idText) -> reply(ctx, tardis, "traits", traitList(TardisMinds.mind(tardis)), false));
    }

    private static int addTrait(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        String id = StringArgumentType.getString(ctx, "trait");
        return withTardis(ctx, (tardis, idText) -> {
            Optional<TardisTrait> trait = TardisTraits.byId(id);
            if (trait.isEmpty()) return fail(ctx, "unknown_trait", id);

            List<TardisTrait> traits = new ArrayList<>(TardisMinds.mind(tardis).traits());
            if (traits.contains(trait.get())) return fail(ctx, "trait_already", id);
            if (traits.size() >= TardisMind.MAX_TRAITS) return fail(ctx, "traits_full", TardisMind.MAX_TRAITS);
            TardisPersonality personality = TardisMinds.mind(tardis).personality();
            if (!trait.get().fits(personality)) return fail(ctx, "trait_doesnt_fit", id, shortId(personality.id()));
            Optional<TardisTrait> clash =
                    traits.stream().filter(trait.get()::clashesWith).findFirst();
            if (clash.isPresent())
                return fail(ctx, "trait_clashes", id, shortId(clash.get().id()));

            traits.add(trait.get());
            return saveTraits(ctx, tardis, traits);
        });
    }

    private static int removeTrait(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        String id = StringArgumentType.getString(ctx, "trait");
        return withTardis(ctx, (tardis, idText) -> {
            Optional<TardisTrait> trait = TardisTraits.byId(id);
            if (trait.isEmpty()) return fail(ctx, "unknown_trait", id);

            List<TardisTrait> traits = new ArrayList<>(TardisMinds.mind(tardis).traits());
            if (!traits.remove(trait.get())) return fail(ctx, "trait_missing", id);

            return saveTraits(ctx, tardis, traits);
        });
    }

    private static int clearTraits(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return withTardis(ctx, (tardis, idText) -> saveTraits(ctx, tardis, List.of()));
    }

    private static int saveTraits(CommandContext<CommandSourceStack> ctx, Tardis tardis, List<TardisTrait> traits) {
        TardisMinds.setTraits(tardis, traits);
        return reply(ctx, tardis, "traits", traitList(TardisMinds.mind(tardis)), true);
    }

    private static int reply(
            CommandContext<CommandSourceStack> ctx, Tardis tardis, String what, String value, boolean changed) {
        return success(
                ctx,
                changed,
                changed ? "set_mind" : "get_mind",
                what,
                tardis.id().toString(),
                value);
    }

    private static CompletableFuture<Suggestions> suggestDisabilities(
            CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        for (TardisDisability disability : TardisDisability.values()) {
            builder.suggest(disability.id());
        }
        return builder.buildFuture();
    }

    private static int getDisabilities(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return withTardis(
                ctx,
                (tardis, idText) ->
                        reply(ctx, tardis, "disabilities", disabilityList(TardisMinds.mind(tardis)), false));
    }

    private static int changeDisability(CommandContext<CommandSourceStack> ctx, boolean add)
            throws CommandSyntaxException {
        String id = StringArgumentType.getString(ctx, "disability");
        return withTardis(ctx, (tardis, idText) -> {
            Optional<TardisDisability> disability = TardisDisability.byId(id);
            if (disability.isEmpty()) return fail(ctx, "unknown_disability", id);

            List<TardisDisability> disabilities =
                    new ArrayList<>(TardisMinds.mind(tardis).disabilities());
            if (add && disabilities.contains(disability.get())) return fail(ctx, "disability_already", id);
            if (!add && !disabilities.contains(disability.get())) return fail(ctx, "disability_missing", id);

            if (add) disabilities.add(disability.get());
            else disabilities.remove(disability.get());
            return saveDisabilities(ctx, tardis, disabilities);
        });
    }

    private static int clearDisabilities(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return withTardis(ctx, (tardis, idText) -> saveDisabilities(ctx, tardis, List.of()));
    }

    private static int saveDisabilities(
            CommandContext<CommandSourceStack> ctx, Tardis tardis, List<TardisDisability> disabilities) {
        TardisMinds.setDisabilities(tardis, disabilities);
        return reply(ctx, tardis, "disabilities", disabilityList(TardisMinds.mind(tardis)), true);
    }

    private static String disabilityList(TardisMind mind) {
        if (mind.disabilities().isEmpty()) return "none";
        return String.join(
                ", ", mind.disabilities().stream().map(TardisDisability::id).toList());
    }

    private static String traitList(TardisMind mind) {
        if (mind.traits().isEmpty()) return "none";
        return String.join(
                ", ", mind.traits().stream().map(trait -> shortId(trait.id())).toList());
    }

    private static String shortId(String id) {
        String prefix = Constants.MOD_ID + ":";
        return id.startsWith(prefix) ? id.substring(prefix.length()) : id;
    }

    private static String oneDecimal(float value) {
        return String.format("%.1f", value);
    }
}
