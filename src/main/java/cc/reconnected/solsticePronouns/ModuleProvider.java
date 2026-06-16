package cc.reconnected.solsticePronouns;

import cc.reconnected.solsticePronouns.modules.pronouns.PronounsModule;
import me.alexdevs.solstice.api.module.ModuleBase;
import me.alexdevs.solstice.api.module.ModuleEntrypoint;
import me.alexdevs.solstice.api.utils.SolsticeIdentifier;

import java.util.HashSet;
import java.util.Set;

public class ModuleProvider implements ModuleEntrypoint {
    private static final Set<ModuleBase> MODULES = new HashSet<>();

    public static final PronounsModule PRONOUNS = add(new PronounsModule(path("pronouns")));

    private static SolsticeIdentifier path(String path) {
        return SolsticePronouns.ID.withPath(path);
    }

    private static <T extends ModuleBase> T add(T module) {
        MODULES.add(module);
        return module;
    }

    @Override
    public HashSet<ModuleBase> register() {
        return new HashSet<>(MODULES);
    }
}
