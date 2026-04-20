package com.acme.orderquestionnaire.application.questionnaire.dto.view;

import com.acme.orderquestionnaire.domain.question.answer.AnswerConfiguration;
import com.acme.orderquestionnaire.domain.questionnaire.tree.AnswerConfigurationTreeNode;

import java.util.Map;

public record AnswerConfigurationView(
        String type,
        Map<String, Object> attributes
) {

    public static AnswerConfigurationView from(AnswerConfiguration answerConfiguration) {
        return answerConfiguration == null ? null : from(answerConfiguration.toTreeNode());
    }

    public static AnswerConfigurationView from(AnswerConfigurationTreeNode treeNode) {
        if (treeNode == null) {
            return null;
        }

        return new AnswerConfigurationView(treeNode.type().name(), treeNode.attributes());
    }
}

