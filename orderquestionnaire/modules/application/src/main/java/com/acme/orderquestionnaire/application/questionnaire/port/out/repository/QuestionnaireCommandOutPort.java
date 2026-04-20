package com.acme.orderquestionnaire.application.questionnaire.port.out.repository;

import com.acme.orderquestionnaire.domain.questionnaire.Questionnaire;
import com.acme.orderquestionnaire.domain.questionnaire.vo.QuestionnaireId;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;
import com.acme.shared.stereotypes.core.OutputPort;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@OutputPort
public interface QuestionnaireCommandOutPort {
    Result<Questionnaire, List<DomainError>> create(Questionnaire entry);

    Result<Questionnaire, List<DomainError>> update(Questionnaire questionnaire);

    boolean existsById(QuestionnaireId id);

    Optional<Questionnaire> findQuestionnaireById(QuestionnaireId id);

    Result<Void, List<DomainError>> deleteById(QuestionnaireId id);

    Map<String, List<String>> findReferencingQuestionnaireIdsByQuestionIds(List<String> questionIds);
}
