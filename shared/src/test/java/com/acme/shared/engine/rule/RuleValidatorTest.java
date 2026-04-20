package com.acme.shared.engine.rule;

import com.acme.shared.stereotypes.test.UnitTest;
import com.acme.shared.testutils.MockDomainClass;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@UnitTest
@DisplayName("SpecValidator")
class RuleValidatorTest {

    @Test
    @DisplayName("addSpec should be fluent and append a specification")
    void addSpec_shouldBeFluentAndAppendSpecification() {
        RuleValidator<String> validator = new RuleValidator<>();

        RuleValidator<String> returned = validator
                .addSpec(new GenericRule<>(value -> !value.isBlank(), "blank"));

        assertSame(validator, returned);
        FailNotification notification = validator.validate(" ");
        assertEquals(List.of("blank"), notification.getErrors());
    }

    @Test
    @DisplayName("addSpecs should append all provided specifications")
    void addSpecs_shouldAppendAllProvidedSpecifications() {
        RuleValidator<MockDomainClass> validator = new RuleValidator<MockDomainClass>()
                .addSpecs(List.of(
                        new GenericRule<MockDomainClass>(
                                mock -> mock.id() != null,
                                "id required"
                        ),
                        new GenericRule<MockDomainClass>(
                                mock -> mock.name() != null && !mock.name().isBlank(),
                                "name required"
                        )
                ));

        FailNotification notification = validator.validate(
                new MockDomainClass(null, "", "nickname")
        );

        assertEquals(List.of("id required", "name required"), notification.getErrors());
    }

    @Test
    @DisplayName("validate should return notification without errors when no spec is satisfied")
    void validate_shouldReturnNotificationWithoutErrorsWhenNoSpecIsSatisfied() {
        RuleValidator<MockDomainClass> validator = new RuleValidator<MockDomainClass>()
                .addSpec(new GenericRule<MockDomainClass>(
                        mock -> mock.id() != null,
                        "id required")
                );

        FailNotification notification = validator.validate(new MockDomainClass("1", "John", "J"));

        assertFalse(notification.hasErrors());
        assertTrue(notification.getErrors().isEmpty());
    }
}
