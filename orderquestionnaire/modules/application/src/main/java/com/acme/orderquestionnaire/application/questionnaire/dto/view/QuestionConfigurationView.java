package com.acme.orderquestionnaire.application.questionnaire.dto.view;

import com.acme.orderquestionnaire.application.question.dto.view.QuestionView;
import com.acme.orderquestionnaire.domain.questionnaire.ConfiguredQuestion;

public record QuestionConfigurationView(
        QuestionView question,
        Integer order,
        AnswerConfigurationView answerConfiguration,
        QuestionConditionView rootCondition
) {

    public static QuestionConfigurationView from(ConfiguredQuestion configuredQuestion) {
        if (configuredQuestion == null) {
            return null;
        }

        return new QuestionConfigurationView(
                QuestionView.from(configuredQuestion.question()),
                configuredQuestion.order(),
                AnswerConfigurationView.from(configuredQuestion.answerConfiguration()),
                QuestionConditionView.from(configuredQuestion.rootCondition())
        );
    }
}

