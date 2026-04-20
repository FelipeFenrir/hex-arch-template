package com.acme.orderquestionnaire.application.questionnaire.service.support;

import com.acme.orderquestionnaire.application.questionnaire.dto.view.QuestionAnswerViolationView;
import com.acme.orderquestionnaire.domain.question.enumerator.AnswerType;
import com.acme.orderquestionnaire.domain.questionnaire.ConfiguredQuestion;
import com.acme.orderquestionnaire.domain.questionnaire.tree.AnswerConfigurationTreeNode;
import com.acme.orderquestionnaire.domain.questionnaire.tree.QuestionConditionTreeNode;
import com.acme.shared.enumerator.ParameterizationStatus;
import com.acme.shared.pattern.result.DomainError;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class ViolationViewExtractor {

    static final String SOURCE_ANSWER_CONFIGURATION = "ANSWER_CONFIGURATION";
    static final String SOURCE_QUESTION_CONDITION = "QUESTION_CONDITION";
    static final String SOURCE_QUESTION_STATUS = "QUESTION_STATUS";

    private ViolationViewExtractor() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Builds a violation view by dispatching on the error code.
     *
     * @param error                  the domain error
     * @param configuredQuestion     the configured question that produced the error (may be null)
     * @param configuredByQuestionId all configured questions indexed by question id (for ref-status lookup)
     */
    public static QuestionAnswerViolationView build(DomainError error,
                                                     ConfiguredQuestion configuredQuestion,
                                                     Map<String, ConfiguredQuestion> configuredByQuestionId) {
        return switch (error.code()) {
            case "QUESTION_NOT_ACTIVE" ->
                    buildQuestionStatusViolation(error, configuredQuestion);
            case "CONDITION_REFERENCED_QUESTION_NOT_ACTIVE" ->
                    buildConditionRefInactiveViolation(error, configuredQuestion, configuredByQuestionId);
            case "ANSWER_NOT_ALLOWED_BY_CONDITION" ->
                    buildConditionViolation(error, configuredQuestion);
            default ->
                    buildAnswerConfigViolation(error, configuredQuestion);
        };
    }

    // ── QUESTION_STATUS ───────────────────────────────────────────────────────

    private static QuestionAnswerViolationView buildQuestionStatusViolation(DomainError error,
                                                                              ConfiguredQuestion cq) {
        Map<String, Object> attrs = new LinkedHashMap<>();
        if (cq != null) {
            attrs.put("currentStatus", cq.question().status().name());
            attrs.put("expectedStatus", ParameterizationStatus.ACTIVE.name());
        }
        return new QuestionAnswerViolationView(
                error.code(), error.message(),
                SOURCE_QUESTION_STATUS,
                "QUESTION_STATUS",
                attrs,
                "question"
        );
    }

    // ── QUESTION_CONDITION ────────────────────────────────────────────────────

    private static QuestionAnswerViolationView buildConditionRefInactiveViolation(
            DomainError error,
            ConfiguredQuestion cq,
            Map<String, ConfiguredQuestion> configuredByQuestionId) {

        Map<String, Object> attrs = new LinkedHashMap<>();
        String ruleType = "UNKNOWN_CONDITION";

        if (cq != null && cq.rootCondition() != null) {
            QuestionConditionTreeNode treeNode = cq.rootCondition().toTreeNode();
            ruleType = resolveConditionRuleType(treeNode);
            attrs.putAll(treeNode.attributes());

            // Collect inactive referenced questions and their statuses
            Set<String> referencedIds = cq.rootCondition().referencedQuestionIds();
            Map<String, String> inactiveStatuses = referencedIds.stream()
                    .filter(id -> {
                        ConfiguredQuestion ref = configuredByQuestionId.get(id);
                        return ref != null && !ref.question().isActive();
                    })
                    .collect(Collectors.toMap(
                            id -> id,
                            id -> configuredByQuestionId.get(id).question().status().name(),
                            (a, b) -> a,
                            LinkedHashMap::new
                    ));

            attrs.put("inactiveQuestionReferences", List.copyOf(inactiveStatuses.keySet()));
            attrs.put("inactiveQuestionStatuses", inactiveStatuses);
        }

        return new QuestionAnswerViolationView(
                error.code(), error.message(),
                SOURCE_QUESTION_CONDITION,
                ruleType,
                attrs,
                "rootCondition"
        );
    }

    private static QuestionAnswerViolationView buildConditionViolation(DomainError error, ConfiguredQuestion cq) {
        Map<String, Object> attrs = new LinkedHashMap<>();
        String ruleType = "UNKNOWN_CONDITION";

        if (cq != null && cq.rootCondition() != null) {
            QuestionConditionTreeNode treeNode = cq.rootCondition().toTreeNode();
            ruleType = resolveConditionRuleType(treeNode);
            attrs.putAll(treeNode.attributes());
        }

        return new QuestionAnswerViolationView(
                error.code(), error.message(),
                SOURCE_QUESTION_CONDITION,
                ruleType,
                attrs,
                "rootCondition"
        );
    }

    // ── ANSWER_CONFIGURATION ──────────────────────────────────────────────────

    private static QuestionAnswerViolationView buildAnswerConfigViolation(DomainError error, ConfiguredQuestion cq) {
        Map<String, Object> attrs = new LinkedHashMap<>();
        String ruleType = "UNKNOWN";

        if (cq != null) {
            AnswerConfigurationTreeNode treeNode = cq.answerConfiguration().toTreeNode();
            attrs.putAll(treeNode.attributes());
            ruleType = resolveAnswerConfigRuleType(error.code(), cq.answerConfiguration().getConfigurationType());
        }

        return new QuestionAnswerViolationView(
                error.code(), error.message(),
                SOURCE_ANSWER_CONFIGURATION,
                ruleType,
                attrs,
                "answerConfiguration"
        );
    }

    // ── Rule type resolvers ────────────────────────────────────────────────────

    static String resolveConditionRuleType(QuestionConditionTreeNode treeNode) {
        return switch (treeNode.type()) {
            case "COMPOSITE" -> {
                Object op = treeNode.attributes().get("operator");
                yield "AND".equals(op) ? "COMPOSITE_AND" : "COMPOSITE_OR";
            }
            case "EQUAL"      -> "EQUAL_CONDITION";
            case "NUMERIC"    -> "NUMERIC_CONDITION";
            case "VISIBILITY" -> "VISIBILITY_CONDITION";
            default           -> treeNode.type() + "_CONDITION";
        };
    }

    static String resolveAnswerConfigRuleType(String errorCode, AnswerType type) {
        String prefix = type.name(); // TEXT, NUMBER, DATE, OPTION_LIST
        return switch (errorCode) {
            case "MANDATORY_ANSWER"      -> prefix + "_REQUIRED";
            case "INVALID_ANSWER_TYPE"   -> prefix + "_TYPE";
            case "PATTERN_MISMATCH"      -> "TEXT_PATTERN";
            case "VALUE_BELOW_MIN",
                 "VALUE_ABOVE_MAX",
                 "NEGATIVE_NOT_ALLOWED",
                 "DECIMAL_NOT_ALLOWED",
                 "INVALID_STEP"          -> "NUMBER_RANGE";
            case "PAST_DATE_NOT_ALLOWED" -> "DATE_RANGE";
            case "INVALID_OPTION"        -> "OPTION_LIST_INVALID";
            default                      -> prefix + "_RULE";
        };
    }
}


