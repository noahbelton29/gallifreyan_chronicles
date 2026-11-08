package com.noahtnt2009.gallifreyan_chronicles.addon;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class VersionRange {
    public static final VersionRange ANY = new VersionRange("*", new ArrayList<List<Bound>>());

    private static final Pattern TOKEN = Pattern.compile("(>=|<=|>|<|=|\\^|~)?\\s*([0-9A-Za-z.*+_-]+)");
    private static final Pattern INTERVAL = Pattern.compile("^([\\[(])\\s*([^,\\]\\)]*?)\\s*,\\s*([^,\\]\\)]*?)\\s*([\\])])$");

    private static final class Bound {
        private final Version version;
        private final boolean lower;
        private final boolean inclusive;

        private Bound(Version version, boolean lower, boolean inclusive) {
            this.version = version;
            this.lower = lower;
            this.inclusive = inclusive;
        }

        private boolean accepts(Version candidate) {
            int comparison = candidate.compareTo(version);
            if (lower) return inclusive ? comparison >= 0 : comparison > 0;
            return inclusive ? comparison <= 0 : comparison < 0;
        }
    }

    private final String text;
    private final List<List<Bound>> alternatives;

    private VersionRange(String text, List<List<Bound>> alternatives) {
        this.text = text;
        this.alternatives = alternatives;
    }

    public static VersionRange parse(String text) {
        String trimmed = text == null ? "" : text.trim();
        if (trimmed.isEmpty() || trimmed.equals("*"))
            return new VersionRange(trimmed.isEmpty() ? "*" : trimmed, new ArrayList<List<Bound>>());

        List<List<Bound>> alternatives = new ArrayList<>();
        for (String alternative : trimmed.split("\\|\\|")) {
            String part = alternative.trim();
            if (part.isEmpty()) throw new IllegalArgumentException("'" + text + "' has an empty choice around ||");
            List<Bound> bounds = new ArrayList<>();
            Matcher interval = INTERVAL.matcher(part);
            if (interval.matches()) {
                addInterval(bounds, interval, text);
            } else {
                addComparators(bounds, part, text);
            }
            alternatives.add(bounds);
        }
        return new VersionRange(trimmed, alternatives);
    }

    private static void addInterval(List<Bound> bounds, Matcher interval, String original) {
        String low = interval.group(2);
        String high = interval.group(3);
        if (low.isEmpty() && high.isEmpty())
            throw new IllegalArgumentException("'" + original + "' has no versions in it");
        if (!low.isEmpty()) bounds.add(new Bound(Version.parse(low), true, interval.group(1).equals("[")));
        if (!high.isEmpty()) bounds.add(new Bound(Version.parse(high), false, interval.group(4).equals("]")));
    }

    private static void addComparators(List<Bound> bounds, String part, String original) {
        Matcher matcher = TOKEN.matcher(part.replace(",", " "));
        int end = 0;
        boolean any = false;
        while (matcher.find()) {
            if (!part.replace(",", " ").substring(end, matcher.start()).trim().isEmpty()) {
                throw new IllegalArgumentException("'" + original + "' is not a valid version range");
            }
            end = matcher.end();
            any = true;
            addOne(bounds, matcher.group(1) == null ? "" : matcher.group(1), matcher.group(2), original);
        }
        if (!any || !part.replace(",", " ").substring(end).trim().isEmpty()) {
            throw new IllegalArgumentException("'" + original + "' is not a valid version range");
        }
    }

    private static void addOne(List<Bound> bounds, String operator, String value, String original) {
        boolean wildcard = value.equals("*") || value.matches("(?i).*(^|\\.)[x*](\\.|$).*");
        try {
            if (wildcard) {
                addWildcard(bounds, value);
                return;
            }
            Version version = Version.parse(value);
            switch (operator) {
                case ">=":
                    bounds.add(new Bound(version, true, true));
                    break;
                case ">":
                    bounds.add(new Bound(version, true, false));
                    break;
                case "<=":
                    bounds.add(new Bound(version, false, true));
                    break;
                case "<":
                    bounds.add(new Bound(version, false, false));
                    break;
                case "^":
                    bounds.add(new Bound(version, true, true));
                    bounds.add(new Bound(caret(version), false, false));
                    break;
                case "~":
                    bounds.add(new Bound(version, true, true));
                    bounds.add(new Bound(Version.parse(version.part(0) + "." + (version.part(1) + 1)), false, false));
                    break;
                default: // "=" or nothing: exactly this version
                    bounds.add(new Bound(version, true, true));
                    bounds.add(new Bound(version, false, true));
                    break;
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("'" + original + "' is not a valid version range: " + e.getMessage());
        }
    }

    private static Version caret(Version version) {
        if (version.part(0) > 0) return Version.parse((version.part(0) + 1) + ".0.0");
        if (version.part(1) > 0) return Version.parse("0." + (version.part(1) + 1) + ".0");
        return Version.parse("0.0." + (version.part(2) + 1));
    }

    private static void addWildcard(List<Bound> bounds, String value) {
        String[] pieces = value.split("\\.");
        List<Integer> fixed = new ArrayList<>();
        for (String piece : pieces) {
            if (piece.equals("*") || piece.equalsIgnoreCase("x")) break;
            fixed.add(Integer.parseInt(piece));
        }
        if (fixed.isEmpty()) return;
        StringBuilder low = new StringBuilder();
        StringBuilder high = new StringBuilder();
        for (int i = 0; i < fixed.size(); i++) {
            if (i > 0) {
                low.append('.');
                high.append('.');
            }
            low.append(fixed.get(i));
            high.append(i == fixed.size() - 1 ? fixed.get(i) + 1 : fixed.get(i));
        }
        bounds.add(new Bound(Version.parse(low.toString()), true, true));
        bounds.add(new Bound(Version.parse(high.toString()), false, false));
    }

    public boolean matches(Version version) {
        if (alternatives.isEmpty()) return true;
        for (List<Bound> bounds : alternatives) {
            boolean all = true;
            for (Bound bound : bounds) {
                if (!bound.accepts(version)) {
                    all = false;
                    break;
                }
            }
            if (all) return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return text;
    }
}
