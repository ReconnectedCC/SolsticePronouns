package cc.reconnected.solsticePronouns;

import cc.reconnected.solsticePronouns.modules.pronouns.PronounsModule;
import me.alexdevs.solstice.api.module.ModuleBase;
import me.alexdevs.solstice.api.module.ModuleEntrypoint;

import java.util.HashSet;
import java.util.List;

public class ModuleProvider implements ModuleEntrypoint {
    private static final List<ModuleBase> modules = List.of(
            new PronounsModule()
    );
    @Override
    public HashSet<ModuleBase> register() {
        return new HashSet<>(modules);
    }
}
