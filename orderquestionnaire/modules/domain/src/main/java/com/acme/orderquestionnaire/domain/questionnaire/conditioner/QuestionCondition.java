package com.acme.orderquestionnaire.domain.questionnaire.conditioner;

import com.acme.orderquestionnaire.domain.questionnaire.tree.QuestionConditionTreeNode;

import java.util.Map;
import java.util.Set;

public interface QuestionCondition {
    boolean isSatisfy(Map<String, Object> answers);
    QuestionConditionTreeNode toTreeNode();
    Set<String> referencedQuestionIds();
}
