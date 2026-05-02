package com.acme.orderquestionnaire.adapters.out.mongo.questionnaire.mapper;

import com.acme.orderquestionnaire.adapters.out.mongo.questionnaire.entity.QuestionnaireQuestionEntity;
import com.acme.orderquestionnaire.domain.question.Question;
import com.acme.orderquestionnaire.domain.questionnaire.ConfiguredQuestion;
import com.acme.orderquestionnaire.domain.questionnaire.Questionnaire;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class QuestionnaireQuestionEntityMapper {

    public QuestionnaireQuestionEntity toEntity(String questionnaireDocumentId,
                                                Questionnaire questionnaire,
                                                ConfiguredQuestion configuredQuestion) {
        return new QuestionnaireQuestionEntity(
                toDocumentId(questionnaireDocumentId, configuredQuestion.question().id()),
                questionnaireDocumentId,
                questionnaire.id(),
                questionnaire.channelDistributionId(),
                questionnaire.journeyDistributionId(),
                configuredQuestion.question().id(),
                configuredQuestion.answerConfiguration(),
                configuredQuestion.rootCondition(),
                configuredQuestion.order()
        );
    }

    public List<ConfiguredQuestion> toConfiguredQuestions(List<QuestionnaireQuestionEntity> links,
                                                          Map<String, Question> questionsById) {
        return links.stream()
                .sorted(java.util.Comparator.comparingInt(link -> link.order() == null ? 0 : link.order()))
                .map(link -> {
                    Question question = questionsById.get(link.questionId());
                    if (question == null) {
                        throw new IllegalStateException("Question not found for questionnaire relation [questionId=%s]"
                                .formatted(link.questionId()));
                    }

                    ConfiguredQuestion configuredQuestion = ConfiguredQuestion.createNew(
                            question,
                            link.answerConfiguration(),
                            link.order() == null ? 0 : link.order()
                    );
                    if (link.rootCondition() != null) {
                        configuredQuestion.rootCondition(link.rootCondition());
                    }
                    return configuredQuestion;
                })
                .toList();
    }

    private String toDocumentId(String questionnaireDocumentId, String questionId) {
        return questionnaireDocumentId + "|" + questionId;
    }
}

