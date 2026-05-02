package com.acme.shared.stereotypes.test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.api.Tag;

/**
 * Marks a test class as an Architecture Test (ArchUnit-based boundary validation).
 * <p>
 * Architecture tests enforce hexagonal boundaries at compile-time:
 * - Domain isolation (no Spring/Framework dependencies)
 * - Adapter contracts (in/out port implementations)
 * - Bootstrap wiring (no leakage to domain/application)
 * - Service/handler stereotypes (correct layer classification)
 * </p>
 *
 * @see <a href="https://github.com/TNG/ArchUnit">ArchUnit Project</a>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Tag("architecture")
public @interface ArchitectureTest {
}

