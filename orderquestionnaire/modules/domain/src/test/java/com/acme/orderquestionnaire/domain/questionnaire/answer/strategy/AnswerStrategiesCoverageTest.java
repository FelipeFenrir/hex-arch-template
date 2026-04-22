package com.acme.orderquestionnaire.domain.questionnaire.answer.strategy;

import com.acme.orderquestionnaire.domain.question.enumerator.AnswerType;
import com.acme.orderquestionnaire.domain.questionnaire.answer.AnswerOptionItem;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;
import com.acme.shared.stereotypes.test.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

@UnitTest
@DisplayName("Answer strategies additional coverage")
class AnswerStrategiesCoverageTest {

    @Test
    @DisplayName("AnswerConfigurationFactory should instantiate every overload")
    void shouldInstantiateEveryFactoryOverload() {
        assertNotNull(new AnswerConfigurationFactory());

        AnswerTextStrategy defaultText = AnswerConfigurationFactory.createTextStrategy();
        AnswerTextStrategy regexText = AnswerConfigurationFactory.createTextStrategy("[A-Z]+$");
        AnswerTextStrategy regexTextWithMessage = AnswerConfigurationFactory.createTextStrategy("[A-Z]+$", "Uppercase only");

        AnswerNumberStrategy defaultNumber = AnswerConfigurationFactory.createNumberStrategy();
        AnswerNumberStrategy boundedNumber = AnswerConfigurationFactory.createNumberStrategy(1.0, 10.0);
        AnswerNumberStrategy customNumber = AnswerConfigurationFactory.createNumberStrategy(0.0, 10.0, 2.0,
                false, false, "Invalid number");

        AnswerDateStrategy defaultDate = AnswerConfigurationFactory.createDateStrategy();
        AnswerDateStrategy maskedDate = AnswerConfigurationFactory.createDateStrategy("yyyy-MM-dd", false);
        AnswerDateStrategy customDate = AnswerConfigurationFactory.createDateStrategy("yyyy-MM-dd", false,
                "Future only");

        AnswerOptionItem yesOption = AnswerOptionItem.rehydrate("yes", "Yes");
        AnswerOptionItem noOption = AnswerOptionItem.createNew("no", "No");
        AnswerOptionListStrategy listFromList = AnswerConfigurationFactory.createListStrategy(List.of(yesOption));
        AnswerOptionListStrategy listFromListWithMessage = AnswerConfigurationFactory.createListStrategy(
                List.of(yesOption, noOption), "Select an option"
        );
        AnswerOptionListStrategy listFromVarargs = AnswerConfigurationFactory.createListStrategy(yesOption);
        AnswerOptionListStrategy listFromVarargsWithMessage = AnswerConfigurationFactory.createListStrategy(
                "custom", yesOption
        );

        assertSame(AnswerType.TEXT, defaultText.getConfigurationType());
        assertEquals("[A-Z]+$", regexText.getRegexPattern());
        assertEquals("Uppercase only", regexTextWithMessage.getCustomErrorMessage());

        assertSame(AnswerType.NUMBER, defaultNumber.getConfigurationType());
        assertEquals(1.0, boundedNumber.getMin());
        assertEquals(10.0, boundedNumber.getMax());
        assertEquals(2.0, customNumber.getStep());
        assertEquals("Invalid number", customNumber.getCustomErrorMessage());

        assertSame(AnswerType.DATE, defaultDate.getConfigurationType());
        assertEquals("yyyy-MM-dd", maskedDate.getMaskFormat());
        assertEquals("Future only", customDate.getCustomErrorMessage());

        assertSame(AnswerType.OPTION_LIST, listFromList.getConfigurationType());
        assertNull(listFromList.getCustomErrorMessage());
        assertEquals("Select an option", listFromListWithMessage.getCustomErrorMessage());
        assertEquals(1, listFromVarargs.getAnswerOptions().size());
        assertEquals("custom", listFromVarargsWithMessage.getCustomErrorMessage());
    }

    @Test
    @DisplayName("Text strategy should validate type, regex and tree attributes")
    void shouldValidateTextStrategy() {
        AnswerTextStrategy strategy = AnswerConfigurationFactory.createTextStrategy("[A-Z]+$", "Uppercase only");

        Result<Void, List<DomainError>> invalidType = strategy.validate(10);
        Result<Void, List<DomainError>> patternMismatch = strategy.validate("abc");
        Result<Void, List<DomainError>> success = strategy.validate("ABC");

        assertInstanceOf(Result.Failure.class, invalidType);
        assertEquals("INVALID_ANSWER_TYPE", ((Result.Failure<Void, List<DomainError>>) invalidType).error().getFirst().code());

        assertInstanceOf(Result.Failure.class, patternMismatch);
        assertEquals("PATTERN_MISMATCH", ((Result.Failure<Void, List<DomainError>>) patternMismatch).error().getFirst().code());
        assertEquals("Uppercase only", ((Result.Failure<Void, List<DomainError>>) patternMismatch).error().getFirst().message());

        assertInstanceOf(Result.Success.class, success);
        assertEquals("[A-Z]+$", strategy.toTreeNode().attributes().get("regexPattern"));
        assertEquals("Uppercase only", strategy.toTreeNode().attributes().get("customErrorMessage"));
    }

