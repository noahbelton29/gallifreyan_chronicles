package com.noahtnt2009.gallifreyan_chronicles.addon;

/**
 * Why an addon is not running, shown in red or orange in the Addons screen.
 *
 * @param kind   what is wrong
 * @param detail extra text for the message, such as the version it needs; see each {@link Kind}
 */
public record AddonProblem(Kind kind, String detail) {
    public enum Kind {
        /** addon.json (or the file) could not be read. detail: the reason. */
        INVALID(true),
        /** It was written for a newer addon API. detail: "addon api|supported api". */
        INCOMPATIBLE_API(true),
        /** It does not support this version of the mod. detail: "needed range|your version". */
        INCOMPATIBLE_MOD(true),
        /** A mod or addon it requires is missing or off. detail: "name|needed range". */
        MISSING_REQUIREMENT(true),
        /** A required mod or addon is the wrong version. detail: "name|needed range|found version". */
        WRONG_REQUIREMENT_VERSION(true),
        /** A Java addon the player has not approved yet. detail: empty. */
        NEEDS_APPROVAL(false),
        /** A Java addon threw an error while starting. detail: the error. */
        FAILED_TO_START(true);

        private final boolean cannotBeSwitchedOn;

        Kind(boolean cannotBeSwitchedOn) {
            this.cannotBeSwitchedOn = cannotBeSwitchedOn;
        }

        /** True when flipping the switch cannot fix it. */
        public boolean cannotBeSwitchedOn() {
            return cannotBeSwitchedOn;
        }
    }
}
