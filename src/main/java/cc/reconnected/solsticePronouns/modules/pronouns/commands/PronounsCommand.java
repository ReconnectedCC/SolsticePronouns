package cc.reconnected.solsticePronouns.modules.pronouns.commands;

import cc.reconnected.solsticePronouns.modules.pronouns.PronounsModule;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import me.alexdevs.solstice.api.command.LocalGameProfile;
import me.alexdevs.solstice.api.module.ModCommand;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class PronounsCommand extends ModCommand<PronounsModule> {
    public PronounsCommand(PronounsModule module) {
        super(module);
    }

    @Override
    public List<String> getNames() {
        return List.of("pronouns");
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> command(String name) {
        return Commands.literal(name)
                .requires(require(true))
                .then(Commands.literal("clear")
                        .executes(this::executeClear)
                        .then(Commands.argument("player", StringArgumentType.word())
                                .requires(require("clear.other", 3))
                                .suggests(LocalGameProfile::suggest)
                                .executes(context -> executeClearOther(
                                        context,
                                        LocalGameProfile.getProfile(context, "player")
                                ))
                        )
                )
                .then(Commands.literal("set")
                        .then(Commands.argument("first", StringArgumentType.word())
                                .suggests(this::suggestFirst)
                                .executes(context -> executeSet(
                                        context,
                                        StringArgumentType.getString(context, "first"),
                                        null
                                ))
                                .then(Commands.argument("second", StringArgumentType.word())
                                        .suggests(this::suggestSecond)
                                        .executes(context -> executeSet(
                                                context,
                                                StringArgumentType.getString(context, "first"),
                                                StringArgumentType.getString(context, "second")
                                        ))
                                )
                        )
                )

                .then(Commands.literal("forceset")
                        .requires(require("forceset", 3))
                        .then(Commands.argument("player", StringArgumentType.word())
                                .suggests(LocalGameProfile::suggest)
                                .then(Commands.argument("first", StringArgumentType.word())
                                        .suggests(this::suggestFirst)
                                        .executes(context -> executeSetOther(
                                                context,
                                                LocalGameProfile.getProfile(context, "player"),
                                                StringArgumentType.getString(context, "first"),
                                                null
                                        ))
                                        .then(Commands.argument("second", StringArgumentType.word())
                                                .suggests(this::suggestSecond)
                                                .executes(context -> executeSetOther(
                                                        context,
                                                        LocalGameProfile.getProfile(context, "player"),
                                                        StringArgumentType.getString(context, "first"),
                                                        StringArgumentType.getString(context, "second")
                                                ))
                                        )
                                )
                        )
                );
    }

    private CompletableFuture<Suggestions> suggestFirst(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        var firsts = module.getFirstAndMeta();
        return SharedSuggestionProvider.suggest(firsts, builder);
    }

    private CompletableFuture<Suggestions> suggestSecond(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        var first = StringArgumentType.getString(context, "first");
        var secondMatching = module.getSecondMatching(first);
        return SharedSuggestionProvider.suggest(secondMatching, builder);
    }

    private int executeSet(CommandContext<CommandSourceStack> context, String first, @Nullable String second) throws CommandSyntaxException {
        var player = context.getSource().getPlayerOrException();

        if (second == null) {
            var candidates = module.getSecondMatching(first);
            if (!candidates.isEmpty()) {
                second = candidates.get(0);
            }
        }

        var config = module.getConfig();

        if (config.meta.contains(first)) {
            second = null;
        } else if (!config.first.contains(first)) {
            context.getSource().sendSuccess(() -> module.locale().get("invalidFirst"), false);
            return 0;
        }

        if (second != null && !module.getSecondMatching(first).contains(second)) {
            context.getSource().sendSuccess(() -> module.locale().get("invalidSecond"), false);
            return 0;
        }

        var data = module.getPlayer(player.getUUID());

        data.first = first;
        data.second = second;

        var map = Map.of(
                "pronouns", module.getPlayerPronouns(player.getUUID())
        );

        context.getSource().sendSuccess(() -> module.locale().get("pronounsSet", map), false);
        return 1;
    }

    private int executeSetOther(CommandContext<CommandSourceStack> context, GameProfile player, String first, @Nullable String second) throws CommandSyntaxException {
        var data = module.getPlayer(player.getId());

        if (second == null) {
            var candidates = module.getSecondMatching(first);
            if (!candidates.isEmpty()) {
                second = candidates.get(0);
            }
        }

        data.first = first;
        data.second = second;

        var map = Map.of(
                "player", Component.nullToEmpty(player.getName()),
                "pronouns", module.getPlayerPronouns(player.getId())
        );

        context.getSource().sendSuccess(() -> module.locale().get("otherPronounsSet", map), true);

        return 1;
    }

    private int executeClear(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var player = context.getSource().getPlayerOrException();
        var data = module.getPlayer(player.getUUID());

        data.first = null;
        data.second = null;

        context.getSource().sendSuccess(() -> module.locale().get("cleared"), false);
        return 1;
    }

    private int executeClearOther(CommandContext<CommandSourceStack> context, GameProfile player) throws CommandSyntaxException {
        var data = module.getPlayer(player.getId());

        data.first = null;
        data.second = null;

        var map = Map.of(
                "player", Component.nullToEmpty(player.getName())
        );

        context.getSource().sendSuccess(() -> module.locale().get("otherCleared", map), true);
        return 1;
    }
}