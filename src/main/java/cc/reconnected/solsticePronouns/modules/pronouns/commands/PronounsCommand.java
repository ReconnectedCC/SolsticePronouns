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
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
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
    public LiteralArgumentBuilder<ServerCommandSource> command(String name) {
        return CommandManager.literal(name)
                .requires(require(true))
                .then(CommandManager.literal("clear")
                        .executes(this::executeClear)
                        .then(CommandManager.argument("player", StringArgumentType.word())
                                .requires(require("clear.other", 3))
                                .suggests(LocalGameProfile::suggest)
                                .executes(context -> executeClearOther(
                                        context,
                                        LocalGameProfile.getProfile(context, "player")
                                ))
                        )
                )
                .then(CommandManager.literal("set")
                        .then(CommandManager.argument("first", StringArgumentType.word())
                                .suggests(this::suggestFirst)
                                .executes(context -> executeSet(
                                        context,
                                        StringArgumentType.getString(context, "first"),
                                        null
                                ))
                                .then(CommandManager.argument("second", StringArgumentType.word())
                                        .suggests(this::suggestSecond)
                                        .executes(context -> executeSet(
                                                context,
                                                StringArgumentType.getString(context, "first"),
                                                StringArgumentType.getString(context, "second")
                                        ))
                                )
                        )
                )

                .then(CommandManager.literal("forceset")
                        .requires(require("forceset", 3))
                        .then(CommandManager.argument("player", StringArgumentType.word())
                                .suggests(LocalGameProfile::suggest)
                                .then(CommandManager.argument("first", StringArgumentType.word())
                                        .suggests(this::suggestFirst)
                                        .executes(context -> executeSetOther(
                                                context,
                                                LocalGameProfile.getProfile(context, "player"),
                                                StringArgumentType.getString(context, "first"),
                                                null
                                        ))
                                        .then(CommandManager.argument("second", StringArgumentType.word())
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

    private CompletableFuture<Suggestions> suggestFirst(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        var firsts = module.getFirstAndMeta();
        return CommandSource.suggestMatching(firsts, builder);
    }

    private CompletableFuture<Suggestions> suggestSecond(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        var first = StringArgumentType.getString(context, "first");
        var secondMatching = module.getSecondMatching(first);
        return CommandSource.suggestMatching(secondMatching, builder);
    }

    private int executeSet(CommandContext<ServerCommandSource> context, String first, @Nullable String second) throws CommandSyntaxException {
        var player = context.getSource().getPlayerOrThrow();

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
            context.getSource().sendFeedback(() -> module.locale().get("invalidFirst"), false);
            return 0;
        }

        if (second != null && !module.getSecondMatching(first).contains(second)) {
            context.getSource().sendFeedback(() -> module.locale().get("invalidSecond"), false);
            return 0;
        }

        var data = module.getPlayer(player.getUuid());

        data.first = first;
        data.second = second;

        var map = Map.of(
                "pronouns", module.getPlayerPronouns(player.getUuid())
        );

        context.getSource().sendFeedback(() -> module.locale().get("pronounsSet", map), false);
        return 1;
    }

    private int executeSetOther(CommandContext<ServerCommandSource> context, GameProfile player, String first, @Nullable String second) throws CommandSyntaxException {
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
                "player", Text.of(player.getName()),
                "pronouns", module.getPlayerPronouns(player.getId())
        );

        context.getSource().sendFeedback(() -> module.locale().get("otherPronounsSet", map), true);

        return 1;
    }

    private int executeClear(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        var player = context.getSource().getPlayerOrThrow();
        var data = module.getPlayer(player.getUuid());

        data.first = null;
        data.second = null;

        context.getSource().sendFeedback(() -> module.locale().get("cleared"), false);
        return 1;
    }

    private int executeClearOther(CommandContext<ServerCommandSource> context, GameProfile player) throws CommandSyntaxException {
        var data = module.getPlayer(player.getId());

        data.first = null;
        data.second = null;

        var map = Map.of(
                "player", Text.of(player.getName())
        );

        context.getSource().sendFeedback(() -> module.locale().get("otherCleared", map), true);
        return 1;
    }
}