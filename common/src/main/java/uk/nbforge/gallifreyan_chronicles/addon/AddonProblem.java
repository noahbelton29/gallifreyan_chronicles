package uk.nbforge.gallifreyan_chronicles.addon;

public record AddonProblem(Kind kind, String detail) {
    public enum Kind {
        /** Cannot read it, the detail is the reason. */
        INVALID(false),
        /** Needs a newer addon api, the detail is the addon api and then ours. */
        INCOMPATIBLE_API(false),
        /** Wrong mod version, the detail is the needed range and then your version. */
        INCOMPATIBLE_MOD(false),
        /** A needed mod or addon is missing, the detail is the name and then the needed range. */
        MISSING_REQUIREMENT(false),
        /** A needed mod or addon is the wrong version, the detail is the name, the needed range and what was found. */
        WRONG_REQUIREMENT_VERSION(false),
        /** Crashed while starting, the detail is the error. */
        FAILED_TO_START(false);

        private final boolean canBeSwitchedOn;

        Kind(boolean canBeSwitchedOn) {
            this.canBeSwitchedOn = canBeSwitchedOn;
        }

        public boolean canBeSwitchedOn() {
            return canBeSwitchedOn;
        }
    }
}
