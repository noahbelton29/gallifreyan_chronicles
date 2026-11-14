package uk.nbforge.gallifreyan_chronicles.addon;

/* why an addon isn't running */
public record AddonProblem(Kind kind, String detail) {
    public enum Kind {
        /* can't read it. detail: the reason */
        INVALID(true),
        /* needs a newer addon api. detail: addon api|our api */
        INCOMPATIBLE_API(true),
        /* wrong mod version. detail: needed range|your version */
        INCOMPATIBLE_MOD(true),
        /* needed mod or addon is missing. detail: name|needed range */
        MISSING_REQUIREMENT(true),
        /* needed mod or addon is the wrong version. detail: name|needed range|found */
        WRONG_REQUIREMENT_VERSION(true),
        /* crashed while starting. detail: the error */
        FAILED_TO_START(true);

        private final boolean cannotBeSwitchedOn;

        Kind(boolean cannotBeSwitchedOn) {
            this.cannotBeSwitchedOn = cannotBeSwitchedOn;
        }

        /* switching it can't fix it */
        public boolean cannotBeSwitchedOn() {
            return cannotBeSwitchedOn;
        }
    }
}
