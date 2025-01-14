package cc.reconnected.solsticePronouns.modules.pronouns.data;

import java.util.Map;

public class PronounsLocale {
    public static final Map<String, String> MODULE = Map.ofEntries(
            Map.entry("pronounsSet", "<green>Pronouns set to <yellow>${pronouns}</yellow>!</green>"),
            Map.entry("invalidFirst", "<gold>The first part is invalid!</gold>"),
            Map.entry("invalidSecond", "<gold>The second part is invalid!</gold>"),
            Map.entry("cleared", "<gold>Pronouns cleared!</gold>")
    );
}
