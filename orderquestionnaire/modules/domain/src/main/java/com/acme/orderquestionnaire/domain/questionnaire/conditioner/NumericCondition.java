package com.acme.orderquestionnaire.domain.questionnaire.conditioner;

import com.acme.orderquestionnaire.domain.questionnaire.tree.QuestionConditionTreeNode;
import com.acme.shared.vo.QuestionId;

import java.beans.ConstructorProperties;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class NumericCondition implements QuestionCondition {
    private final QuestionId questionRootCode;
    private final double expectedValue;
    private final ComparisonOperator operator; // ">", "<", ">=", "<="

    @ConstructorProperties({"questionRootCode", "expectedValue", "operator"})
    public NumericCondition(String questionRootCode, double expectedValue, Object operator) {
        this(QuestionId.of(questionRootCode), expectedValue, operator);
    }

    public NumericCondition(QuestionId questionRootCode, double expectedValue, Object operator) {
        this.questionRootCode = Objects.requireNonNull(questionRootCode, "questionRootCode must not be null");
        this.expectedValue = expectedValue;
        this.operator = resolveOperator(operator);
    }

    public String questionRootCode() {
        return questionRootCode.value();
    }

    public QuestionId questionRootId() {
        return questionRootCode;
    }

    private ComparisonOperator resolveOperator(Object operator) {
        if (operator instanceof ComparisonOperator enumOperator) {
            return enumOperator;
        }
        if (operator instanceof String stringOperator) {
            return ComparisonOperator.fromString(stringOperator);
        }
        throw new IllegalArgumentException("Invalid operator: " + operator);
    }

    @Override
    public boolean isSatisfy(java.util.Map<String, Object> answers) {
        Object answer = answers.get(questionRootCode.value());

        if (answer instanceof Number) {
            double answerValue = ((Number) answer).doubleValue();
            return switch (operator) {
                case GREATER_THAN -> answerValue > expectedValue;
                case GREATER_OR_EQUAL -> answerValue >= expectedValue;
                case LESS_THAN -> answerValue < expectedValue;
                case LESS_OR_EQUAL -> answerValue <= expectedValue;
                case EQUAL -> answerValue == expectedValue;
                case DIFFERENT -> answerValue != expectedValue;
            };
        }
        return false; // If the answer is not a number, it does not satisfy the condition
    }

    public enum ComparisonOperator {
        GREATER_THAN(">"),
        GREATER_OR_EQUAL(">="),
        LESS_THAN("<"),
        LESS_OR_EQUAL("<="),
        EQUAL("=="),
        DIFFERENT("!=");

        private final String symbol;

        ComparisonOperator(String symbol) {
            this.symbol = symbol;
        }

        public static ComparisonOperator fromString(String symbol) {
            for (ComparisonOperator operator : values()) {
                if (operator.symbol.equals(symbol) || operator.name().equals(symbol)) {
                    return operator;
                }
            }
            throw new IllegalArgumentException("Invalid operator: " + symbol);
        }
    }

    @Override
    public QuestionConditionTreeNode toTreeNode() {
        Map<String, Object> attributes = new LinkedHashMap<>();
        attributes.put("questionRootCode", questionRootCode.value());
        attributes.put("expectedValue", expectedValue);
        attributes.put("operator", operator.name());
        return new QuestionConditionTreeNode("NUMERIC", attributes, List.of());
    }

    @Override
    public Set<QuestionId> referencedQuestionIds() {
        return Set.of(questionRootCode);
    }
}
