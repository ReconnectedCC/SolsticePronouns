package cc.reconnected.solsticePronouns.modules.pronouns.data;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

import java.util.List;

@ConfigSerializable
public class PronounsConfig {

    @Comment("Format of the nominative pronouns.")
    public String nominativeFormat = "${first}/${second}";

    @Comment("Format of the meta pronoun.")
    public String metaFormat = "${pronoun}";

    @Comment("Format of the placeholder tag %player:pronouns%.")
    public String tagFormat = " (${pronouns})";

    @Comment("Format of the placeholder tag when no pronouns are set.")
    public String emptyTag = "";

    @Comment("Pronouns that do not take a second part.")
    public List<String> meta = List.of(
            "any",
            "ask",
            "avoid",
            "other"
    );

    @Comment("First part of the nominative pronouns: *first*/second. The indices have to match the set. i.e. he/him, she/her, ...")
    public List<String> first = List.of(
            "he",
            "she",
            "they",
            "it"
    );

    @Comment("Second part of the nominative pronouns: first/*second*")
    public List<String> second = List.of(
            "him",
            "her",
            "them",
            "its"
    );
}
