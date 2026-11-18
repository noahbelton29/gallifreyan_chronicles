package uk.nbforge.gallifreyan_chronicles.addon;

public record AddonProblem(Kind kind, String detail) {
    public enum Kind {
        /** cant read it, detail is the reason */
        INVALID(false),
        /** needs a newer addon api, detail is their api then the mod's */
        INCOMPATIBLE_API(false),
        /** wrong mod version, detail is the needed range then ur version */
        INCOMPATIBLE_MOD(false),
        /** a needed mod or addon is missing, detail is the name then the needed range */
        MISSING_REQUIREMENT(false),
        /** a needed mod or addon is the wrong version, detail is the name, needed range and what was actually found */
        WRONG_REQUIREMENT_VERSION(false),
        /** crashed while starting, detail is the error */
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
