package com.acme.orderquestionnaire.domain.questionnaire.tree;

import com.acme.orderquestionnaire.domain.question.enumerator.AnswerType;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public record AnswerConfigurationTreeNode(AnswerType type, Map<String, Object> attributes) {

    public AnswerConfigurationTreeNode {
        attributes = attributes == null
                ? Map.of()
                : Collections.unmodifiableMap(new LinkedHashMap<>(attributes));
    }
}


