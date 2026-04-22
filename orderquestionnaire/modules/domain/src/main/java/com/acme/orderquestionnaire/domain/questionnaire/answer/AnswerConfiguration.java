package com.acme.orderquestionnaire.domain.questionnaire.answer;

import com.acme.orderquestionnaire.domain.question.enumerator.AnswerType;
import com.acme.orderquestionnaire.domain.questionnaire.tree.AnswerConfigurationTreeNode;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;

import java.util.List;

public interface AnswerConfiguration {
    Result<Void, List<DomainError>> validate(Object answer);
    AnswerType getConfigurationType();
    AnswerConfigurationTreeNode toTreeNode();
}
