package com.acme.orderquestionnaire.domain.questionnaire.conditioner;

import com.acme.orderquestionnaire.domain.questionnaire.tree.QuestionConditionTreeNode;
import com.acme.shared.vo.QuestionId;

import java.beans.ConstructorProperties;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class VisibilityCondition implements QuestionCondition {
    private final QuestionId questionRootCode;
    private final Object expectedValue;

    @ConstructorProperties({"questionRootCode", "expectedValue"})
    public VisibilityCondition(String questionRootCode, Object expectedValue) {
        this(QuestionId.of(questionRootCode), expectedValue);
    }

    public VisibilityCondition(QuestionId questionRootCode, Object expectedValue) {
        this.questionRootCode = Objects.requireNonNull(questionRootCode, "questionRootCode must not be null");
        this.expectedValue = expectedValue;
    }

    public String questionRootCode() {
        return questionRootCode.value();
    }

    public QuestionId questionRootId() {
        return questionRootCode;
    }

    @Override
    public boolean isSatisfy(Map<String, Object> answers) {
        Object rootAnswer = answers.get(questionRootCode.value());
        return expectedValue.equals(rootAnswer);
    }

    @Override
    public QuestionConditionTreeNode toTreeNode() {
        Map<String, Object> attributes = new LinkedHashMap<>();
        attributes.put("questionRootCode", questionRootCode.value());
        attributes.put("expectedValue", expectedValue);
        return new QuestionConditionTreeNode("VISIBILITY", attributes, List.of());
    }

    @Override
    public Set<QuestionId> referencedQuestionIds() {
        return Set.of(questionRootCode);
    }
}
