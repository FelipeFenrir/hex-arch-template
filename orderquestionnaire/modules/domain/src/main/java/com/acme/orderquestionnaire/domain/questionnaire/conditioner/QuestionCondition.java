package com.acme.orderquestionnaire.domain.questionnaire.conditioner;

import com.acme.orderquestionnaire.domain.questionnaire.tree.QuestionConditionTreeNode;
import com.acme.shared.vo.QuestionId;

import java.util.Map;
import java.util.Set;

public interface QuestionCondition {
    boolean isSatisfy(Map<String, Object> answers);
    QuestionConditionTreeNode toTreeNode();
    Set<QuestionId> referencedQuestionIds();
}
