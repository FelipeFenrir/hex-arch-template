package com.acme.orderquestionnaire.adapters.out.mongo.unit.questionnaire.mapper;

import com.acme.orderquestionnaire.adapters.out.mongo.questionnaire.entity.QuestionnaireQuestionEntity;
import com.acme.orderquestionnaire.adapters.out.mongo.questionnaire.mapper.QuestionnaireQuestionEntityMapper;
import com.acme.orderquestionnaire.adapters.out.mongo.unit.support.MongoTestDataFactory;
import com.acme.orderquestionnaire.domain.questionnaire.ConfiguredQuestion;
import com.acme.orderquestionnaire.domain.questionnaire.conditioner.EqualCondition;
import com.acme.shared.stereotypes.test.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@UnitTest
@DisplayName("QuestionnaireQuestionEntityMapper")
class QuestionnaireQuestionEntityMapperTest {

    private final QuestionnaireQuestionEntityMapper mapper = new QuestionnaireQuestionEntityMapper();

    @Test
    @DisplayName("should serialize root condition as tree map payload")
    void shouldSerializeRootConditionAsTreeMapPayload() {
        var questionnaire = MongoTestDataFactory.newQuestionnaire("qn_mapper_tree", "channel_1", "journey_1");
        var question = MongoTestDataFactory.newQuestion("question_mapper_tree");

        ConfiguredQuestion configuredQuestion = ConfiguredQuestion.createNew(question, null, 2);
        configuredQuestion.rootCondition(new EqualCondition("question_anchor", "YES"));

        var entity = mapper.toEntity("qn_mapper_tree|channel_1|journey_1", questionnaire, configuredQuestion);

        assertNotNull(entity.rootCondition());
        assertEquals("EQUAL", entity.rootCondition().get("type"));

        @SuppressWarnings("unchecked")
        Map<String, Object> attributes = (Map<String, Object>) entity.rootCondition().get("attributes");
        assertEquals("question_anchor", attributes.get("questionRootCode"));
        assertEquals("YES", attributes.get("expectedValue"));
    }

    @Test
    @DisplayName("should map legacy class payload to domain condition")
    void shouldMapLegacyClassPayloadToDomainCondition() {
        var question = MongoTestDataFactory.newQuestion("question_mapper_legacy");

        Map<String, Object> legacyRootCondition = new LinkedHashMap<>();
        legacyRootCondition.put("_class", "com.acme.orderquestionnaire.domain.questionnaire.conditioner.EqualCondition");
        legacyRootCondition.put("questionRootCode", Map.of("value", "question_anchor"));
        legacyRootCondition.put("expectedValue", "YES");

        QuestionnaireQuestionEntity link = new QuestionnaireQuestionEntity(
                "qn_legacy|question_mapper_legacy",
                "qn_legacy",
                "qn_legacy",
                "channel_1",
                "journey_1",
                question.id(),
                null,
                legacyRootCondition,
                1
        );

        var configuredQuestions = mapper.toConfiguredQuestions(List.of(link), Map.of(question.id(), question));

        assertEquals(1, configuredQuestions.size());
        var conditionTree = configuredQuestions.getFirst().rootCondition().toTreeNode();
        assertEquals("EQUAL", conditionTree.type());
        assertEquals("question_anchor", conditionTree.attributes().get("questionRootCode"));
        assertEquals("YES", conditionTree.attributes().get("expectedValue"));
    }
}

