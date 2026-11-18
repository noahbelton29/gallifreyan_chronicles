package uk.nbforge.gallifreyan_chronicles.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.nbforge.gallifreyan_chronicles.Constants;

/**
 * the only place loggers get made, so every line shows up as (Gallifreyan Chronicles/Area) and u can filter by area.
 * messages are sentence case, no full stop, failures start with "Couldn't", and slf4j takes a trailing exception as
 * the stack trace so dont format it into the message
 */
public final class GCLog {
    public static final Logger CORE = area("Core");
    public static final Logger DATA = area("Data");
    public static final Logger ADDONS = area("Addons");
    public static final Logger TARDIS = area("TARDIS");
    public static final Logger CLIENT = area("Client");
    public static final Logger COMPAT = area("Compat");
    public static final Logger REPLAY = area("Replay");
    public static final Logger TESTER = area("Tester");

    private GCLog() {}

    /** what an addon gets from its context, so its lines sit next to the mod's ones */
    public static Logger addon(String addonId) {
        return area("Addon/" + addonId);
    }

    private static Logger area(String name) {
        return LoggerFactory.getLogger(Constants.MOD_NAME + "/" + name);
    }
}
