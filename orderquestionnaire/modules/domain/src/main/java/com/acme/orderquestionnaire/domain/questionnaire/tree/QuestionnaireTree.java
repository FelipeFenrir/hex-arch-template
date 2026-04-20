package com.acme.orderquestionnaire.domain.questionnaire.tree;

import java.util.List;

public record QuestionnaireTree(String id,
                                String channelDistributionId,
                                String journeyDistributionId,
                                String description,
                                String status,
                                List<ConfiguredQuestionTreeNode> questions) {

    public QuestionnaireTree {
        questions = questions == null ? List.of() : List.copyOf(questions);
    }
}

