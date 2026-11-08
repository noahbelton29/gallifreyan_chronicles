package com.noahtnt2009.gallifreyan_chronicles.addon;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Version implements Comparable<Version> {
    private static final Pattern LEADING_NUMBERS = Pattern.compile("^v?(\\d+(?:\\.\\d+)*)");

    private final int[] parts;
    private final String text;

    private Version(int[] parts, String text) {
        this.parts = parts;
        this.text = text;
    }

    public static Version parse(String text) {
        String trimmed = text.trim();
        Matcher matcher = LEADING_NUMBERS.matcher(trimmed);
        if (!matcher.find()) {
            throw new IllegalArgumentException("'" + text + "' is not a version (expected something like 1.2.3)");
        }
        String rest = trimmed.substring(matcher.end());
        if (!rest.isEmpty() && rest.charAt(0) != '-' && rest.charAt(0) != '+') {
            throw new IllegalArgumentException("'" + text + "' is not a version (expected something like 1.2.3)");
        }
        String[] pieces = matcher.group(1).split("\\.");
        List<Integer> numbers = new ArrayList<>();
        for (String piece : pieces) {
            try {
                numbers.add(Integer.parseInt(piece));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("'" + text + "' has a version part that is too large");
            }
        }
        int[] parts = new int[numbers.size()];
        for (int i = 0; i < parts.length; i++) parts[i] = numbers.get(i);
        return new Version(parts, trimmed);
    }

    int part(int index) {
        return index < parts.length ? parts[index] : 0;
    }

    @Override
    public int compareTo(Version other) {
        int length = Math.max(parts.length, other.parts.length);
        for (int i = 0; i < length; i++) {
            int difference = Integer.compare(part(i), other.part(i));
            if (difference != 0) return difference;
        }
        return 0;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Version && compareTo((Version) other) == 0;
    }

    @Override
    public int hashCode() {
        int hash = 1;
        int last = parts.length;
        while (last > 0 && parts[last - 1] == 0) last--;
        for (int i = 0; i < last; i++) hash = hash * 31 + parts[i];
        return hash;
    }

    @Override
    public String toString() {
        return text;
    }
}
