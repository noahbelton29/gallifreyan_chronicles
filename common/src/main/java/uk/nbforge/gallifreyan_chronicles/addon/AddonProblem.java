package uk.nbforge.gallifreyan_chronicles.addon;

/** Why an addon is not running. */
public record AddonProblem(Kind kind, String detail) {
    public enum Kind {
        /** Cannot read it, the detail is the reason. */
        INVALID(true),
        /** Needs a newer addon api, the detail is the addon api and then ours. */
        INCOMPATIBLE_API(true),
        /** Wrong mod version, the detail is the needed range and then your version. */
        INCOMPATIBLE_MOD(true),
        /** A needed mod or addon is missing, the detail is the name and then the needed range. */
        MISSING_REQUIREMENT(true),
        /** A needed mod or addon is the wrong version, the detail is the name, the needed range and what was found. */
        WRONG_REQUIREMENT_VERSION(true),
        /** Crashed while starting, the detail is the error. */
        FAILED_TO_START(true);

        private final boolean cannotBeSwitchedOn;

        Kind(boolean cannotBeSwitchedOn) {
            this.cannotBeSwitchedOn = cannotBeSwitchedOn;
        }

        /** Switching it on cannot fix it. */
        public boolean cannotBeSwitchedOn() {
            return cannotBeSwitchedOn;
        }
    }
}
