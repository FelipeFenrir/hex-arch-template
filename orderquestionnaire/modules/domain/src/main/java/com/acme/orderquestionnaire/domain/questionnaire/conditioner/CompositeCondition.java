package com.acme.orderquestionnaire.domain.questionnaire.conditioner;

import com.acme.orderquestionnaire.domain.questionnaire.tree.QuestionConditionTreeNode;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CompositeCondition implements QuestionCondition {
    private final List<QuestionCondition> conditions = new ArrayList<>();
    private final boolean isAnd;

    public CompositeCondition(boolean isAnd) {
        this.isAnd = isAnd;
    }

    public void addCondition(QuestionCondition condition) {
        conditions.add(condition);
    }

    @Override
    public boolean isSatisfy(Map<String, Object> answers) {
        if (conditions.isEmpty()) return true; // No conditions mean always satisfied

        if (isAnd) {
            return conditions.stream().allMatch(c -> c.isSatisfy(answers));
        } else {
            return conditions.stream().anyMatch(c -> c.isSatisfy(answers));
        }
    }

    @Override
    public QuestionConditionTreeNode toTreeNode() {
        Map<String, Object> attributes = new LinkedHashMap<>();
        attributes.put("operator", isAnd ? "AND" : "OR");
        List<QuestionConditionTreeNode> children = conditions.stream()
                .map(QuestionCondition::toTreeNode)
                .toList();
        return new QuestionConditionTreeNode("COMPOSITE", attributes, children);
    }

    @Override
    public Set<String> referencedQuestionIds() {
        Set<String> ids = new LinkedHashSet<>();
        for (QuestionCondition condition : conditions) {
            ids.addAll(condition.referencedQuestionIds());
        }
        return ids;
    }
}
