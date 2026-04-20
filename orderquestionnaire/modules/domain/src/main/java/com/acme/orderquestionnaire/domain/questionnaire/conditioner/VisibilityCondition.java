package com.acme.orderquestionnaire.domain.questionnaire.conditioner;

import com.acme.orderquestionnaire.domain.questionnaire.tree.QuestionConditionTreeNode;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class VisibilityCondition implements QuestionCondition {
    private final String questionRootCode;
    private final Object expectedValue;

    public VisibilityCondition(String questionRootCode, Object expectedValue) {
        this.questionRootCode = questionRootCode;
        this.expectedValue = expectedValue;
    }

    @Override
    public boolean isSatisfy(Map<String, Object> answers) {
        Object rootAnswer = answers.get(questionRootCode);
        return expectedValue.equals(rootAnswer);
    }

    @Override
    public QuestionConditionTreeNode toTreeNode() {
        Map<String, Object> attributes = new LinkedHashMap<>();
        attributes.put("questionRootCode", questionRootCode);
        attributes.put("expectedValue", expectedValue);
        return new QuestionConditionTreeNode("VISIBILITY", attributes, List.of());
    }

    @Override
    public Set<String> referencedQuestionIds() {
        return Set.of(questionRootCode);
    }
}
