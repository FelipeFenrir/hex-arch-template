package com.acme.orderquestionnaire.domain.questionnaire.tree;

import java.util.List;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public record QuestionConditionTreeNode(String type,
                                        Map<String, Object> attributes,
                                        List<QuestionConditionTreeNode> children) {

    public QuestionConditionTreeNode {
        attributes = attributes == null
                ? Map.of()
                : Collections.unmodifiableMap(new LinkedHashMap<>(attributes));
        children = children == null ? List.of() : List.copyOf(children);
    }
}


