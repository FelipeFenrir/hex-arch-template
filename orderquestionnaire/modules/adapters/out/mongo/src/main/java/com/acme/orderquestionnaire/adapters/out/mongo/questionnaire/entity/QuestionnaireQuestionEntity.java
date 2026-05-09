package com.acme.orderquestionnaire.adapters.out.mongo.questionnaire.entity;

import com.acme.orderquestionnaire.domain.questionnaire.answer.AnswerConfiguration;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Map;

@Document(collection = "questionnaire_questions")
public record QuestionnaireQuestionEntity(
        @Id
        String documentId,

        @Indexed
        @Field("questionnaire_document_id")
        String questionnaireDocumentId,

        @Indexed
        @Field("questionnaire_id")
        String questionnaireId,

        @Indexed
        @Field("channel_distribution_id")
        String channelDistributionId,

        @Indexed
        @Field("journey_distribution_id")
        String journeyDistributionId,

        @Indexed
        @Field("question_id")
        String questionId,

        @Field("answer_configuration")
        AnswerConfiguration answerConfiguration,

        @Field("root_condition")
        Map<String, Object> rootCondition,

        @Field("order")
        Integer order
) {
}

