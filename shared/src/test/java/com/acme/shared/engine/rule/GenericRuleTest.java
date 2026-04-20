package com.acme.shared.engine.rule;

import com.acme.shared.stereotypes.test.MockClass;
import com.acme.shared.testutils.MockDomainClass;
import com.acme.shared.exception.DomainValidationException;

import com.acme.shared.stereotypes.test.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Objects;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@UnitTest
@DisplayName("GenericSpecification")
class GenericRuleTest {

    RuleValidator<MockDomainClass> ruleValidator;

    @BeforeEach
    void setup(){
        ruleValidator = new RuleValidator<MockDomainClass>()
                .addSpec(new GenericRule<>(
                        p -> p.id() != null,
                        "Person ID cannot be null")
                )
                .addSpec(new GenericRule<>(
                        p -> p.name() != null && !p.name().isBlank(),
                        "Person Name cannot be null or blank")
                );
    }

    @ParameterizedTest
    @MethodSource("mockProvider_success")
    @DisplayName("Then perform the validation without errors")
    void isSatisfiedBy_success(MockDomainClass mock) {

        FailNotification notification = ruleValidator.validate(mock);

        assertAll(
                () -> {
                    assertNotNull(mock);
                    assertNotNull(mock.id());
                    assertDoesNotThrow(() -> notification.throwIfHasErrors(DomainValidationException::new));
                }
        );
    }

    @ParameterizedTest
    @MethodSource("mockProvider_error")
    @DisplayName("Then perform the validation with errors")
    void isSatisfiedBy_with_errors(MockDomainClass mock) {

        FailNotification notification = ruleValidator.validate(mock);

        assertAll(
                () -> {
                    assertNotNull(mock);
                    assertNotNull(mock.id());
                    var exception = assertThrows(
                            DomainValidationException.class, () ->
                                    notification.throwIfHasErrors(DomainValidationException::new)
                    );
                    assertNotNull(exception);
                    assertNotNull(exception.getMessage());
                    assertEquals("Domain validation fail:\n" +
                            "Person Name cannot be null or blank", exception.getMessage());
                }
        );
    }

    @ParameterizedTest
    @MethodSource("mockProvider_error")
    @DisplayName("Then perform the validation with errors and demonstrate the custom exception")
    void isSatisfiedBy_with_errors_and_custom_exception(MockDomainClass mock) {

        FailNotification notification = ruleValidator.validate(mock);

        assertAll(
                () -> {
                    assertNotNull(mock);
                    assertNotNull(mock.id());
                    var exception = assertThrows(
                            IllegalArgumentException.class, () ->
                                    notification.throwIfHasErrors(
                                            msg -> new IllegalArgumentException(String.valueOf(msg)
                                            ))
                    );
                    assertNotNull(exception);
                    assertNotNull(exception.getMessage());
                    assertEquals("[Person Name cannot be null or blank]", exception.getMessage());
                }
        );
    }

    @Test
    @DisplayName("Then demonstrate the error messages caught by the Exception")
    void getMessage() {
        var person = new MockDomainClass(null, " ", "Joe Star");

        FailNotification notification = ruleValidator.validate(person);

        var exception = assertThrows(
                IllegalArgumentException.class, () ->
                        notification.throwIfHasErrors(DomainValidationException::new)
        );
        assertNotNull(exception);
        assertNotNull(exception.getMessage());
        assertEquals("""
                Domain validation fail:
                Person ID cannot be null
                Person Name cannot be null or blank""", exception.getMessage());
    }

    @Test
    @DisplayName("isSatisfiedBy should delegate evaluation to predicate")
    void isSatisfiedBy_shouldDelegateToPredicate() {
        GenericRule<Integer> greaterThanTen = new GenericRule<>(value -> value > 10, "must be > 10");

        assertTrue(greaterThanTen.isSatisfiedBy(11));
        assertFalse(greaterThanTen.isSatisfiedBy(10));
    }

    @Test
    @DisplayName("message should expose the configured failure message")
    void message_shouldExposeConfiguredFailureMessage() {
        GenericRule<String> spec = new GenericRule<>(Objects::isNull, "value cannot be null");

        assertEquals("value cannot be null", spec.message());
    }

    @Test
    @DisplayName("Custom Rule is created")
    void customRule_shouldCreateCustomRule() {
        Rule<String> myComplexRule = new CustomRule("TESTE");

        RuleValidator<String> validator = new RuleValidator<>();
        validator.addSpec(new GenericRule<>(
                myComplexRule::isSatisfiedBy,
                "Incorrect text."
        ));

        assertFalse(validator.validate("TESTE").hasErrors());
    }

    static Stream<Arguments> mockProvider_success() {
        var mock_one = new MockDomainClass("1", "Bugs Bunny", "Bunny");
        var mock_two = new MockDomainClass("2", "ACME Corporation", "ACME");

        return Stream.of(
                Arguments.of(mock_one),
                Arguments.of(mock_two)
        );
    }

    static Stream<Arguments> mockProvider_error() {
        var mock_one = new MockDomainClass("1", "", "Bunny");
        var mock_two = new MockDomainClass("2", " ", "ACME");

        return Stream.of(
                Arguments.of(mock_one),
                Arguments.of(mock_two)
        );
    }

    @MockClass
    record CustomRule(String validationString) implements Rule<String> {

        @Override
        public boolean isSatisfiedBy(String candidate) {
            return candidate.equals(validationString) ;
        }
    }
}