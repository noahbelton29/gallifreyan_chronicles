package uk.nbforge.gallifreyan_chronicles.tardis.console;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import uk.nbforge.gallifreyan_chronicles.Constants;
import uk.nbforge.gallifreyan_chronicles.core.dynamic_registries.TardisControlRegistry;

public final class TardisConsoleValidator {
    private TardisConsoleValidator() {}

    public static int validate(Collection<TardisConsole> consoles) {
        int problems = 0;
        for (TardisConsole console : consoles) {
            problems += validate(console);
        }
        if (problems > 0) {
            Constants.LOG.warn("TARDIS console validation found {} problem(s); see warnings above", problems);
        }
        return problems;
    }

    /**
     * Checks a console file and logs what is wrong, returns how many problems it found.
     * Problems are unknown controls, repeated controls and sizes of zero or less.
     */
    private static int validate(TardisConsole console) {
        int problems = 0;
        Set<String> seen = new HashSet<>();
        for (TardisConsole.ControlEntry entry : console.controls()) {
            if (!TardisControlRegistry.isRegistered(entry.id())) {
                Constants.LOG.warn("Console '{}' lists unknown control '{}'", console.id(), entry.id());
                problems++;
            }
            if (!seen.add(entry.id())) {
                Constants.LOG.warn("Console '{}' lists control '{}' more than once", console.id(), entry.id());
                problems++;
            }
            if (entry.width() <= 0.0F || entry.height() <= 0.0F || entry.depth() <= 0.0F) {
                Constants.LOG.warn(
                        "Console '{}' control '{}' has a non-positive size ({} x {} x {})",
                        console.id(),
                        entry.id(),
                        entry.width(),
                        entry.height(),
                        entry.depth());
                problems++;
            }
        }
        for (String id : TardisControlRegistry.allIds()) {
            if (!seen.contains(id)) {
                Constants.LOG.debug("Console '{}' has no entry for control '{}'", console.id(), id);
            }
        }
        return problems;
    }
}
