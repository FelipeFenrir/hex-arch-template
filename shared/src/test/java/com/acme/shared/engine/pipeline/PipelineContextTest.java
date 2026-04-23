package com.acme.shared.engine.pipeline;

import com.acme.shared.stereotypes.test.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@UnitTest
@DisplayName("PipelineContext")
class PipelineContextTest {

    // ── concrete subclass used in all tests ───────────────────────────────

    static class NamedContext extends PipelineContext {
        public void name(String value) { put(String.class, value); }
        public String name()           { return get(String.class); }
    }

    record Token(String value) {}

    // ── put / get ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("put and get")
    class PutAndGet {

        @Test
        @DisplayName("should store and retrieve a value by type")
        void shouldStoreAndRetrieveByType() {
            var ctx = new PipelineContext();
            ctx.put(Token.class, new Token("abc"));

            assertEquals("abc", ctx.get(Token.class).value());
        }

        @Test
        @DisplayName("should overwrite value when same type is put twice")
        void shouldOverwriteOnSameType() {
            var ctx = new PipelineContext();
            ctx.put(String.class, "first");
            ctx.put(String.class, "second");

            assertEquals("second", ctx.get(String.class));
        }

        @Test
        @DisplayName("put(value) should use runtime class as key")
        void putWithoutTypeKeyUsesRuntimeClass() {
            var ctx = new PipelineContext();
            ctx.put(new Token("runtime"));

            assertEquals("runtime", ctx.get(Token.class).value());
        }

        @Test
        @DisplayName("subclass typed accessors should work correctly")
        void subclassTypedAccessorsShouldWork() {
            var ctx = new NamedContext();
            ctx.name("Alice");

            assertEquals("Alice", ctx.name());
        }
    }

    // ── has ───────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("has")
    class Has {

        @Test
        @DisplayName("should return true after put")
        void shouldReturnTrueAfterPut() {
            var ctx = new PipelineContext();
            ctx.put(String.class, "hello");

            assertTrue(ctx.has(String.class));
        }

        @Test
        @DisplayName("should return false when type was never added")
        void shouldReturnFalseForAbsentType() {
            var ctx = new PipelineContext();

            assertFalse(ctx.has(Integer.class));
        }
    }

    // ── error cases ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("error cases")
    class ErrorCases {

        @Test
        @DisplayName("get should throw NoSuchElementException when type not present")
        void getShouldThrowWhenAbsent() {
            var ctx = new PipelineContext();

            assertThrows(java.util.NoSuchElementException.class, () -> ctx.get(Token.class));
        }

        @Test
        @DisplayName("put(value) should throw when value is null")
        void putShouldThrowOnNullValue() {
            var ctx = new PipelineContext();

            assertThrows(IllegalArgumentException.class, () -> ctx.put((Token) null));
        }

        @Test
        @DisplayName("put(type, value) should throw when type is null")
        void putWithTypeShouldThrowOnNullType() {
            var ctx = new PipelineContext();

            assertThrows(IllegalArgumentException.class, () -> ctx.put(null, "value"));
        }

        @Test
        @DisplayName("put(type, value) should throw when value is null")
        void putWithTypeShouldThrowOnNullValue() {
            var ctx = new PipelineContext();

            assertThrows(IllegalArgumentException.class, () -> ctx.put(String.class, null));
        }
    }
}

