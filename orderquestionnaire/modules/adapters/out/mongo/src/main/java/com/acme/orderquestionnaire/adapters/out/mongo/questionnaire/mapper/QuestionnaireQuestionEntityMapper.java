package com.acme.orderquestionnaire.adapters.out.mongo.questionnaire.mapper;

import com.acme.orderquestionnaire.adapters.out.mongo.questionnaire.entity.QuestionnaireQuestionEntity;
import com.acme.orderquestionnaire.domain.question.Question;
import com.acme.orderquestionnaire.domain.questionnaire.ConfiguredQuestion;
import com.acme.orderquestionnaire.domain.questionnaire.Questionnaire;
import com.acme.orderquestionnaire.domain.questionnaire.conditioner.CompositeCondition;
import com.acme.orderquestionnaire.domain.questionnaire.conditioner.EqualCondition;
import com.acme.orderquestionnaire.domain.questionnaire.conditioner.NumericCondition;
import com.acme.orderquestionnaire.domain.questionnaire.conditioner.QuestionCondition;
import com.acme.orderquestionnaire.domain.questionnaire.conditioner.VisibilityCondition;
import com.acme.orderquestionnaire.domain.questionnaire.tree.QuestionConditionTreeNode;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
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
                configuredQuestion.rootCondition() == null
                        ? null
                        : treeNodeToMap(configuredQuestion.rootCondition().toTreeNode()),
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
                        configuredQuestion.rootCondition(toDomainCondition(link.rootCondition()));
                    }
                    return configuredQuestion;
                })
                .toList();
    }

    private QuestionCondition toDomainCondition(Object rawCondition) {
        if (rawCondition == null) {
            return null;
        }
        if (rawCondition instanceof QuestionCondition condition) {
            return condition;
        }
        if (rawCondition instanceof QuestionConditionTreeNode treeNode) {
            return fromTreeNode(treeNode);
        }
        if (rawCondition instanceof Map<?, ?> map) {
            return fromMapCondition(castMap(map));
        }
        throw new IllegalStateException("Unsupported root_condition payload type: " + rawCondition.getClass().getName());
    }

    private QuestionCondition fromMapCondition(Map<String, Object> map) {
        if (map.containsKey("type") && map.containsKey("attributes")) {
            return fromTreeNode(treeNodeFromMap(map));
        }

        String className = asString(map.get("_class"));
        if (className != null && className.endsWith("EqualCondition")) {
            return new EqualCondition(extractQuestionRootCode(map.get("questionRootCode")), map.get("expectedValue"));
        }
        if (className != null && className.endsWith("NumericCondition")) {
            Number expected = (Number) map.get("expectedValue");
            Object operator = map.get("operator");
            return new NumericCondition(
                    extractQuestionRootCode(map.get("questionRootCode")),
                    expected == null ? 0d : expected.doubleValue(),
                    operator
            );
        }
        if (className != null && className.endsWith("VisibilityCondition")) {
            return new VisibilityCondition(extractQuestionRootCode(map.get("questionRootCode")), map.get("expectedValue"));
        }
        if (className != null && className.endsWith("CompositeCondition")) {
            Object isAndRaw = map.get("isAnd");
            Object operatorRaw = map.get("operator");
            boolean isAnd = isAndRaw instanceof Boolean b
                    ? b
                    : "AND".equalsIgnoreCase(asString(operatorRaw));
            CompositeCondition composite = new CompositeCondition(isAnd);
            Object conditionsRaw = map.get("conditions");
            if (conditionsRaw instanceof List<?> list) {
                for (Object child : list) {
                    composite.addCondition(toDomainCondition(child));
                }
            }
            return composite;
        }

        // Backward compatibility for shape without _class where fields are direct.
        if (map.containsKey("questionRootCode") && map.containsKey("expectedValue") && map.containsKey("operator")) {
            Number expected = (Number) map.get("expectedValue");
            return new NumericCondition(
                    extractQuestionRootCode(map.get("questionRootCode")),
                    expected == null ? 0d : expected.doubleValue(),
                    map.get("operator")
            );
        }
        if (map.containsKey("questionRootCode") && map.containsKey("expectedValue")) {
            return new EqualCondition(extractQuestionRootCode(map.get("questionRootCode")), map.get("expectedValue"));
        }

        throw new IllegalStateException("Unsupported root_condition map payload: " + map);
    }

    private QuestionCondition fromTreeNode(QuestionConditionTreeNode treeNode) {
        Map<String, Object> attributes = treeNode.attributes() == null ? Map.of() : treeNode.attributes();
        return switch (treeNode.type()) {
            case "EQUAL" -> new EqualCondition(
                    extractQuestionRootCode(attributes.get("questionRootCode")),
                    attributes.get("expectedValue")
            );
            case "NUMERIC" -> {
                Number expected = (Number) attributes.get("expectedValue");
                Object operator = attributes.get("operator");
                yield new NumericCondition(
                        extractQuestionRootCode(attributes.get("questionRootCode")),
                        expected == null ? 0d : expected.doubleValue(),
                        operator
                );
            }
            case "VISIBILITY" -> new VisibilityCondition(
                    extractQuestionRootCode(attributes.get("questionRootCode")),
                    attributes.get("expectedValue")
            );
            case "COMPOSITE" -> {
                boolean isAnd = "AND".equalsIgnoreCase(asString(attributes.get("operator")));
                CompositeCondition composite = new CompositeCondition(isAnd);
                List<QuestionConditionTreeNode> children = treeNode.children() == null ? List.of() : treeNode.children();
                for (QuestionConditionTreeNode child : children) {
                    composite.addCondition(fromTreeNode(child));
                }
                yield composite;
            }
            default -> throw new IllegalStateException("Unsupported condition type: " + treeNode.type());
        };
    }

    private QuestionConditionTreeNode treeNodeFromMap(Map<String, Object> map) {
        String type = asString(map.get("type"));
        Map<String, Object> attributes = map.get("attributes") instanceof Map<?, ?> attrs
                ? castMap(attrs)
                : Map.of();

        List<QuestionConditionTreeNode> children = new ArrayList<>();
        Object rawChildren = map.get("children");
        if (rawChildren instanceof List<?> list) {
            for (Object child : list) {
                if (child instanceof QuestionConditionTreeNode node) {
                    children.add(node);
                } else if (child instanceof Map<?, ?> childMap) {
                    children.add(treeNodeFromMap(castMap(childMap)));
                }
            }
        }
        return new QuestionConditionTreeNode(type, attributes, children);
    }

    private Map<String, Object> treeNodeToMap(QuestionConditionTreeNode treeNode) {
        Map<String, Object> payload = new java.util.LinkedHashMap<>();
        payload.put("type", treeNode.type());
        payload.put("attributes", treeNode.attributes() == null ? Map.of() : treeNode.attributes());

        List<Map<String, Object>> children = new ArrayList<>();
        if (treeNode.children() != null) {
            for (QuestionConditionTreeNode child : treeNode.children()) {
                children.add(treeNodeToMap(child));
            }
        }
        payload.put("children", children);
        return payload;
    }

    private String extractQuestionRootCode(Object raw) {
        if (raw instanceof String value) {
            return value;
        }
        if (raw instanceof Map<?, ?> valueMap) {
            Object nested = valueMap.get("value");
            return nested == null ? null : String.valueOf(nested);
        }
        return raw == null ? null : String.valueOf(raw);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> castMap(Map<?, ?> map) {
        return (Map<String, Object>) map;
    }

    private String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private String toDocumentId(String questionnaireDocumentId, String questionId) {
        return questionnaireDocumentId + "|" + questionId;
    }
}

