package uk.nbforge.gallifreyan_chronicles.tardis.console;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import uk.nbforge.gallifreyan_chronicles.tardis.control.TardisControls;
import uk.nbforge.gallifreyan_chronicles.util.GCLog;

public final class TardisConsoleValidator {
    private TardisConsoleValidator() {}

    public static int validate(Collection<TardisConsole> consoles) {
        int problems = 0;
        for (TardisConsole console : consoles) {
            problems += validate(console);
        }
        if (problems > 0) {
            GCLog.TARDIS.warn("TARDIS console validation found {} problem(s); see warnings above", problems);
        }
        return problems;
    }

    private static int validate(TardisConsole console) {
        int problems = 0;
        Set<String> seen = new HashSet<>();
        for (TardisConsole.ControlEntry entry : console.controls()) {
            if (!TardisControls.isRegistered(entry.id())) {
                GCLog.TARDIS.warn("Console '{}' lists unknown control '{}'", console.id(), entry.id());
                problems++;
            }
            if (!seen.add(entry.id())) {
                GCLog.TARDIS.warn("Console '{}' lists control '{}' more than once", console.id(), entry.id());
                problems++;
            }
            if (entry.width() <= 0.0f || entry.height() <= 0.0f || entry.depth() <= 0.0f) {
                GCLog.TARDIS.warn(
                        "Console '{}' control '{}' has a non-positive size ({} x {} x {})",
                        console.id(),
                        entry.id(),
                        entry.width(),
                        entry.height(),
                        entry.depth());
                problems++;
            }
        }
        for (String id : TardisControls.ids()) {
            if (!seen.contains(id)) {
                GCLog.TARDIS.debug("Console '{}' has no entry for control '{}'", console.id(), id);
            }
        }
        return problems;
    }
}
