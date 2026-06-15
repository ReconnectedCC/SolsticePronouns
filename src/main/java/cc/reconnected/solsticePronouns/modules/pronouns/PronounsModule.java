package cc.reconnected.solsticePronouns.modules.pronouns;

import cc.reconnected.solsticePronouns.modules.pronouns.commands.PronounsCommand;
import cc.reconnected.solsticePronouns.modules.pronouns.data.PronounsConfig;
import cc.reconnected.solsticePronouns.modules.pronouns.data.PronounsLocale;
import cc.reconnected.solsticePronouns.modules.pronouns.data.PronounsPlayerData;
import eu.pb4.placeholders.api.PlaceholderResult;
import eu.pb4.placeholders.api.Placeholders;
import me.alexdevs.solstice.Solstice;
import me.alexdevs.solstice.api.module.ModuleBase;
import me.alexdevs.solstice.api.text.Format;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PronounsModule extends ModuleBase.Toggleable {
    public PronounsModule(ResourceLocation id) {
        super(id);
    }

    @Override
    public void init() {
        Solstice.configManager.registerData(id, PronounsConfig.class, PronounsConfig::new);
        Solstice.localeManager.registerModule(id, PronounsLocale.MODULE);
        Solstice.playerData.registerData(id, PronounsPlayerData.class, PronounsPlayerData::new);

        commands.add(new PronounsCommand(this));

        Placeholders.register(ResourceLocation.fromNamespaceAndPath("player", "pronouns"), (context, args) -> {
            if (!context.hasPlayer())
                return PlaceholderResult.invalid("No player!");

            return PlaceholderResult.value(getPlayerTag(context.player().getUUID()));
        });
    }

    public PronounsConfig getConfig() {
        return Solstice.configManager.getData(PronounsConfig.class);
    }

    public List<String> getFirstAndMeta() {
        var config = getConfig();
        var list = new ArrayList<String>();
        list.addAll(config.first);
        list.addAll(config.meta);

        return list;
    }

    public List<String> getSecondMatching(String first) {
        var firstAndMeta = getFirstAndMeta();
        var index = firstAndMeta.indexOf(first);

        // first part is invalid
        if (index == -1) {
            return List.of();
        }

        var config = getConfig();

        // first part is meta and cannot have second
        if (index >= config.first.size()) {
            return List.of();
        }

        // so we give the second part matching the first + all first parts (minus first part in arg)
        var list = new ArrayList<String>();
        list.add(config.second.get(index));

        list.addAll(config.first.stream().filter(s -> !s.equals(first)).toList());

        return list;
    }

    public PronounsPlayerData getPlayer(UUID uuid) {
        return Solstice.playerData.get(uuid).getData(PronounsPlayerData.class);
    }

    public Component getPronouns(String first, @Nullable String second) {
        var config = getConfig();

        Map<String, Component> map;
        String format;
        if (second == null) {
            map = Map.of(
                    "pronoun", Component.nullToEmpty(first)
            );
            format = config.metaFormat;
        } else {
            map = Map.of(
                    "first", Component.nullToEmpty(first),
                    "second", Component.nullToEmpty(second)
            );
            format = config.nominativeFormat;
        }

        return Format.parse(format, map);
    }

    public Component getTag(Component pronouns) {
        var config = getConfig();
        var map = Map.of(
                "pronouns", pronouns
        );

        return Format.parse(config.tagFormat, map);
    }

    public boolean hasPronouns(UUID uuid) {
        return getPlayer(uuid).first != null;
    }

    public Component getPlayerPronouns(UUID uuid) {
        var data = getPlayer(uuid);
        return getPronouns(data.first, data.second);
    }

    public Component getPlayerTag(UUID uuid) {
        if (hasPronouns(uuid)) {
            var pronouns = getPlayerPronouns(uuid);
            return getTag(pronouns);
        }

        var config = getConfig();

        return Format.parse(config.emptyTag);
    }
}