    @Test
    @DisplayName("Date strategy should validate type, past dates and tree attributes")
    void shouldValidateDateStrategy() {
        AnswerDateStrategy strategy = AnswerConfigurationFactory.createDateStrategy("yyyy-MM-dd", false, "Future only");
        AnswerDateStrategy permissiveStrategy = AnswerConfigurationFactory.createDateStrategy("yyyy-MM-dd", true);

        Result<Void, List<DomainError>> invalidType = strategy.validate("2025-01-01");
        Result<Void, List<DomainError>> pastDate = strategy.validate(LocalDate.now().minusDays(1));
        Result<Void, List<DomainError>> success = strategy.validate(LocalDate.now().plusDays(1));
        Result<Void, List<DomainError>> allowedPastDate = permissiveStrategy.validate(LocalDate.now().minusDays(1));

        assertInstanceOf(Result.Failure.class, invalidType);
        assertEquals("INVALID_ANSWER_TYPE", ((Result.Failure<Void, List<DomainError>>) invalidType).error().getFirst().code());

        assertInstanceOf(Result.Failure.class, pastDate);
        assertEquals("PAST_DATE_NOT_ALLOWED", ((Result.Failure<Void, List<DomainError>>) pastDate).error().getFirst().code());

        assertInstanceOf(Result.Success.class, success);
        assertInstanceOf(Result.Success.class, allowedPastDate);
        assertEquals("yyyy-MM-dd", strategy.toTreeNode().attributes().get("maskFormat"));
        assertEquals(false, strategy.toTreeNode().attributes().get("allowPastDates"));
        assertEquals("Future only", strategy.toTreeNode().attributes().get("customErrorMessage"));
    }

    @Test
    @DisplayName("Number strategy should validate all numeric restrictions and tree attributes")
    void shouldValidateNumberStrategy() {
        AnswerNumberStrategy strategy = AnswerConfigurationFactory.createNumberStrategy(0.0, 10.0, 2.0,
                false, false, "Invalid number");

        Result<Void, List<DomainError>> invalidType = strategy.validate("x");
        Result<Void, List<DomainError>> invalidValue = strategy.validate(-1.5);
        Result<Void, List<DomainError>> aboveMax = strategy.validate(12);
        Result<Void, List<DomainError>> success = strategy.validate(4);

        assertInstanceOf(Result.Failure.class, invalidType);
        assertEquals("INVALID_ANSWER_TYPE", ((Result.Failure<Void, List<DomainError>>) invalidType).error().getFirst().code());

        assertInstanceOf(Result.Failure.class, invalidValue);
        List<DomainError> invalidErrors = ((Result.Failure<Void, List<DomainError>>) invalidValue).error();
        assertEquals(List.of("DECIMAL_NOT_ALLOWED", "NEGATIVE_NOT_ALLOWED", "VALUE_BELOW_MIN", "INVALID_STEP"),
                invalidErrors.stream().map(DomainError::code).toList());

        assertInstanceOf(Result.Failure.class, aboveMax);
        assertEquals("VALUE_ABOVE_MAX", ((Result.Failure<Void, List<DomainError>>) aboveMax).error().getFirst().code());

        assertInstanceOf(Result.Success.class, success);
        assertEquals(0.0, strategy.toTreeNode().attributes().get("min"));
        assertEquals(10.0, strategy.toTreeNode().attributes().get("max"));
        assertEquals(2.0, strategy.toTreeNode().attributes().get("step"));
        assertEquals(false, strategy.toTreeNode().attributes().get("allowedDecimal"));
        assertEquals(false, strategy.toTreeNode().attributes().get("allowedNegative"));
        assertEquals("Invalid number", strategy.toTreeNode().attributes().get("customErrorMessage"));
    }

    @Test
    @DisplayName("Option list strategy should validate configured options and handle null tree options")
    void shouldValidateOptionListStrategy() {
        AnswerOptionItem activeOption = AnswerOptionItem.rehydrate("yes", "Yes");
        AnswerOptionItem draftOption = AnswerOptionItem.createNew("no", "No");
        AnswerOptionListStrategy strategy = AnswerConfigurationFactory.createListStrategy(
                List.of(activeOption, draftOption), "Select an option"
        );

        Result<Void, List<DomainError>> invalidType = strategy.validate(10);
        Result<Void, List<DomainError>> invalidOption = strategy.validate("maybe");
        Result<Void, List<DomainError>> success = strategy.validate("yes");

        assertInstanceOf(Result.Failure.class, invalidType);
        assertEquals("INVALID_ANSWER_TYPE", ((Result.Failure<Void, List<DomainError>>) invalidType).error().getFirst().code());

        assertInstanceOf(Result.Failure.class, invalidOption);
        assertEquals("INVALID_OPTION", ((Result.Failure<Void, List<DomainError>>) invalidOption).error().getFirst().code());

        assertInstanceOf(Result.Success.class, success);
        assertEquals(2, ((List<?>) strategy.toTreeNode().attributes().get("answerOptions")).size());
        assertEquals("Select an option", strategy.toTreeNode().attributes().get("customErrorMessage"));

        strategy.setAnswerOptions(null);
        assertEquals(List.of(), strategy.toTreeNode().attributes().get("answerOptions"));
    }
}


