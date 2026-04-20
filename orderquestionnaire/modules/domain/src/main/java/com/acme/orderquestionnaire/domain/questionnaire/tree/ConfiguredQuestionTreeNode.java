package com.acme.orderquestionnaire.domain.questionnaire.tree;

public record ConfiguredQuestionTreeNode(int order,
                                         QuestionTreeNode question,
                                         AnswerConfigurationTreeNode answerConfiguration,
                                         QuestionConditionTreeNode condition) {
}

