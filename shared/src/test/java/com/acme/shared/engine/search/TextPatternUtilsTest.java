package com.acme.shared.engine.search;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TextPatternUtilsTest {

    @Test
    void shouldCreateCaseInsensitiveContainsPattern() {
        var pattern = TextPatternUtils.containsIgnoreCase("Quest");

        assertTrue(pattern.matcher("my question label").find());
        assertTrue(pattern.matcher("QUEST_VALUE").find());
        assertFalse(pattern.matcher("answer").find());
    }

    @Test
    void shouldEscapeRegexCharacters() {
        var pattern = TextPatternUtils.containsIgnoreCase("a+b");

        assertTrue(pattern.matcher("field a+b value").find());
        assertFalse(pattern.matcher("field aaab value").find());
    }
}

