package com.acme.orderquestionnaire.domain.questionnaire.conditioner;

import com.acme.orderquestionnaire.domain.questionnaire.tree.QuestionConditionTreeNode;
import com.acme.shared.vo.QuestionId;

import java.beans.ConstructorProperties;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class EqualCondition implements QuestionCondition {
    private final QuestionId questionRootCode;
    private final Object expectedValue;

    @ConstructorProperties({"questionRootCode", "expectedValue"})
    public EqualCondition(String questionRootCode, Object expectedValue) {
        this(QuestionId.of(questionRootCode), expectedValue);
    }

    public EqualCondition(QuestionId questionRootCode, Object expectedValue) {
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
    public boolean isSatisfy(java.util.Map<String, Object> answers) {
        return Objects.equals(answers.get(questionRootCode.value()), expectedValue);
    }

    @Override
    public QuestionConditionTreeNode toTreeNode() {
        Map<String, Object> attributes = new LinkedHashMap<>();
        attributes.put("questionRootCode", questionRootCode.value());
        attributes.put("expectedValue", expectedValue);
        return new QuestionConditionTreeNode("EQUAL", attributes, List.of());
    }

    @Override
    public Set<QuestionId> referencedQuestionIds() {
        return Set.of(questionRootCode);
    }
}