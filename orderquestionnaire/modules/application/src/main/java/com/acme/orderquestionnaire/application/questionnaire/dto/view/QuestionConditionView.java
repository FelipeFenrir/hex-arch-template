package com.acme.orderquestionnaire.application.questionnaire.dto.view;

import com.acme.orderquestionnaire.domain.questionnaire.conditioner.QuestionCondition;
import com.acme.orderquestionnaire.domain.questionnaire.tree.QuestionConditionTreeNode;

import java.util.List;

public record QuestionConditionView(
        String type,
        java.util.Map<String, Object> attributes,
        List<QuestionConditionView> children
) {

    public static QuestionConditionView from(QuestionCondition questionCondition) {
        return questionCondition == null ? null : from(questionCondition.toTreeNode());
    }

    public static QuestionConditionView from(QuestionConditionTreeNode treeNode) {
        if (treeNode == null) {
            return null;
        }

        return new QuestionConditionView(
                treeNode.type(),
                treeNode.attributes(),
                treeNode.children().stream()
                        .map(QuestionConditionView::from)
                        .toList()
        );
    }
}

