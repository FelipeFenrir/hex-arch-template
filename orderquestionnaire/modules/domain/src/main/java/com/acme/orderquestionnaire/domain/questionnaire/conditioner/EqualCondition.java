package com.acme.orderquestionnaire.domain.questionnaire.conditioner;

import com.acme.orderquestionnaire.domain.questionnaire.tree.QuestionConditionTreeNode;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class EqualCondition implements QuestionCondition {
    private final String questionRootCode;
    private final Object expectedValue;

    public EqualCondition(String questionRootCode, Object expectedValue) {
        this.questionRootCode = questionRootCode;
        this.expectedValue = expectedValue;
    }

    @Override
    public boolean isSatisfy(java.util.Map<String, Object> answers) {
        return Objects.equals(answers.get(questionRootCode), expectedValue);
    }

    @Override
    public QuestionConditionTreeNode toTreeNode() {
        Map<String, Object> attributes = new LinkedHashMap<>();
        attributes.put("questionRootCode", questionRootCode);
        attributes.put("expectedValue", expectedValue);
        return new QuestionConditionTreeNode("EQUAL", attributes, List.of());
    }

    @Override
    public Set<String> referencedQuestionIds() {
        return Set.of(questionRootCode);
    }
}