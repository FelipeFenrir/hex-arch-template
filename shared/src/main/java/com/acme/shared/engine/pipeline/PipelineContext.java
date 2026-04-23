package com.acme.shared.engine.pipeline;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Type-safe Data Bag for sharing state between pipeline {@link Step}s.
 *
 * <p>Values are stored and retrieved by their exact {@code Class<?>} key, eliminating
 * unsafe string keys and unchecked casts. Use {@link #put(Class, Object)} when the
 * value's runtime type differs from the desired key (e.g. storing an implementation
 * under an interface key).</p>
 *
 * <p>Intended to be subclassed per use-case to expose a fluent, named API:
 * <pre>{@code
 * public class CreateQuestionnairePipelineContext extends PipelineContext {
 *     public void auditInfo(AuditInfo info) { put(AuditInfo.class, info); }
 *     public AuditInfo auditInfo()          { return get(AuditInfo.class); }
 * }
 * }</pre>
 *
 * <p>Steps must validate required context entries via {@link #has(Class)} before accessing
 * them, especially at the start of steps that perform I/O.
 */
public class PipelineContext {

    private final Map<Class<?>, Object> values = new HashMap<>();

    /**
     * Stores a value keyed by its own runtime class.
     * Prefer {@link #put(Class, Object)} when storing under an interface or supertype.
     */
    public <T> void put(T value) {
        if (value == null) throw new IllegalArgumentException("Context value must not be null");
        values.put(value.getClass(), value);
    }

    /**
     * Stores a value keyed by the given explicit type. Use this when the value's
     * declared type differs from its runtime class (e.g. interface → implementation).
     */
    public <T> void put(Class<T> type, T value) {
        if (type == null) throw new IllegalArgumentException("Context key type must not be null");
        if (value == null) throw new IllegalArgumentException("Context value must not be null");
        values.put(type, value);
    }

    /**
     * Retrieves a value by type, using {@code type.cast()} to avoid unchecked casts.
     *
     * @throws NoSuchElementException when no value is registered for the given type
     */
    public <T> T get(Class<T> type) {
        Object value = values.get(type);
        if (value == null) {
            throw new NoSuchElementException(
                "PipelineContext: no value registered for type [" + type.getName() + "]. " +
                "Check that the required step ran before this one."
            );
        }
        return type.cast(value);
    }

    /**
     * Returns {@code true} if a value is registered for the given type.
     * Use in a step's precondition check before calling {@link #get(Class)}.
     */
    public <T> boolean has(Class<T> type) {
        return values.containsKey(type);
    }
}

