package com.acme.orderquestionnaire.domain.questionnaire.conditioner;

import com.acme.orderquestionnaire.domain.questionnaire.tree.QuestionConditionTreeNode;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NumericCondition implements QuestionCondition {
    private final String questionRootCode;
    private final double expectedValue;
    private final ComparisonOperator operator; // ">", "<", ">=", "<="

    public NumericCondition(String questionRootCode, double expectedValue, ComparisonOperator operator) {
        this.questionRootCode = questionRootCode;
        this.expectedValue = expectedValue;
        this.operator = operator;
    }

    public NumericCondition(String questionRootCode, double expectedValue, String operator) {
        this.questionRootCode = questionRootCode;
        this.expectedValue = expectedValue;
        this.operator = ComparisonOperator.fromString(operator);
    }

    @Override
    public boolean isSatisfy(java.util.Map<String, Object> answers) {
        Object answer = answers.get(questionRootCode);

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

        ComparisonOperator(String symbol) {}

        public static ComparisonOperator fromString(String symbol) {
            return switch (symbol) {
                case ">" -> GREATER_THAN;
                case ">=" -> GREATER_OR_EQUAL;
                case "<" -> LESS_THAN;
                case "<=" -> LESS_OR_EQUAL;
                case "==" -> EQUAL;
                case "!=" -> DIFFERENT;
                default -> throw new IllegalArgumentException("Invalid operator: " + symbol);
            };
        }
    }

    @Override
    public QuestionConditionTreeNode toTreeNode() {
        Map<String, Object> attributes = new LinkedHashMap<>();
        attributes.put("questionRootCode", questionRootCode);
        attributes.put("expectedValue", expectedValue);
        attributes.put("operator", operator.name());
        return new QuestionConditionTreeNode("NUMERIC", attributes, List.of());
    }

    @Override
    public Set<String> referencedQuestionIds() {
        return Set.of(questionRootCode);
    }
}
