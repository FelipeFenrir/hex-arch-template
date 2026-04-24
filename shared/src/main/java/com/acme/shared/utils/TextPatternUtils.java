package com.acme.shared.utils;

import java.util.Objects;
import java.util.regex.Pattern;

public final class TextPatternUtils {

    private TextPatternUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static Pattern containsIgnoreCase(String value) {
        Objects.requireNonNull(value, "value must not be null");
        return Pattern.compile(Pattern.quote(value), Pattern.CASE_INSENSITIVE);
    }
}

